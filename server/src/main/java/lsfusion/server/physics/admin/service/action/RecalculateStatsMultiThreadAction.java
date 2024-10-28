package lsfusion.server.physics.admin.service.action;

import lsfusion.interop.action.MessageClientAction;
import lsfusion.server.logics.classes.ValueClass;
import lsfusion.server.logics.property.controller.init.GroupPropertiesSingleTask;
import lsfusion.server.physics.admin.service.ServiceLogicsModule;
import lsfusion.server.physics.admin.service.task.RecalculateStatsTask;
import lsfusion.server.physics.dev.i18n.LocalizedString;

import static lsfusion.server.base.controller.thread.ThreadLocalContext.localize;

public class RecalculateStatsMultiThreadAction extends MultiThreadAction {

    public RecalculateStatsMultiThreadAction(ServiceLogicsModule LM, ValueClass... classes) {
        super(LM,classes);
    }

    @Override
    protected GroupPropertiesSingleTask createTask() {
        return new RecalculateStatsTask();
    }

    @Override
    protected String getCaptionError() {
        return localize("{logics.recalculation.stats.error}");
    }

    @Override
    protected Messages getMessages(GroupPropertiesSingleTask task, boolean errorOccurred) {
        return new Messages(localize(LocalizedString.createFormatted(errorOccurred ? "{logics.recalculation.failed}" : "{logics.recalculation.completed}",
                localize("{logics.recalculation.stats}"))) + task.getMessages(), localize("{logics.recalculation.stats}"));
    }
}