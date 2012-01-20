package platform.server.logics.scripted;

import platform.base.BaseUtils;
import platform.interop.ClassViewType;
import platform.server.classes.CustomClass;
import platform.server.classes.ValueClass;
import platform.server.form.entity.*;
import platform.server.form.entity.filter.NotNullFilterEntity;
import platform.server.form.entity.filter.RegularFilterEntity;
import platform.server.form.entity.filter.RegularFilterGroupEntity;
import platform.server.form.navigator.NavigatorElement;
import platform.server.form.view.FormView;
import platform.server.logics.linear.LP;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platform.base.BaseUtils.nvl;

/**
 * User: DAle
 * Date: 26.07.11
 * Time: 19:27
 */

public class ScriptingFormEntity extends FormEntity {
    private ScriptingLogicsModule LM;
    private Map<String, ObjectEntity> objectEntities = new HashMap<String, ObjectEntity>();

    public ScriptingFormEntity(NavigatorElement parent, ScriptingLogicsModule LM, String sID, String caption) {
        super(parent, sID, caption);
        this.LM = LM;
    }

    @Override
    public FormView createDefaultRichDesign() {
        return new ScriptingFormView(this, true, LM);
    }

    public void addScriptedGroupObjects(List<String> groupNames, List<List<String>> names, List<List<String>> classNames,
                                        List<List<String>> captions, List<ClassViewType> viewTypes, List<Boolean> isInitType) throws ScriptingErrorLog.SemanticErrorException {
        assert names.size() == groupNames.size() && names.size() == classNames.size() && names.size() == captions.size();
        for (int i = 0; i < names.size();  i++) {
            List<String> groupObjectNames = names.get(i);
            List<String> groupClassNames = classNames.get(i);
            List<String> groupCaptions = captions.get(i);

            assert groupObjectNames.size() == groupClassNames.size() && groupCaptions.size() == groupObjectNames.size();

            GroupObjectEntity groupObj = new GroupObjectEntity(genID());

            for (int j = 0; j < groupObjectNames.size(); j++) {
                String className = groupClassNames.get(j);
                ValueClass cls = LM.findClassByCompoundName(groupClassNames.get(j));
                String objectName = nvl(groupObjectNames.get(j), className);
                String objectCaption = nvl(groupCaptions.get(j), cls.getCaption());

                ObjectEntity obj = new ObjectEntity(genID(), objectName, cls, objectCaption);
                addObjectEntity(objectName, obj);

                groupObj.add(obj);
            }

            String groupName = groupNames.get(i);
            if (groupName == null) {
                groupName = "";
                for (ObjectEntity obj : groupObj.objects) {
                    groupName = (groupName.length() == 0 ? "" : groupName + ".") + obj.getSID();
                }
            }

            ClassViewType viewType = viewTypes.get(i);
            if (viewType != null) {
                if (isInitType.get(i)) {
                    groupObj.setInitClassView(viewType);
                } else {
                    groupObj.setSingleClassView(viewType);
                }
            }

            addGroupObjectEntity(groupName, groupObj);
        }
    }

    private void addGroupObjectEntity(String groupName, GroupObjectEntity group) throws ScriptingErrorLog.SemanticErrorException {
        if (getGroupObject(groupName) != null) {
            LM.getErrLog().emitAlreadyDefinedError(LM.getParser(), "group object", groupName);
        }
        group.setSID(groupName);
        addGroup(group);
    }

    private void addObjectEntity(String objectName, ObjectEntity obj) throws ScriptingErrorLog.SemanticErrorException {
        if (objectEntities.containsKey(objectName)) {
            LM.getErrLog().emitAlreadyDefinedError(LM.getParser(), "object", objectName);
        }
        objectEntities.put(objectName, obj);
    }

    public static final class MappedProperty {
        public LP<?> property;
        public PropertyObjectInterfaceEntity[] mapping;

        public MappedProperty(LP<?> property, PropertyObjectInterfaceEntity[] mapping) {
            this.property = property;
            this.mapping = mapping;
        }
    }

