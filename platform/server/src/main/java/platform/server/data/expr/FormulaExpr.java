package platform.server.data.expr;

import platform.server.caches.ParamLazy;
import platform.server.classes.ConcreteValueClass;
import platform.server.data.query.*;
import platform.server.data.translator.KeyTranslator;
import platform.server.data.translator.QueryTranslator;
import platform.server.data.translator.TranslateExprLazy;
import platform.server.data.expr.where.MapWhere;
import platform.server.data.type.Type;
import platform.server.data.where.DataWhereSet;
import platform.server.data.where.Where;

import java.util.HashMap;
import java.util.Map;

@TranslateExprLazy
public class FormulaExpr extends StaticClassExpr {

    private final String formula;
    private final ConcreteValueClass valueClass;
    private final Map<String, BaseExpr> params;

    // этот конструктор напрямую можно использовать только заведомо зная что getClassWhere не null или через оболочку create 
    private FormulaExpr(String iFormula,Map<String, BaseExpr> iParams, ConcreteValueClass iValueClass) {
        formula = iFormula;
        params = iParams;
        valueClass = iValueClass;
    }

    public static Expr create(String iFormula,Map<String, BaseExpr> iParams, ConcreteValueClass iValueClass) {
        return BaseExpr.create(new FormulaExpr(iFormula, iParams, iValueClass));
    }


    public void enumerate(SourceEnumerator enumerator) {
        enumerator.fill(params);
    }

    public void fillAndJoinWheres(MapWhere<JoinData> joins, Where andWhere) {
        for(BaseExpr param : params.values())
            param.fillJoinWheres(joins, andWhere);
    }

    public String getSource(CompileSource compile) {
        String sourceString = formula;
        for(String prm : params.keySet())
            sourceString = sourceString.replace(prm, params.get(prm).getSource(compile));
         return "("+sourceString+")";
     }

    public Type getType(Where where) {
        return valueClass.getType();
    }

    @ParamLazy
    public Expr translateQuery(QueryTranslator translator) {
        return Expr.formula(formula,valueClass,translator.translate(params));
    }

    @ParamLazy
    public BaseExpr translateDirect(KeyTranslator translator) {
        return new FormulaExpr(formula,translator.translateDirect(params),valueClass);
    }

    @Override
    public BaseExpr packFollowFalse(Where where) {
        Map<String, BaseExpr> transParams = new HashMap<String, BaseExpr>();
        for(Map.Entry<String, BaseExpr> param : params.entrySet())
            transParams.put(param.getKey(), param.getValue().packFollowFalse(where));
        assert !transParams.containsValue(null); // предпологается что сверху был andFollowFalse
        return new FormulaExpr(formula,transParams,valueClass);
    }

    // возвращает Where без следствий
    public Where calculateWhere() {
        Where result = Where.TRUE;
        for(Expr param : params.values())
            result = result.and(param.getWhere());
        return result;
    }

    public DataWhereSet getFollows() {
        DataWhereSet follows = new DataWhereSet();
        for(BaseExpr param : params.values())
            follows.addAll(param.getFollows());
        return follows;
    }

    public boolean twins(AbstractSourceJoin o) {
        return formula.equals(((FormulaExpr) o).formula) && params.equals(((FormulaExpr) o).params) && valueClass.equals(((FormulaExpr) o).valueClass);
    }

    public int hashContext(HashContext hashContext) {
        int hash = 0;
        for(Map.Entry<String, BaseExpr> param : params.entrySet())
            hash += param.getKey().hashCode() ^ param.getValue().hashContext(hashContext);
        return valueClass.hashCode()*31*31 + hash*31 + formula.hashCode();
    }

    public ConcreteValueClass getStaticClass() {
        return valueClass;
    }
}

