package lsfusion.server.data.expr.formula;

import lsfusion.base.BaseUtils;
import lsfusion.base.col.interfaces.immutable.ImCol;
import lsfusion.base.col.interfaces.immutable.ImList;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.mutable.MMap;
import lsfusion.base.mutability.TwinImmutableObject;
import lsfusion.server.data.caches.hash.HashContext;
import lsfusion.server.data.expr.BaseExpr;
import lsfusion.server.data.expr.Expr;
import lsfusion.server.data.expr.classes.StaticClassNullableExpr;
import lsfusion.server.data.expr.classes.VariableSingleClassExpr;
import lsfusion.server.data.expr.join.inner.InnerBaseJoin;
import lsfusion.server.data.expr.key.KeyType;
import lsfusion.server.data.query.compile.CompileSource;
import lsfusion.server.data.query.compile.FJData;
import lsfusion.server.data.stat.KeyStat;
import lsfusion.server.data.stat.Stat;
import lsfusion.server.data.translate.ExprTranslator;
import lsfusion.server.data.translate.MapTranslate;
import lsfusion.server.data.type.Type;
import lsfusion.server.data.where.Where;
import lsfusion.server.logics.classes.ConcreteClass;
import lsfusion.server.logics.classes.user.set.AndClassSet;

public class FormulaNullableExpr extends StaticClassNullableExpr implements FormulaExprInterface {

    protected final ImList<BaseExpr> exprs;

    protected final FormulaJoinImpl formula;

    public FormulaJoinImpl getFormula() {
        return formula;
    }

    public ImList<BaseExpr> getFParams() {
        return exprs;
    }

    protected ImCol<Expr> getParams() {
        return BaseUtils.immutableCast(exprs.getCol());
    }

    public FormulaNullableExpr(ImList<BaseExpr> exprs, FormulaJoinImpl formula) {
        assert formula.hasNotNull(exprs);
        this.exprs = exprs;
        this.formula = formula;
    }

    public void fillAndJoinWheres(MMap<FJData, Where> joins, Where andWhere) {
        FormulaExpr.fillAndJoinWheres(this, joins, andWhere);
    }

    public Expr packFollowFalse(Where where) {
        return FormulaExpr.packFollowFalse(this, where);
    }

    public String getSource(final CompileSource compile, boolean needValue) {
        return FormulaExpr.getSource(this, compile, needValue);
    }
    public String toString() {
        return FormulaExpr.toString(this);
    }

    public ConcreteClass getStaticClass(KeyType keyType) {
        return FormulaExpr.getStaticClass(this, keyType);
    }
    public AndClassSet getAndClassSet(ImMap<VariableSingleClassExpr, AndClassSet> and) {
        return FormulaExpr.getFormulaAndClassSet(this, and);
    }

    @Override
    public Type getType(final KeyType keyType) {
        return FormulaExpr.getType(this, keyType);
    }

    public Expr translate(ExprTranslator translator) {
        return FormulaExpr.translateExpr(this, translator);
    }

    public Stat getTypeStat(KeyStat keyStat, boolean forJoin) {
        return FormulaExpr.getTypeStat(this, keyStat, forJoin);
    }

    public InnerBaseJoin<?> getBaseJoin() {
        return FormulaExpr.getBaseJoin(this);
    }

    protected boolean isComplex() {
        return FormulaExpr.isComplex(this);
    }

    public boolean calcTwins(TwinImmutableObject o) {
        return exprs.equals(((FormulaNullableExpr) o).exprs) && formula.equals(((FormulaNullableExpr) o).formula);
    }

    public int hash(HashContext hashContext) {
        return 31 * hashOuter(exprs, hashContext) + formula.hashCode();
    }

    protected FormulaNullableExpr translate(MapTranslate translator) {
        return new FormulaNullableExpr(translator.translateDirect(exprs), formula);
    }
}
