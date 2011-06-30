package platform.client.descriptor.editor;

import platform.client.ClientResourceBundle;
import platform.client.code.CodeGenerator;
import platform.client.descriptor.FormDescriptor;
import platform.client.descriptor.editor.base.NodeEditor;
import platform.client.descriptor.editor.base.TitledPanel;
import platform.client.descriptor.increment.editor.IncrementTextEditor;

import javax.swing.*;
import java.awt.*;

public class FormEditor extends JPanel implements NodeEditor {
    private final FormDescriptor form;

    public FormEditor(FormDescriptor form) {
        this.form = form;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(new TitledPanel(ClientResourceBundle.getString("descriptor.editor.common.title"), new IncrementTextEditor(form, "caption")));
        add(Box.createRigidArea(new Dimension(5, 5)));
        add(new TitledPanel(ClientResourceBundle.getString("descriptor.editor.order.by.default"), new DefaultOrdersEditor(form, null)));
        add(Box.createRigidArea(new Dimension(5, 5)));
        add(new TitledPanel(ClientResourceBundle.getString("descriptor.editor.code"), CodeGenerator.getComponent(form)));
    }

    public JComponent getComponent() {
        return this;
    }

    public boolean validateEditor() {
        return true;
    }
}