    private ObjectEntity[] getMappingObjectsArray(List<String> mapping) throws ScriptingErrorLog.SemanticErrorException {
        ObjectEntity[] objects = new ObjectEntity[mapping.size()];
        for (int i = 0; i < mapping.size(); i++) {
            objects[i] = getObjectEntity(mapping.get(i));
        }
        return objects;
    }

    private ObjectEntity getObjectEntity(String name) throws ScriptingErrorLog.SemanticErrorException {
        ObjectEntity obj = objectEntities.get(name);
        if (obj == null) {
            LM.getErrLog().emitParamNotFoundError(LM.getParser(), name);
        }
        return obj;
    }

    public List<GroupObjectEntity> getGroupObjectsList(List<String> mapping) throws ScriptingErrorLog.SemanticErrorException {
        List<GroupObjectEntity> groupObjects = new ArrayList<GroupObjectEntity>();
        for (int i = 0; i < mapping.size(); i++) {
            GroupObjectEntity groupObject = getGroupObject(mapping.get(i));
            if (groupObject == null) {
                LM.getErrLog().emitParamNotFoundError(LM.getParser(), mapping.get(i));
            } else {
                groupObjects.add(groupObject);
            }

        }
        return groupObjects;
    }

    public MappedProperty getPropertyWithMapping(String name, List<String> mapping) throws ScriptingErrorLog.SemanticErrorException {
        LP<?> property = LM.findLPByCompoundName(name);
        if (property.property.interfaces.size() != mapping.size()) {
            LM.getErrLog().emitParamCountError(LM.getParser(), property, mapping.size());
        }
        return new MappedProperty(property, getMappingObjectsArray(mapping));
    }

    public void addScriptedPropertyDraws(List<String> properties, List<List<String>> mappings, FormPropertyOptions commonOptions, List<FormPropertyOptions> options) throws ScriptingErrorLog.SemanticErrorException {
        assert properties.size() == mappings.size();
        for (int i = 0; i < properties.size(); i++) {
            List<String> mapping = mappings.get(i);
            PropertyDrawEntity property;
            if (properties.get(i).equals("OBJVALUE")) {
                if (mapping.size() != 1) {
                    LM.getErrLog().emitParamCountError(LM.getParser(), 1, mapping.size());
                }

                //assertion, что создастся только один PropertyDrawEntity
                property = BaseUtils.<PropertyDrawEntity>single(
                        addPropertyDraw(LM.baseLM.objectValue, false, getMappingObjectsArray(mapping))
                );
            } else if (properties.get(i).equals("SELECTION")) {
                //assertion, что создастся только один PropertyDrawEntity
                property = BaseUtils.<PropertyDrawEntity>single(
                        addPropertyDraw(LM.baseLM.sessionGroup, false, getMappingObjectsArray(mapping))
                );
            } else if (properties.get(i).equals("ADDOBJ")) {
                if (mapping.size() != 1) {
                    LM.getErrLog().emitParamCountError(LM.getParser(), 1, mapping.size());
                }

                ObjectEntity[] obj = getMappingObjectsArray(mapping);
                LP<?> addObjAction = LM.getAddObjectAction(obj[0].baseClass);
                property = addPropertyDraw(addObjAction);
            } else if (properties.get(i).equals("ADDFORM") || properties.get(i).equals("ADDSESSIONFORM")) {
                if (mapping.size() != 1) {
                    LM.getErrLog().emitParamCountError(LM.getParser(), 1, mapping.size());
                }

                ObjectEntity[] obj = getMappingObjectsArray(mapping);
                property = LM.addAddFormAction(this, obj[0], properties.get(i).equals("ADDSESSIONFORM"));
            } else if (properties.get(i).equals("EDITFORM") || properties.get(i).equals("EDITSESSIONFORM")) {
                if (mapping.size() != 1) {
                    LM.getErrLog().emitParamCountError(LM.getParser(), 1, mapping.size());
                }

                ObjectEntity[] obj = getMappingObjectsArray(mapping);
                property = LM.addEditFormAction(this, obj[0], properties.get(i).equals("EDITSESSIONFORM"));
            } else {
                MappedProperty prop = getPropertyWithMapping(properties.get(i), mapping);
                property = addPropertyDraw(prop.property, prop.mapping);
            }
            applyPropertyOptions(property, commonOptions.overrideWith(options.get(i)));
        }
    }

