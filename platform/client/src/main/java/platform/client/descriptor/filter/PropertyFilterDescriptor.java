package platform.client.descriptor.filter;

import platform.client.descriptor.PropertyObjectDescriptor;
import platform.client.descriptor.GroupObjectDescriptor;
import platform.client.descriptor.nodes.filters.FilterNode;
import platform.client.descriptor.nodes.filters.PropertyFilterNode;
import platform.client.serialization.ClientSerializationPool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class PropertyFilterDescriptor extends FilterDescriptor {
    public PropertyObjectDescriptor property;

    public GroupObjectDescriptor getGroupObject(List<GroupObjectDescriptor> groupList) {
        if (property == null) return null;
        return property.getGroupObject(groupList);
    }

    @Override
    public FilterNode createNode(Object group) {
        return new PropertyFilterNode((GroupObjectDescriptor) group, this);
    }

    public void customSerialize(ClientSerializationPool pool, DataOutputStream outStream, String serializationType) throws IOException {
        pool.serializeObject(outStream, property);
    }

    public void customDeserialize(ClientSerializationPool pool, int iID, DataInputStream inStream) throws IOException {
        property = (PropertyObjectDescriptor) pool.deserializeObject(inStream);
    }
}
