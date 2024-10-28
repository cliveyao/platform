package lsfusion.server.data.expr.join.base;

import lsfusion.base.Result;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.mutability.TwinImmutableObject;
import lsfusion.server.data.expr.BaseExpr;
import lsfusion.server.data.expr.inner.InnerExpr;
import lsfusion.server.data.expr.join.inner.InnerBaseJoin;
import lsfusion.server.data.expr.join.where.WhereJoins;
import lsfusion.server.data.stat.*;

public abstract class CalculateJoin<K> extends TwinImmutableObject implements InnerBaseJoin<K> {

    public StatKeys<K> getStatKeys(final KeyStat keyStat, StatType type) {
        return getCalcStatKeys(keyStat);
    }

    private StatKeys<K> getCalcStatKeys(final KeyStat keyStat) {
        Stat totalStat = Stat.ONE;
        ImMap<K, BaseExpr> joins = WhereJoins.getJoinsForStat(this);
        ImMap<K, Stat> distinct = joins.mapValues(value -> value.getTypeStat(keyStat, true));
        for(Stat stat : distinct.valueIt())
            totalStat = totalStat.mult(stat);
        return new StatKeys<>(totalStat, new DistinctKeys<>(distinct)); // , Cost.CALC
    }

    @Override
    public Cost getPushedCost(KeyStat keyStat, StatType type, Cost pushCost, Stat pushStat, ImMap<K, Stat> pushKeys, ImMap<K, Stat> pushNotNullKeys, ImMap<BaseExpr, Stat> pushProps, Result<ImSet<K>> rPushedKeys, Result<ImSet<BaseExpr>> rPushedProps) {
//        StatKeys<K> statKeys = getCalcStatKeys(keyStat);
        if(pushKeys.size() < getJoins().size()) // не все ключи есть, запретим выбирать
            return Cost.ALOT;
        return pushCost; // иначе cost равен cost'у контекста
    }

    public boolean hasExprFollowsWithoutNotNull() {
        return InnerExpr.hasExprFollowsWithoutNotNull(this);
    }
}
