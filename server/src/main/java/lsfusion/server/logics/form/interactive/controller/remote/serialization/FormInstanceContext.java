package lsfusion.server.logics.form.interactive.controller.remote.serialization;

import com.google.common.base.Throwables;
import lsfusion.base.Pair;
import lsfusion.server.base.controller.thread.AssertSynchronized;
import lsfusion.server.data.sql.exception.SQLHandledException;
import lsfusion.server.logics.form.interactive.action.input.InputValueList;
import lsfusion.server.logics.form.interactive.design.FormView;
import lsfusion.server.logics.form.interactive.property.AsyncMode;
import lsfusion.server.logics.form.interactive.property.PropertyAsync;
import lsfusion.server.logics.form.struct.FormEntity;
import lsfusion.server.logics.property.Property;
import lsfusion.server.physics.admin.Settings;
import lsfusion.server.physics.admin.authentication.security.policy.SecurityPolicy;
import lsfusion.server.physics.exec.db.controller.manager.DBManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FormInstanceContext {
    // static part
    public final FormEntity entity;
    public final FormView view;

    // dynamic part
    public final SecurityPolicy securityPolicy;
    public final boolean useBootstrap;
    public final DBManager dbManager;

    public FormInstanceContext(FormEntity entity, FormView view, SecurityPolicy securityPolicy, boolean useBootstrap, DBManager dbManager) {
        this.entity = entity;
        this.view = view;

        this.securityPolicy = securityPolicy;
        this.useBootstrap = useBootstrap;
        this.dbManager = dbManager;
    }

    // when we have real context, but want to cache the result so we'll use GLOBAL context
    public static FormInstanceContext CACHE(FormEntity formEntity) {
        return formEntity.getGlobalContext();
    }

    private final Map<Pair<Property, DBManager.Param>, Pair<Integer, Integer>> values = new HashMap<>();
    // assert that it is synchronized in all remote calls / form instancing
    @AssertSynchronized
//    @ManualParamStrongLazy
    public Pair<Integer, Integer> getValues(InputValueList propValues) {
        Pair<Property, DBManager.Param> cacheKey = new Pair<>(propValues.getCacheKey(),
                propValues.getCacheParam("", 0, AsyncMode.OBJECTS, null));
                // we need static neededCount to guarantee that getValues will be the same during the form lifecycle
                // env can be null, because getEnvDepends should be empty (see getSelectProperty)
        Pair<Integer, Integer> result = values.get(cacheKey);
        if(result == null) {
            result = readValues(propValues);
            values.put(cacheKey, result);
        }
        return result;
    }

    // assert that it is called during form instancing
    @AssertSynchronized
    private Pair<Integer, Integer> readValues(InputValueList values) {
        int maxValuesNeeded = Settings.get().getMaxInterfaceStatForValueCombo();
        PropertyAsync[] asyncValues;
        try {
            asyncValues = dbManager.getAsyncValues(values, "", maxValuesNeeded + 1, AsyncMode.OBJECTS);
        } catch (SQLException | SQLHandledException e) {
            throw Throwables.propagate(e);
        }
        int count = asyncValues.length;

        int sumLength = 0;
        for(PropertyAsync asyncValue : asyncValues)
            sumLength += asyncValue.rawString.trim().length();

        return new Pair<>(sumLength, count);
    }
}
