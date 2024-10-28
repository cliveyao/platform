package lsfusion.server.data.expr.key;

import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImOrderSet;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.col.interfaces.mutable.MMap;
import lsfusion.base.col.interfaces.mutable.MSet;
import lsfusion.base.comb.map.GlobalInteger;
import lsfusion.server.data.expr.BaseExpr;
import lsfusion.server.data.expr.Expr;
import lsfusion.server.data.expr.NullableExpr;
import lsfusion.server.data.expr.NullableExprInterface;
import lsfusion.server.data.expr.join.where.GroupJoinsWheres;
import lsfusion.server.data.expr.where.NotNullWhere;
import lsfusion.server.data.query.compile.CompileSource;
import lsfusion.server.data.query.compile.FJData;
import lsfusion.server.data.query.compile.where.UpWhere;
import lsfusion.server.data.stat.KeyStat;
import lsfusion.server.data.stat.StatType;
import lsfusion.server.data.table.IndexType;
import lsfusion.server.data.where.DataWhere;
import lsfusion.server.data.where.Where;
import lsfusion.server.data.where.classes.ClassExprWhere;

public class NullableKeyExpr extends ParamExpr implements NullableExprInterface {

    private final int ID;
    public NullableKeyExpr(int ID) {
        this.ID = ID;
    }

    @Override
    public void fillAndJoinWheres(MMap<FJData, Where> joins, Where andWhere) {
        throw new RuntimeException("not supported");
    }

    @Override
    public String getSource(CompileSource compile, boolean needValue) {
        if(compile instanceof ToString)
            return "I_" + ID;
        throw new RuntimeException("not supported");
    }

    public class NotNull extends NotNullWhere {

        protected BaseExpr getExpr() {
            return NullableKeyExpr.this;
        }

        public ClassExprWhere calculateClassWhere() {
            return ClassExprWhere.TRUE;
        }

        public <K extends BaseExpr> GroupJoinsWheres groupJoinsWheres(ImSet<K> keepStat, StatType statType, KeyStat keyStat, ImOrderSet<Expr> orderTop, GroupJoinsWheres.Type type) {
            throw new RuntimeException("not supported");
        }

        @Override
        protected UpWhere getUpWhere() {
            throw new UnsupportedOperationException();
        }
    }

    public NotNull calculateNotNullWhere() {
        return new NotNull();
    }

    public void fillFollowSet(MSet<DataWhere> result) {
        NullableExpr.fillFollowSet(this, result);
    }

    public boolean hasNotNull() {
        return NullableExpr.hasNotNull(this);
    }

    // упрощенная копия аналогичного метода в NullableExpr
    public ImSet<NullableExprInterface> getExprFollows(boolean includeThis, boolean includeInnerWithoutNotNull, boolean recursive) {
        assert includeThis || recursive;
        if(includeThis) {
            return SetFact.singleton(this);
        }
        return SetFact.EMPTY();
    }

    @Override
    protected IndexType getIndexType() {
        throw new UnsupportedOperationException();
    }

    private final static GlobalInteger keyClass = new GlobalInteger(39316401);

    public GlobalInteger getKeyClass() {
        return keyClass;
    }
}

