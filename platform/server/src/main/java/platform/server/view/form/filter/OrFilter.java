package platform.server.view.form.filter;

import platform.server.view.form.GroupObjectImplement;
import platform.server.view.form.ObjectImplement;
import platform.server.view.form.RemoteForm;
import platform.server.logics.properties.Property;
import platform.server.where.Where;
import platform.server.data.query.exprs.KeyExpr;
import platform.server.session.TableChanges;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.sql.SQLException;
import java.io.DataInputStream;
import java.io.IOException;

public class OrFilter extends Filter {

    Filter op1;
    Filter op2;

    public OrFilter(Filter op1, Filter op2) {
        this.op1 = op1;
        this.op2 = op2;
    }

    protected OrFilter(DataInputStream inStream, RemoteForm form) throws IOException, SQLException {
        super(inStream, form);
        op1 = Filter.deserialize(inStream, form);
        op2 = Filter.deserialize(inStream, form);
    }

    public boolean classUpdated(GroupObjectImplement classGroup) {
        return op1.classUpdated(classGroup) || op2.classUpdated(classGroup);
    }

    public boolean objectUpdated(GroupObjectImplement classGroup) {
        return op1.objectUpdated(classGroup) || op2.objectUpdated(classGroup);
    }

    public boolean dataUpdated(Collection<Property> changedProps) {
        return op1.dataUpdated(changedProps) || op2.dataUpdated(changedProps);
    }

    protected void fillProperties(Set<Property> properties) {
        op1.fillProperties(properties);
        op2.fillProperties(properties);
    }

    public GroupObjectImplement getApplyObject() {
        GroupObjectImplement apply1 = op1.getApplyObject();
        GroupObjectImplement apply2 = op2.getApplyObject();
        if(apply1.order>apply2.order)
            return apply1;
        else
            return apply2;
    }

    public Where getWhere(Map<ObjectImplement, KeyExpr> mapKeys, Set<GroupObjectImplement> classGroup, TableChanges session, Property.TableDepends<? extends Property.TableUsedChanges> depends) throws SQLException {
        return op1.getWhere(mapKeys, classGroup, session, depends).or(op2.getWhere(mapKeys, classGroup, session, depends));
    }
}
