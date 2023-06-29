package lsfusion.gwt.client.base.exception;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import lsfusion.gwt.client.ClientMessages;
import lsfusion.gwt.client.base.GwtClientUtils;
import lsfusion.gwt.client.base.view.DialogModalWindow;
import lsfusion.gwt.client.base.view.FormButton;
import lsfusion.gwt.client.base.view.ResizableComplexPanel;
import lsfusion.gwt.client.form.design.view.flex.FlexTabbedPanel;

public class ErrorDialog extends DialogModalWindow {
    private static final ClientMessages messages = ClientMessages.Instance.get();

    private final FormButton closeButton;

    private FlexTabbedPanel stacks = null;

    public ErrorDialog(String caption, String messageHTML, String javaStack, String lsfStack) {
        super(caption, false, ModalWindowSize.EXTRA_LARGE);

        ResizableComplexPanel body = new ResizableComplexPanel();

        Widget message = new HTML(messageHTML);
        body.add(message);

        if (javaStack != null || lsfStack != null) {
            stacks = new FlexTabbedPanel();
            if (javaStack != null) {
                TextArea javaTA = new TextArea();
                javaTA.setStyleName("dialog-error-stack form-control");
                javaTA.setText(javaStack);
                stacks.addTab(javaTA, "Java");
            }
            if (lsfStack != null) {
                TextArea lsfTA = new TextArea();
                lsfTA.setStyleName("dialog-error-stack form-control");
                lsfTA.setText(lsfStack);
                stacks.addTab(lsfTA, "LSF");
            }
            stacks.selectTab(0);
            stacks.setVisible(false);

            body.add(stacks);
        }

        setBodyWidget(body);

        closeButton = new FormButton(messages.close(), FormButton.ButtonStyle.PRIMARY, event -> hide());
        addFooterWidget(closeButton);

        if (stacks != null) {
            FormButton moreButton = new FormButton(messages.more(), FormButton.ButtonStyle.SECONDARY, event -> stacks.setVisible(!stacks.isVisible()));
            addFooterWidget(moreButton);
        }
        
        body.addDomHandler(event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                GwtClientUtils.stopPropagation(event);
                hide();
            }
        }, KeyDownEvent.getType());
    }

    @Override
    public void show() {
        super.show();

        closeButton.setFocus(true);
    }

    public static void show(String caption, String message, String javaStack, String lsfStack) {
        new ErrorDialog(caption, message, javaStack, lsfStack).show();
    }
}
