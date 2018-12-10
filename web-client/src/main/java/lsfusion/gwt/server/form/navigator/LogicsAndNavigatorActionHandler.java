package lsfusion.gwt.server.form.navigator;

import lsfusion.gwt.server.form.SimpleActionHandlerEx;
import lsfusion.gwt.server.form.navigator.provider.LogicsAndNavigatorProvider;
import lsfusion.gwt.server.form.navigator.provider.LogicsAndNavigatorSessionObject;
import lsfusion.gwt.server.form.LSFusionDispatchServlet;
import lsfusion.gwt.shared.form.actions.navigator.LogicsAndNavigatorAction;
import lsfusion.interop.RemoteLogicsInterface;
import net.customware.gwt.dispatch.shared.Result;

import java.rmi.RemoteException;

public abstract class LogicsAndNavigatorActionHandler<A extends LogicsAndNavigatorAction<R>, R extends Result> extends SimpleActionHandlerEx<A, R, RemoteLogicsInterface> {

    public LogicsAndNavigatorActionHandler(LSFusionDispatchServlet servlet) {
        super(servlet);
    }

    private LogicsAndNavigatorProvider getLogicsAndNavigatorProvider() {
        return servlet.getLogicsAndNavigatorProvider();
    }
    protected LogicsAndNavigatorSessionObject getLogicsAndNavigatorSessionObject(String sessionID) {
        return getLogicsAndNavigatorProvider().getLogicsAndNavigatorSessionObject(sessionID);
    }
    protected void removeLogicsAndNavigatorSessionObject(String sessionID) throws RemoteException {
        servlet.getFormProvider().removeFormSessionObjects(sessionID);
        getLogicsAndNavigatorProvider().removeLogicsAndNavigatorSessionObject(sessionID);
    }

    // shortcut's
    protected LogicsAndNavigatorSessionObject getLogicsAndNavigatorSessionObject(A action) {
        return getLogicsAndNavigatorSessionObject(action.sessionID);
    }

}
