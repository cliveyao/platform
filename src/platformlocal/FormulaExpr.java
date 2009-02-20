package platformlocal;

import java.util.*;

class FormulaExpr extends AndExpr {

    String formula;
    Type dbType;
    Map<String, SourceExpr> params;

    FormulaExpr(String iFormula,Map<String,AndExpr> iParams,Type iDBType) {
        formula = iFormula;
        params = (Map<String, SourceExpr>) (Map<String, ? extends SourceExpr>) iParams;
        dbType = iDBType;
    }

    FormulaExpr(String iFormula,Type iDBType,Map<String,SourceExpr> iParams) {
        formula = iFormula;
        params = iParams;
        dbType = iDBType;
    }

    // линейный конструктор (сумма/разница)
    FormulaExpr(SourceExpr op1,SourceExpr op2,boolean sum) {
        formula = "prm1"+(sum?"+":"-")+"prm2";
        params = new HashMap<String,SourceExpr>();
        params.put("prm1",op1);
        params.put("prm2",op2);
        dbType = op1.getType();
    }

    // линейный конструктор (коэффициент)
    FormulaExpr(SourceExpr expr,Integer coeff) {
        formula = coeff+"*prm1";
        params = new HashMap<String,SourceExpr>();
        params.put("prm1",expr);
        dbType = expr.getType();
    }

    public <J extends Join> void fillJoins(List<J> joins, Set<ValueExpr> values) {
        for(SourceExpr param : params.values())
            param.fillJoins(joins, values);
    }

    public void fillAndJoinWheres(MapWhere<JoinData> joins, Where andWhere) {
        for(SourceExpr param : params.values())
            param.fillJoinWheres(joins, andWhere);
    }

    public String getSource(Map<QueryData, String> queryData, SQLSyntax syntax) {
        String SourceString = formula;
        for(String prm : params.keySet())
            SourceString = SourceString.replace(prm, params.get(prm).getSource(queryData, syntax));
         return "("+SourceString+")";
     }

    public String toString() {
        String result = formula;
        for(String prm : params.keySet())
            result = result.replace(prm, params.get(prm).toString());
         return "("+result+")";
    }

    Type getType() {
        return dbType;
    }

    public SourceExpr translate(Translator translator) {
        MapCaseList<String> caseList = CaseExpr.translateCase(params, translator, false, false);
        if(caseList==null)
            return this;

        ExprCaseList result = new ExprCaseList();
        for(MapCase<String> mapCase : caseList)  // здесь напрямую потому как MapCaseList уже все проверил
            result.add(new ExprCase(mapCase.where,SourceExpr.containsNull(mapCase.data)?getType().getExpr(null):new FormulaExpr(formula,mapCase.data, dbType)));
        return result.getExpr(getType());
    }

    // возвращает Where без следствий
    Where calculateWhere() {
        Where result = Where.TRUE;
        for(SourceExpr param : params.values())
            result = result.and(param.getWhere());
        return result;
    }

    DataWhereSet getFollows() {
        DataWhereSet follows = new DataWhereSet();
        for(SourceExpr param : params.values())
            follows.addAll(((AndExpr)param).getFollows());
        return follows;
    }

    public boolean equals(Object o) {
        return this==o || o instanceof FormulaExpr && formula.equals(((FormulaExpr) o).formula) && params.equals(((FormulaExpr) o).params);
    }

    public int hashCode() {
        return 31 * formula.hashCode() + params.hashCode();
    }

    // для кэша
    boolean equals(SourceExpr expr, Map<ObjectExpr, ObjectExpr> mapExprs, Map<JoinWhere, JoinWhere> mapWheres) {
        if(!(expr instanceof FormulaExpr)) return false;

        FormulaExpr formulaExpr = (FormulaExpr) expr;

        if(!formula.equals(formulaExpr.formula) || params.size()!=formulaExpr.params.size()) return false;

        for(Map.Entry<String,SourceExpr> param : params.entrySet())
            if(!param.getValue().equals(formulaExpr.params.get(param.getKey()), mapExprs, mapWheres))
                return false;
        return true;
    }

    int getHash() {
        int hash = 0;
        for(Map.Entry<String,SourceExpr> param : params.entrySet())
            hash += param.getKey().hashCode()+param.getValue().hash();
        return hash*31 + formula.hashCode();
    }
}

