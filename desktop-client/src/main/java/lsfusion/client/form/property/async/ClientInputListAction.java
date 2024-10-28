package lsfusion.client.form.property.async;

import lsfusion.base.file.AppImage;
import lsfusion.interop.form.event.BindingMode;

import javax.swing.*;
import java.util.List;

public class ClientInputListAction {
    public AppImage action;
    public String id;
    public ClientAsyncEventExec asyncExec;
    public KeyStroke keyStroke;
    public BindingMode editingBindingMode;
    public List<ClientQuickAccess> quickAccessList;
    public int index;

    public ClientInputListAction(AppImage action, String id, ClientAsyncEventExec asyncExec, KeyStroke keyStroke, BindingMode editingBindingMode, List<ClientQuickAccess> quickAccessList, int index) {
        this.action = action;
        this.id = id;
        this.asyncExec = asyncExec;
        this.keyStroke = keyStroke;
        this.editingBindingMode = editingBindingMode;
        this.quickAccessList = quickAccessList;
        this.index = index;
    }
}