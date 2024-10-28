package lsfusion.server.data.expr.formula;

import lsfusion.server.data.query.exec.MStaticExecuteEnvironment;
import lsfusion.server.data.sql.syntax.SQLSyntax;
import lsfusion.server.data.type.Type;

public class MaxFormulaImpl extends AbstractFormulaImpl implements FormulaUnionImpl {

    public boolean supportRemoveNull() {
        return true;
    }

    public boolean supportNeedValue() {
        return true;
    }

    public final boolean isMin;
    public final boolean notObjectType;

    public MaxFormulaImpl(boolean isMin) {
        this(isMin, false);
    }

    public MaxFormulaImpl(boolean isMin, boolean notObjectType) {
        this.isMin = isMin;
        this.notObjectType = notObjectType;
    }

    public boolean supportSingleSimplify() {
        return true;
    }

    @Override
    public String getSource(ExprSource source) {
        int exprCount = source.getExprCount();
        assert exprCount > 1;
        if (exprCount == 0) {
            return "";
        }

        Type type = getType(source);
        SQLSyntax syntax = source.getSyntax();
        MStaticExecuteEnvironment env = source.getMEnv();
        boolean noMaxImplicitCast = syntax.noMaxImplicitCast();

        String result = type.getCast(source.getSource(0), syntax, env, source.getType(0), Type.CastType.MAX); // чтобы когда NULL'ы тип правильно определило

        for (int i = 1; i < exprCount; i++) {
            String exprSource = source.getSource(i);
            if(noMaxImplicitCast)
                exprSource = type.getCast(exprSource, syntax, env, source.getType(i), Type.CastType.MAX);
            result = syntax.getMaxMin(!isMin, result , exprSource, type, env);
        }
        return result;
    }

    public boolean equals(Object o) {
        return this == o || o instanceof MaxFormulaImpl && isMin == ((MaxFormulaImpl) o).isMin && notObjectType == ((MaxFormulaImpl) o).notObjectType;
    }

    public int hashCode() {
        return 31 * (isMin ? 1 : 0) + (notObjectType ? 1 : 0);
    }
}
