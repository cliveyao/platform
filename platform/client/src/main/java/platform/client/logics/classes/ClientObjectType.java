package platform.client.logics.classes;

import platform.client.form.ClientFormController;
import platform.client.form.PropertyEditorComponent;
import platform.client.form.PropertyRendererComponent;
import platform.client.form.cell.CellView;
import platform.client.form.cell.TableCellView;
import platform.client.form.editor.ObjectPropertyEditor;
import platform.client.form.renderer.IntegerPropertyRenderer;
import platform.client.logics.ClientCell;
import platform.interop.ComponentDesign;

import java.awt.*;
import java.io.IOException;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;

public class ClientObjectType implements ClientType {

    public int getMinimumWidth(FontMetrics fontMetrics) {
        return fontMetrics.stringWidth("999 999") + 8;
    }

    public int getPreferredWidth(FontMetrics fontMetrics) {
        return fontMetrics.stringWidth("9 999 999") + 8;
    }

    public int getPreferredHeight(FontMetrics fontMetrics) {
        return fontMetrics.getHeight() + 1;
    }

    public int getMaximumWidth(FontMetrics fontMetrics) {
        return getPreferredWidth(fontMetrics);
    }

    public Format getDefaultFormat() {
        return NumberFormat.getInstance();
    }

    public PropertyRendererComponent getRendererComponent(Format format, String caption, ComponentDesign design) {
        return new IntegerPropertyRenderer(format, design);
    }

    public CellView getPanelComponent(ClientCell key, ClientFormController form) {
        return new TableCellView(key, form);
    }

    public PropertyEditorComponent getEditorComponent(ClientFormController form, ClientCell property, Object value, Format format, ComponentDesign design) throws IOException, ClassNotFoundException {
        return new ObjectPropertyEditor(form.getComponent(), form.clientNavigator.remoteNavigator, property.createEditorForm(form.remoteForm));
    }

    public PropertyEditorComponent getClassComponent(ClientFormController form, ClientCell property, Object value, Format format) throws IOException, ClassNotFoundException {
        return new ObjectPropertyEditor(form.getComponent(), form.clientNavigator.remoteNavigator, property.createClassForm(form.remoteForm, (Integer) value));
    }

    public boolean shouldBeDrawn(ClientFormController form) {
        return true;
    }

    public Object parseString(String s) throws ParseException {
        throw new ParseException("ObjectClass не поддерживает конвертацию из строки.", 0);
    }
}
