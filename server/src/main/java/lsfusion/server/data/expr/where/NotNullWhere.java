package lsfusion.server.data.expr.where;

import lsfusion.base.BaseUtils;
import lsfusion.base.Result;
import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImOrderSet;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.col.interfaces.mutable.MMap;
import lsfusion.base.mutability.TwinImmutableObject;
import lsfusion.interop.form.property.Compare;
import lsfusion.server.data.caches.OuterContext;
import lsfusion.server.data.caches.hash.HashContext;
import lsfusion.server.data.expr.BaseExpr;
import lsfusion.server.data.expr.Expr;
import lsfusion.server.data.expr.NullableExpr;
import lsfusion.server.data.expr.NullableExprInterface;
import lsfusion.server.data.expr.join.select.ExprIndexedJoin;
import lsfusion.server.data.expr.join.where.GroupJoinsWheres;
import lsfusion.server.data.expr.where.classes.data.BinaryWhere;
import lsfusion.server.data.query.compile.CompileSource;
import lsfusion.server.data.query.compile.FJData;
import lsfusion.server.data.stat.KeyStat;
import lsfusion.server.data.stat.StatType;
import lsfusion.server.data.translate.ExprTranslator;
import lsfusion.server.data.translate.MapTranslate;
import lsfusion.server.data.where.DataWhere;
import lsfusion.server.data.where.Where;

// из-за отсутствия множественного наследования приходится выделять (так было бы внутренним классом в NullableExpr)
public abstract class NotNullWhere extends DataWhere {

    protected abstract BaseExpr getExpr();

    protected boolean isComplex() {
        return false;
    }

    public String getSource(CompileSource compile) {
        return getExpr().getNotNullSource(compile);
    }

    @Override
    protected String getNotSource(CompileSource compile) {
        return getExpr().getNullSource(compile);
    }

    protected Where translate(MapTranslate translator) {
        return getExpr().translateOuter(translator).getNotNullWhere();
    }

    @Override
    public <K extends BaseExpr> GroupJoinsWheres groupNotJoinsWheres(ImSet<K> keepStat, StatType statType, KeyStat keyStat, ImOrderSet<Expr> orderTop, GroupJoinsWheres.Type type) {
        BaseExpr expr = getExpr();
        Result<Boolean> isOrderTop = new Result<>();
        if(BinaryWhere.needIndexedJoin(expr, Compare.EQUALS, null, orderTop, isOrderTop))
            return groupDataNotJoinsWheres(new ExprIndexedJoin(expr, isOrderTop.result), type); // кривовато конечно, но пока достаточно
        return super.groupNotJoinsWheres(keepStat, statType, keyStat, orderTop, type);
    }

    @Override
    public Where packFollowFalse(Where falseWhere) {
        BaseExpr expr = getExpr();
        Expr packExpr = expr.packFollowFalse(falseWhere);
//            if(packExpr instanceof BaseExpr) // чтобы бесконечных циклов не было
//                return ((BaseExpr)packExpr).getNotNullWhere();
        if(BaseUtils.hashEquals(packExpr, expr)) // чтобы бесконечных циклов не было
            return this;
        else
            return packExpr.getWhere();
    }

    public Where translate(ExprTranslator translator) {
        Expr expr = getExpr();
        Expr translateExpr = expr.translateExpr(translator);
//            if(translateExpr instanceof BaseExpr) // ??? в pack на это нарвались, здесь по идее может быть аналогичная ситуация
//                return ((BaseExpr)translateExpr).getNotNullWhere();
        if(BaseUtils.hashEquals(translateExpr, expr)) // чтобы бесконечных циклов не было
            return this;
        else
            return translateExpr.getWhere();
    }

    public ImSet<OuterContext> calculateOuterDepends() {
        return SetFact.singleton(getExpr());
    }

    protected void fillDataJoinWheres(MMap<FJData, Where> joins, Where andWhere) {
        getExpr().fillAndJoinWheres(joins,andWhere);
    }

    public int hash(HashContext hashContext) {
        return getExpr().hashOuter(hashContext);
    }

    protected ImSet<NullableExprInterface> getExprFollows() {
        return getExpr().getExprFollows(false, NullableExpr.FOLLOW, true);
    }

    public boolean calcTwins(TwinImmutableObject o) {
        return getExpr().equals(((NotNullWhere) o).getExpr());
    }
}
