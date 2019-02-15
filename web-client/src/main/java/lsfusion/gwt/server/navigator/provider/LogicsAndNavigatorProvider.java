package lsfusion.gwt.server.navigator.provider;

import lsfusion.gwt.server.MainDispatchServlet;
import lsfusion.interop.RemoteLogicsInterface;

import java.rmi.RemoteException;

public interface LogicsAndNavigatorProvider {

    String createNavigator(RemoteLogicsInterface remoteLogics, MainDispatchServlet servlet, String logicsName) throws RemoteException;
    LogicsAndNavigatorSessionObject getLogicsAndNavigatorSessionObject(String sessionID);
    void removeLogicsAndNavigatorSessionObject(String sessionID) throws RemoteException;

    String getLogicsName(String sessionID);
    
    String getSessionInfo();
}
