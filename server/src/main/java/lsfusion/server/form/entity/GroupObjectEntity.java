package lsfusion.server.form.entity;

import lsfusion.base.BaseUtils;
import lsfusion.base.col.MapFact;
import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.immutable.ImOrderSet;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.col.interfaces.mutable.LongMutable;
import lsfusion.base.col.interfaces.mutable.MExclMap;
import lsfusion.base.col.interfaces.mutable.MOrderExclSet;
import lsfusion.base.col.interfaces.mutable.mapvalue.GetIndex;
import lsfusion.base.col.interfaces.mutable.mapvalue.GetValue;
import lsfusion.base.identity.IdentityObject;
import lsfusion.interop.ClassViewType;
import lsfusion.server.classes.ValueClass;
import lsfusion.server.form.instance.GroupObjectInstance;
import lsfusion.server.form.instance.InstanceFactory;
import lsfusion.server.form.instance.Instantiable;
import lsfusion.server.logics.property.CalcPropertyRevImplement;
import lsfusion.server.logics.property.ClassPropertyInterface;
import lsfusion.server.logics.property.derived.DerivedProperty;

import java.util.ArrayList;
import java.util.List;

import static lsfusion.interop.ClassViewType.*;

public class GroupObjectEntity extends IdentityObject implements Instantiable<GroupObjectInstance> {

    public static int PAGE_SIZE_DEFAULT_VALUE = 50;

    public TreeGroupEntity treeGroup;

    public CalcPropertyObjectEntity<?> reportPathProp;
    
    public boolean noClassFilter = false;

    public GroupObjectEntity() {
    }

    public GroupObjectEntity(int ID) {
        this(ID, (String)null);
    }

    public GroupObjectEntity(int ID, String sID) {
        super(ID, sID != null ? sID : "groupObj" + ID);
    }

    public ClassViewType initClassView = GRID;
    public List<ClassViewType> banClassView = new ArrayList<ClassViewType>();
    public Integer pageSize;

    public CalcPropertyObjectEntity<?> propertyBackground;
    public CalcPropertyObjectEntity<?> propertyForeground;

    private boolean finalizedProps = false;
    private Object props = MapFact.mExclMap();
    public CalcPropertyRevImplement<ClassPropertyInterface, ObjectEntity> getProperty(GroupObjectProp type) {
        assert finalizedObjects && !finalizedProps;
        MExclMap<GroupObjectProp, CalcPropertyRevImplement<ClassPropertyInterface, ObjectEntity>> mProps = (MExclMap<GroupObjectProp, CalcPropertyRevImplement<ClassPropertyInterface, ObjectEntity>>) props;
        CalcPropertyRevImplement<ClassPropertyInterface, ObjectEntity> prop = mProps.get(type);
        if(prop==null) { // type.getSID() + "_" + getSID() нельзя потому как надо еще SID формы подмешивать
            prop = DerivedProperty.createDataPropRev(type.toString() + " (" + objects.toString() + ")", getObjects().mapValues(new GetValue<ValueClass, ObjectEntity>() {
                public ValueClass getMapValue(ObjectEntity value) {
                    return value.baseClass;
                }}), type.getValueClass());
            mProps.exclAdd(type, prop);
        }
        return prop;
    }

    public ImMap<GroupObjectProp, CalcPropertyRevImplement<ClassPropertyInterface, ObjectEntity>> getProperties() {
        if(!finalizedProps) {
            props = ((MExclMap<GroupObjectProp, CalcPropertyRevImplement<ClassPropertyInterface, ObjectEntity>>) props).immutable();
            finalizedProps = true;
        }
        return (ImMap<GroupObjectProp, CalcPropertyRevImplement<ClassPropertyInterface, ObjectEntity>>) props;
    }

    public GroupObjectInstance getInstance(InstanceFactory instanceFactory) {
        return instanceFactory.getInstance(this);
    }

    public ImMap<ObjectEntity, CalcPropertyObjectEntity<?>> isParent = null;

    public void setIsParents(final CalcPropertyObjectEntity... properties) {
        isParent = getOrderObjects().mapOrderValues(new GetIndex<CalcPropertyObjectEntity<?>>() {
            public CalcPropertyObjectEntity<?> getMapValue(int i) {
                return properties[i];
            }});
    }

    public void setInitClassView(ClassViewType type) {
        initClassView = type;
    }

    public void setSingleClassView(ClassViewType type) {
        setInitClassView(type);
        banClassView.addAll(BaseUtils.toList(PANEL, GRID, HIDE));
        banClassView.remove(type);
    }

    public boolean isAllowedClassView(ClassViewType type) {
        return !banClassView.contains(type);
    }

    public boolean isForcedPanel() {
        return initClassView == PANEL && !isAllowedClassView(GRID) && !isAllowedClassView(HIDE);
    }

    private boolean finalizedObjects;
    private Object objects = SetFact.mOrderExclSet();

    public ImSet<ObjectEntity> getObjects() {
        return getOrderObjects().getSet();
    }
    @LongMutable
    public ImOrderSet<ObjectEntity> getOrderObjects() {
        if(!finalizedObjects) {
            finalizedObjects = true;
            objects = ((MOrderExclSet<ObjectEntity>)objects).immutableOrder();
        }

        return (ImOrderSet<ObjectEntity>)objects;
    }

    public void add(ObjectEntity objectEntity) {
        assert !finalizedObjects;
        objectEntity.groupTo = this;
        ((MOrderExclSet<ObjectEntity>)objects).exclAdd(objectEntity);
    }
    
    public void setObjects(ImOrderSet<ObjectEntity> objects) {
        assert !finalizedObjects;
        finalizedObjects = true;
        this.objects = objects;
    }
    public GroupObjectEntity(int ID, ImOrderSet<ObjectEntity> objects) {
        this(ID, objects, false);
    }

    public GroupObjectEntity(int ID, ImOrderSet<ObjectEntity> objects, boolean noClassFilter) {
        this(ID, (String)null);

        this.noClassFilter = noClassFilter; 
        setObjects(objects);
    }

}
