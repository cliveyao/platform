package platform.server.view.form.filter;

import platform.server.data.query.exprs.SourceExpr;
import platform.server.logics.properties.Property;
import platform.server.session.TableChanges;
import platform.server.view.form.GroupObjectImplement;
import platform.server.view.form.ObjectImplement;
import platform.server.view.form.Updated;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public interface CompareValue extends Updated {

//    AndClassSet getValueClass(GroupObjectImplement ClassGroup) {return null;}

    SourceExpr getSourceExpr(Set<GroupObjectImplement> classGroup, Map<ObjectImplement, ? extends SourceExpr> classSource, TableChanges session, Property.TableDepends<? extends Property.TableUsedChanges> depends) throws SQLException;
}
