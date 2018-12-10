package lsfusion.gwt.server.form.form.handlers;

import lsfusion.gwt.server.form.LSFusionDispatchServlet;
import lsfusion.gwt.server.form.form.FormServerResponseActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;
import lsfusion.gwt.server.form.form.provider.FormSessionObject;
import lsfusion.gwt.server.form.convert.GwtToClientConverter;
import lsfusion.gwt.shared.form.actions.form.ExecuteEditAction;
import lsfusion.gwt.shared.form.actions.form.ServerResponseResult;

import java.io.IOException;

public class ExecuteEditActionHandler extends FormServerResponseActionHandler<ExecuteEditAction> {
    private static GwtToClientConverter gwtConverter = GwtToClientConverter.getInstance();

    public ExecuteEditActionHandler(LSFusionDispatchServlet servlet) {
        super(servlet);
    }

    @Override
    public ServerResponseResult executeEx(ExecuteEditAction action, ExecutionContext context) throws DispatchException, IOException {
        FormSessionObject form = getFormSessionObject(action.formSessionID);

        byte[] fullKey = gwtConverter.convertOrCast(action.fullKey);

        return getServerResponseResult(form, form.remoteForm.executeEditAction(action.requestIndex, defaultLastReceivedRequestIndex, action.propertyId, fullKey, action.actionSID));
    }
}
