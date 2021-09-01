package lsfusion.gwt.client.form.controller.dispatch;

import lsfusion.gwt.client.GForm;
import lsfusion.gwt.client.RemoteDispatchAsync;
import lsfusion.gwt.client.base.result.ListResult;
import lsfusion.gwt.client.base.result.VoidResult;
import lsfusion.gwt.client.controller.remote.action.BaseAction;
import lsfusion.gwt.client.controller.remote.action.PriorityErrorHandlingCallback;
import lsfusion.gwt.client.controller.remote.action.RequestAction;
import lsfusion.gwt.client.controller.remote.action.form.*;
import lsfusion.gwt.client.form.controller.GFormController;
import net.customware.gwt.dispatch.shared.Result;
import net.customware.gwt.dispatch.shared.general.StringResult;

public class FormDispatchAsync extends RemoteDispatchAsync {
    private final GForm form;
    private final GFormController formController;

    //отдельный флаг закрытой формы нужен, чтобы не посылать случайных запросов в закрытую форму (в частности changePageSize)
    private boolean formClosed = false;

    public FormDispatchAsync(GFormController formController) {
        this.formController = formController;
        this.form = formController.getForm();
    }

    @Override
    protected <A extends BaseAction<R>, R extends Result> void fillAction(A action) {
        ((FormAction) action).formSessionID = form.sessionID;
    }

    @Override
    protected <A extends RequestAction<R>, R extends Result> long fillQueuedAction(A action) {
        FormRequestAction formRequestAction = (FormRequestAction) action;
        if (action instanceof FormRequestCountingAction)
            formRequestAction.requestIndex = nextRequestIndex++;
        formRequestAction.lastReceivedRequestIndex = lastReceivedRequestIndex;
        return formRequestAction.requestIndex;
    }

    @Override
    protected void showAsync(boolean set) {
        formController.showAsync(set);
    }

    @Override
    protected boolean isEditing() {
        return formController.isEditing();
    }

    @Override
    protected boolean isClosed() {
        return formClosed;
    }

    public void close() {
        formClosed = true;
    }

    @Override
    public void getServerActionMessage(PriorityErrorHandlingCallback<StringResult> callback) {
        executePriority(new GetRemoteActionMessage(), callback);
    }

    @Override
    public void getServerActionMessageList(PriorityErrorHandlingCallback<ListResult> callback) {
        executePriority(new GetRemoteActionMessageList(), callback);
    }

    @Override
    public void interrupt(boolean cancelable) {
        executePriority(new Interrupt(cancelable), new PriorityErrorHandlingCallback<>());
    }

}
