package lsfusion.gwt.server.form.navigator;

import lsfusion.gwt.server.form.LSFusionDispatchServlet;
import lsfusion.gwt.shared.form.actions.navigator.CloseNavigator;
import lsfusion.gwt.shared.form.actions.navigator.NavigatorAction;
import lsfusion.interop.navigator.RemoteNavigatorInterface;
import lsfusion.interop.remote.ClientCallBackInterface;
import net.customware.gwt.dispatch.shared.Result;

import java.rmi.RemoteException;

public abstract class NavigatorActionHandler<A extends NavigatorAction<R>, R extends Result> extends LogicsAndNavigatorActionHandler<A, R> {

    // shortcut's
    protected RemoteNavigatorInterface getRemoteNavigator(A action) {
        return getLogicsAndNavigatorSessionObject(action).remoteNavigator;
    }
    protected ClientCallBackInterface getClientCallback(A action) throws RemoteException {
        return getLogicsAndNavigatorSessionObject(action).getRemoteCallback();
    }

    public NavigatorActionHandler(LSFusionDispatchServlet servlet) {
        super(servlet);
    }

    protected String getActionDetails(A action) {
        String message = super.getActionDetails(action);

        if (action instanceof CloseNavigator) {
            message += " TAB ID " + ((CloseNavigator) action).sessionID + " IN " + servlet.getSessionInfo();
        }
        return message;
    }
}
