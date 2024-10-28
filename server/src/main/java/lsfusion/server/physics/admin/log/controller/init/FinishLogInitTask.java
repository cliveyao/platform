package lsfusion.server.physics.admin.log.controller.init;

import lsfusion.server.logics.property.Property;
import lsfusion.server.logics.property.controller.init.GroupPropertiesTask;
import lsfusion.server.logics.property.oraction.ActionOrProperty;

public class FinishLogInitTask extends GroupPropertiesTask {
    public String getCaption() {
        return "Setting up loggables";
    }

    protected void runTask(ActionOrProperty property) {
        if(property instanceof Property)
            getBL().finishLogInit((Property) property);
    }
}
