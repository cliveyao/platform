package platform.server.session;

import platform.server.data.expr.Expr;
import platform.server.data.where.WhereBuilder;
import platform.server.logics.property.Property;
import platform.server.logics.property.PropertyInterface;
import platform.base.ImmutableObject;

import java.util.Collection;
import java.util.Map;

public abstract class Modifier<U extends Changes<U>> extends ImmutableObject {

    public abstract U used(Property property,U usedChanges);
    public abstract U newChanges();

    public abstract U fullChanges();
    public abstract SessionChanges getSession();

    public abstract <P extends PropertyInterface> Expr changed(Property<P> property, Map<P, ? extends Expr> joinImplement, WhereBuilder changedWhere);

    public U getUsedChanges(Collection<Property> col) {
        U result = newChanges();
        for(Property<?> property : col)
            result = result.add(property.getUsedChanges(this));
        return result;
    }

    public U getUsedDataChanges(Collection<Property> col) {
        U result = newChanges();
        for(Property<?> property : col)
            result = result.add(property.getUsedDataChanges(this));
        return result;
    }

    public abstract boolean neededClass(Changes changes);
}
