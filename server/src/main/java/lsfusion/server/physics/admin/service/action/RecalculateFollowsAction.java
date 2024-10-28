package lsfusion.server.physics.admin.service.action;

import lsfusion.server.data.sql.exception.SQLHandledException;
import lsfusion.server.logics.action.controller.context.ExecutionContext;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.physics.admin.service.ServiceLogicsModule;
import lsfusion.server.physics.dev.i18n.LocalizedString;
import lsfusion.server.physics.dev.integration.internal.to.InternalAction;

import java.sql.SQLException;

import static lsfusion.server.base.controller.thread.ThreadLocalContext.localize;

public class RecalculateFollowsAction extends InternalAction {
    public RecalculateFollowsAction(ServiceLogicsModule LM) {
        super(LM);
    }
    @Override
    public void executeInternal(final ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        ServiceDBAction.runData(context, (session, isolatedTransaction) -> {
            String result = context.getBL().recalculateFollows(session, isolatedTransaction, context.stack);
            if(result != null)
                context.message(result, localize("{logics.recalculation.follows}"));
        });

        context.messageSuccess(localize(LocalizedString.createFormatted("{logics.recalculation.completed}", localize("{logics.recalculation.follows}"))), localize("{logics.recalculation.follows}"));
    }
}