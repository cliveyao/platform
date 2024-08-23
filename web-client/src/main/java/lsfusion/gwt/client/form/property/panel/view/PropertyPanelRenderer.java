package lsfusion.gwt.client.form.property.panel.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.user.client.ui.Widget;
import lsfusion.gwt.client.base.*;
import lsfusion.gwt.client.base.view.GFlexAlignment;
import lsfusion.gwt.client.base.view.SizedFlexPanel;
import lsfusion.gwt.client.base.view.SizedWidget;
import lsfusion.gwt.client.form.controller.GFormController;
import lsfusion.gwt.client.form.design.GFont;
import lsfusion.gwt.client.form.design.view.CaptionWidget;
import lsfusion.gwt.client.form.design.view.ComponentViewWidget;
import lsfusion.gwt.client.form.design.view.GFormLayout;
import lsfusion.gwt.client.form.design.view.InlineComponentViewWidget;
import lsfusion.gwt.client.form.object.GGroupObjectValue;
import lsfusion.gwt.client.form.property.GPropertyDraw;
import lsfusion.gwt.client.form.property.PValue;

public class PropertyPanelRenderer extends PanelRenderer {

    private final ComponentViewWidget sizedView;

    private Widget label;

    private Widget comment;

    public PropertyPanelRenderer(final GFormController form, ActionOrPropertyValueController controller, GPropertyDraw property, GGroupObjectValue columnKey, Result<CaptionWidget> captionContainer) {
        super(form, controller, property, columnKey, property.isAction()); // assert if is Action that property has alignCaption() true and captionContainer != null

        SizedWidget valueWidget = value.getSizedWidget(false);

        setStyles(valueWidget.widget.getElement(), property.isEditableNotNull(), property.hasChangeAction);

        sizedView = initCaption(valueWidget, property, captionContainer);

        finalizeInit();
    }

    public static void setStyles(Element panelElement, boolean notNull, boolean hasChangeAction) {
        if (notNull)
            panelElement.addClassName("property-not-null");
        else if(hasChangeAction)
            panelElement.addClassName("property-has-change");
    }

    @Override
    public void update(PValue value, boolean loading, AppBaseImage image, String valueElementClass,
                       GFont font, String background, String foreground, Boolean readOnly, String placeholder, String pattern,
                       String regexp, String regexpMessage, String valueTooltip) {
        if(property.hasDynamicImage() && !property.isAction()) {
            BaseImage.updateImage(image, label);
            image = null;
        }

        // we don't need image in value
        super.update(value, loading, image, valueElementClass, font, background, foreground, readOnly, placeholder, pattern,
                regexp, regexpMessage, valueTooltip);
    }

