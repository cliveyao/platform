package platform.server.logics.property;

import platform.base.col.interfaces.immutable.ImCol;
import platform.base.col.interfaces.immutable.ImMap;
import platform.base.col.interfaces.immutable.ImOrderSet;
import platform.server.data.expr.Expr;
import platform.server.data.where.WhereBuilder;
import platform.server.form.entity.drilldown.DrillDownFormEntity;
import platform.server.form.entity.drilldown.SumUnionDrillDownFormEntity;
import platform.server.logics.BusinessLogics;
import platform.server.session.PropertyChanges;

import static platform.base.BaseUtils.capitalize;
import static platform.server.logics.ServerResourceBundle.getString;

public class SumUnionProperty extends IncrementUnionProperty {

    public SumUnionProperty(String sID, String caption, ImOrderSet<Interface> interfaces, ImMap<CalcPropertyInterfaceImplement<Interface>, Integer> operands) {
        super(sID, caption, interfaces);
        this.operands = operands;

        finalizeInit();
    }

    private final ImMap<CalcPropertyInterfaceImplement<Interface>,Integer> operands;

    public ImCol<CalcPropertyInterfaceImplement<Interface>> getOperands() {
        return operands.keys();
    }

    protected Expr calculateNewExpr(ImMap<Interface, ? extends Expr> joinImplement, boolean propClasses, PropertyChanges propChanges, WhereBuilder changedWhere) {
        Expr result = Expr.NULL;
        for(int i=0,size=operands.size();i<size;i++)
            result = result.sum(operands.getKey(i).mapExpr(joinImplement, propClasses, propChanges, changedWhere).scale(operands.getValue(i)));
        return result;
    }

    protected Expr calculateIncrementExpr(ImMap<Interface, ? extends Expr> joinImplement, PropertyChanges propChanges, Expr prevExpr, WhereBuilder changedWhere) {
        Expr result = prevExpr;
        for(int i=0,size=operands.size();i<size;i++) {
            WhereBuilder changedOperandWhere = new WhereBuilder();
            CalcPropertyInterfaceImplement<Interface> operand = operands.getKey(i);
            Expr newOperandExpr = operand.mapExpr(joinImplement, propChanges, changedOperandWhere);
            Expr prevOperandExpr = operand.mapExpr(joinImplement);
            result = result.sum(newOperandExpr.diff(prevOperandExpr).and(changedOperandWhere.toWhere()).scale(operands.getValue(i)));
            if(changedWhere!=null) changedWhere.add(changedOperandWhere.toWhere());
        }
        return result;
    }

    @Override
    public boolean supportsDrillDown() {
        return true;
    }

    @Override
    public DrillDownFormEntity createDrillDownForm(BusinessLogics BL) {
        return new SumUnionDrillDownFormEntity(
                "drillDown" + capitalize(getSID()) + "Form",
                getString("logics.property.drilldown.form.sum.union"), this, BL
        );
    }
}
