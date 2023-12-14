package lsfusion.server.logics.form.stat.struct.export.hierarchy.json;

import lsfusion.base.Pair;
import lsfusion.base.col.interfaces.immutable.*;
import lsfusion.server.logics.classes.ValueClass;
import lsfusion.server.logics.classes.data.file.JSONClass;
import lsfusion.server.logics.classes.data.file.JSONTextClass;
import lsfusion.server.logics.form.open.FormAction;
import lsfusion.server.logics.form.open.FormSelector;
import lsfusion.server.logics.form.open.ObjectSelector;
import lsfusion.server.logics.form.stat.AbstractFormDataInterface;
import lsfusion.server.logics.form.stat.StaticDataGenerator;
import lsfusion.server.logics.form.stat.struct.hierarchy.ParseNode;
import lsfusion.server.logics.form.struct.FormEntity;
import lsfusion.server.logics.form.struct.filter.ContextFilterSelector;
import lsfusion.server.logics.form.struct.object.GroupObjectEntity;
import lsfusion.server.logics.form.struct.object.ObjectEntity;
import lsfusion.server.logics.property.LazyProperty;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.logics.property.classes.infer.ExClassSet;
import lsfusion.server.logics.property.classes.infer.InferType;
import lsfusion.server.logics.property.implement.PropertyMapImplement;
import lsfusion.server.logics.property.oraction.PropertyInterface;
import lsfusion.server.physics.dev.i18n.LocalizedString;

public class JSONProperty<O extends ObjectSelector> extends LazyProperty {

    public final FormSelector<O> form;
    public final ImRevMap<O, ClassPropertyInterface> mapObjects;

    private final ImSet<ClassPropertyInterface> notNullInterfaces;

    // CONTEXT
    protected final ImSet<ClassPropertyInterface> contextInterfaces;
    protected final ImSet<ContextFilterSelector<ClassPropertyInterface, O>> contextFilters;

    private boolean returnString;

    public JSONProperty(LocalizedString caption, FormSelector<O> form, ImList<O> objectsToSet, ImList<Boolean> nulls,
                            ImOrderSet<PropertyInterface> orderContextInterfaces,
                        ImSet<ContextFilterSelector<PropertyInterface, O>> contextFilters, boolean returnString) {
        super(caption, FormAction.getValueClasses(form, objectsToSet, orderContextInterfaces.size(), new ValueClass[0]));

        this.form = form;

        ImOrderSet<ClassPropertyInterface> orderInterfaces = getOrderInterfaces();
        ImOrderSet<ClassPropertyInterface> objectInterfaces = orderInterfaces.subOrder(0, objectsToSet.size());

        mapObjects = objectInterfaces.mapOrderRevKeys(objectsToSet::get);
        this.notNullInterfaces = objectInterfaces.mapOrderValues(nulls::get).filterFnValues(element -> !element).keys();

        ImRevMap<PropertyInterface, ClassPropertyInterface> mapContextInterfaces = orderContextInterfaces.mapSet(orderInterfaces.subOrder(objectsToSet.size(), objectsToSet.size() + orderContextInterfaces.size()));
        this.contextInterfaces = mapContextInterfaces.valuesSet();
        this.contextFilters = contextFilters.mapSetValues(filter -> filter.map(mapContextInterfaces));

        this.returnString = returnString;
    }

    @Override
    protected ExClassSet calcInferValueClass(ImMap<ClassPropertyInterface, ExClassSet> inferred, InferType inferType) {
        return ExClassSet.toExValue(returnString ? JSONTextClass.instance : JSONClass.instance);
    }

    @Override
    protected PropertyMapImplement<?, ClassPropertyInterface> createProperty() {
        Pair<FormEntity, ImRevMap<ObjectEntity, O>> staticForm = this.form.getForm(getBaseLM());
        ImRevMap<ObjectEntity, ClassPropertyInterface> mappedObjects = staticForm.second.rightJoin(this.mapObjects);

        ImSet<GroupObjectEntity> valueGroups = AbstractFormDataInterface.getValueGroupObjects(mappedObjects.keys());

        StaticDataGenerator.Hierarchy staticHierarchy = staticForm.first.getStaticHierarchy(false, valueGroups, null);

        ParseNode parseNode = staticHierarchy.getIntegrationHierarchy();

        FormPropertyDataInterface<ClassPropertyInterface> formInterface = new FormPropertyDataInterface<>(staticForm.first, valueGroups, ContextFilterSelector.getEntities(contextFilters).mapSetValues(entity -> entity.mapObjects(staticForm.second.reverse())));

        return parseNode.getJSONProperty(formInterface, contextInterfaces.toRevMap(), mappedObjects, returnString);
    }
}