    private ComponentViewWidget initCaption(SizedWidget valuePanel, GPropertyDraw property, Result<CaptionWidget> captionContainer) {
        if(property.caption == null && property.comment == null) // if there is no (empty) static caption and no dynamic caption
            return valuePanel.view;

        // id and for we need to support editing when clicking on the label
        // however only CLICK and CHANGE (for boolean props) are propagated to the input, and not MOUSEDOWN
        // but since we use MOUSEDOWN as a change event (so it starts editing) we need to propagate MOUSEDOWN manually
        // we need id to be global (otherwise everything stops working if the same form is opened twice)
        String globalID = form.globalID + "->" + property.propertyFormName;
        value.getElement().setId(globalID);

        SizedWidget sizedLabel = null;
        if(property.caption != null) {
            label = GFormLayout.createLabelCaptionWidget();
            BaseImage.initImageText(label, null, property.appImage, property.panelCaptionVertical ? ImageHtmlOrTextType.PANEL_CAPTION_VERT : ImageHtmlOrTextType.PANEL_CAPTION_HORZ);
            label.addStyleName("panel-property-label");

            label.getElement().setAttribute("for", globalID);
            label.addDomHandler(event -> {
                GwtClientUtils.fireOnMouseDown(value.getElement());
                GwtClientUtils.stopPropagation(event.getNativeEvent()); // need this because otherwise default handler will lead to the blur event
            }, MouseDownEvent.getType());

            // mostly it is needed to handle margins / paddings / layouting but we do it ourselves
//        CellRenderer cellRenderer = property.getCellRenderer();
//        cellRenderer.renderPanelLabel(label);

            sizedLabel = new SizedWidget(label, property.getCaptionWidth(), property.getCaptionHeight());
        }

        GFlexAlignment panelCaptionAlignment = property.getPanelCaptionAlignment(); // vertical alignment
        boolean captionLast = property.isPanelCaptionLast();

        GFlexAlignment panelValueAlignment = property.getPanelValueAlignment(); // vertical alignment

        GFlexAlignment panelCommentAlignment = property.getPanelCommentAlignment(); // vertical alignment
        boolean commentFirst = property.isPanelCommentFirst();

        boolean isAlignCaption = property.isAlignCaption() && captionContainer != null;
        boolean inline = !isAlignCaption && property.isInline();
        boolean verticalDiffers = property.caption != null && property.comment != null && !inline && property.panelCaptionVertical != property.panelCommentVertical;
        boolean panelVertical = property.caption != null ? property.panelCaptionVertical : property.panelCommentVertical;

        SizedWidget sizedComment = null;
        if(property.comment != null) {
            comment = GFormLayout.createLabelCaptionWidget();
            GwtClientUtils.initCaptionHtmlOrText(comment.getElement(), property.panelCommentVertical ? CaptionHtmlOrTextType.COMMENT_VERT : CaptionHtmlOrTextType.COMMENT_HORZ);
            comment.addStyleName("panel-comment");

            //            setCommentText(property.comment);
            sizedComment = new SizedWidget(comment, property.getCaptionWidth(), property.getCaptionHeight());
            if (isAlignCaption || verticalDiffers) {
                SizedFlexPanel valueCommentPanel = new SizedFlexPanel(property.panelCommentVertical);

                if (commentFirst)
                    sizedComment.add(valueCommentPanel, panelCommentAlignment);

                valuePanel.addFill(valueCommentPanel);

                if (!commentFirst)
                    sizedComment.add(valueCommentPanel, panelCommentAlignment);

                valuePanel = new SizedWidget(valueCommentPanel);
            }
        }

        if (isAlignCaption) { // align caption
            if(!panelCaptionAlignment.equals(GFlexAlignment.END))
                captionLast = false; // it's odd having caption last for alignments other than END

            captionContainer.set(new CaptionWidget(captionLast ? valuePanel : sizedLabel, GFlexAlignment.START, panelCaptionAlignment, panelValueAlignment));
            return (captionLast ? sizedLabel : valuePanel).view;
        }

        InlineComponentViewWidget componentViewWidget = new InlineComponentViewWidget(panelVertical);

        if (sizedLabel != null && !captionLast)
            componentViewWidget.add(sizedLabel, panelCaptionAlignment, false, "caption");

        if (sizedComment != null && commentFirst && !verticalDiffers)
            componentViewWidget.add(sizedComment, panelCommentAlignment, false, "comment");

        componentViewWidget.add(valuePanel, panelValueAlignment, true, "");

        if (sizedComment != null && !verticalDiffers && !commentFirst)
            componentViewWidget.add(sizedComment, panelCommentAlignment, false, "comment");

        if (sizedLabel != null && captionLast)
            componentViewWidget.add(sizedLabel, panelCaptionAlignment, false, "caption");

        if(inline)
            return componentViewWidget;

        SizedFlexPanel panel = new SizedFlexPanel(panelVertical);
        panel.transparentResize = true;
        panel.addStyleName("panel-container");
        componentViewWidget.add(panel, 0);
        // mostly it is needed to handle margins / paddings / layouting but we do it ourselves
//        cellRenderer.renderPanelContainer(panel);

        return new SizedWidget(panel).view;
    }

    @Override
    public ComponentViewWidget getComponentViewWidget() {
        return sizedView;
    }

    @Override
    protected Widget getLabelWidget() {
        return label;
    }

    @Override
    protected Widget getTooltipWidget() {
        return label != null ? label : (comment != null ? comment : super.getTooltipWidget());
    }

    protected void setLabelText(String text) {
        if(label == null) {
            assert text == null || text.isEmpty();
            return;
        }

        BaseImage.updateText(label, text);
    }

    protected void setLabelClasses(String classes) {
        // there can be no caption
        if(label == null) {
            return;
        }

        BaseImage.updateClasses(label, classes);
    }

    protected void setCommentText(String text) {
        if(comment == null) {
            assert text == null || text.isEmpty();
            return;
        }

        GwtClientUtils.setCaptionHtmlOrText(comment.getElement(), text);
    }

    protected void setCommentClasses(String classes) {
        if(comment == null) {
            return;
        }

        comment.getElement().addClassName(classes);
    }
}
