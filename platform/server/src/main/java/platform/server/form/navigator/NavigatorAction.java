package platform.server.form.navigator;

import platform.server.logics.BusinessLogics;
import platform.server.logics.property.ActionProperty;
import platform.server.logics.property.Property;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class NavigatorAction<T extends BusinessLogics<T>> extends NavigatorElement<T> {
    private ImageIcon image;

    private ActionProperty property;

    public NavigatorAction(String sID, String caption) {
        this(null, sID, caption);
    }

    public NavigatorAction(NavigatorElement<T> parent, String sID, String caption) {
        this(parent, sID, caption, null);
    }

    public NavigatorAction(NavigatorElement<T> parent, String sID, String caption, String icon) {
        super(parent, sID, caption, icon);
        image = new ImageIcon(NavigatorAction.class.getResource(icon!=null ? icon : "/images/action.png"));
    }

    @Override
    public byte getTypeID() {
        return 2;
    }

    @Override
    public void serialize(DataOutputStream outStream) throws IOException {
        super.serialize(outStream);
    }

    @Override
    public ImageIcon getImage() {
        return image;
    }

    public void setProperty(ActionProperty property) {
        this.property = property;
    }

    public ActionProperty getProperty() {
        return property;
    }
}