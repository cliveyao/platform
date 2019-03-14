package lsfusion.server.physics.dev.debug;

import com.google.common.base.Throwables;
import lsfusion.base.BaseUtils;
import lsfusion.base.ExceptionUtils;
import lsfusion.base.Pair;
import lsfusion.base.lambda.Processor;
import lsfusion.base.col.MapFact;
import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.immutable.ImOrderMap;
import lsfusion.base.col.interfaces.immutable.ImOrderSet;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.col.interfaces.mutable.MOrderExclSet;
import lsfusion.base.col.interfaces.mutable.SymmAddValue;
import lsfusion.base.col.interfaces.mutable.add.MAddMap;
import lsfusion.base.col.interfaces.mutable.mapvalue.GetKeyValue;
import lsfusion.base.col.interfaces.mutable.mapvalue.GetValue;
import lsfusion.server.ServerLoggers;
import lsfusion.server.SystemProperties;
import lsfusion.server.base.caches.IdentityLazy;
import lsfusion.server.language.linear.LA;
import lsfusion.server.logics.action.Action;
import lsfusion.server.logics.action.ExecutionContext;
import lsfusion.server.logics.classes.sets.ResolveClassSet;
import lsfusion.server.base.context.ExecutionStack;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.logics.BusinessLogics;
import lsfusion.server.data.ObjectValue;
import lsfusion.server.language.linear.LCP;
import lsfusion.server.logics.property.*;
import lsfusion.server.logics.form.interactive.instance.FormEnvironment;
import lsfusion.server.logics.action.flow.FlowResult;
import lsfusion.server.language.EvalUtils;
import lsfusion.server.language.ScriptingErrorLog;
import lsfusion.server.language.ScriptingLogicsModule;
import lsfusion.server.logics.action.session.DataSession;
import lsfusion.server.logics.action.session.change.PropertyChange;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.logics.property.data.DataProperty;
import lsfusion.server.logics.property.oraction.PropertyInterface;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import sun.management.jmxremote.LocalRMIServerSocketFactory;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.*;

import static java.util.Arrays.asList;
import static lsfusion.server.context.ThreadLocalContext.getRmiManager;
import static lsfusion.server.physics.dev.debug.ActionDelegationType.*;

public class ActionPropertyDebugger implements DebuggerService {
    public static final String DELEGATES_HOLDER_CLASS_PACKAGE = "lsfusion.server.logics.debug";
    public static final String DELEGATES_HOLDER_CLASS_NAME_PREFIX = "DebugDelegatesHolder_";
    public static final String DELEGATES_HOLDER_CLASS_FQN_PREFIX = DELEGATES_HOLDER_CLASS_PACKAGE + "." + DELEGATES_HOLDER_CLASS_NAME_PREFIX;

    private static final Logger logger = ServerLoggers.systemLogger;

    private static final ActionPropertyDebugger instance = new ActionPropertyDebugger();

    public static ActionPropertyDebugger getInstance() {
        return instance;
    }
    
    public boolean steppingMode = false;

    public boolean isEnabled() {
//        return true;
        return SystemProperties.isActionDebugEnabled;
    }

    private final MAddMap<Pair<String, Integer>, DebugInfo> firstInLineDelegates = MapFact.mAddMap(new SymmAddValue<Pair<String, Integer>, DebugInfo>() {
        public DebugInfo addValue(Pair<String, Integer> key, DebugInfo prevValue, DebugInfo newValue) {
            return newValue.getDebuggerOffset() > prevValue.getDebuggerOffset() ? prevValue : newValue;
        }
    });
    public boolean isDebugFirstInLine(DebugInfo debugInfo) {
        return BaseUtils.hashEquals(firstInLineDelegates.get(debugInfo.getDebuggerModuleLine()), debugInfo);
    }

    //в Java есть ограничение на количество имён в файле (~65000), поэтому нельзя всё впихнуть в один файл
    //приходится разбивать - пока просто для каждого модуля - свой класс
    private Map<String, Class> delegatesHolderClasses = new HashMap<>();

    private ActionPropertyDebugger() {
    } //singleton

