package lsfusion.server.language;

import com.google.common.base.Throwables;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.data.DataObject;
import lsfusion.server.data.ObjectValue;
import lsfusion.server.language.linear.LA;
import lsfusion.server.logics.action.SystemExplicitAction;
import lsfusion.server.physics.dev.debug.ActionDelegationType;
import lsfusion.server.physics.dev.i18n.LocalizedString;
import lsfusion.server.language.linear.LCP;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.logics.property.infer.ClassType;
import lsfusion.server.logics.action.ExecutionContext;
import lsfusion.server.logics.property.oraction.PropertyInterface;
import lsfusion.server.logics.action.flow.ChangeFlowType;
import org.antlr.runtime.RecognitionException;

import java.sql.SQLException;
import java.util.List;

public class EvalActionProperty<P extends PropertyInterface> extends SystemExplicitAction {
    private final LCP<P> source;
    private final List<LCP<P>> params;
    private final ImMap<P, ClassPropertyInterface> mapSource;
    private boolean action;

    public EvalActionProperty(LocalizedString caption, LCP<P> source, List<LCP<P>> params, boolean action) {
        super(caption, source.getInterfaceClasses(ClassType.aroundPolicy));
        mapSource = source.listInterfaces.mapSet(getOrderInterfaces());
        this.source = source;
        this.params = params;
        this.action = action;
    }

    private String getScript(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        ImMap<P, ? extends ObjectValue> sourceToData = mapSource.join(context.getKeys());
        return (String) source.read(context, source.listInterfaces.mapList(sourceToData).toArray(new ObjectValue[interfaces.size()]));
    }

    private ObjectValue[] getParams(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        ObjectValue[] result = new ObjectValue[params.size()];
        for(int i = 0; i < params.size(); i++) {
            LCP<P> param = params.get(i);
            ImMap<P, ? extends ObjectValue> paramToData = param.listInterfaces.mapSet(getOrderInterfaces()).join(context.getKeys());
            result[i] = param.readClasses(context, (DataObject[]) param.listInterfaces.mapList(paramToData).toArray(new DataObject[param.listInterfaces.size()]));
        }
        return result;
    }

    @Override
    protected void executeCustom(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        String script = getScript(context);

        try {
            LA<?> runAction = context.getBL().evaluateRun(script, action);
            if (runAction != null)
                runAction.execute(context, getParams(context));
        } catch (EvalUtils.EvaluationException | RecognitionException e) {
            throw Throwables.propagate(e);
        }
    }
    @Override
    public boolean hasFlow(ChangeFlowType type) {
        return true;
    }

    @Override
    public ActionDelegationType getDelegationType(boolean modifyContext) {
        return ActionDelegationType.IN_DELEGATE; // execute just like EXEC operator
    }

    private String getMessage(Throwable e) {
        return e.getMessage() == null ? String.valueOf(e) : e.getMessage();
    }
}
