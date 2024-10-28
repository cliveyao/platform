package lsfusion.server.data.stat;

import lsfusion.base.BaseUtils;
import lsfusion.base.Result;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.mutability.TwinImmutableObject;
import lsfusion.server.data.caches.AbstractOuterContext;
import lsfusion.server.data.caches.OuterContext;
import lsfusion.server.data.caches.hash.HashContext;
import lsfusion.server.data.expr.BaseExpr;
import lsfusion.server.data.expr.join.inner.InnerJoins;
import lsfusion.server.data.expr.join.where.WhereJoin;
import lsfusion.server.data.translate.MapTranslate;

public class StatKeysJoin<K extends BaseExpr> extends AbstractOuterContext<StatKeysJoin<K>> implements WhereJoin<K, StatKeysJoin<K>> {
    
    private final StatKeys<K> stat;

    public StatKeysJoin(StatKeys<K> stat) {
        this.stat = stat;
    }

    protected ImSet<OuterContext> calculateOuterDepends() {
        return BaseUtils.immutableCast(stat.getKeys());
    }

    protected StatKeysJoin<K> translate(MapTranslate translator) {
        return new StatKeysJoin<>(StatKeys.translateOuter(stat, translator));
    }

    public int hash(HashContext hash) {
        return StatKeys.hashOuter(stat, hash);
    }

    public ImMap<K, BaseExpr> getJoins() {
        return BaseUtils.immutableCast(stat.getKeys().toMap());
    }

    public StatKeys<K> getStatKeys(KeyStat keyStat, StatType type) {
        return stat;
    }

    public boolean calcTwins(TwinImmutableObject o) {
        return stat.equals(((StatKeysJoin<K>) o).stat);
    }

    @Override
    public Cost getPushedCost(KeyStat keyStat, StatType type, Cost pushCost, Stat pushStat, ImMap<K, Stat> pushKeys, ImMap<K, Stat> pushNotNullKeys, ImMap<BaseExpr, Stat> pushProps, Result<ImSet<K>> rPushedKeys, Result<ImSet<BaseExpr>> rPushedProps) {
        return stat.getCost(); // считаем, что не редуцируется
    }

    public InnerJoins getInnerJoins() {
        throw new UnsupportedOperationException();
    }
}
