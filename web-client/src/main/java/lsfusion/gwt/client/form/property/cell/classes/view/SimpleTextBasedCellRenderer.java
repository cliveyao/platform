package lsfusion.gwt.client.form.property.cell.classes.view;

import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.Event;
import lsfusion.gwt.client.base.EscapeUtils;
import lsfusion.gwt.client.base.GwtClientUtils;
import lsfusion.gwt.client.base.view.LabelWidget;
import lsfusion.gwt.client.form.property.GPropertyDraw;
import lsfusion.gwt.client.form.property.cell.classes.controller.SimpleTextBasedCellEditor;
import lsfusion.gwt.client.form.property.cell.view.CellRenderer;
import lsfusion.gwt.client.form.property.cell.view.RenderContext;

// the renderer which may be renderered as plain input (or td in table)
public abstract class SimpleTextBasedCellRenderer extends TextBasedCellRenderer {

    public SimpleTextBasedCellRenderer(GPropertyDraw property) {
        super(property);
    }

    public static void render(GPropertyDraw property, Element element, RenderContext renderContext, boolean multiLine) {
        CellRenderer.setBasedTextFonts(property, element, renderContext);

        element.getStyle().setWhiteSpace(multiLine ? Style.WhiteSpace.PRE_WRAP : Style.WhiteSpace.PRE);
    }

    @Override
    public boolean canBeRenderedInTD() {
        if(isTagInput()) // input / textareas has fixed sizes, so can be used with multiline fixed sizes
            return true;

        // td always respects the inner text size, so if it is multi line and not autosized, we have wrap the content into a div
        if (isMultiLine() && !property.autoSize)
            return false;

        // input we have to render in td, since input is a void element, and it can not have children (and they are needed for the toolbar)
        // so the hack is to render it
        return getTag() == null || isTagInput();
    }

    @Override
    public Element createRenderElement() {
        if(isTagInput()) {
            if(needToRenderToolbarContent()) { // for an input with a toolbar we have to wrap it in a div to draw a toolbar
                DivElement toolbarContainer = Document.get().createDivElement();
                toolbarContainer.addClassName("prop-w-toolbar");
                setToolbarContainer(toolbarContainer);
                return toolbarContainer;
            } else
                return createInputElement(property);
        }

        return super.createRenderElement();
    }

    @Override
    public void renderPanelLabel(LabelWidget label) {
        if(property.panelCaptionVertical)
            label.addStyleName("form-label");
        else
            label.addStyleName("col-form-label");
    }

    public static InputElement createInputElement(GPropertyDraw property) {
        return property.createTextInputElement();
    }

    private final static String inputElementProp = "textInputElement";

    public static InputElement getInputEventTarget(Element parent, Event event) {
        InputElement inputElement = getInputElement(parent);
        return inputElement == event.getEventTarget().cast() ? inputElement : null;
    }

    public static boolean isMultiLineInput(Element parent) {
        return TextAreaElement.is(getInputElement(parent));
    }

    public static InputElement getInputElement(Element parent) {
        return (InputElement) parent.getPropertyObject(inputElementProp);
    }

    public static void setInputElement(Element element, InputElement inputElement) {
        element.setPropertyObject(inputElementProp, inputElement);
    }

    public static void setSizeElement(Element element, InputElement inputElement) {
        element.setPropertyObject(inputElementProp, inputElement);
    }

    private final static String toolbarContainerProp = "toolbarContainer";

    private static void setToolbarContainer(Element element) {
        element.setPropertyBoolean(toolbarContainerProp, true);
    }
    public  static boolean isToolbarContainer(Element element) {
        return element.getPropertyBoolean(toolbarContainerProp);
    }

    public static Element getSizeElement(Element element) {
        InputElement sizeElement;
        if(isToolbarContainer(element) && (sizeElement = getInputElement(element)) != null)
            return sizeElement;
        return element;
    }

    @Override
    public boolean renderContent(Element element, RenderContext renderContext) {

        boolean renderedAlignment = false;
        InputElement inputElement = null;

        boolean isTDOrTH = GwtClientUtils.isTDorTH(element); // because canBeRenderedInTD can be true
        boolean isInput = isTagInput();
        boolean multiLine = isMultiLine();

        if(isInput && (isTDOrTH || isToolbarContainer(element))) {
            // assert isTDOrTH != isToolbarContainer(element);
            inputElement = SimpleTextBasedCellEditor.renderInputElement(element, property, multiLine, renderContext, isTDOrTH, true);
            renderedAlignment = true;
        } else {
            // otherwise we'll use flex alignment (however text alignment would also do)
            // there is some difference in div between align-items center and vertical align baseline / middle, and align items center seems to be more accurate (and better match input vertical align baseline / middle)
            if(isTDOrTH || isInput) {
                renderTextAlignment(property, element, isInput);
                renderedAlignment = true;
            }
            SimpleTextBasedCellRenderer.render(property, element, renderContext, multiLine);

            if(isInput)
                inputElement = (InputElement) element;
        }

        if(inputElement != null)
            setInputElement(element, inputElement);

        super.renderContent(element, renderContext);

        return renderedAlignment;
    }

    @Override
    public boolean clearRenderContent(Element element, RenderContext renderContext) {

//        boolean renderedAlignment = false;
        boolean inputElement = false;

        boolean isTDOrTH = GwtClientUtils.isTDorTH(element); // because canBeRenderedInTD can be true
        boolean isInput = isTagInput();
        if (isInput && (isTDOrTH || needToRenderToolbarContent())) {
            inputElement = true;
            SimpleTextBasedCellEditor.clearInputElement(element, true);
//            renderedAlignment = true;
        } else {
//            if(isTDOrTH || isInput) {
                clearRenderTextAlignment(property, element, isInput);
//                renderedAlignment = true;
//            }

            if(isInput)
                inputElement = true;

            CellRenderer.clearBasedTextFonts(property, element, renderContext);
            SimpleTextBasedCellRenderer.clearRender(property, element, renderContext);
        }

        if(inputElement)
            setInputElement(element, null);

        super.clearRenderContent(element, renderContext);

        return true; // renderedAlignment;
    }

    protected boolean setInnerContent(Element element, String innerText) {
        Element inputElement = getInputElement(element);
        if(inputElement != null) {
            assert isTagInput();
            if(property.isEditableNotNull()) {
                if (innerText.isEmpty()) {
                    inputElement.addClassName("is-invalid");
                } else {
                    inputElement.removeClassName("is-invalid");
                }
            }
            SimpleTextBasedCellEditor.setInputValue(inputElement.cast(), innerText);
            return false;
        }

        // important to make paste work (otherwise DataGrid.sinkPasteEvent cannot put empty selection), plus for sizing
        element.setInnerText(innerText.isEmpty() ? EscapeUtils.UNICODE_NBSP : innerText);
        return true;
    }
}
