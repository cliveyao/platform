package platform.server.data.expr.query;

import platform.base.BaseUtils;
import platform.base.QuickSet;
import platform.base.TwinImmutableInterface;
import platform.server.caches.AbstractOuterContext;
import platform.server.caches.OuterContext;
import platform.server.caches.hash.HashContext;
import platform.server.classes.IntegralClass;
import platform.server.classes.LongClass;
import platform.server.data.expr.BaseExpr;
import platform.server.data.expr.Expr;
import platform.server.data.expr.InnerExpr;
import platform.server.data.expr.KeyExpr;
import platform.server.data.expr.where.pull.ExprPullWheres;
import platform.server.data.query.CompileSource;
import platform.server.data.translator.MapTranslate;
import platform.server.data.translator.PartialQueryTranslator;
import platform.server.data.translator.QueryTranslator;
import platform.server.data.where.Where;
import platform.server.data.where.classes.ClassExprWhere;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static platform.base.BaseUtils.reverse;

public class RecursiveExpr extends QueryExpr<KeyExpr, RecursiveExpr.Query, RecursiveJoin, RecursiveExpr, RecursiveExpr.QueryInnerContext> {

    public RecursiveExpr(Query query, Map<KeyExpr, BaseExpr> group) {
        super(query, group);
    }

    protected RecursiveExpr createThis(Query query, Map<KeyExpr, BaseExpr> group) {
        return new RecursiveExpr(query, group);
    }

    public RecursiveExpr(RecursiveExpr queryExpr, MapTranslate translator) {
        super(queryExpr, translator);
    }

    protected InnerExpr translate(MapTranslate translator) {
        return new RecursiveExpr(this, translator);
    }

    public static class Query extends AbstractOuterContext<Query> {
        public final Map<KeyExpr, KeyExpr> mapIterate; // новые на старые
        public final Expr initial; // может содержать и старый и новый контекст
        public final Expr step; // может содержать и старый и новый контекст

        protected boolean isComplex() {
            return true;
        }

        protected Query(Map<KeyExpr, KeyExpr> mapIterate, Expr initial, Expr step) {
            this.mapIterate = mapIterate;
            this.initial = initial;
            this.step = step;
        }

        protected Query(Query query, MapTranslate translator) {
            this.mapIterate = translator.translateMap(query.mapIterate);
            this.initial = query.initial.translateOuter(translator);
            this.step = query.step.translateOuter(translator);
        }

        protected Query translate(MapTranslate translator) {
            return new Query(this, translator);
        }

        protected QuickSet<OuterContext> calculateOuterDepends() {
            return new QuickSet<OuterContext>(step, initial);
        }

        public boolean twins(TwinImmutableInterface o) {
            return mapIterate.equals(((Query)o).mapIterate) && initial.equals(((Query)o).initial) && step.equals(((Query)o).step);
        }

        public Query calculatePack() {
            return new Query(mapIterate, initial.pack(), step.pack());
        }

        protected int hash(HashContext hash) {
            return 31 * (31 * hashMapOuter(mapIterate, hash) + initial.hashOuter(hash)) + step.hashOuter(hash);
        }
    }

    public final static IntegralClass type = LongClass.instance;
    public final static long maxvalue = Math.round(Math.sqrt(type.getInfiniteValue().doubleValue()));

    public static class QueryInnerContext extends QueryExpr.QueryInnerContext<KeyExpr, Query, RecursiveJoin, RecursiveExpr, QueryInnerContext> {
        public QueryInnerContext(RecursiveExpr thisObj) {
            super(thisObj);
        }

        public IntegralClass getType() {
            return type;
        }

        @Override
        protected Stat getTypeStat() {
            return getType().getTypeStat();
        }

        @Override
        protected Stat getStatValue() {
            return Stat.ALOT;
        }

        protected Expr getMainExpr() {
            throw new RuntimeException("should not be");
        }

        protected Where getFullWhere() {
            throw new RuntimeException("should not be");
        }

        protected boolean isSelect() {
            throw new RuntimeException("should not be");
        }
    }
    protected QueryInnerContext createInnerContext() {
        return new QueryInnerContext(this);
    }

    public RecursiveJoin getInnerJoin() {
        return new RecursiveJoin(getInner().getInnerKeys(), getInner().getInnerValues(), query.initial.getWhere(), query.step.getWhere(), query.mapIterate, group);
    }

    public Expr translateQuery(QueryTranslator translator) {
        return create(query.mapIterate, query.initial, query.step, translator.translate(group));
    }

    // новый на старый
    public static Expr create(final Map<KeyExpr, KeyExpr> mapIterate, final Expr initial, final Expr step, Map<KeyExpr, ? extends Expr> group) {
        return new ExprPullWheres<KeyExpr>() {
            protected Expr proceedBase(Map<KeyExpr, BaseExpr> map) {
                return createBase(mapIterate, initial, step, map);
            }
        }.proceed(group);
    }

    public static Expr createBase(Map<KeyExpr, KeyExpr> mapIterate, Expr initial, Expr step, Map<KeyExpr, BaseExpr> group) {
        Map<KeyExpr,BaseExpr> restGroup = new HashMap<KeyExpr, BaseExpr>();
        Map<KeyExpr,BaseExpr> translate = new HashMap<KeyExpr, BaseExpr>();
        for(Map.Entry<KeyExpr,BaseExpr> groupKey : group.entrySet())
            if(groupKey.getValue().isValue() && !mapIterate.containsKey(groupKey.getKey())) // если статичная часть
                translate.put(groupKey.getKey(), groupKey.getValue());
            else
                restGroup.put(groupKey.getKey(), groupKey.getValue());
        if(translate.size()>0) {
            QueryTranslator translator = new PartialQueryTranslator(translate);
            initial = initial.translateQuery(translator);
            step = step.translateQuery(translator);
        }

        RecursiveExpr expr = new RecursiveExpr(new Query(mapIterate, initial, step), restGroup);
        if(expr.getInnerJoin().getFullStepWhere().isFalse()) // чтобы кэшировалось
            return GroupExpr.create(BaseUtils.toMap(restGroup.keySet()), initial, GroupType.SUM, restGroup);

        return BaseExpr.create(expr);
    }

    public class NotNull extends QueryExpr.NotNull {

        @Override
        public ClassExprWhere calculateClassWhere() {
            Where initialWhere = query.initial.getWhere();
            if(initialWhere.isFalse()) return ClassExprWhere.FALSE;

//            RecursiveJoin.getClassWhere(initialWhere, query.step.getWhere(), query.mapIterate)
            // отдельно отрабатываем не рекурсивные (которые сохранятся от initial) и рекурсивные которые постепенно появляются
            return getInnerJoin().getClassWhere().mapBack(reverse(group, true)).
                    and(new ClassExprWhere(RecursiveExpr.this, getInner().getType())).and(getWhere(group).getClassWhere());
        }
    }

    @Override
    public Expr packFollowFalse(Where falseWhere) {
        Map<KeyExpr, Expr> packedGroup = packPushFollowFalse(group, falseWhere);
        Query packedQuery = query.pack();
        if(!(BaseUtils.hashEquals(packedQuery, query) && BaseUtils.hashEquals(packedGroup,group)))
            return create(packedQuery.mapIterate, packedQuery.initial, packedQuery.step, packedGroup);
        else
            return this;
    }

    public String getSource(CompileSource compile) {
        return compile.getSource(this);
    }

    public Where calculateWhere() {
        return new NotNull();
    }
}