    private Set<DebugInfo> delegates = new HashSet<>();

    public ImMap<String, ImSet<DebugInfo>> getGroupDelegates() {
        return SetFact.fromJavaSet(delegates).group(new BaseUtils.Group<String, DebugInfo>() {
            @Override
            public String group(DebugInfo key) {
                return key.getModuleName();
            }
        });
    }

    public synchronized <P extends PropertyInterface> void addDelegate(DebugInfo debugInfo) {
        delegates.add(debugInfo);
        firstInLineDelegates.add(debugInfo.getDebuggerModuleLine(), debugInfo);
    }

    public synchronized <P extends PropertyInterface> void setNewDebugStack(Action<P> property) {
        property.setNewDebugStack(true);
    }

    public synchronized <P extends PropertyInterface> void addParamInfo(Action<P> property, Map<String, P> paramsToInterfaces, Map<String, String> paramsToClassFQN) {
        ParamDebugInfo<P> paramInfo = new ParamDebugInfo<>(MapFact.fromJavaRevMap(paramsToInterfaces), MapFact.fromJavaMap(paramsToClassFQN));

        property.setParamInfo(paramInfo);
    }

    public void compileDelegatesHolders(File sourceDir, ImMap<String, ImSet<DebugInfo>> modules) throws IOException, ClassNotFoundException {
        List<InMemoryJavaFileObject> filesToCompile = new ArrayList<>();

        generateDelegateClasses(modules, filesToCompile);

        compileDelegateClasses(sourceDir.getAbsolutePath(), filesToCompile);

        loadDelegateClasses(modules.keys(), sourceDir);
    }

    private void generateDelegateClasses(ImMap<String, ImSet<DebugInfo>> groupedActions, List<InMemoryJavaFileObject> filesToCompile) {
        for (int i = 0,size = groupedActions.size(); i < size; i++) {
            filesToCompile.add(createJavaFileObject(groupedActions.getKey(i), groupedActions.getValue(i)));
        }
    }

    private InMemoryJavaFileObject createJavaFileObject(String moduleName, ImSet<DebugInfo> infos) {
        String holderClassName = DELEGATES_HOLDER_CLASS_NAME_PREFIX + moduleName;

        String holderFQN = DELEGATES_HOLDER_CLASS_FQN_PREFIX + moduleName;

        String sourceString =
            "package " + DELEGATES_HOLDER_CLASS_PACKAGE + ";\n" +
            "\n" +
            "import lsfusion.server.data.SQLHandledException;\n" +
            "import lsfusion.server.logics.action.ActionProperty;\n" +
            "import lsfusion.server.logics.property.Property;\n" +
            "import lsfusion.server.logics.property.classes.ClassPropertyInterface;\n" +
            "import lsfusion.server.logics.property.data.DataProperty;\n" +
            "import lsfusion.server.logics.action.ExecutionContext;\n" +
            "import lsfusion.server.logics.action.flow.FlowResult;\n" +
            "import ClassChange;\n" +
            "import DataSession;\n" +
            "import PropertyChange;\n" +
            "\n" +
            "import java.sql.SQLException;\n" +
            "\n" +
            "public class " + holderClassName + " {\n";

        for (DebugInfo info : infos) {
            String methodName = getMethodName(info);

            if (info instanceof ActionDebugInfo) {
                ActionDebugInfo actionDebugInfo = (ActionDebugInfo) info;
                String body = (actionDebugInfo.delegationType == IN_DELEGATE ? "return action.executeImpl(context);" : "return null;");
                sourceString +=
                        "    public static FlowResult " + methodName + "(ActionProperty action, ExecutionContext context) throws SQLException, SQLHandledException {\n" +
                        "        " + body + "\n" +
                        "    }\n";
            } else if (info instanceof CalcPropertyDebugInfo) {
                sourceString +=
                        "    public static void " + methodName + "(DataSession session, Property property, PropertyChange<ClassPropertyInterface> change) throws SQLException, SQLHandledException {\n" +
                        "        session.changePropertyImpl((DataProperty) property, change);\n" +
                        "    }\n";
            } else { // class change
                sourceString +=
                        "    public static void " + methodName + "() throws SQLException, SQLHandledException {\n" +
                        "    }\n";
            }
        }
        sourceString += "}";

        try {
            return new InMemoryJavaFileObject(holderFQN, sourceString);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void compileDelegateClasses(String outputFolder, List<InMemoryJavaFileObject> filesToCompile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticListener diagnostics = new IgnoreDiagnosticListener();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ENGLISH, null);

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, asList("-g", "-d", outputFolder), null, filesToCompile);
        if (!task.call()) {
            throw new IllegalStateException("Compilation of debugger delegate files failed. ");
        }
    }

