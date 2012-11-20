package platform.gwt.form.shared.view.grid.editor;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.impl.TextBoxImpl;
import platform.gwt.form.shared.view.grid.EditEvent;
import platform.gwt.form.shared.view.grid.EditManager;
import platform.gwt.form.shared.view.grid.NativeEditEvent;

import static com.google.gwt.dom.client.BrowserEvents.*;
import static platform.gwt.base.client.GwtClientUtils.stopPropagation;

public abstract class TextFieldGridEditor implements GridCellEditor {
    interface Template extends SafeHtmlTemplates {
        @Template("<input style=\"border: 0px; margin: 0px; width: 100%; font-size: 8pt; \" type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
        SafeHtml input(String value);

        @Template("<input style=\"border: 0px; margin: 0px; width: 100%; font-size: 8pt; text-align: {0};\" type=\"text\" value=\"{1}\" tabindex=\"-1\"></input>")
        SafeHtml aligned(String alignment, String value);
    }

    protected static Template template;

    protected final class ParseException extends Exception {
    }

    public TextFieldGridEditor(EditManager editManager) {
        this(editManager, null);
    }

    public TextFieldGridEditor(EditManager editManager, Style.TextAlign textAlign) {
        if (template == null) {
            template = GWT.create(Template.class);
        }
        this.textAlign = textAlign == Style.TextAlign.LEFT ? null : textAlign;
        this.editManager = editManager;
    }

    protected EditManager editManager;
    protected Style.TextAlign textAlign;
    protected String currentText = "";

    private static TextBoxImpl textBoxImpl = GWT.create(TextBoxImpl.class);

    @Override
    public void startEditing(EditEvent editEvent, Cell.Context context, Element parent, Object oldValue) {
        currentText = oldValue == null ? "" : oldValue.toString();
        InputElement inputElement = getInputElement(parent);
        boolean selectAll = true;
        if (editEvent instanceof NativeEditEvent) {
            NativeEvent nativeEvent = ((NativeEditEvent) editEvent).getNativeEvent();
            String eventType = nativeEvent.getType();
            if (KEYDOWN.equals(eventType) && nativeEvent.getKeyCode() == KeyCodes.KEY_DELETE) {
                currentText = "";
                selectAll = false;
            } else if (KEYPRESS.equals(eventType)) {
                currentText = String.valueOf((char)nativeEvent.getCharCode());
                selectAll = false;
            }
        }
        inputElement.setValue(currentText);
        inputElement.focus();

        if (selectAll) {
            textBoxImpl.setSelectionRange((com.google.gwt.user.client.Element) (Element) inputElement, 0, currentText.length());
        } else {
            //перемещаем курсор в конец текста
            textBoxImpl.setSelectionRange((com.google.gwt.user.client.Element) (Element) inputElement, currentText.length(), 0);
        }
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent, Object value, NativeEvent event, ValueUpdater<Object> valueUpdater) {
        String type = event.getType();
        boolean keyDown = KEYDOWN.equals(type);
        boolean keyPress = KEYPRESS.equals(type);
        if (keyDown || keyPress) {
            int keyCode = event.getKeyCode();
            if (keyPress && keyCode == KeyCodes.KEY_ENTER) {
                stopPropagation(event);
                validateAndCommit(parent);
            } else if (keyDown && keyCode == KeyCodes.KEY_ESCAPE) {
                stopPropagation(event);
                editManager.cancelEditing();
            } else {
                currentText = getCurrentText(parent);
            }
        } else if (BLUR.equals(type)) {
            // Cancel the change. Ensure that we are blurring the input element and
            // not the parent element itself.
            EventTarget eventTarget = event.getEventTarget();
            if (Element.is(eventTarget)) {
                Element target = Element.as(eventTarget);
                if ("input".equals(target.getTagName().toLowerCase())) {
                    editManager.cancelEditing();
                }
            }
        }
    }

    @Override
    public void render(Cell.Context context, Object value, SafeHtmlBuilder sb) {
        if (textAlign != null) {
            sb.append(template.aligned(textAlign.getCssName(), currentText));
        } else {
            sb.append(template.input(currentText));
        }
    }

    @Override
    public boolean resetFocus(Cell.Context context, Element parent, Object value) {
        getInputElement(parent).focus();
        return true;
    }

    private void validateAndCommit(Element parent) {
        String value = getCurrentText(parent);
        try {
            editManager.commitEditing(tryParseInputText(value));
        } catch (ParseException ignore) {
            //если выкинулся ParseException, то не заканчиваем редактирование
        }
    }

    protected InputElement getInputElement(Element parent) {
        return parent.getFirstChild().cast();
    }

    private String getCurrentText(Element parent) {
        return getInputElement(parent).getValue();
    }

    protected abstract Object tryParseInputText(String inputText) throws ParseException;
}
