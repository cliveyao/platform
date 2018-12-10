package lsfusion.gwt.server.form.form.handlers;

import lsfusion.gwt.server.form.form.FormServerResponseActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;
import lsfusion.gwt.server.form.LSFusionDispatchServlet;
import lsfusion.gwt.server.form.form.provider.FormSessionObject;
import lsfusion.gwt.server.form.convert.GwtToClientConverter;
import lsfusion.gwt.shared.form.actions.form.ChangeClassView;
import lsfusion.gwt.shared.form.actions.form.ServerResponseResult;
import lsfusion.gwt.shared.form.view.GClassViewType;
import lsfusion.interop.ClassViewType;

import java.io.IOException;

public class ChangeClassViewHandler extends FormServerResponseActionHandler<ChangeClassView> {
    public ChangeClassViewHandler(LSFusionDispatchServlet servlet) {
        super(servlet);
    }

    @Override
    public ServerResponseResult executeEx(ChangeClassView action, ExecutionContext context) throws DispatchException, IOException {
        FormSessionObject form = getFormSessionObject(action.formSessionID);
        return getServerResponseResult(
                form,
                form.remoteForm.changeClassView(action.requestIndex, defaultLastReceivedRequestIndex, action.groupObjectId, convertClassView(action.newClassView))
        );
    }

    private ClassViewType convertClassView(GClassViewType newClassView) {
        return GwtToClientConverter.getInstance().convertOrNull(newClassView);
    }
}
