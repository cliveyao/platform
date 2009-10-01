package platform.server.data.classes.where;

import platform.server.data.query.*;
import platform.server.data.query.translators.KeyTranslator;
import platform.server.data.query.translators.QueryTranslator;
import platform.server.data.query.wheres.MapWhere;
import platform.server.where.DataWhere;
import platform.server.where.DataWhereSet;
import platform.server.where.Where;

public class MeanClassWhere extends DataWhere {

    ClassExprWhere packWhere;

    public MeanClassWhere(ClassExprWhere iPackWhere) {
        packWhere = iPackWhere;

        assert !packWhere.isFalse();
        assert !packWhere.isTrue();
    }

    protected DataWhereSet getExprFollows() {
        return packWhere.getFollows();
    }

    public ClassExprWhere calculateClassWhere() {
        return packWhere;
    }

    public void fillContext(Context context) {
//        throw new RuntimeException("Not supported");
    }

    protected void fillDataJoinWheres(MapWhere<JoinData> joins, Where andWhere) {
        throw new RuntimeException("Not supported");
    }

    public int hashContext(HashContext hashContext) {
        return System.identityHashCode(this);
    }

    public boolean twins(AbstractSourceJoin obj) {
        return false;
    }

    public String getSource(CompileSource compile) {
        if(compile instanceof ToString)
            return packWhere.toString();

        throw new RuntimeException("Not supported");
    }

    @Override
    public String toString() {
        return packWhere.toString();
    }

    public Where translateDirect(KeyTranslator translator) {
        throw new RuntimeException("Not supported");
    }
    public Where translateQuery(QueryTranslator translator) {
        throw new RuntimeException("Not supported");
    }

    public InnerJoins getInnerJoins() {
        throw new RuntimeException("Not supported");
    }
}
