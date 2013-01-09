package platform.server.logics.service;

import platform.server.classes.ValueClass;
import platform.server.logics.ServiceLogicsModule;
import platform.server.logics.property.ClassPropertyInterface;
import platform.server.logics.property.ExecutionContext;
import platform.server.logics.property.actions.AdminActionProperty;
import platform.server.logics.scripted.ScriptingActionProperty;

import java.sql.SQLException;

public class RestartActionProperty extends ScriptingActionProperty {
    public RestartActionProperty(ServiceLogicsModule LM) {
        super(LM, new ValueClass[]{});
    }

    public void executeCustom(ExecutionContext<ClassPropertyInterface> context) throws SQLException {
        context.getBL().restartController.scheduleRestart();
        context.getBL().updateRestartProperty();
        context.getBL().LM.formRefresh.execute(context);
    }
}