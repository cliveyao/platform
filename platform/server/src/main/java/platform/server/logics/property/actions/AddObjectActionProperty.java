package platform.server.logics.property.actions;

import platform.base.OrderedMap;
import platform.interop.ClassViewType;
import platform.interop.KeyStrokes;
import platform.server.classes.AbstractCustomClass;
import platform.server.classes.ConcreteCustomClass;
import platform.server.classes.CustomClass;
import platform.server.classes.ObjectClass;
import platform.server.data.expr.Expr;
import platform.server.data.expr.KeyExpr;
import platform.server.data.where.Where;
import platform.server.form.entity.FormEntity;
import platform.server.form.entity.PropertyDrawEntity;
import platform.server.form.view.DefaultFormView;
import platform.server.form.view.PropertyDrawView;
import platform.server.logics.DataObject;
import platform.server.logics.ObjectValue;
import platform.server.logics.ServerResourceBundle;
import platform.server.logics.property.*;
import platform.server.logics.property.actions.flow.ExtendContextActionProperty;
import platform.server.logics.property.actions.flow.FlowResult;
import platform.server.logics.property.derived.DerivedProperty;
import platform.server.session.PropertyChange;
import platform.server.session.PropertySet;
import platform.server.session.SinglePropertyTableUsage;

import java.sql.SQLException;
import java.util.*;

import static platform.base.BaseUtils.merge;

public class AddObjectActionProperty<T extends PropertyInterface, I extends PropertyInterface> extends ExtendContextActionProperty<I> {

    protected final CustomClass valueClass; // обозначает класс объекта, который нужно добавить
    private final boolean forceDialog; // если класс конкретный и имеет потомков

    protected CalcPropertyMapImplement<T, I> where;
    private CalcPropertyMapImplement<?, I> result; // только extend интерфейсы

    public <T extends PropertyInterface> AddObjectActionProperty(String sID, CustomClass valueClass, boolean forceDialog, CalcProperty<T> result) {
        this(sID, valueClass, forceDialog, new ArrayList<I>(), new ArrayList<I>(), null, result!=null ? new CalcPropertyMapImplement<T, I>(result) : null);
    }

    public AddObjectActionProperty(String sID, CustomClass valueClass, boolean forceDialog, Collection<I> innerInterfaces, List<I> mapInterfaces, CalcPropertyMapImplement<T, I> where, CalcPropertyMapImplement<?, I> result) {
        super(sID, ServerResourceBundle.getString("logics.add"), innerInterfaces, mapInterfaces);
        
        this.valueClass = valueClass;
        this.forceDialog = forceDialog;
        
        this.where = where;
        this.result = result;
        
        assert where==null || !needDialog();

        assert where==null || result==null || innerInterfaces.containsAll(merge(where.mapping.values(), result.mapping.values()));
    }
    
    protected boolean needDialog() {
        return valueClass instanceof AbstractCustomClass || (forceDialog && valueClass.hasChildren());
    }

    public Set<ActionProperty> getDependActions() {
        return new HashSet<ActionProperty>();
    }

    @Override
    public PropsNewSession aspectUsedExtProps() {
        if(where==null)
            return new PropsNewSession();
        return getUsedProps(where);
    }

    @Override
    public PropsNewSession aspectChangeExtProps() {
        PropsNewSession result = new PropsNewSession();
        if(this.result!=null)
            result.addAll(this.result.property.getChangeProps());
        result.addAll(valueClass.getParentSetProps());
        result.add(valueClass.getBaseClass().getObjectClassProperty());
        return result;
    }

    @Override
    public String getCode() {
        return "getAddObjectAction(" + valueClass.getSID() + ")";
    }

    @Override
    public CustomClass getSimpleAdd() {
        if(where==null && !needDialog())
            return valueClass;
        return null;
    }

    protected FlowResult executeExtend(ExecutionContext<PropertyInterface> context, Map<I, KeyExpr> innerKeys, Map<I, DataObject> innerValues, Map<I, Expr> innerExprs) throws SQLException {
        ObjectClass readClass;
        if (needDialog()) {
            ObjectValue objectValue = context.requestUserClass(valueClass, valueClass, true);
            if (!(objectValue instanceof DataObject)) // cancel
                return FlowResult.FINISH;
            readClass = valueClass.getBaseClass().findClassID((Integer) ((DataObject) objectValue).object);
        } else
            readClass = valueClass;

        executeRead(context, innerKeys, innerExprs, (ConcreteCustomClass) readClass);

        return FlowResult.FINISH;
    }

    protected void executeRead(ExecutionContext<PropertyInterface> context, Map<I, KeyExpr> innerKeys, Map<I, Expr> innerExprs, ConcreteCustomClass readClass) throws SQLException {
        PropertyChange<I> resultChange;
        if(where==null) // оптимизация, один объект добавляем
            resultChange = new PropertyChange<I>(context.addObject(readClass));
        else {
            Where exprWhere = where.mapExpr(innerExprs, context.getModifier()).getWhere();
            if(exprWhere.isFalse()) // оптимизация, важна так как во многих event'ах может учавствовать
                return;
            resultChange = SinglePropertyTableUsage.getChange(context.addObjects(readClass, new PropertySet<I>(innerKeys, exprWhere, new OrderedMap<Expr, Boolean>(), false)));
        }

        if(result != null) {
            context.getSession().dropChanges((SessionDataProperty) result.property); // предполагается что пишем в SessionData, потом можно дообобщить
            result.change(context.getEnv(), resultChange);
        }
    }

    @Override
    public void proceedDefaultDraw(PropertyDrawEntity<PropertyInterface> entity, FormEntity<?> form) {
        super.proceedDefaultDraw(entity, form);
        entity.setDrawToToolbar(true);
        entity.shouldBeLast = true;
        entity.forceViewType = ClassViewType.PANEL;

        entity.toDraw = form.getObject(valueClass).groupTo;
    }

    @Override
    public void proceedDefaultDesign(PropertyDrawView propertyView, DefaultFormView view) {
        super.proceedDefaultDesign(propertyView, view);
        propertyView.editKey = KeyStrokes.getAddActionPropertyKeyStroke();
        propertyView.design.setIconPath("add.png");
        propertyView.showEditKey = false;
    }

    protected CalcPropertyMapImplement<?, I> getGroupWhereProperty() {
        if(where==null)
            return DerivedProperty.createTrue();
        return where;
    }
}
