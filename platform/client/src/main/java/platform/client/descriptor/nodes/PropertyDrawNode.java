package platform.client.descriptor.nodes;

import platform.client.ClientTree;
import platform.client.descriptor.PropertyDrawDescriptor;
import platform.client.descriptor.GroupObjectDescriptor;
import platform.client.descriptor.FormDescriptor;
import platform.client.descriptor.editor.PropertyDrawEditor;
import platform.client.descriptor.editor.base.NodeEditor;
import platform.interop.serialization.RemoteDescriptorInterface;

import javax.swing.*;

public class PropertyDrawNode extends GroupElementNode<PropertyDrawDescriptor, PropertyDrawNode> {

    private FormDescriptor form;

    public PropertyDrawNode(GroupObjectDescriptor groupObject, PropertyDrawDescriptor userObject, FormDescriptor form) {
        super(groupObject, userObject);

        this.form = form;
    }

    public NodeEditor createEditor(FormDescriptor form, RemoteDescriptorInterface remote) {
        return new PropertyDrawEditor(groupObject, getTypedObject(), this.form, remote);
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        return getSiblingNode(info) != null;
    }

    @Override
    public boolean importData(ClientTree tree, TransferHandler.TransferSupport info) {
        return form.movePropertyDraw(getSiblingNode(info).getTypedObject(), getTypedObject());
    }
}
