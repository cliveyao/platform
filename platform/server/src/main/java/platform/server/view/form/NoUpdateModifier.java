package platform.server.view.form;

import platform.server.session.Modifier;
import platform.server.session.Changes;
import platform.server.logics.property.Property;
import platform.server.logics.property.PropertyInterface;
import platform.server.data.expr.Expr;
import platform.server.data.where.WhereBuilder;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;

public abstract class NoUpdateModifier extends Modifier<NoUpdateModifier.UsedChanges> {

    public Collection<Property> hintsNoUpdate;

    protected NoUpdateModifier() {
        hintsNoUpdate = new HashSet<Property>();
    }
    protected NoUpdateModifier(Collection<Property> hintsNoUpdate) {
        this.hintsNoUpdate = hintsNoUpdate;
    }

    public static class UsedChanges extends Changes<UsedChanges> {
        final Collection<Property> noUpdateProps = new ArrayList<Property>();

        @Override
        public void add(UsedChanges add) {
            super.add(add);
            noUpdateProps.addAll(add.noUpdateProps);
        }

        @Override
        public boolean equals(Object o) {
            return this==o || o instanceof UsedChanges && noUpdateProps.equals(((UsedChanges)o).noUpdateProps) && super.equals(o);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + noUpdateProps.hashCode();
        }
    }

    public <P extends PropertyInterface> Expr changed(Property<P> property, Map<P, ? extends Expr> joinImplement, WhereBuilder changedWhere) {
        if(hintsNoUpdate.contains(property)) // если так то ничего не менять
            return Expr.NULL;
        else
            return null;
    }

    public UsedChanges used(Property property, UsedChanges changes) {
        if(changes.hasChanges() && hintsNoUpdate.contains(property)) {
            changes = new UsedChanges();
            changes.noUpdateProps.add(property);
        }
        return changes;
    }

    public UsedChanges newChanges() {
        return new UsedChanges();
    }

}
