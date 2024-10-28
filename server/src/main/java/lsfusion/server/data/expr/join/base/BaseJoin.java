package lsfusion.server.data.expr.join.base;

import lsfusion.base.Result;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.server.data.expr.BaseExpr;
import lsfusion.server.data.expr.NullableExprInterface;
import lsfusion.server.data.expr.inner.InnerExpr;
import lsfusion.server.data.stat.*;

public interface BaseJoin<K> {

    default ImSet<NullableExprInterface> getExprFollows(boolean includeInnerWithoutNotNull, boolean recursive) {
        return InnerExpr.getInnerFollows(this).getExprFollows(getJoins(), includeInnerWithoutNotNull, recursive);
    }

    ImMap<K, BaseExpr> getJoins();

    StatKeys<K> getStatKeys(KeyStat keyStat, StatType type);

    // pushedKeys могут / должны заполняться только для InnerJoin
    Cost getPushedCost(KeyStat keyStat, StatType type, Cost pushCost, Stat pushStat, ImMap<K, Stat> pushKeys, ImMap<K, Stat> pushNotNullKeys, ImMap<BaseExpr, Stat> pushProps, Result<ImSet<K>> rPushedKeys, Result<ImSet<BaseExpr>> rPushedProps); // должен кэшироваться, так как несколько раз в цикле выполняется
}
