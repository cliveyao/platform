package platform.interop.form;

import platform.interop.action.ClientAction;

import java.util.List;
import java.io.Serializable;

public class RemoteChanges implements Serializable {

    public final byte[] form;
    public final List<ClientAction> actions;
    public final int classID;

    public RemoteChanges(byte[] form, List<ClientAction> actions, int classID) {
        this.form = form;
        this.actions = actions;
        this.classID = classID;
    }
}
