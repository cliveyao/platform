package platform.client.descriptor.editor;

import platform.client.descriptor.ObjectDescriptor;
import platform.client.descriptor.editor.base.NodeEditor;
import platform.client.descriptor.editor.base.TitledPanel;
import platform.client.descriptor.increment.editor.IncrementCheckBox;
import platform.client.descriptor.increment.editor.IncrementSingleListSelectionModel;
import platform.client.descriptor.increment.editor.IncrementTextEditor;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ObjectEditor extends JPanel implements NodeEditor {

    public ObjectEditor(ObjectDescriptor object) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(new TitledPanel("Заголовок", new IncrementTextEditor(object, "caption")));
        add(Box.createRigidArea(new Dimension(5, 5)));

        add(new TitledPanel(null, new IncrementCheckBox("Добавлять новый объект при транзакции", object, "addOnTransaction")));
        add(Box.createRigidArea(new Dimension(5, 5)));

        add(new TitledPanel("Класс", new JComboBox(new IncrementSingleListSelectionModel(object, "baseClass") {
            public java.util.List<?> getList() {
//                return form.getProperties(groupObject);
                //todo: список классов
                return Arrays.asList("");
            }
        })));
    }

    public JComponent getComponent() {
        return this;
    }

    public boolean validateEditor() {
        return true;
    }
}
