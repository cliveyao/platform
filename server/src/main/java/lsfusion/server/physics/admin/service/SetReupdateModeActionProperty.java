package lsfusion.server.physics.admin.service;

import lsfusion.server.logics.classes.ValueClass;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.physics.exec.DBManager;
import lsfusion.server.logics.property.ClassPropertyInterface;
import lsfusion.server.logics.action.ExecutionContext;
import lsfusion.server.language.ScriptingActionProperty;

import java.sql.SQLException;

public class SetReupdateModeActionProperty extends ScriptingActionProperty {

    public SetReupdateModeActionProperty(ServiceLogicsModule LM, ValueClass... classes) {
        super(LM, classes);
    }

    protected void executeCustom(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        Object value = context.getSingleKeyObject();
        DBManager.PROPERTY_REUPDATE = value!=null;
    }

    @Override
    protected boolean allowNulls() {
        return true;
    }

}
