package platform.server.data.expr.cases;

import platform.server.data.where.classes.ClassExprWhere;
import platform.server.data.expr.BaseExpr;

import java.util.Map;

public interface CaseClassWhere<K,V> {
    V getCaseClassWhere(Map<K, BaseExpr> mapCase, ClassExprWhere caseClassWhere);
}