    public void loadDelegateClasses(ImSet<String> moduleNames, File sourceDir) throws MalformedURLException, ClassNotFoundException {
        // Load and instantiate compiled class.
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{sourceDir.toURI().toURL()});

        for (String moduleName : moduleNames) {
            Class holderClass = Class.forName(DELEGATES_HOLDER_CLASS_FQN_PREFIX + moduleName, true, classLoader);
            delegatesHolderClasses.put(moduleName, holderClass);
        }
    }

    private String getMethodName(DebugInfo info) {
        return info.getDebuggerMethodName(isDebugFirstInLine(info));
    }

    public <P extends PropertyInterface> FlowResult delegate(Action<P> action, ExecutionContext<P> context) throws SQLException, SQLHandledException {
        ActionDebugInfo debugInfo = action.getDebugInfo();

        if (debugInfo == null || !isEnabled()) {
            throw new IllegalStateException("Shouldn't happen: debug isn't enabled");
        }

        Class<?> delegatesHolderClass = delegatesHolderClasses.get(debugInfo.getModuleName());
        if (delegatesHolderClass == null)
            return action.executeImpl(context);

        FlowResult result = null;
        if (debugInfo.delegationType == BEFORE_DELEGATE) {
            result = action.executeImpl(context);
        }

        try {
            Method method = delegatesHolderClass.getMethod(getMethodName(debugInfo), Action.class, ExecutionContext.class);
            FlowResult delegateResult = (FlowResult) commonExecuteDelegate(delegatesHolderClass, method, action, context);
            if (debugInfo.delegationType == IN_DELEGATE) {
                return delegateResult;
            }
        } catch (InvocationTargetException e) {
            throw ExceptionUtils.propagate(e.getCause(), SQLException.class, SQLHandledException.class);
        } catch (Exception e) {
            logger.warn("Error while delegating to ActionPropertyDebugger: ", e);
            //если упало исключение в reflection, то просто вызываем оригинальный execute
            if(debugInfo.delegationType == IN_DELEGATE)
                return action.executeImpl(context);
        }

        if (debugInfo.delegationType == AFTER_DELEGATE) {
            result = action.executeImpl(context);
        }

        return result;
    }

    public void delegate(DataSession dataSession, DataProperty property, PropertyChange<ClassPropertyInterface> change) throws SQLException, SQLHandledException {
        CalcPropertyDebugInfo debugInfo = property.getDebugInfo();

        if (debugInfo == null || !isEnabled()) {
            throw new IllegalStateException("Shouldn't happen: debug isn't enabled");
        }

        Class<?> delegatesHolderClass = delegatesHolderClasses.get(debugInfo.getModuleName());
        if (delegatesHolderClass != null) {
            try {
                Method method = delegatesHolderClass.getMethod(getMethodName(debugInfo), DataSession.class, Property.class, PropertyChange.class);
                method.invoke(delegatesHolderClass, dataSession, property, change);
                return;
            } catch (InvocationTargetException e) {
                throw ExceptionUtils.propagate(e.getCause(), SQLException.class, SQLHandledException.class);
            } catch (Exception e) {
                logger.warn("Error while delegating to ActionPropertyDebugger: ", e);
                dataSession.changePropertyImpl(property, change);
            }
        }

        dataSession.changePropertyImpl(property, change);
    }

    public void delegate(ClassDebugInfo debugInfo) throws SQLException, SQLHandledException {
        if (debugInfo == null || !isEnabled()) {
            throw new IllegalStateException("Shouldn't happen: debug isn't enabled");
        }

        Class<?> delegatesHolderClass = delegatesHolderClasses.get(debugInfo.getModuleName());
        if (delegatesHolderClass != null) {
            try {
                Method method = delegatesHolderClass.getMethod(getMethodName(debugInfo));
                method.invoke(delegatesHolderClass);
            } catch (InvocationTargetException e) {
                throw ExceptionUtils.propagate(e.getCause(), SQLException.class, SQLHandledException.class);
            } catch (Exception e) {
                logger.warn("Error while delegating to ActionPropertyDebugger: ", e);
            }
        }
    }
    
    private Object commonExecuteDelegate(Class<?> clazz, Method method, Action action, ExecutionContext context) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(clazz, action, context);
    }
    
    public static ThreadLocal<Boolean> watchHack = new ThreadLocal<>();

    @SuppressWarnings("UnusedDeclaration") //this method is used by IDEA plugin
    private Object evalAction(Action action, ExecutionContext context, String namespace, String require, String priorities, String statements)
            throws EvalUtils.EvaluationException, ScriptingErrorLog.SemanticErrorException, SQLException, SQLHandledException {
        return evalAction(context, namespace, require, priorities, "{" + StringEscapeUtils.unescapeJava(statements) + "}", null);
    }

    @SuppressWarnings("UnusedDeclaration") //this method is used by IDEA plugin
    private Object eval(Action action, ExecutionContext<?> context, String namespace, String require, String priorities, String expression)
        throws EvalUtils.EvaluationException, ScriptingErrorLog.SemanticErrorException, SQLException, SQLHandledException {

//        context.showStack();
        if (!isEnabled()) {
            throw new IllegalStateException("Action debugger isn't enabled!");
        }

        final String valueName = "sfdjdfkljgfk";

        String escapedExpression = StringEscapeUtils.unescapeJava(expression);
        if (!escapedExpression.contains("ORDER")) // жестковато конечно пока, но будет работать
            escapedExpression = "(" + escapedExpression + ")";
        String actionText = "FOR " + valueName + " == " + escapedExpression + " DO watch();";
        return evalAction(context, namespace, require, priorities, actionText, valueName);
    }

    private Object evalAction(ExecutionContext<?> context, String namespace, String require, String priorities, String expression, final String valueName) throws EvalUtils.EvaluationException, ScriptingErrorLog.SemanticErrorException, SQLException, SQLHandledException {
        //используем все доступные в контексте параметры
        ExecutionStack stack = context.stack;

        ImOrderMap<String, String> paramsWithClasses = stack.getAllParamsWithClassesInStack().toOrderMap();
        ImMap<String, ObjectValue> paramsWithValues = stack.getAllParamsWithValuesInStack();

        ImSet<Pair<LCP, List<ResolveClassSet>>> locals = stack.getAllLocalsInStack();

        ExecutionContext<PropertyInterface> watchContext = context.override(MapFact.<PropertyInterface, ObjectValue>EMPTY(), (FormEnvironment<PropertyInterface>) null);

        Pair<LA<PropertyInterface>, Boolean> evalResult = evalAction(namespace, require, priorities, expression, paramsWithClasses, locals, watchContext.isPrevEventScope(), context.getBL());
        LA<PropertyInterface> evalAction = evalResult.first;
        boolean forExHack = evalResult.second; // hack для выяснения есть расширение контекста или нет (чтобы знать пустой список или null светить)

        final MOrderExclSet<ImMap<String, ObjectValue>> mResult = SetFact.mOrderExclSet();
        final ImSet<String> externalParamNames = paramsWithClasses.keys();
        watchContext.setWatcher(new Processor<ImMap<String, ObjectValue>>() {
            public void proceed(ImMap<String, ObjectValue> value) {
                mResult.exclAdd(value.remove(externalParamNames));
            }
        });

        ObjectValue[] orderedValues = paramsWithClasses.keyOrderSet().mapOrderMap(paramsWithValues).valuesList().toArray(new ObjectValue[paramsWithClasses.size()]);
        evalAction.execute(watchContext, orderedValues);

        ImOrderSet<ImMap<String, ObjectValue>> result = mResult.immutableOrder();
        if(result.size() == 0) {
            if(forExHack)
                return new ArrayList();
            else
                return null;
        }
        if(result.size() == 1) {
            ImMap<String, ObjectValue> value = result.single();
            if(value.size() == 1)
                return value.singleValue();
        }
        return result.mapOrderSetValues(new GetValue<ActionWatchEntry, ImMap<String,ObjectValue>>() {
            public ActionWatchEntry getMapValue(ImMap<String, ObjectValue> value) {
                return getWatchEntry(value, valueName);
            }
        }).toJavaList();
    }

    @IdentityLazy
    private Pair<LA<PropertyInterface>, Boolean> evalAction(String namespace, String require, String priorities, String action, ImOrderMap<String, String> paramWithClasses, ImSet<Pair<LCP, List<ResolveClassSet>>> locals, boolean prevEventScope, BusinessLogics bl) throws EvalUtils.EvaluationException, ScriptingErrorLog.SemanticErrorException {
        
        String paramString = "";
        for (int i = 0, size = paramWithClasses.size(); i < size; i++) {
            String param = paramWithClasses.getKey(i);
            String clazz = paramWithClasses.getValue(i);
            
            if (!paramString.isEmpty()) {
                paramString += ", "; 
            }
            
            if (clazz != null) {
                paramString += clazz + " ";
            }

            paramString += param;
        }

        String script = "evalStub(" + paramString + ") {" + action + " } ";

        watchHack.set(false);

        ScriptingLogicsModule module = EvalUtils.evaluate(bl, namespace, require, priorities, locals, prevEventScope, script);

        boolean forExHack = watchHack.get();
        watchHack.set(null);

        String evalPropName = module.getNamespace() + "." + "evalStub";

        return new Pair<>((LA<PropertyInterface>) module.findAction(evalPropName), forExHack);
    }

    private static ActionWatchEntry getWatchEntry(ImMap<String, ObjectValue> row, String valueName) {
        ObjectValue value = null;
        if(valueName !=  null) {
            value = row.get(valueName);
            // непонятно зачем, сбивает в свойствах с Logical значениями
//            if (value instanceof DataObject && ((DataObject) value).objectClass instanceof LogicalClass) {
//                value = null;
//            }
            row = row.remove(valueName);
        }
        return new ActionWatchEntry(row.toOrderMap().mapOrderSetValues(new GetKeyValue<ActionWatchEntry.Param, String, ObjectValue>() {
            public ActionWatchEntry.Param getMapValue(String key, ObjectValue value) {
                return new ActionWatchEntry.Param(key, value);
            }
        }).toJavaList(), value);
    }
    
    private Map<Pair<String, Integer>, Object> breakpoints = MapFact.getGlobalConcurrentHashMap();

    @Override
    public void registerBreakpoint(String module, Integer line) throws RemoteException {
        breakpoints.put(new Pair<>(module, line), 0);
    }

    @Override
    public void unregisterBreakpoint(String module, Integer line) throws RemoteException {
        breakpoints.remove(new Pair<>(module, line));
    }
    
    public boolean hasBreakpoint(ImSet<Pair<String, Integer>> actions, ImSet<Pair<String, Integer>> changeProps) {
        for (Pair<String, Integer> breakpoint : breakpoints.keySet()) {
            if (actions.contains(breakpoint) || changeProps.contains(breakpoint)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void registerStepping() throws RemoteException {
        steppingMode = true;
    }

    @Override
    public void unregisterStepping() throws RemoteException {
        steppingMode = false;
    }

    public static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private String contents = null;

        public InMemoryJavaFileObject(String className, String contents) throws Exception {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return contents;
        }
    }

    private static class IgnoreDiagnosticListener implements DiagnosticListener {
        @Override
        public void report(Diagnostic diagnostic) {
            // ignore
        }
    }
}
