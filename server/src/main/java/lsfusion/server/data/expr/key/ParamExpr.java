package lsfusion.server.data.expr.key;

import lsfusion.base.Result;
import lsfusion.base.col.MapFact;
import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.comb.map.GlobalInteger;
import lsfusion.base.mutability.TwinImmutableObject;
import lsfusion.server.data.caches.hash.HashContext;
import lsfusion.server.data.expr.BaseExpr;
import lsfusion.server.data.expr.Expr;
import lsfusion.server.data.expr.NullableExprInterface;
import lsfusion.server.data.expr.classes.VariableSingleClassExpr;
import lsfusion.server.data.expr.inner.InnerExpr;
import lsfusion.server.data.expr.join.inner.InnerBaseJoin;
import lsfusion.server.data.stat.*;
import lsfusion.server.data.translate.ExprTranslator;
import lsfusion.server.data.translate.MapTranslate;
import lsfusion.server.data.type.Type;

public abstract class ParamExpr extends VariableSingleClassExpr implements InnerBaseJoin<Object> {

    public Type getType(KeyType keyType) {
        return keyType.getKeyType(this);
    }
    public Stat getTypeStat(KeyStat keyStat, boolean forJoin) {
        return keyStat.getKeyStat(this, forJoin);
    }

    public Expr translate(ExprTranslator translator) {
        return this;
    }

    protected ParamExpr translate(MapTranslate translator) {
        return translator.translate(this);
    }
    public ParamExpr translateOuter(MapTranslate translator) {
        return (ParamExpr) aspectTranslate(translator);
    }

    @Override
    public int immutableHashCode() {
        return System.identityHashCode(this);
    }

    public int hash(HashContext hashContext) {
        return hashContext.keys.hash(this);
    }

    public boolean calcTwins(TwinImmutableObject obj) {
        return false;
    }

    public PropStat getStatValue(KeyStat keyStat, StatType type) {
        return PropStat.ALOT;
//        return FormulaExpr.getStatValue(this, keyStat);
    }

    public StatKeys<Object> getStatKeys(KeyStat keyStat, StatType type) {
        return new StatKeys<>(Stat.ALOT);
//        return new StatKeys<Object>(SetFact.EMPTY(), keyStat.getKeyStat(this));
    }

    @Override
    public Cost getPushedCost(KeyStat keyStat, StatType type, Cost pushCost, Stat pushStat, ImMap<Object, Stat> pushKeys, ImMap<Object, Stat> pushNotNullKeys, ImMap<BaseExpr, Stat> pushProps, Result<ImSet<Object>> rPushedKeys, Result<ImSet<BaseExpr>> rPushedProps) {
        assert pushKeys.isEmpty(); // входов нет
        assert pushProps.size() <= 1;
        if(pushProps.isEmpty())
            return Cost.ALOT;
        return new Cost(pushProps.get(this));
    }

    public InnerBaseJoin<?> getBaseJoin() {
        return this;
    }

    public ImMap<Object, BaseExpr> getJoins() {
        return MapFact.EMPTY();
    }

    @Override
    public ImSet<NullableExprInterface> getExprFollows(boolean includeInnerWithoutNotNull, boolean recursive) {
        return SetFact.EMPTY(); // to prevent infinite recursion
    }

    @Override
    public boolean hasExprFollowsWithoutNotNull() {
        return InnerExpr.hasExprFollowsWithoutNotNull(this);
    }

    protected ImSet<ParamExpr> getKeys() {
        return SetFact.singleton(this);
    }

    public abstract GlobalInteger getKeyClass();
}
