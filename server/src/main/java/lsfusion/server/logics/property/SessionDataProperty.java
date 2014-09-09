package lsfusion.server.logics.property;

import lsfusion.base.Pair;
import lsfusion.base.col.MapFact;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.mutable.add.MAddExclMap;
import lsfusion.server.caches.ManualLazy;
import lsfusion.server.classes.ValueClass;
import lsfusion.server.data.expr.Expr;
import lsfusion.server.data.expr.where.cases.CaseExpr;
import lsfusion.server.data.where.WhereBuilder;
import lsfusion.server.logics.property.derived.DerivedProperty;
import lsfusion.server.session.PropertyChanges;

public class SessionDataProperty extends DataProperty {

    public SessionDataProperty(String caption, ValueClass value) {
        this(caption, new ValueClass[0], value);
    }

    public SessionDataProperty(String caption, ValueClass[] classes, ValueClass value) {
        this(caption, classes, value, false);
    }

    private final boolean noClasses;
    public SessionDataProperty(String caption, ValueClass[] classes, ValueClass value, boolean noClasses) {
        super(caption, classes, value);

        this.noClasses = noClasses;
        
        finalizeInit();
    }

    @Override
    protected boolean noClasses() {
        return noClasses;
    }

    @Override
    public Expr calculateExpr(ImMap<ClassPropertyInterface, ? extends Expr> joinImplement, CalcType calcType, PropertyChanges propChanges, WhereBuilder changedWhere) {
        if(calcType instanceof CalcClassType)
            return getClassTableExpr(joinImplement, (CalcClassType) calcType);
        if(propChanges.isEmpty())
            return CaseExpr.NULL;
        return super.calculateExpr(joinImplement, calcType, propChanges, changedWhere);
    }

    public boolean isStored() {
        return false;
    }

}

