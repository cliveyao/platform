package lsfusion.gwt.client.form.property.async;

import com.google.gwt.user.client.Event;
import lsfusion.gwt.client.base.AppStaticImage;
import lsfusion.gwt.client.base.view.EventHandler;
import lsfusion.gwt.client.form.controller.FormsController;
import lsfusion.gwt.client.form.controller.GFormController;
import lsfusion.gwt.client.form.property.GEventSource;
import lsfusion.gwt.client.form.property.cell.controller.EditContext;
import lsfusion.gwt.client.form.property.cell.controller.ExecContext;
import lsfusion.gwt.client.form.view.FormContainer;
import lsfusion.gwt.client.navigator.window.GModalityWindowFormType;
import lsfusion.gwt.client.navigator.window.GWindowFormType;

import java.util.function.Consumer;

public class GAsyncOpenForm extends GAsyncExec {
    public String canonicalName;
    public String caption;
    public AppStaticImage appImage;
    public boolean forbidDuplicate;
    public boolean modal;
    public GWindowFormType type;

    @SuppressWarnings("UnusedDeclaration")
    public GAsyncOpenForm() {
    }

    public GAsyncOpenForm(String canonicalName, String caption, AppStaticImage appImage, boolean forbidDuplicate, boolean modal, GWindowFormType type) {
        this.canonicalName = canonicalName;
        this.caption = caption;
        this.appImage = appImage;
        this.forbidDuplicate = forbidDuplicate;
        this.modal = modal;
        this.type = type;
    }

    @Override
    public void exec(GFormController formController, EventHandler handler, EditContext editContext, ExecContext execContext, String actionSID, GPushAsyncInput pushAsyncResult, GEventSource eventSource, Consumer<Long> onExec) {
        formController.asyncOpenForm(this, editContext, execContext, handler, actionSID, pushAsyncResult, eventSource, onExec);
    }

    public GWindowFormType getWindowType(boolean canShowDockedModal) {
        if(type.isDocked()) {
            //if current form is modal, new async form can't be non-modal
            if(modal && !canShowDockedModal)
                return GModalityWindowFormType.FLOAT;
        }
        return type;
    }

    @Override
    public void exec(FormsController formsController, GFormController formController, FormContainer formContainer, Event editEvent, GAsyncExecutor asyncExecutor) {
        formsController.asyncOpenForm(asyncExecutor.execute(), this, editEvent, null, null, formController);
    }
}