    public void applyPropertyOptions(PropertyDrawEntity property, FormPropertyOptions options) {
        if (options.getReadOnly() != null) {
            property.readOnly = options.getReadOnly();
        }

        if (options.getColumns() != null) {
            property.columnGroupObjects = options.getColumns();
        }

        property.propertyCaption = options.getHeader();
        property.propertyFooter = options.getFooter();
        property.propertyHighlight = options.getHighlightIf();
        MappedProperty showIf = options.getShowIf();
        if (showIf != null) {
            LM.showIf(this, property, showIf.property, showIf.mapping);
        }
    }

    public void addScriptedFilters(List<String> properties, List<List<String>> mappings) throws ScriptingErrorLog.SemanticErrorException {
        assert properties.size() == mappings.size();
        for (int i = 0; i < properties.size(); i++) {
            MappedProperty prop = getPropertyWithMapping(properties.get(i), mappings.get(i));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(prop.property, prop.mapping)));
        }
    }

    public void addScriptedRegularFilterGroup(String sid, List<String> captions, List<String> keystrokes, List<String> properties, List<List<String>> mappings) throws ScriptingErrorLog.SemanticErrorException {
        assert captions.size() == mappings.size() && keystrokes.size() == mappings.size() && properties.size() == mappings.size();

        RegularFilterGroupEntity regularFilterGroup = new RegularFilterGroupEntity(genID());
        regularFilterGroup.setSID(sid);

        for (int i = 0; i < properties.size(); i++) {
            String caption = captions.get(i);
            KeyStroke keyStroke = KeyStroke.getKeyStroke(keystrokes.get(i));
            MappedProperty property = getPropertyWithMapping(properties.get(i), mappings.get(i));

            if (keyStroke == null) {
                LM.getErrLog().emitWrongKeyStrokeFormat(LM.getParser(), keystrokes.get(i));
            }

            regularFilterGroup.addFilter(
                    new RegularFilterEntity(genID(), new NotNullFilterEntity(addPropertyObject(property.property, property.mapping)), caption, keyStroke)
            );
        }

        addRegularFilterGroup(regularFilterGroup);
    }

    public PropertyObjectEntity addPropertyObject(String property, List<String> mapping) throws ScriptingErrorLog.SemanticErrorException {
        MappedProperty prop = getPropertyWithMapping(property, mapping);
        return addPropertyObject(prop.property, prop.mapping);
    }

    public void addScriptedDefaultOrder(List<String> properties, List<Boolean> orders) throws ScriptingErrorLog.SemanticErrorException {
        for (int i = 0; i < properties.size(); ++i) {
            String alias = properties.get(i);
            Boolean order = orders.get(i);

            addDefaultOrder(getPropertyDrawByAlias(alias), order);
        }
    }

    private PropertyDrawEntity getPropertyDrawByAlias(String alias) throws ScriptingErrorLog.SemanticErrorException {
        //todo: переделать, когда будут реализованы алиасы для свойств в форме

        PropertyDrawEntity property = getPropertyDraw(LM.findLPByCompoundName(alias));
        if (property == null) {
            LM.getErrLog().emitPropertyNotFoundError(LM.getParser(), alias);
        }

        return property;
    }

    public void setAsDialogForm(String className, String objectID) throws ScriptingErrorLog.SemanticErrorException {
        findCustomClassForFormSetup(className).setDialogForm(this, getObjectEntity(objectID));
    }

    public void setAsEditForm(String className, String objectID) throws ScriptingErrorLog.SemanticErrorException {
        findCustomClassForFormSetup(className).setEditForm(this, getObjectEntity(objectID));
    }

    public void setAsListForm(String className, String objectID) throws ScriptingErrorLog.SemanticErrorException {
        findCustomClassForFormSetup(className).setListForm(this, getObjectEntity(objectID));
    }

    private CustomClass findCustomClassForFormSetup(String className) throws ScriptingErrorLog.SemanticErrorException {
        ValueClass valueClass = LM.findClassByCompoundName(className);
        if (!(valueClass instanceof CustomClass)) {
            LM.getErrLog().emitBuiltInClassFormSetupError(LM.getParser(), className);
        }

        return (CustomClass) valueClass;
    }
}
