package lsfusion.server.language;

import com.google.common.base.Throwables;
import lsfusion.base.BaseUtils;
import lsfusion.base.Pair;
import lsfusion.base.Result;
import lsfusion.base.col.ListFact;
import lsfusion.base.col.MapFact;
import lsfusion.base.col.SetFact;
import lsfusion.base.col.heavy.OrderedMap;
import lsfusion.base.col.interfaces.immutable.ImList;
import lsfusion.base.col.interfaces.immutable.ImOrderMap;
import lsfusion.base.col.interfaces.immutable.ImOrderSet;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.col.interfaces.mutable.*;
import lsfusion.base.col.interfaces.mutable.mapvalue.GetIndex;
import lsfusion.base.col.interfaces.mutable.mapvalue.GetValue;
import lsfusion.base.file.IOUtils;
import lsfusion.base.lambda.set.FunctionSet;
import lsfusion.interop.form.ModalityType;
import lsfusion.interop.form.WindowFormType;
import lsfusion.interop.form.design.Alignment;
import lsfusion.interop.form.print.FormPrintType;
import lsfusion.interop.form.property.ClassViewType;
import lsfusion.interop.form.property.ExtInt;
import lsfusion.server.base.caches.IdentityLazy;
import lsfusion.server.base.version.Version;
import lsfusion.server.data.expr.formula.CustomFormulaSyntax;
import lsfusion.server.data.expr.formula.SQLSyntaxType;
import lsfusion.server.data.expr.query.GroupType;
import lsfusion.server.data.expr.query.PartitionType;
import lsfusion.server.data.type.Type;
import lsfusion.server.language.action.ActionSettings;
import lsfusion.server.language.action.LA;
import lsfusion.server.language.form.FormPropertyOptions;
import lsfusion.server.language.form.ScriptingFormEntity;
import lsfusion.server.language.form.design.ScriptingFormView;
import lsfusion.server.language.metacode.MetaCodeFragment;
import lsfusion.server.language.navigator.window.BorderPosition;
import lsfusion.server.language.navigator.window.DockPosition;
import lsfusion.server.language.navigator.window.NavigatorWindowOptions;
import lsfusion.server.language.navigator.window.Orientation;
import lsfusion.server.language.property.LP;
import lsfusion.server.language.property.PropertySettings;
import lsfusion.server.language.property.oraction.ActionOrPropertySettings;
import lsfusion.server.language.property.oraction.LAP;
import lsfusion.server.language.property.oraction.MappedActionOrProperty;
import lsfusion.server.logics.BaseLogicsModule;
import lsfusion.server.logics.BusinessLogics;
import lsfusion.server.logics.LogicsModule;
import lsfusion.server.logics.action.Action;
import lsfusion.server.logics.action.ExplicitAction;
import lsfusion.server.logics.action.flow.BreakAction;
import lsfusion.server.logics.action.flow.ListCaseAction;
import lsfusion.server.logics.action.flow.ReturnAction;
import lsfusion.server.logics.action.implement.ActionMapImplement;
import lsfusion.server.logics.action.session.DataSession;
import lsfusion.server.logics.action.session.LocalNestedType;
import lsfusion.server.logics.action.session.changed.IncrementType;
import lsfusion.server.logics.classes.ValueClass;
import lsfusion.server.logics.classes.data.ColorClass;
import lsfusion.server.logics.classes.data.DataClass;
import lsfusion.server.logics.classes.data.LogicalClass;
import lsfusion.server.logics.classes.data.StringClass;
import lsfusion.server.logics.classes.data.file.FileClass;
import lsfusion.server.logics.classes.data.file.StaticFormatFileClass;
import lsfusion.server.logics.classes.data.integral.DoubleClass;
import lsfusion.server.logics.classes.data.integral.IntegerClass;
import lsfusion.server.logics.classes.data.integral.LongClass;
import lsfusion.server.logics.classes.data.integral.NumericClass;
import lsfusion.server.logics.classes.data.time.DateClass;
import lsfusion.server.logics.classes.data.time.DateTimeClass;
import lsfusion.server.logics.classes.data.time.TimeClass;
import lsfusion.server.logics.classes.user.AbstractCustomClass;
import lsfusion.server.logics.classes.user.ConcreteCustomClass;
import lsfusion.server.logics.classes.user.ConcreteObjectClass;
import lsfusion.server.logics.classes.user.CustomClass;
import lsfusion.server.logics.classes.user.set.ResolveClassSet;
import lsfusion.server.logics.event.BaseEvent;
import lsfusion.server.logics.event.Event;
import lsfusion.server.logics.event.PrevScope;
import lsfusion.server.logics.event.SessionEnvEvent;
import lsfusion.server.logics.form.interactive.ManageSessionType;
import lsfusion.server.logics.form.interactive.UpdateType;
import lsfusion.server.logics.form.interactive.action.edit.FormSessionScope;
import lsfusion.server.logics.form.interactive.action.focus.ActivateAction;
import lsfusion.server.logics.form.interactive.action.focus.IsActiveFormAction;
import lsfusion.server.logics.form.interactive.design.ComponentView;
import lsfusion.server.logics.form.interactive.design.FormView;
import lsfusion.server.logics.form.interactive.property.GroupObjectProp;
import lsfusion.server.logics.form.open.MappedForm;
import lsfusion.server.logics.form.open.ObjectSelector;
import lsfusion.server.logics.form.stat.struct.FormIntegrationType;
import lsfusion.server.logics.form.struct.FormEntity;
import lsfusion.server.logics.form.struct.group.AbstractGroup;
import lsfusion.server.logics.form.struct.object.GroupObjectEntity;
import lsfusion.server.logics.form.struct.object.ObjectEntity;
import lsfusion.server.logics.form.struct.property.PropertyDrawEntity;
import lsfusion.server.logics.navigator.DefaultIcon;
import lsfusion.server.logics.navigator.NavigatorElement;
import lsfusion.server.logics.navigator.window.*;
import lsfusion.server.logics.property.AggregateProperty;
import lsfusion.server.logics.property.Property;
import lsfusion.server.logics.property.PropertyFact;
import lsfusion.server.logics.property.Union;
import lsfusion.server.logics.property.cases.CaseUnionProperty;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.logics.property.classes.IsClassProperty;
import lsfusion.server.logics.property.classes.infer.ClassType;
import lsfusion.server.logics.property.data.DataProperty;
import lsfusion.server.logics.property.data.SessionDataProperty;
import lsfusion.server.logics.property.data.StoredDataProperty;
import lsfusion.server.logics.property.implement.PropertyInterfaceImplement;
import lsfusion.server.logics.property.oraction.ActionOrProperty;
import lsfusion.server.logics.property.oraction.PropertyInterface;
import lsfusion.server.logics.property.set.AggregateGroupProperty;
import lsfusion.server.logics.property.set.Cycle;
import lsfusion.server.logics.property.value.ValueProperty;
import lsfusion.server.physics.admin.Settings;
import lsfusion.server.physics.admin.log.ServerLoggers;
import lsfusion.server.physics.admin.reflection.ReflectionPropertyType;
import lsfusion.server.physics.admin.reflection.property.CanonicalNameProperty;
import lsfusion.server.physics.dev.debug.*;
import lsfusion.server.physics.dev.i18n.LocalizedString;
import lsfusion.server.physics.dev.id.name.ClassCanonicalNameUtils;
import lsfusion.server.physics.dev.id.name.PropertyCanonicalNameUtils;
import lsfusion.server.physics.dev.id.name.PropertyCompoundNameParser;
import lsfusion.server.physics.dev.id.resolve.ResolvingErrors;
import lsfusion.server.physics.dev.id.resolve.ResolvingErrors.ResolvingError;
import lsfusion.server.physics.dev.integration.external.to.ExternalDBAction;
import lsfusion.server.physics.dev.integration.external.to.ExternalDBFAction;
import lsfusion.server.physics.dev.integration.external.to.ExternalHTTPAction;
import lsfusion.server.physics.dev.integration.external.to.ExternalHttpMethod;
import lsfusion.server.physics.dev.integration.external.to.file.ReadAction;
import lsfusion.server.physics.dev.integration.external.to.file.WriteAction;
import lsfusion.server.physics.dev.integration.external.to.mail.SendEmailAction;
import lsfusion.server.physics.dev.integration.internal.to.StringFormulaProperty;
import lsfusion.server.physics.exec.db.table.ImplementTable;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;
import org.codehaus.janino.SimpleCompiler;

import javax.mail.Message;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static lsfusion.base.BaseUtils.*;
import static lsfusion.server.language.navigator.window.AlignmentUtils.*;
import static lsfusion.server.logics.property.oraction.ActionOrPropertyUtils.*;

public class ScriptingLogicsModule extends LogicsModule {

    private static final Logger scriptLogger = ServerLoggers.scriptLogger;

    protected final BusinessLogics BL;

    private String code = null;
    private String filename = null;
    private String path = null;
    private final ScriptingErrorLog errLog;
    private ScriptParser parser;
    private ScriptingLogicsModuleChecks checks;
    private List<String> warningList = new ArrayList<>();
    private Map<Property, String> alwaysNullProperties = new HashMap<>();

    private String lastOptimizedJPropSID = null;

    public enum ConstType { STATIC, INT, REAL, NUMERIC, STRING, LOGICAL, LONG, DATE, DATETIME, TIME, COLOR, NULL }
    public enum InsertPosition {IN, BEFORE, AFTER, FIRST}
    public enum WindowType {MENU, PANEL, TOOLBAR, TREE}
    public enum GroupingType {SUM, MAX, MIN, CONCAT, AGGR, EQUAL, LAST, NAGGR}

    public ScriptingLogicsModule(String filename, BaseLogicsModule baseModule, BusinessLogics BL) {
        this(baseModule, BL);
        this.filename = filename;
    }

    public ScriptingLogicsModule(InputStream stream, String path, BaseLogicsModule baseModule, BusinessLogics BL) throws IOException {
        this(stream, path, "utf-8", baseModule, BL);
    }

    public ScriptingLogicsModule(InputStream stream, String path, String charsetName, BaseLogicsModule baseModule, BusinessLogics BL) throws IOException {
        this(baseModule, BL);
        this.code = IOUtils.readStreamToString(stream, charsetName);
        this.path = path;
        errLog.setModuleId(getIdentifier());
    }

    public ScriptingLogicsModule(BaseLogicsModule baseModule, BusinessLogics BL, String code) {
        this(baseModule, BL);
        this.code = code;
    }

    private ScriptingLogicsModule(BaseLogicsModule baseModule, BusinessLogics BL) {
        setBaseLogicsModule(baseModule);
        this.BL = BL;
        errLog = new ScriptingErrorLog("");
        parser = new ScriptParser(errLog);
        checks = new ScriptingLogicsModuleChecks(this);
    }

    @Override
    public void initModuleDependencies() throws RecognitionException {
        parseStep(ScriptParser.State.PRE);
    }

    @Override
    public void initMetaAndClasses() throws RecognitionException {
        parseStep(ScriptParser.State.META_CLASS_TABLE);
    }

    @Override
    public void initTables() throws RecognitionException {
        addScriptedTables();
    }

    @Override
    public void initMainLogic() throws RecognitionException {
        warningList.clear();
        
        parseStep(ScriptParser.State.MAIN);
        
        if (!parser.isInsideMetacode()) {
            showWarnings();
        }
    }

    @Override
    public void initIndexes() throws RecognitionException {
        for (TemporaryIndexInfo info : tempIndicies) {
            addIndex(info.keyNames, info.params);
        }
        tempIndicies.clear();
        
        for (LP property : indexedProperties) {
            addIndex(property);
        }
        indexedProperties.clear();
    }

    public void initScriptingModule(String name, String namespace, List<String> requiredModules, List<String> namespacePriority) {
        setModuleName(name);
        setNamespace(namespace == null ? name : namespace);
        setDefaultNamespace(namespace == null);
        if (requiredModules.isEmpty() && !getName().equals("System")) {
            requiredModules.add("System");
        }
        setRequiredNames(new LinkedHashSet<>(requiredModules));
        setNamespacePriority(namespacePriority);
    }

    @Override
    public String getErrorsDescription() {
        return errLog.toString();
    }

    private void setModuleName(String moduleName) {
        setName(moduleName);
        errLog.setModuleId(getIdentifier());
    }
    
    private String getIdentifier() {
        String id = getName();
        if (id == null) {
            id = path;
        }
        return (id == null ? "" : id);
    }

    private CharStream createStream() throws IOException {
        if (code != null) {
            return new ANTLRStringStream(code);
        } else {
            return new ANTLRFileStream(filename, "UTF-8");
        }
    }

    @Override
    @IdentityLazy
    public int getModuleComplexity() {
        try {
            return createStream().size();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public ScriptingErrorLog getErrLog() {
        return errLog;
    }

    public ScriptParser getParser() {
        return parser;
    }

    public String getPath() {
        return path;
    }

    public String getCode() {
        return code;
    }

    public ScriptingLogicsModuleChecks getChecks() {
        return checks;
    }

    public String transformStringLiteral(String s) throws ScriptingErrorLog.SemanticErrorException {
        try {
            return ScriptedStringUtils.transformStringLiteral(s);
        } catch (ScriptedStringUtils.TransformationError e) {
            errLog.emitSimpleError(parser, e.getMessage());
        }
        return null;
    }

    public LocalizedString transformLocalizedStringLiteral(String s) throws ScriptingErrorLog.SemanticErrorException {
        try {
            return ScriptedStringUtils.transformLocalizedStringLiteral(s);
        } catch (ScriptedStringUtils.TransformationError | LocalizedString.FormatError e) {
            errLog.emitSimpleError(parser, e.getMessage());
        }
        return null;
    }

    public ImOrderSet<ObjectEntity> getMappingObjectsArray(FormEntity form, ImOrderSet<String> mapping) throws ScriptingErrorLog.SemanticErrorException {
        MOrderExclSet<ObjectEntity> mObjects = SetFact.mOrderExclSet(mapping.size()); // throwing exception
        for (int i = 0; i < mapping.size(); i++) {
            mObjects.exclAdd(getNFObjectEntityByName(form, mapping.get(i)));
        }
        return mObjects.immutableOrder();
    }

    public List<ResolveClassSet> getMappingClassesArray(FormEntity form, List<String> mapping) throws ScriptingErrorLog.SemanticErrorException {
        List<ResolveClassSet> classes = new ArrayList<>();
        for (String paramName : mapping) {
            ObjectEntity obj = getNFObjectEntityByName(form, paramName);
            classes.add(obj.getResolveClassSet());
        }
        return classes;
    }

    public ObjectEntity getObjectEntityByName(FormEntity form, String name) throws ScriptingErrorLog.SemanticErrorException {
        ObjectEntity obj = form.getObject(name);
        if (obj == null) {
            getErrLog().emitObjectNotFoundError(parser, name);
        }
        return obj;
    }

    public ObjectEntity getNFObjectEntityByName(FormEntity form, String name) throws ScriptingErrorLog.SemanticErrorException {
        ObjectEntity obj = form.getNFObject(name, getVersion());
        if (obj == null) {
            getErrLog().emitObjectNotFoundError(parser, name);
        }
        return obj;
    }

    public MappedActionOrProperty getPropertyWithMapping(FormEntity form, AbstractFormActionOrPropertyUsage pDrawUsage, Result<Pair<ActionOrProperty, String>> inherited) throws ScriptingErrorLog.SemanticErrorException {
        assert !(pDrawUsage instanceof FormPredefinedUsage);
        LAP<?, ?> property;
        ImOrderSet<String> mapping;
        if(pDrawUsage instanceof FormActionOrPropertyUsage) {
            List<String> usageMapping = ((FormActionOrPropertyUsage<?>) pDrawUsage).mapping;
            LAP usageProperty = findLPByActionOrPropertyUsage(((FormActionOrPropertyUsage) pDrawUsage).usage, form, usageMapping);
            
            ImList<String> uMapping = ListFact.fromJavaList(usageMapping);
            mapping = uMapping.toOrderSet();
            if(mapping.size() == usageMapping.size())
                property = usageProperty;
            else {
                final ImOrderSet<String> fMapping = mapping;
                ImList<Integer> indexMapping = uMapping.mapListValues(new GetValue<Integer, String>() {
                    public Integer getMapValue(String value) {
                        return fMapping.indexOf(value) + 1;
                    }
                });
                if(usageProperty instanceof LP)
                    property = addJProp((LP)usageProperty, indexMapping.toArray(new Integer[uMapping.size()]));
                else
                    property = addJoinAProp((LA)usageProperty, indexMapping.toArray(new Integer[uMapping.size()]));
                
                if(inherited != null) {
                    inherited.set(new Pair<>(usageProperty.property, usageProperty.property.isNamed() ? PropertyDrawEntity.createSID(usageProperty.property.getName(), usageMapping) : null));                    
                }
            }
        } else {
            property = ((FormLAPUsage)pDrawUsage).lp;
            mapping = ((FormLAPUsage<?>) pDrawUsage).mapping;
        }

//        if (property.property.interfaces.size() != mapping.size()) {
//            getErrLog().emitParamCountError(parser, property, mapping.size());
//        }
        return new MappedActionOrProperty(property, getMappingObjectsArray(form, mapping));
    }

    public LP<?> findLPByPropertyUsage(NamedPropertyUsage pUsage, FormEntity form, List<String> mapping, boolean nullIfNotFound) throws ScriptingErrorLog.SemanticErrorException {
        if (pUsage.classNames != null)
            return findLPByPropertyUsage(nullIfNotFound, pUsage);
        List<ResolveClassSet> classes = getMappingClassesArray(form, mapping);
        return findLCPByNameAndClasses(pUsage.name, pUsage.getSourceName(), classes, nullIfNotFound);
    }
    public LA<?> findLAByPropertyUsage(NamedPropertyUsage pUsage, FormEntity form, List<String> mapping) throws ScriptingErrorLog.SemanticErrorException {
        if (pUsage.classNames != null)
            return findLAByPropertyUsage(pUsage);
        List<ResolveClassSet> classes = getMappingClassesArray(form, mapping);
        return findLAByNameAndClasses(pUsage.name, pUsage.getSourceName(), classes);
    }

    public LAP<?, ?> findLPByActionOrPropertyUsage(ActionOrPropertyUsage orUsage) throws ScriptingErrorLog.SemanticErrorException {
        NamedPropertyUsage pUsage = orUsage.property;
        if(orUsage instanceof PropertyUsage) {
            return findLPByPropertyUsage(pUsage);
        }
        if(orUsage instanceof ActionUsage) {
            return findLAByPropertyUsage(pUsage);
        }
        assert orUsage instanceof PropertyElseActionUsage;
        LAP<?, ?> result = findLPByPropertyUsage(true, pUsage);
        if(result == null)
            result = findLAByPropertyUsage(pUsage);
        return result;
    }
    public LAP<?, ?> findLPByActionOrPropertyUsage(ActionOrPropertyUsage orUsage, FormEntity form, List<String> mapping) throws ScriptingErrorLog.SemanticErrorException {
        NamedPropertyUsage pUsage = orUsage.property;
        if(orUsage instanceof PropertyUsage) {
            return findLPByPropertyUsage(pUsage, form, mapping, false);
        }
        if(orUsage instanceof ActionUsage) {
            return findLAByPropertyUsage(pUsage, form, mapping);
        }
        assert orUsage instanceof PropertyElseActionUsage;
        LAP<?, ?> result = findLPByPropertyUsage(pUsage, form, mapping, true);
        if(result == null)
            result = findLAByPropertyUsage(pUsage, form, mapping);
        return result;
    }

    private void convertResolveError(ResolvingError e) throws ScriptingErrorLog.SemanticErrorException {
        try {
            throw e;
        } catch (ResolvingErrors.ResolvingAmbiguousError re) {
            errLog.emitAmbiguousNameError(parser, re.modules, re.name);
        } catch (ResolvingErrors.ResolvingAmbiguousPropertyError re) {
            errLog.emitAmbiguousPropertyNameError(parser, re.foundItems, re.name);
        } catch (ResolvingErrors.ResolvingNamespaceError re) {
            errLog.emitNamespaceNotFoundError(parser, re.namespaceName);
        } catch (ResolvingError re) {
            assert false;
        }
    }

    public ValueClass findClass(String name) throws ScriptingErrorLog.SemanticErrorException {
        ValueClass valueClass = ClassCanonicalNameUtils.getScriptedDataClass(name);
        if (valueClass == null) {
            try {
                valueClass = resolveClass(name);
            } catch (ResolvingError e) {
                convertResolveError(e);
            }
        }
        checks.checkClass(valueClass, name);
        return valueClass;
    }

    public void addScriptedClass(String className, LocalizedString captionStr, boolean isAbstract,
                                 List<String> instNames, List<LocalizedString> instCaptions, List<String> parentNames, boolean isComplex,
                                 DebugInfo.DebugPoint point) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDuplicateClass(className);
        checks.checkStaticClassConstraints(isAbstract, instNames, instCaptions);
        checks.checkClassParents(parentNames);

        LocalizedString caption = (captionStr == null ? LocalizedString.create(className) : captionStr);

        ImList<CustomClass> parents = BaseUtils.immutableCast(findClasses(parentNames));

        List<LocalizedString> captions = new ArrayList<>();
        for (int i = 0; i < instCaptions.size(); i++) {
            captions.add(instCaptions.get(i) == null ? LocalizedString.create(instNames.get(i)) : instCaptions.get(i));
        }

        CustomClass cls;
        if (isAbstract) {
            cls = addAbstractClass(className, caption, parents);
        } else {
            cls = addConcreteClass(className, caption, instNames, captions, parents);
        }
        cls.isComplex = isComplex;

        ClassDebugInfo debugInfo = new ClassDebugInfo(point);
        if (debugger.isEnabled() && point.needToCreateDelegate()) {
            debugger.addDelegate(debugInfo);
            cls.setDebugInfo(debugInfo);
        }
    }

    public void extendClass(String className, List<String> instNames, List<LocalizedString> instCaptions, List<String> parentNames) throws ScriptingErrorLog.SemanticErrorException {
        Version version = getVersion();

        CustomClass cls = (CustomClass) findClass(className);
        boolean isAbstract = cls instanceof AbstractCustomClass;

        List<String> names = instNames;
        List<LocalizedString> captions = instCaptions;
        if (!isAbstract) {
            ((ConcreteCustomClass) cls).addStaticObjects(instNames, instCaptions, version);

            if(!instNames.isEmpty())
                cls.addParentClass(getBaseClass().staticObjectClass, version);
            names = ((ConcreteCustomClass) cls).getNFStaticObjectsNames(version);
            captions = ((ConcreteCustomClass) cls).getNFStaticObjectsCaptions(version);
        }

        checks.checkStaticClassConstraints(isAbstract, names, captions);
        checks.checkClassParents(parentNames);

        for (String parentName : parentNames) {
            CustomClass parentClass = (CustomClass) findClass(parentName);
            if (cls.containsNFParents(parentClass, version)) {
                errLog.emitDuplicateClassParentError(parser, parentName);
            }
            cls.addParentClass(parentClass, version);
        }
    }

    public AbstractGroup findGroup(String name) throws ScriptingErrorLog.SemanticErrorException {
        try {
            AbstractGroup group = resolveGroup(name);
            checks.checkGroup(group, name);
            return group;
        } catch (ResolvingError e) {
            convertResolveError(e);
        }
        return null;
    }

    public LA<?> findAction(String name) throws ScriptingErrorLog.SemanticErrorException {
        PropertyCompoundNameParser parser = new PropertyCompoundNameParser(this, name);
        return findLAByNameAndClasses(parser.propertyCompoundNameWithoutSignature(), name, parser.getSignature());
    }

    public LP<?> findProperty(String name) throws ScriptingErrorLog.SemanticErrorException {
        PropertyCompoundNameParser parser = new PropertyCompoundNameParser(this, name);
        return findLCPByNameAndClasses(parser.propertyCompoundNameWithoutSignature(), name, parser.getSignature());
    }

    public LP<?>[] findProperties(String... names) throws ScriptingErrorLog.SemanticErrorException {
        LP<?>[] result = new LP[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = findProperty(names[i]);
        }
        return result;
    }

    public LP<?> findLCPByNameAndClasses(String name, String sourceName, List<ResolveClassSet> params) throws ScriptingErrorLog.SemanticErrorException {
        return findLCPByNameAndClasses(name, sourceName, params, false);
    }
    public LP<?> findLCPByNameAndClasses(String name, String sourceName, List<ResolveClassSet> params, boolean nullIfNotFound) throws ScriptingErrorLog.SemanticErrorException {
        return findLCPByNameAndClasses(name, sourceName, params, false, false, nullIfNotFound);
    }
    public LP<?> findLCPByNameAndClasses(String name, String sourceName, List<ResolveClassSet> params, boolean onlyAbstract, boolean prioritizeNotEqual) throws ScriptingErrorLog.SemanticErrorException {
        return findLCPByNameAndClasses(name, sourceName, params, onlyAbstract, prioritizeNotEqual, false);
    }
    public LP<?> findLCPByNameAndClasses(String name, String sourceName, List<ResolveClassSet> params, boolean onlyAbstract, boolean prioritizeNotEqual, boolean nullIfNotFound) throws ScriptingErrorLog.SemanticErrorException {
        LP<?> property = null;

        try {
            if (onlyAbstract) {
                property = resolveAbstractProperty(name, params, prioritizeNotEqual);
            } else {
                property = resolveProperty(name, params);
            }
        } catch (ResolvingErrors.ResolvingAmbiguousPropertyError e) {
            if (sourceName != null) {
                e.name = sourceName;
            }
            convertResolveError(e);
        } catch (ResolvingError e) {
            convertResolveError(e);
        }

        if(!nullIfNotFound)
            checks.checkProperty(property, sourceName == null ? name : sourceName, params);
        return property;
    }
    private LA<?> findLAByNameAndClasses(String name, String sourceName, List<ResolveClassSet> params) throws ScriptingErrorLog.SemanticErrorException {
        return findLAByNameAndClasses(name, sourceName, params, false, false);
    }
    private LA<?> findLAByNameAndClasses(String name, String sourceName, List<ResolveClassSet> params, boolean onlyAbstract, boolean prioritizeNotEqual) throws ScriptingErrorLog.SemanticErrorException {
        LA<?> property = null;

        try {
            if (onlyAbstract) {
                property = resolveAbstractAction(name, params, prioritizeNotEqual);
            } else {
                property = resolveAction(name, params);
            }
        } catch (ResolvingErrors.ResolvingAmbiguousPropertyError e) {
            if (sourceName != null) {
                e.name = sourceName;
            }
            convertResolveError(e);
        } catch (ResolvingError e) {
            convertResolveError(e);
        }

        checks.checkAction(property, sourceName == null ? name : sourceName, params);
        return property;
    }

    public LP<?> findLPByPropertyUsage(NamedPropertyUsage pUsage) throws ScriptingErrorLog.SemanticErrorException {
        return findLPByPropertyUsage(pUsage, false);
    }
    public LP<?> findLPByPropertyUsage(boolean nullIfNotFound, NamedPropertyUsage pUsage) throws ScriptingErrorLog.SemanticErrorException {
        return findLPByPropertyUsage(pUsage, false, nullIfNotFound);
    }

    public LA<?> findLAByPropertyUsage(NamedPropertyUsage pUsage) throws ScriptingErrorLog.SemanticErrorException {
        return findLAByPropertyUsage(pUsage, false);
    }

    public LP<?> findLPByPropertyUsage(NamedPropertyUsage pUsage, boolean isAbstract) throws ScriptingErrorLog.SemanticErrorException {
        return findLPByPropertyUsage(pUsage, isAbstract, false);
    }
    public LP<?> findLPByPropertyUsage(NamedPropertyUsage pUsage, boolean isAbstract, boolean nullIfNotFound) throws ScriptingErrorLog.SemanticErrorException {
        return findLCPByNameAndClasses(pUsage.name, pUsage.getSourceName(), getParamClasses(pUsage), isAbstract, false, nullIfNotFound);
    }
    public LA<?> findLAByPropertyUsage(NamedPropertyUsage pUsage, boolean isAbstract) throws ScriptingErrorLog.SemanticErrorException {
        return findLAByNameAndClasses(pUsage.name, pUsage.getSourceName(), getParamClasses(pUsage), isAbstract, false);
    }

    public LA<?> findLANoParamsByPropertyUsage(NamedPropertyUsage pUsage) throws ScriptingErrorLog.SemanticErrorException {
        if (pUsage.classNames == null) {
            pUsage.classNames = Collections.emptyList();
        }
        LA<?> LA = findLAByPropertyUsage(pUsage);
        ValueClass[] paramClasses = LA.getInterfaceClasses(ClassType.signaturePolicy);
        if (paramClasses.length != 0) {
            errLog.emitPropertyWithParamsExpectedError(getParser(), pUsage.name, "[]");
        }
        return LA;
    }

    public LP<?> findLPNoParamsByPropertyUsage(NamedPropertyUsage pUsage) throws ScriptingErrorLog.SemanticErrorException {
        return findLPParamByPropertyUsage(pUsage, ListFact.<ValueClass>EMPTY());
    }

    public AbstractWindow findWindow(String name) throws ScriptingErrorLog.SemanticErrorException {
        try {
            AbstractWindow window = resolveWindow(name);
            checks.checkWindow(window, name);
            return window;
        } catch (ResolvingError e) {
            convertResolveError(e);
        }
        return null;
    }

    public FormEntity findForm(String name) throws ScriptingErrorLog.SemanticErrorException {
        return findForm(name, false);
    }
    public FormEntity findForm(String name, boolean nullIfNotFound) throws ScriptingErrorLog.SemanticErrorException {
        try {
            FormEntity form = resolveForm(name);
            if(form == null && nullIfNotFound)
                return null;
            checks.checkForm(form, name);
            return form;
        } catch (ResolvingError e) {
            convertResolveError(e);
        }
        return null;
    }

    private List<FormEntity> findForms(List<String> names) throws ScriptingErrorLog.SemanticErrorException {
        List<FormEntity> forms = new ArrayList<>();
        for (String name : names) {
            forms.add(findForm(name));
        }
        return forms;
    }

    public Event createScriptedEvent(BaseEvent base, List<String> formIds, List<NamedPropertyUsage> afterIds) throws ScriptingErrorLog.SemanticErrorException {
        return new Event(base, formIds != null ? new SessionEnvEvent(SetFact.fromJavaSet(new HashSet<>(findForms(formIds)))) : SessionEnvEvent.ALWAYS, afterIds == null? null : SetFact.fromJavaSet(findPropsByPropertyUsages(afterIds)));
    }

    public MetaCodeFragment findMetaCodeFragment(String name, int paramCnt) throws ScriptingErrorLog.SemanticErrorException {
        try {
            MetaCodeFragment code = resolveMetaCodeFragment(name, paramCnt);
            checks.checkMetaCodeFragment(code, name);
            return code;
        } catch (ResolvingError e) {
            convertResolveError(e);
        }
        return null;
    }

    public NavigatorElement findNavigatorElement(String name) throws ScriptingErrorLog.SemanticErrorException {
        try {
            NavigatorElement element = resolveNavigatorElement(name);
            checks.checkNavigatorElement(element, name);
            return element;
        } catch (ResolvingError e) {
            convertResolveError(e);
        }
        return null;
    }

    public ImplementTable findTable(String name) throws ScriptingErrorLog.SemanticErrorException {
        try {
            ImplementTable table = resolveTable(name);
            checks.checkTable(table, name);
            return table;
        } catch (ResolvingError e) {
            convertResolveError(e);
        }
        return null;
    }

    public void addScriptedGroup(String groupName, LocalizedString captionStr, String integrationSID, String parentName) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDuplicateGroup(groupName);
        LocalizedString caption = (captionStr == null ? LocalizedString.create(groupName) : captionStr);
        AbstractGroup parentGroup = (parentName == null ? null : findGroup(parentName));
        AbstractGroup group = addAbstractGroup(groupName, caption, parentGroup);
        group.setIntegrationSID(integrationSID);
    }

    public ScriptingFormEntity createScriptedForm(String formName, LocalizedString caption, DebugInfo.DebugPoint point, String icon,
                                                  ModalityType modalityType, int autoRefresh) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDuplicateForm(formName);
        caption = (caption == null ? LocalizedString.create(formName) : caption);

        String canonicalName = elementCanonicalName(formName);

        ScriptingFormEntity form = new ScriptingFormEntity(this, new FormEntity(canonicalName, point, caption, icon, getVersion()));
        form.setModalityType(modalityType);
        form.setAutoRefresh(autoRefresh);

        return form;
    }

    public ScriptingFormView getFormDesign(String formName, LocalizedString caption, boolean custom) throws ScriptingErrorLog.SemanticErrorException {
        Version version = getVersion();

        FormEntity form = findForm(formName);
        FormView view;
        if (custom) {
            view = new FormView(form, version);
            form.setRichDesign(view, version);
        } else {
            view = form.getNFRichDesign(version);
        }

        if (view != null && caption != null) {
            view.setCaption(caption);
        }

        return new ScriptingFormView(view, this);
    }

    public void addScriptedForm(ScriptingFormEntity form, DebugInfo.DebugPoint point) {
        FormEntity formEntity = addFormEntity(form.getForm());
        formEntity.setDebugPoint(point);
    }

    public void finalizeScriptedForm(ScriptingFormEntity form) {
        form.getForm().finalizeInit(getVersion());
    }

    public ScriptingFormEntity getFormForExtending(String name) throws ScriptingErrorLog.SemanticErrorException {
        FormEntity form = findForm(name);
        return new ScriptingFormEntity(this, form);
    }

    public LP addScriptedDProp(String returnClass, List<String> paramClasses, boolean sessionProp, boolean innerProp, boolean isLocalScope, LocalNestedType nestedType) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkNoInline(innerProp);

        ValueClass value = findClass(returnClass);
        ValueClass[] params = new ValueClass[paramClasses.size()];
        for (int i = 0; i < paramClasses.size(); i++) {
            params[i] = findClass(paramClasses.get(i));
        }

        if (sessionProp) {
            return addSDProp(LocalizedString.NONAME, isLocalScope, value, nestedType, params);
        } else {
            assert nestedType == null;
            return addDProp(LocalizedString.NONAME, value, params);
        }
    }

    public LP<?> addScriptedAbstractProp(CaseUnionProperty.Type type, String returnClass, List<String> paramClasses, boolean isExclusive, boolean isChecked, boolean isLast, boolean innerPD) throws ScriptingErrorLog.SemanticErrorException {
        ValueClass value = findClass(returnClass);
        ValueClass[] params = new ValueClass[paramClasses.size()];
        for (int i = 0; i < paramClasses.size(); i++) {
            params[i] = findClass(paramClasses.get(i));
        }
        return addAUProp(null, false, isExclusive, isChecked, isLast, type, LocalizedString.NONAME, value, params);
    }

    public LA addScriptedAbstractAction(ListCaseAction.AbstractType type, List<String> paramClasses, boolean isExclusive, boolean isChecked, boolean isLast) throws ScriptingErrorLog.SemanticErrorException {
        ValueClass[] params = new ValueClass[paramClasses.size()];
        for (int i = 0; i < paramClasses.size(); i++) {
            params[i] = findClass(paramClasses.get(i));
        }
        LA<?> result;
        if (type == ListCaseAction.AbstractType.LIST) {
            result = addAbstractListAProp(isChecked, isLast, params);
        } else {
            result = addAbstractCaseAProp(type, isExclusive, isChecked, isLast, params);
        }
        return result;
    }

    // todo [dale]: выделить общий код    
    public void addImplementationToAbstractAction(NamedPropertyUsage abstractPropUsage, List<TypedParameter> context, LAWithParams implement, LPWithParams when) throws ScriptingErrorLog.SemanticErrorException {
        LA abstractLP = findLAByPropertyUsage(abstractPropUsage, context, true);
        checks.checkParamCount(abstractLP, context.size());
        checks.checkImplementIsNotMain(abstractLP, implement.getLP());

        List<LAPWithParams> allProps = new ArrayList<>();
        allProps.add(implement);
        if (when != null) {
            allProps.add(when);
        }
        List<Object> params = getParamsPlainList(allProps);

        List<ResolveClassSet> signature = getClassesFromTypedParams(context);
        addImplementationToAbstractAction(abstractPropUsage.name, abstractLP, signature, when != null, params);
    }

    public void addImplementationToAbstractProp(NamedPropertyUsage abstractPropUsage, List<TypedParameter> context, LPWithParams implement, LPWithParams when) throws ScriptingErrorLog.SemanticErrorException {
        LP abstractLP = findLPByPropertyUsage(abstractPropUsage, context, true);
        checks.checkParamCount(abstractLP, context.size());
        checks.checkImplementIsNotMain(abstractLP, implement.getLP());

        List<LAPWithParams> allProps = new ArrayList<>();
        allProps.add(implement);
        if (when != null) {
            allProps.add(when);
        }
        List<Object> params = getParamsPlainList(allProps);

        List<ResolveClassSet> signature = getClassesFromTypedParams(context);
        addImplementationToAbstractProp(abstractPropUsage.name, abstractLP, signature, when != null, params);
    }

    private void addImplementationToAbstractProp(String propName, LP abstractProp, List<ResolveClassSet> signature, boolean isCase, List<Object> params) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkAbstractProperty(abstractProp, propName);
        CaseUnionProperty.Type type = ((CaseUnionProperty)abstractProp.property).getAbstractType();
        checks.checkAbstractTypes(type == CaseUnionProperty.Type.CASE, isCase);

        try {
            abstractProp.addOperand(isCase, signature, getVersion(), params.toArray());
        } catch (ScriptParsingException e) {
            errLog.emitSimpleError(parser, e.getMessage());
        }
    }

    private void addImplementationToAbstractAction(String actionName, LA abstractAction, List<ResolveClassSet> signature, boolean isCase, List<Object> params) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkAbstractAction(abstractAction, actionName);
        ListCaseAction.AbstractType type = ((ListCaseAction)abstractAction.property).getAbstractType();
        checks.checkAbstractTypes(type == ListCaseAction.AbstractType.CASE, isCase);

        try {
            abstractAction.addOperand(isCase, signature, getVersion(), params.toArray());
        } catch (ScriptParsingException e) {
            errLog.emitSimpleError(parser, e.getMessage());
        }
    }

    private int indexOf(List<TypedParameter> params, String paramName) {
        if (params == null) return -1;
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).paramName.equals(paramName)) {
                return i;
            }
        }
        return -1;
    }

    public List<ResolveClassSet> createClassSetsFromClassNames(List<String> classNames) throws ScriptingErrorLog.SemanticErrorException {
        List<ResolveClassSet> params = new ArrayList<>();
        for (String className : classNames) {
            ValueClass cls = findClass(className);
            params.add(cls.getResolveSet());
        }
        return params;
    }

    public void getParamIndices(List<TypedParameter> typedParams, List<TypedParameter> context, boolean dynamic, boolean insideRecursion) throws ScriptingErrorLog.SemanticErrorException {
        for(TypedParameter typedParam : typedParams) {
            getParamIndex(typedParam, context, dynamic, insideRecursion);
        }
    }

    public int getParamIndex(TypedParameter param, List<TypedParameter> context, boolean dynamic, boolean insideRecursion) throws ScriptingErrorLog.SemanticErrorException {
        String paramName = param.paramName;
        int index = indexOf(context, paramName);

        if (index < 0 && paramName.startsWith("$")) {
            if (param.cls != null) {
                errLog.emitParamClassNonDeclarationError(parser, paramName);
            } else if (!insideRecursion) {
                errLog.emitRecursiveParamsOutideRecursionError(parser, paramName);
            } else if (indexOf(context, paramName.substring(1)) < 0) {
                errLog.emitParamNotFoundError(parser, paramName.substring(1));
            }
        }

        if (index >= 0 && param.cls != null && context != null) {
            if (context.get(index).cls != null) {
                errLog.emitParamClassRedefinitionError(parser, paramName);
            } else {
                errLog.emitParamClassNonDeclarationError(parser, paramName);
            }
        }
        if (index < 0 && context != null && (dynamic || paramName.startsWith("$") && insideRecursion)) {
            if (paramName.startsWith("$") && insideRecursion) {
                param.cls = context.get(indexOf(context, paramName.substring(1))).cls;
            }
            index = context.size();
            context.add(param);
        }
        if (index < 0) {
            errLog.emitParamNotFoundError(parser, paramName);
        }
        return index;
    }

    public static abstract class LAPWithParams {
        private final LAP<?, ?> property; // nullable
        public final List<Integer> usedParams; // immutable order set zero-based

        public LAPWithParams(LAP<?, ?> property, List<Integer> usedParams) {
            this.property = property;
            this.usedParams = Collections.unmodifiableList(new ArrayList<>(usedParams));
        }

        @Override
        public String toString() {
            return String.format("[%s, %s]", property, usedParams);
        }

        public LAP<?, ?> getLP() {
            return property;
        }
    }

    public static class LAWithParams extends LAPWithParams {
        public LAWithParams(LA<?> property, List<Integer> usedParams) {
            super(property, usedParams);
        }

        public LA<?> getLP() {
            return (LA<?>) super.getLP();
        }
    }

    public static class LPWithParams extends LAPWithParams {
        public LPWithParams(LP<?> property, List<Integer> usedParams) {
            super(property, usedParams);
        }

        public LPWithParams(LP<?> property, LAPWithParams mapLP) {
            this(property, mapLP.usedParams);
        }

        public LPWithParams(LP<?> property, Integer usedParam) {
            this(property, Collections.singletonList(usedParam));
        }

        public LPWithParams(Integer usedParam) {
            this(null, usedParam);
        }

        public LPWithParams(LP<?> property) {
            this(property, Collections.<Integer>emptyList());
        }

        public LP<?> getLP() {
            return (LP<?>) super.getLP();
        }
    }

    private boolean isTrivialParamList(List<Object> paramList) {
        int index = 1;
        for (Object param : paramList) {
            if (!(param instanceof Integer) || ((Integer)param) != index) return false;
            ++index;
        }
        return true;
    }

    public static class LPContextIndependent {
        public final LP property;
        public final List<ResolveClassSet> signature;
        public final List<Integer> usedContext;

        public LPContextIndependent(LP property, List<ResolveClassSet> signature, List<Integer> usedContext) {
            this.property = property;
            this.signature = signature;
            this.usedContext = usedContext;
        }
    }

    private List<String> getParamNamesFromTypedParams(List<TypedParameter> params) {
        List<String> paramNames = new ArrayList<>();
        for (TypedParameter param : params) {
            paramNames.add(param.paramName);
        }
        return paramNames;
    }

    public List<ResolveClassSet> getClassesFromTypedParams(List<TypedParameter> params) {
        List<ResolveClassSet> paramClasses = new ArrayList<>();
        for (TypedParameter param : params) {
            if (param.cls == null) {
                paramClasses.add(null);
            } else {
                paramClasses.add(param.cls.getResolveSet());
            }
        }
        return paramClasses;
    }

    public ImList<ValueClass> getValueClassesFromTypedParams(List<TypedParameter> params) {
        MList<ValueClass> mResult = ListFact.mList(params.size());
        for (TypedParameter param : params) {
            mResult.add(param.cls);
        }
        return mResult.immutableList();
    }

    public LAP makeActionOrPropertyPublic(FormEntity form, String alias, FormLAPUsage<?> lpUsage) {
        String name = "_FORM_" + form.getCanonicalName().replace('.', '_') + "_" + alias;
        LAP property = lpUsage.lp;
        if (property instanceof LP) {
            property = checkPropertyIsNew((LP)property);
        }
        makeActionOrPropertyPublic(property, name, lpUsage.signature);
        return property;
    }

    public void addSettingsToActionOrProperty(LAP property, String name, LocalizedString caption, List<TypedParameter> params, List<ResolveClassSet> signature,
                                              ActionOrPropertySettings ps) throws ScriptingErrorLog.SemanticErrorException {
        property.property.annotation = ps.annotation;

        List<String> paramNames = getParamNamesFromTypedParams(params);
        checks.checkDistinctParameters(paramNames);
        checks.checkNamedParams(property, paramNames);
        checks.checkParamsClasses(params, signature);

        String groupName = ps.groupName;
        AbstractGroup group = (groupName == null ? null : findGroup(groupName));
        property.property.caption = (caption == null ? LocalizedString.create(name) : caption);
        addPropertyToGroup(property.property, group);
    }

    public void addSettingsToAction(LA property, String name, LocalizedString caption, List<TypedParameter> params, List<ResolveClassSet> signature, ActionSettings ps) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDuplicateAction(name, signature);

        addSettingsToActionOrProperty(property, name, caption, params, signature, ps);

        makeActionPublic(property, name, signature);
    }

    public LP addSettingsToProperty(LP<?> baseProperty, String name, LocalizedString caption, List<TypedParameter> params, List<ResolveClassSet> signature,
                                    PropertySettings ps) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDuplicateProperty(name, signature);

        LP<?> property = baseProperty;
        property = checkPropertyIsNew(property);

        addSettingsToActionOrProperty(property, name, caption, params, signature, ps);

        makePropertyPublic(property, name, signature);

        ImplementTable targetTable = null;
        String tableName = ps.table;
        if (tableName != null) {
            targetTable = findTable(tableName);
            if (!targetTable.equalClasses(property.property.getOrderTableInterfaceClasses(ClassType.storedPolicy))) {
                // todo : проверка неправильная - должна быть на ClassWhere
                //errLog.emitWrongClassesForTable(parser, name, tableName);
            }
        }
        if (property.property instanceof StoredDataProperty) {
            property.property.markStored(baseLM.tableFactory, targetTable);
        } else if (ps.isPersistent && (property.property instanceof AggregateProperty)) {
            addPersistent(property, targetTable);
        }

        if(ps.isComplex)
            property.property.complex = true;

        if(ps.noHint)
            property.property.noHint = true;

        BooleanDebug notNull = ps.notNull;
        if (notNull != null) {
            BooleanDebug notNullResolve = ps.notNullResolve;
            setNotNull(property, notNull.debugPoint, ps.notNullEvent,
                    notNullResolve != null ? ListFact.singleton(new PropertyFollowsDebug(false, true, notNullResolve.debugPoint)) :
                                             ListFact.<PropertyFollowsDebug>EMPTY());

            if(notNullResolve != null)
                property.property.setAggr(true);
        }

        if (Settings.get().isCheckAlwaysNull()) {
            checks.checkPropertyValue(property, alwaysNullProperties);
            if (!alwaysNullProperties.isEmpty()) {
                showAlwaysNullErrors();
            }
        }

//            if (Settings.get().isCheckClassWhere()) {
//                checks.checkClassWhere((LP) property, name);
//            }
        makeLoggable(baseProperty, ps.isLoggable);
        return property;
    }

    /** Проверяет нужно ли обернуть свойство в join.
     *  Свойства нужно обернуть, если это не только что созданное свойство, а свойство, созданное ранее с уже установленными 
     *  параметрами (например, с установленным каноническим именем или debug point'ом). Такая ситуация возникает, если 
     *  была произведена какая-то оптимизация: кэширование (например, с помощью IdentityLazy) либо логика с lastOptimizedJPropSID.
     *  todo [dale]: Сейчас проверяются только основные частные случаи.
     */
    private boolean propertyNeedsToBeWrapped(LP<?> property) {
        // Если объявление имеет вид f(x, y) = g(x, y), то нужно дополнительно обернуть свойство g в join
        return property.property.getSID().equals(lastOptimizedJPropSID)
                || property.property instanceof ValueProperty
                || property.property instanceof IsClassProperty;
    }

    private LP<?> wrapProperty(LP<?> property) {
        return addJProp(property, BaseUtils.consecutiveList(property.property.interfaces.size(), 1).toArray());
    }

    public LP<?> checkPropertyIsNew(LP<?> property) {
        if (propertyNeedsToBeWrapped(property)) {
            property = wrapProperty(property);
        }
        return property;
    }

    public FormLPUsage checkPropertyIsNew(FormLPUsage property) {
        if(propertyNeedsToBeWrapped(property.lp))
            property = new FormLPUsage(wrapProperty(property.lp), property.mapping, property.signature);
        return property;
    }

    public LPWithParams checkPropertyIsNew(LPWithParams property) {
        if(propertyNeedsToBeWrapped(property.getLP()))
            property = new LPWithParams(wrapProperty(property.getLP()), property);
        return property;
    }

    public LPWithParams checkAndSetExplicitClasses(LPWithParams property, List<ResolveClassSet> signature) {
        if(!BaseUtils.nullHashEquals(property.getLP().getExplicitClasses(), signature)) { // optimization
            property = checkPropertyIsNew(property); // // we need new property to guarantee that explicit classes will be set for correct property
            property.getLP().setExplicitClasses(signature);
        }
        return property;
    }

    private void showAlwaysNullErrors() throws ScriptingErrorLog.SemanticErrorException {
        StringBuilder errorMessage = new StringBuilder();
        for (Property property : alwaysNullProperties.keySet()) {
            if (errorMessage.length() > 0) {
                errorMessage.append("\n");
            }
            String location = alwaysNullProperties.get(property);
            errorMessage.append("[error]:\t" + location + " property '" + property.getName() + "' is always NULL");
        }
        alwaysNullProperties.clear();
        ScriptingErrorLog.emitSemanticError(errorMessage.toString(), new ScriptingErrorLog.SemanticErrorException(parser.getCurrentParser().input));
    }

    public void addToContextMenuFor(LA onContextAction, LocalizedString contextMenuCaption, ActionOrPropertyUsage mainPropertyUsage) throws ScriptingErrorLog.SemanticErrorException {
        assert mainPropertyUsage != null;

        LAP<?, ?> mainProperty = findLPByActionOrPropertyUsage(mainPropertyUsage);
        onContextAction.addToContextMenuFor(mainProperty, contextMenuCaption);

        onContextAction.setAsEditActionFor(onContextAction.property.getSID(), mainProperty);
    }

    public void setAsEditActionFor(LA onEditAction, String editActionSID, ActionOrPropertyUsage mainPropertyUsage) throws ScriptingErrorLog.SemanticErrorException {
        assert mainPropertyUsage != null;

        LAP<?, ?> mainProperty = findLPByActionOrPropertyUsage(mainPropertyUsage);
        onEditAction.setAsEditActionFor(editActionSID, mainProperty);
    }

    public void setForceViewType(LAP property, ClassViewType viewType) {
        property.setForceViewType(viewType);
    }

    public void setFixedCharWidth(LAP property, Integer fixedCharWidth) {
        if (fixedCharWidth != null && fixedCharWidth > 0)
            property.setFixedCharWidth(fixedCharWidth);
    }

    public void setCharWidth(LAP property, Integer charWidth) {
        if (charWidth != null)
            property.setCharWidth(charWidth);
    }

    public void setImage(LAP property, String path) {
        property.setImage(path);
    }

    public void setDefaultCompare(LAP property, String defaultCompare) {
        property.setDefaultCompare(defaultCompare);
    }

    public void setChangeKey(LAP property, String code, Boolean showEditKey) {
        property.setChangeKey(KeyStroke.getKeyStroke(code));
        if (showEditKey != null)
            property.setShowChangeKey(showEditKey);
    }

    public void setAutoset(LP property, boolean autoset) {
        property.setAutoset(autoset);
    }

    public void setAskConfirm(LAP property, boolean askConfirm) {
        property.setAskConfirm(askConfirm);
    }

    public void setRegexp(LAP property, String regexp, String regexpMessage) {
        property.setRegexp(regexp);
        if (regexpMessage != null) {
            property.setRegexpMessage(regexpMessage);
        }
    }

    public void makeLoggable(LP property, boolean isLoggable) throws ScriptingErrorLog.SemanticErrorException {
        if (isLoggable && property != null) {
            property.makeLoggable(this, BL.systemEventsLM);
        }
    }

    public void setEchoSymbols(LAP property) {
        property.setEchoSymbols(true);
    }

    public void setAggr(LAP property) {
        ((Property)property.property).setAggr(true);
    }

    public void setScriptedEditAction(LAP property, String actionType, LAWithParams action) {
        List<Object> params = getParamsPlainList(Collections.singletonList(action));
        ImList<ActionMapImplement<?, PropertyInterface>> actionImplements = readActionImplements(((LAP<PropertyInterface, ?>)property).listInterfaces, params.toArray());
        property.property.setEditAction(actionType, actionImplements.get(0));
    }

    public void setScriptedContextMenuAction(LAP property, LocalizedString contextMenuCaption, LAWithParams action) {
        List<Object> params = getParamsPlainList(Collections.singletonList(action));
        ImList<ActionMapImplement<?, PropertyInterface>> actionImplements = readActionImplements(((LAP<PropertyInterface, ?>)property).listInterfaces, params.toArray());
        ActionMapImplement<?, PropertyInterface> actionImplement = actionImplements.get(0);

        String actionSID = actionImplement.property.getSID();
        property.property.setContextMenuAction(actionSID, FormPropertyOptions.getContextMenuCaption(contextMenuCaption, actionImplement.property));
        property.property.setEditAction(actionSID, actionImplement);
    }

    public void setScriptedKeyPressAction(LAP property, String key, LAWithParams action) {
        List<Object> params = getParamsPlainList(Collections.singletonList(action));
        ImList<ActionMapImplement<?, PropertyInterface>> actionImplements = readActionImplements(((LAP<PropertyInterface, ?>)property).listInterfaces, params.toArray());
        ActionMapImplement<?, PropertyInterface> actionImplement = actionImplements.get(0);

        String actionSID = actionImplement.property.getSID();
        property.property.setKeyAction(KeyStroke.getKeyStroke(key), actionSID);
        property.property.setEditAction(actionSID, actionImplement);
    }

    public void setEventId(LAP property, String id) {
        property.property.drawOptions.setEventID(id);
    }

    public List<ResolveClassSet> getParamClasses(NamedPropertyUsage usage) throws ScriptingErrorLog.SemanticErrorException {
        List<ValueClass> valueClasses = getValueClasses(usage);
        if (valueClasses == null) {
            return null;
        }

        List<ResolveClassSet> classes = new ArrayList<>();
        for (ValueClass valueClass : valueClasses) {
            if (valueClass == null) {
                classes.add(null);
            } else {
                classes.add(valueClass.getResolveSet());
            }
        }
        return classes;
    }

    public List<ValueClass> getValueClasses(NamedPropertyUsage usage) throws ScriptingErrorLog.SemanticErrorException {
        if (usage.classNames == null) {
            return null;
        }

        List<ValueClass> classes = new ArrayList<>();
        for (String className : usage.classNames) {
            if (className.equals(PropertyCanonicalNameUtils.UNKNOWNCLASS)) {
                classes.add(null);
            } else {
                ValueClass cls = findClass(className);
                classes.add(cls);
            }
        }
        return classes;
    }

    private List<ResolveClassSet> getParamClassesByParamProperties(List<LPWithParams> paramProps, List<TypedParameter> params) {
        List<ResolveClassSet> classes = new ArrayList<>();
        for (LPWithParams paramProp : paramProps) {
            if (paramProp.getLP() != null) {
                LP lcp = paramProp.getLP();
                List<ResolveClassSet> usedClasses = getUsedClasses(params, paramProp.usedParams);
                classes.add(lcp.getResolveClassSet(usedClasses));
            } else {
                TypedParameter param = params.get(paramProp.usedParams.get(0));
                if (param.cls == null) {
                    classes.add(null);
                } else {
                    classes.add(param.cls.getResolveSet());
                }
            }
        }
        return classes;
    }

    private LP findLPByPropertyUsage(NamedPropertyUsage mainProp, List<LPWithParams> paramProps, List<TypedParameter> context) throws ScriptingErrorLog.SemanticErrorException {
        if (mainProp.classNames != null)
            return findLPByPropertyUsage(mainProp);
        List<ResolveClassSet> classes = getParamClassesByParamProperties(paramProps, context);
        return findLCPByNameAndClasses(mainProp.name, mainProp.getSourceName(), classes);
    }

    private LA findLAByPropertyUsage(NamedPropertyUsage mainProp, List<LPWithParams> paramProps, List<TypedParameter> context) throws ScriptingErrorLog.SemanticErrorException {
        if (mainProp.classNames != null)
            return findLAByPropertyUsage(mainProp);
        List<ResolveClassSet> classes = getParamClassesByParamProperties(paramProps, context);
        return findLAByNameAndClasses(mainProp.name, mainProp.getSourceName(), classes);
    }

    private LP findLPByPropertyUsage(NamedPropertyUsage mainProp, List<TypedParameter> params) throws ScriptingErrorLog.SemanticErrorException {
        return findLPByPropertyUsage(mainProp, params, false);
    }

    private LP findLPByPropertyUsage(NamedPropertyUsage mainProp, List<TypedParameter> params, boolean onlyAbstract) throws ScriptingErrorLog.SemanticErrorException {
        if (mainProp.classNames != null)
            return findLPByPropertyUsage(mainProp, onlyAbstract);
        return findLCPByNameAndClasses(mainProp.name, mainProp.getSourceName(), getClassesFromTypedParams(params), onlyAbstract, true);
    }

    private LA findLAByPropertyUsage(NamedPropertyUsage mainProp, List<TypedParameter> params) throws ScriptingErrorLog.SemanticErrorException {
        return findLAByPropertyUsage(mainProp, params, false);
    }

    private LA findLAByPropertyUsage(NamedPropertyUsage mainProp, List<TypedParameter> params, boolean onlyAbstract) throws ScriptingErrorLog.SemanticErrorException {
        if (mainProp.classNames != null)
            return findLAByPropertyUsage(mainProp, onlyAbstract);
        return findLAByNameAndClasses(mainProp.name, mainProp.getSourceName(), getClassesFromTypedParams(params), onlyAbstract, true);
    }

    public LPWithParams addScriptedJProp(boolean user, NamedPropertyUsage pUsage, List<LPWithParams> paramProps, List<TypedParameter> params) throws ScriptingErrorLog.SemanticErrorException {
        LP mainProp = findLPByPropertyUsage(pUsage, paramProps, params);
        return addScriptedJProp(user, mainProp, paramProps);
    }

    public LPWithParams addScriptedJProp(LP mainProp, List<LPWithParams> paramProps) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(false, mainProp, paramProps);
    }

    public LPWithParams addScriptedJProp(boolean user, LP mainProp, List<LPWithParams> paramProps, List<Integer> usedContext, boolean ci) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(user, mainProp, getAllGroupProps(usedContext, paramProps, ci));
    }

    public LPWithParams addScriptedJProp(boolean user, LP mainProp, List<LPWithParams> paramProps) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkParamCount(mainProp, paramProps.size());
        List<Object> resultParams = getParamsPlainList(paramProps);
        LP prop;
        if (isTrivialParamList(resultParams)) {
            prop = mainProp;
            lastOptimizedJPropSID = mainProp.property.getSID();
        } else {
            prop = addJProp(user, mainProp, resultParams.toArray());
        }
        return new LPWithParams(prop, mergeAllParams(paramProps));
    }

    private LP getRelationProp(String op) {
        switch (op) {
            case "==":
                return baseLM.equals2;
            case "=":
                return baseLM.equals2;
            case "!=":
                return baseLM.diff2;
            case ">":
                return baseLM.greater2;
            case "<":
                return baseLM.less2;
            case ">=":
                return baseLM.groeq2;
            case "<=":
                return baseLM.lsoeq2;
        }
        assert false;
        return null;
    }

    private LP getArithProp(String op) {
        switch (op) {
            case "+":
                return baseLM.sum;
            case "-":
                return baseLM.subtract;
            case "*":
                return baseLM.multiply;
            case "/":
                return baseLM.divide;
        }
        assert false;
        return null;
    }

    public LPWithParams addScriptedEqualityProp(String op, LPWithParams leftProp, LPWithParams rightProp, List<TypedParameter> context) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkComparisonCompatibility(leftProp, rightProp, context);
        return addScriptedJProp(getRelationProp(op), asList(leftProp, rightProp));
    }

    public LPWithParams addScriptedRelationalProp(String op, LPWithParams leftProp, LPWithParams rightProp, List<TypedParameter> context) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkComparisonCompatibility(leftProp, rightProp, context);
        return addScriptedJProp(getRelationProp(op), asList(leftProp, rightProp));
    }

    public LPWithParams addScriptedOverrideProp(List<LPWithParams> properties, boolean isExclusive) throws ScriptingErrorLog.SemanticErrorException {
        if (isExclusive) {
            return addScriptedUProp(Union.EXCLUSIVE, properties, "EXCLUSIVE");
        } else {
            return addScriptedUProp(Union.OVERRIDE, properties, "OVERRIDE");
        }
    }

    public LPWithParams addScriptedLikeProp(LPWithParams leftProp, LPWithParams rightProp) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(baseLM.like2, asList(leftProp, rightProp));
    }

    public LPWithParams addScriptedIfProp(List<LPWithParams> properties) throws ScriptingErrorLog.SemanticErrorException {
        LPWithParams curLP = properties.get(0);
        if (properties.size() > 1) {
            boolean[] notsArray = new boolean[properties.size() - 1];
            Arrays.fill(notsArray, false);
            curLP = addScriptedJProp(and(notsArray), properties);
        }
        return curLP;
    }

    public LPWithParams addScriptedOrProp(List<LPWithParams> properties) throws ScriptingErrorLog.SemanticErrorException {
        LPWithParams res = properties.get(0);
        if (properties.size() > 1) {
            List<LPWithParams> logicalProperties = convertToLogical(properties);
            res = addScriptedUProp(Union.OVERRIDE, logicalProperties, "OR");
        }
        return res;
    }

    public LPWithParams addScriptedXorProp(List<LPWithParams> properties) throws ScriptingErrorLog.SemanticErrorException {
        LPWithParams res = properties.get(0);
        if (properties.size() > 1) {
            List<LPWithParams> logicalProperties = convertToLogical(properties);
            res = addScriptedUProp(Union.XOR, logicalProperties, "XOR");
        }
        return res;
    }

    public LPWithParams addScriptedAndProp(List<LPWithParams> properties) throws ScriptingErrorLog.SemanticErrorException {
        LPWithParams curLP = properties.get(0);
        if (properties.size() > 1) {
            boolean[] notsArray = new boolean[properties.size() - 1];
            Arrays.fill(notsArray, false);

            LPWithParams firstArgument = properties.get(0);
            if (!isLogical(firstArgument.getLP())) {
                properties.set(0, new LPWithParams(toLogical(firstArgument).getLP(), firstArgument));
            }
            curLP = addScriptedJProp(and(notsArray), properties);
        }
        return curLP;
    }

    private List<LPWithParams> convertToLogical(List<LPWithParams> properties) throws ScriptingErrorLog.SemanticErrorException {
        List<LPWithParams> logicalProperties = new ArrayList<>();
        for (LPWithParams prop : properties) {
            LPWithParams logicalProp = prop;
            if (!isLogical(prop.getLP())) {
                logicalProp = new LPWithParams(toLogical(prop).getLP(), prop);
            }
            logicalProperties.add(logicalProp);
        }
        return logicalProperties;
    }

    private LPWithParams toLogical(LPWithParams property) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(and(false), Arrays.asList(new LPWithParams(baseLM.vtrue), property));
    }

    public LPWithParams addScriptedIfElseUProp(LPWithParams ifProp, LPWithParams thenProp, LPWithParams elseProp) throws ScriptingErrorLog.SemanticErrorException {
        List<LPWithParams> lpParams = new ArrayList<>();
        lpParams.add(addScriptedJProp(and(false), asList(thenProp, ifProp)));
        if (elseProp != null) {
            lpParams.add(addScriptedJProp(and(true), asList(elseProp, ifProp)));
        }
        return addScriptedUProp(Union.EXCLUSIVE, lpParams, "IF");
    }

    public LPWithParams addScriptedCaseUProp(List<LPWithParams> whenProps, List<LPWithParams> thenProps, LPWithParams elseProp, boolean isExclusive) {
        assert whenProps.size() > 0 && whenProps.size() == thenProps.size();

        List<LPWithParams> caseParamProps = new ArrayList<>();
        for (int i = 0; i < whenProps.size(); i++) {
            caseParamProps.add(whenProps.get(i));
            caseParamProps.add(thenProps.get(i));
        }
        if (elseProp != null) {
            caseParamProps.add(elseProp);
        }

        LP caseProp = addCaseUProp(null, false, LocalizedString.NONAME, isExclusive, getParamsPlainList(caseParamProps).toArray());
        return new LPWithParams(caseProp, mergeAllParams(caseParamProps));
    }

    public LPWithParams addScriptedMultiProp(List<LPWithParams> properties, boolean isExclusive) throws ScriptingErrorLog.SemanticErrorException {
        if (isExclusive) {
            return addScriptedUProp(Union.CLASS, properties, "MULTI");
        } else {
            return addScriptedUProp(Union.CLASSOVERRIDE, properties, "MULTI");
        }
    }

    public LA addScriptedCustomAction(String javaClassName, List<String> classes, boolean allowNullValue) throws ScriptingErrorLog.SemanticErrorException {
        try {
            Action instance;
            if (classes == null || classes.isEmpty()) {
                instance = (Action) Class.forName(javaClassName).getConstructor(this.getClass()).newInstance(this);
            } else {
                ValueClass[] classList = new ValueClass[classes.size()];
                for (int i = 0; i < classes.size(); i++) {
                    classList[i] = findClass(classes.get(i));
                }
                instance = (Action) Class.forName(javaClassName).getConstructor(new Class[] {this.getClass(), ValueClass[].class}).newInstance(this, classList);
            }
            if (instance instanceof ExplicitAction && allowNullValue) {
                ((ExplicitAction) instance).allowNullValue = true;
            }
            return baseLM.addAProp(null, instance);
        } catch (ClassNotFoundException e) {
            errLog.emitClassNotFoundError(parser, javaClassName);
        } catch (Exception e) {
            errLog.emitCreatingClassInstanceError(parser, e.getMessage(), javaClassName);
        }
        return null;
    }

    public LA addScriptedCustomAction(String code, boolean allowNullValue) throws ScriptingErrorLog.SemanticErrorException {
        String script = "";
        try {

            script = code.substring(1, code.length() - 1); //remove brackets

            String javaClass = "import lsfusion.server.data.sql.exception.SQLHandledException;\n" +
                    "import lsfusion.server.logics.property.classes.ClassPropertyInterface;\n" +
                    "import lsfusion.server.logics.action.controller.context.ExecutionContext;\n" +
                    "import lsfusion.server.physics.dev.integration.internal.to.InternalAction;\n" +
                    "import lsfusion.server.language.ScriptingLogicsModule;\n" +
                    "\n" +
                    "import java.sql.SQLException;\n" +
                    "\n" +
                    "public class ExecuteAction extends InternalAction {\n" +
                    "\n" +
                    "    public ExecuteAction(ScriptingLogicsModule LM) {\n" +
                    "        super(LM);\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public void executeInternal(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {\n" +
                    "        try {\n" +
                    script +
                    "        } catch (Exception e) {\n" +
                    "            e.printStackTrace();\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

            SimpleCompiler sc = new SimpleCompiler();
            sc.cook(javaClass);
            Class<?> executeClass = sc.getClassLoader().loadClass("ExecuteAction");

            Action instance = (Action) executeClass.getConstructor(ScriptingLogicsModule.class).newInstance(this);
            if (instance instanceof ExplicitAction && allowNullValue) {
                ((ExplicitAction) instance).allowNullValue = true;
            }
            return baseLM.addAProp(null, instance);
        } catch (Exception e) {
            errLog.emitCreatingClassInstanceError(parser, e.getMessage(), script);
        }
        return null;
    }


    public ImList<Type> getTypesByParamProperties(List<LPWithParams> paramProps, List<TypedParameter> params) {
        List<ResolveClassSet> classes = getParamClassesByParamProperties(paramProps, params);
        MList<Type> mTypes = ListFact.mList(classes.size());
        for(int i=0,size=paramProps.size();i<size;i++) {
            Type type = null;

            ResolveClassSet paramClass = classes.get(i);
            if(paramClass != null)
                type = paramClass.getType();
            else {
                LP<?> property = paramProps.get(i).getLP();
                if(property != null) {
                    ValueClass valueClass = property.property.getValueClass(ClassType.valuePolicy);
                    if (valueClass != null)
                        type = valueClass.getType();
                }
            }
            mTypes.add(type);
        }
        return mTypes.immutableList();
    }

    public ImList<ValueClass> getValueClassesByParamProperties(List<LPWithParams> paramProps, List<TypedParameter> params) {
        List<ResolveClassSet> classes = getParamClassesByParamProperties(paramProps, params);
        MList<ValueClass> mValueClasses = ListFact.mList(classes.size());
        for(int i=0,size=paramProps.size();i<size;i++) {
            ValueClass valueClass = null;

            LP<?> property = paramProps.get(i).getLP();
            if(property != null)
                valueClass = property.property.getValueClass(ClassType.valuePolicy);

            if(valueClass == null) {
                ResolveClassSet paramClass = classes.get(i);
                if(paramClass != null)
                    valueClass = paramClass.getCommonClass();
            }
            mValueClasses.add(valueClass);
        }
        return mValueClasses.immutableList();
    }

    public Type getTypeByParamProperty(LPWithParams paramProp, List<TypedParameter> params) {
        return getTypesByParamProperties(Collections.singletonList(paramProp), params).single();
    }

    public ValueClass getValueClassByParamProperty(LPWithParams paramProp, List<TypedParameter> params) {
        return getValueClassesByParamProperties(Collections.singletonList(paramProp), params).single();
    }

    public ImList<Type> getTypesForExternalProp(List<LPWithParams> paramProps, List<TypedParameter> params) {
        return getTypesByParamProperties(paramProps, params);
    }

    public LAWithParams addScriptedExternalJavaAction(List<LPWithParams> params, List<TypedParameter> context, List<NamedPropertyUsage> toPropertyUsageList) {
        throw new UnsupportedOperationException("EXTERNAL JAVA not supported");
    }

    public LAWithParams addScriptedExternalDBAction(LPWithParams connectionString, LPWithParams exec, List<LPWithParams> params, List<TypedParameter> context, List<NamedPropertyUsage> toPropertyUsageList) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJoinAProp(addAProp(new ExternalDBAction(getTypesForExternalProp(params, context), findLPsNoParamsByPropertyUsage(toPropertyUsageList))),
                BaseUtils.mergeList(Arrays.asList(connectionString, exec), params));
    }

    public LAWithParams addScriptedExternalDBFAction(LPWithParams connectionString, String charset, List<LPWithParams> params, List<TypedParameter> context, List<NamedPropertyUsage> toPropertyUsageList) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJoinAProp(addAProp(new ExternalDBFAction(getTypesForExternalProp(params, context), charset, findLPsNoParamsByPropertyUsage(toPropertyUsageList))),
                BaseUtils.addList(connectionString, params));
    }

    public LAWithParams addScriptedExternalHTTPAction(ExternalHttpMethod method, LPWithParams connectionString, LPWithParams bodyUrl,
                                                      NamedPropertyUsage headers, NamedPropertyUsage cookies, NamedPropertyUsage headersTo, NamedPropertyUsage cookiesTo,
                                                      List<LPWithParams> params, List<TypedParameter> context, List<NamedPropertyUsage> toPropertyUsageList) throws ScriptingErrorLog.SemanticErrorException {
        LP headersProperty = headers != null ? findLPStringParamByPropertyUsage(headers) : null;
        LP cookiesProperty = cookies != null ? findLPStringParamByPropertyUsage(cookies) : null;
        LP headersToProperty = headersTo != null ? findLPStringParamByPropertyUsage(headersTo) : null;
        LP cookiesToProperty = cookiesTo != null ? findLPStringParamByPropertyUsage(cookiesTo) : null;
        return addScriptedJoinAProp(addAProp(new ExternalHTTPAction(method != null ? method : ExternalHttpMethod.POST,
                        getTypesForExternalProp(params, context), findLPsNoParamsByPropertyUsage(toPropertyUsageList),
                        headersProperty, cookiesProperty, headersToProperty, cookiesToProperty, bodyUrl != null)),
                bodyUrl != null ? BaseUtils.mergeList(Arrays.asList(connectionString, bodyUrl), params) : BaseUtils.addList(connectionString, params));
    }

    public LAWithParams addScriptedExternalLSFAction(LPWithParams connectionString, LPWithParams actionLCP, boolean eval, boolean action, List<LPWithParams> params, List<TypedParameter> context, List<NamedPropertyUsage> toPropertyUsageList) throws ScriptingErrorLog.SemanticErrorException {
        String request = eval ? (action ? "eval/action" : "eval") : "/exec?action=$" + (params.size()+1);
        return addScriptedExternalHTTPAction(ExternalHttpMethod.POST,
                addScriptedJProp(getArithProp("+"), Arrays.asList(connectionString, new LPWithParams(addCProp(StringClass.text, LocalizedString.create(request, false))))),
                null, null, null, null, null, BaseUtils.add(params, actionLCP), context, toPropertyUsageList);
    }

    private ImList<LP> findLPsNoParamsByPropertyUsage(List<NamedPropertyUsage> propUsages) throws ScriptingErrorLog.SemanticErrorException {
        if(propUsages == null)
            return ListFact.EMPTY();

        MList<LP> mProps = ListFact.mList(propUsages.size());
        for (NamedPropertyUsage propUsage : propUsages) {
            LP<?> lcp = findLPNoParamsByPropertyUsage(propUsage);
            mProps.add(lcp);
        }
        return mProps.immutableList();
    }

    public LAWithParams addScriptedEmailProp(LPWithParams fromProp,
                                             LPWithParams subjProp,
                                             LPWithParams bodyProp,
                                             List<Message.RecipientType> recipTypes,
                                             List<LPWithParams> recipProps,
                                             List<LPWithParams> attachFileNames,
                                             List<LPWithParams> attachFiles) throws ScriptingErrorLog.SemanticErrorException {

        List<LAPWithParams> allProps = new ArrayList<>();

        if (fromProp != null) {
            allProps.add(fromProp);
        }
        if(subjProp != null) {
            allProps.add(subjProp);
        }
        if(bodyProp != null) {
            allProps.add(bodyProp);
        }
        allProps.addAll(recipProps);

        for (int i = 0; i < attachFileNames.size(); i++) {
            if (attachFileNames.get(i) != null) {
                allProps.add(attachFileNames.get(i));
            }
            allProps.add(attachFiles.get(i));
        }

        Object[] allParams = getParamsPlainList(allProps).toArray();

        ImOrderSet<PropertyInterface> tempContext = genInterfaces(getIntNum(allParams));
        ValueClass[] eaClasses = Property.getCommonClasses(tempContext, readCalcImplements(tempContext, allParams).getCol());

        LA<ClassPropertyInterface> eaPropLP = BL.emailLM.addEAProp(null, LocalizedString.NONAME, eaClasses);
        SendEmailAction eaProp = (SendEmailAction) eaPropLP.property;

        ImList<PropertyInterfaceImplement<ClassPropertyInterface>> allImplements = readCalcImplements(eaPropLP.listInterfaces, allParams);

        int i = 0;
        
        if (fromProp != null) {
            eaProp.setFromAddressAccount(allImplements.get(i++));
        }

        if(subjProp != null) {
            eaProp.setSubject(allImplements.get(i++));
        }

        if(bodyProp != null) {
            eaProp.addInlineFile(allImplements.get(i++));
        }

        for (Message.RecipientType recipType : recipTypes) {
            eaProp.addRecipient(allImplements.get(i++), recipType);
        }

        for (LPWithParams fileName : attachFileNames) {
            eaProp.addAttachmentFile(fileName != null ? allImplements.get(i++) : null, allImplements.get(i++));
        }

        return new LAWithParams(eaPropLP, mergeAllParams(allProps));
    }

    public LPWithParams addScriptedAdditiveOrProp(List<String> operands, List<LPWithParams> properties) {
        assert operands.size() + 1 == properties.size();

        LPWithParams res = properties.get(0);
        if (operands.size() > 0) {
            List<Object> resultParams;
            int[] coeffs = new int[properties.size()];
            for (int i = 0; i < coeffs.length; i++) {
                if (i == 0 || operands.get(i-1).equals("(+)")) {
                    coeffs[i] = 1;
                } else {
                    coeffs[i] = -1;
                }
            }
            resultParams = getParamsPlainList(properties);
            res = new LPWithParams(addUProp(null, LocalizedString.NONAME, Union.SUM, null, coeffs, resultParams.toArray()), mergeAllParams(properties));
        }
        return res;
    }

    public LPWithParams addScriptedAdditiveProp(List<String> operands, List<LPWithParams> properties) throws ScriptingErrorLog.SemanticErrorException {
        assert operands.size() + 1 == properties.size();

        LPWithParams sumLP = properties.get(0);
        for (int i = 1; i < properties.size(); i++) {
            LPWithParams currLP = properties.get(i);
            sumLP = addScriptedJProp(getArithProp(operands.get(i-1)), asList(sumLP, currLP));
        }
        return sumLP;
    }


    public LPWithParams addScriptedMultiplicativeProp(List<String> operands, List<LPWithParams> properties) throws ScriptingErrorLog.SemanticErrorException {
        assert operands.size() + 1 == properties.size();

        LPWithParams curLP = properties.get(0);
        for (int i = 1; i < properties.size(); i++) {
            String op = operands.get(i-1);
            curLP = addScriptedJProp(getArithProp(op), asList(curLP, properties.get(i)));
        }
        return curLP;
    }

    public LPWithParams addScriptedUnaryMinusProp(LPWithParams prop) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(baseLM.minus, Collections.singletonList(prop));
    }

    public LPWithParams addScriptedNotProp(LPWithParams prop) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(not(), Collections.singletonList(prop));
    }

    public LPWithParams addScriptedCastProp(String typeName, LPWithParams prop) throws ScriptingErrorLog.SemanticErrorException {
        ValueClass cls = findClass(typeName);

        //cls всегда будет DataClass из-за грамматики
        assert cls instanceof DataClass;

        return addScriptedJProp(addCastProp((DataClass) cls), Collections.singletonList(prop));
    }

    private boolean doesExtendContext(int contextSize, List<? extends LAPWithParams> list, List<LPWithParams> orders) {
        Set<Integer> listContext = new HashSet<>();
        for(int i=0;i<contextSize;i++)
            listContext.add(i);
        for(LAPWithParams lp : list)
            if(lp.getLP() != null)
                listContext.addAll(lp.usedParams);
        return !listContext.containsAll(mergeAllParams(orders));
    }

    private List<Integer> mergeAllParams(List<? extends LAPWithParams> lpList) {
        Set<Integer> s = new TreeSet<>();
        for (LAPWithParams mappedLP : lpList) {
            s.addAll(mappedLP.usedParams);
        }
        return new ArrayList<>(s);
    }

    private List<Integer> mergeIntLists(List<List<Integer>> lists) {
        Set<Integer> s = new TreeSet<>();
        for (List<Integer> list : lists) {
            s.addAll(list);
        }
        return new ArrayList<>(s);
    }

    public LAWithParams addScriptedListAProp(List<LAWithParams> properties, List<LP> localProps) {
        List<Object> resultParams = getParamsPlainList(properties);

        MExclSet<Pair<LP, List<ResolveClassSet>>> mDebugLocals = null;
        if(debugger.isEnabled()) {
            mDebugLocals = SetFact.mExclSet(localProps.size());
        }
        MSet<SessionDataProperty> mLocals = SetFact.mSet();
        for (LP<?> localProp : localProps) {
            if (mDebugLocals != null) {
                List<ResolveClassSet> localSignature = getLocalSignature(localProp);
                mDebugLocals.exclAdd(new Pair<LP, List<ResolveClassSet>>(localProp, localSignature));
            }
            mLocals.add((SessionDataProperty) localProp.property);

            removeLocal(localProp);
        }

        LA<?> listLP = addListAProp(mLocals.immutable(), resultParams.toArray());

        if(mDebugLocals != null) {
            listLP.property.setDebugLocals(mDebugLocals.immutable());
        }

        List<Integer> usedParams = mergeAllParams(properties);
        return new LAWithParams(listLP, usedParams);
    }

    public LAWithParams addScriptedNewSessionAProp(LAWithParams action, List<NamedPropertyUsage> migrateSessionProps, boolean migrateAllSessionProps,
                                                   boolean isNested, boolean singleApply, boolean newSQL) throws ScriptingErrorLog.SemanticErrorException {
        LA<?> sessionLP = addNewSessionAProp(null, action.getLP(), isNested, singleApply, newSQL, getMigrateProps(migrateSessionProps, migrateAllSessionProps));
        return new LAWithParams(sessionLP, action.usedParams);
    }

    public DataClass getInputDataClass(String paramName, List<TypedParameter> context, String typeId, LPWithParams oldValue, boolean insideRecursion) throws ScriptingErrorLog.SemanticErrorException {
        DataClass requestDataClass;
        if(typeId != null) {
            requestDataClass = ClassCanonicalNameUtils.getScriptedDataClass(typeId);
        } else {
            ValueClass valueClass = getValueClassByParamProperty(oldValue, context);
            checks.checkInputDataClass(valueClass);
            requestDataClass = (DataClass) valueClass;
        }

        if(paramName != null)
            getParamIndex(new TypedParameter(requestDataClass, paramName), context, true, insideRecursion);
        return requestDataClass;
    }

    public LAWithParams addScriptedInputAProp(DataClass requestDataClass, LPWithParams oldValue, NamedPropertyUsage targetProp, LAWithParams doAction, LAWithParams elseAction, List<TypedParameter> oldContext, List<TypedParameter> newContext, boolean assign, LPWithParams changeProp, DebugInfo.DebugPoint assignDebugPoint) throws ScriptingErrorLog.SemanticErrorException {
        assert targetProp == null;
        LP<?> tprop = getInputProp(targetProp, requestDataClass, null);

        LA property = addInputAProp(requestDataClass, tprop != null ? tprop.property : null);
        
        if (changeProp == null)
            changeProp = oldValue;

        // optimization. we don't use files on client side (see also DefaultChangeAction.executeCustom()) 
        if (oldValue == null || getTypeByParamProperty(oldValue, oldContext) instanceof FileClass)
            oldValue = new LPWithParams(baseLM.vnull);
        LAWithParams inputAction = addScriptedJoinAProp(property, Collections.singletonList(oldValue));

        return proceedInputDoClause(doAction, elseAction, oldContext, newContext, ListFact.<LP>singleton(tprop), inputAction,
                ListFact.singleton(assign ? new Pair<>(changeProp, assignDebugPoint) : null));
    }


    public LAWithParams addScriptedRequestAProp(LAWithParams requestAction, LAWithParams doAction, LAWithParams elseAction) throws ScriptingErrorLog.SemanticErrorException {
        List<LAPWithParams> propParams = new ArrayList<>();
        propParams.add(requestAction);
        propParams.add(doAction);
        if(elseAction != null)
            propParams.add(elseAction);

        List<Integer> allParams = mergeAllParams(propParams);
        LA result = addRequestAProp(null, LocalizedString.NONAME, getParamsPlainList(propParams).toArray());
        return new LAWithParams(result, allParams);
    }

    public LAWithParams addScriptedActiveFormAProp(String formName) throws ScriptingErrorLog.SemanticErrorException {
        FormEntity form = findForm(formName);
        return new LAWithParams(addAProp(null, new IsActiveFormAction(LocalizedString.NONAME, form, baseLM.getIsActiveFormProperty())), new ArrayList<Integer>());
    }

    public LAWithParams addScriptedActivateAProp(FormEntity form, ComponentView component) throws ScriptingErrorLog.SemanticErrorException {
        return new LAWithParams(addAProp(null, new ActivateAction(LocalizedString.NONAME, form, component)), new ArrayList<Integer>());
    }

    public List<LP<?>> addLocalDataProperty(List<String> names, String returnClassName, List<String> paramClassNames,
                                            LocalNestedType nestedType, DebugInfo.DebugPoint point) throws ScriptingErrorLog.SemanticErrorException {

        List<ResolveClassSet> signature = new ArrayList<>();
        for (String className : paramClassNames) {
            signature.add(findClass(className).getResolveSet());
        }

        List<LP<?>> res = new ArrayList<>();
        for (String name : names) {
            LP<?> lcp = addScriptedDProp(returnClassName, paramClassNames, true, false, true, nestedType);
            addLocal(lcp, new LocalPropertyData(name, signature));
            lcp.property.setDebugInfo(new PropertyDebugInfo(point, false));
            res.add(lcp);
        }
        return res;
    }

    public LP addWatchLocalDataProperty(LP lp, List<ResolveClassSet> signature) {
        assert lp.property instanceof SessionDataProperty;
        addModuleLP(lp);
        propClasses.put(lp, signature);
        return lp;
    }

    public LAWithParams addScriptedJoinAProp(NamedPropertyUsage pUsage, List<LPWithParams> properties, List<TypedParameter> params) throws ScriptingErrorLog.SemanticErrorException {
        LA mainProp = findLAByPropertyUsage(pUsage, properties, params);
        return addScriptedJoinAProp(mainProp, properties);
    }

    public LAWithParams addScriptedJoinAProp(LA mainProp, List<LPWithParams> properties) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkParamCount(mainProp, properties.size());

        List<Object> resultParams = getParamsPlainList(properties);
        List<Integer> usedParams = mergeAllParams(properties);
        LA prop = addJoinAProp(null, LocalizedString.NONAME, mainProp, resultParams.toArray());
        return new LAWithParams(prop, usedParams);
    }

    public LAWithParams addScriptedConfirmProp(LPWithParams msgProp, LAWithParams doAction, LAWithParams elseAction, boolean yesNo, List<TypedParameter> oldContext, List<TypedParameter> newContext) throws ScriptingErrorLog.SemanticErrorException {
        LP targetProp = null;
        if(yesNo)
            targetProp = getInputProp(null, LogicalClass.instance, null);

        List<Object> resultParams = getParamsPlainList(singletonList(msgProp));
        LA asyncLA = addConfirmAProp("lsFusion", yesNo, targetProp, resultParams.toArray());
        LAWithParams inputAction = new LAWithParams(asyncLA, msgProp.usedParams);

        return proceedInputDoClause(doAction, elseAction, oldContext, newContext, yesNo ? ListFact.singleton(targetProp) : ListFact.<LP>EMPTY(), inputAction, yesNo ? ListFact.<Pair<LPWithParams, DebugInfo.DebugPoint>>singleton(null) : ListFact.<Pair<LPWithParams, DebugInfo.DebugPoint>>EMPTY());
    }

    public LAWithParams addScriptedMessageProp(LPWithParams msgProp, boolean noWait) {
        List<Object> resultParams = getParamsPlainList(singletonList(msgProp));
        LA asyncLA = addMAProp("lsFusion", noWait, resultParams.toArray());
        return new LAWithParams(asyncLA, msgProp.usedParams);
    }

    public LAWithParams addScriptedAsyncUpdateProp(LPWithParams asyncProp) {
        List<Object> resultParams = getParamsPlainList(singletonList(asyncProp));
        LA asyncLA = addAsyncUpdateAProp(resultParams.toArray());
        return new LAWithParams(asyncLA, asyncProp.usedParams);
    }

    private FormEntity getFormFromSeekObjectName(String formObjectName) throws ScriptingErrorLog.SemanticErrorException {
        int pointPos = formObjectName.lastIndexOf('.');
        assert pointPos > 0;

        String formName = formObjectName.substring(0, pointPos);
        return findForm(formName);
    }

    private ObjectEntity getSeekObject(FormEntity form, String formObjectName) throws ScriptingErrorLog.SemanticErrorException {
        return form.getNFObject(getSeekObjectName(formObjectName), getVersion());
    }

    private GroupObjectEntity getSeekGroupObject(FormEntity form, String formObjectName) throws ScriptingErrorLog.SemanticErrorException {
        return form.getNFGroupObject(getSeekObjectName(formObjectName), getVersion());
    }

    private String getSeekObjectName(String formObjectName) {
        int pointPos = formObjectName.lastIndexOf('.');
        assert pointPos > 0;

        return formObjectName.substring(pointPos + 1);
    }

    public LAWithParams addScriptedObjectSeekProp(String name, LPWithParams seekProp, UpdateType type) throws ScriptingErrorLog.SemanticErrorException {
        FormEntity form = getFormFromSeekObjectName(name);
        ObjectEntity object = getSeekObject(form, name);

        if (object != null) {
            List<Object> resultParams = getParamsPlainList(singletonList(seekProp));
            LA LA = addOSAProp(object, type, resultParams.toArray());
            return new LAWithParams(LA, seekProp.usedParams);
        } else {
            errLog.emitObjectNotFoundError(parser, getSeekObjectName(name));
            return null;
        }
    }

    public LAWithParams addScriptedGroupObjectSeekProp(String name, List<String> objNames, List<LPWithParams> values, UpdateType type) throws ScriptingErrorLog.SemanticErrorException {
        FormEntity form = getFormFromSeekObjectName(name);
        GroupObjectEntity groupObject = getSeekGroupObject(form, name);

        List<ObjectEntity> objects = new ArrayList<>();
        if (objNames != null) {
            for (String objName : objNames) {
                ObjectEntity obj = form.getNFObject(objName, getVersion());
                if (obj == null) {
                    errLog.emitObjectNotFoundError(parser, objName);
                } else if (obj.groupTo != groupObject) {
                    errLog.emitObjectOfGroupObjectError(parser, obj.getSID(), groupObject.getSID());
                }
                objects.add(obj);
            }
        }

        if (groupObject != null) {
            List<Object> resultParams = getParamsPlainList(values);
            LA LA = addGOSAProp(groupObject, objects, type, resultParams.toArray());
            return new LAWithParams(LA, mergeAllParams(values));
        } else {
            errLog.emitGroupObjectNotFoundError(parser, getSeekObjectName(name));
            return null;
        }
    }

    public LAWithParams addScriptedEvalAction(LPWithParams property, List<LPWithParams> params, List<TypedParameter> contextParams, boolean action) throws ScriptingErrorLog.SemanticErrorException {
        Type exprType = getTypeByParamProperty(property, contextParams);
        if (!(exprType instanceof StringClass)) {
            errLog.emitEvalExpressionError(parser);
        }

        List<LP<?>> paramsLCP = new ArrayList<>();
        Set<Integer> allParams = new TreeSet<>(property.usedParams);
        if (params != null) {
            for (LPWithParams param : params) {
                paramsLCP.add(param.getLP());
                allParams.addAll(param.usedParams);
            }
        }

        LA<?> res = addEvalAProp(property.getLP(), paramsLCP, action);
        return new LAWithParams(res, new ArrayList<>(allParams));
    }

    public LAWithParams addScriptedDrillDownAction(LPWithParams property) {
        LA<?> res = addDrillDownAProp(property.getLP());
        return new LAWithParams(res, property.usedParams);
    }

    public LAWithParams addScriptedAssignPropertyAProp(List<TypedParameter> context, NamedPropertyUsage toPropertyUsage, List<LPWithParams> toPropertyMapping, LPWithParams fromProperty, LPWithParams whereProperty, List<TypedParameter> newContext) throws ScriptingErrorLog.SemanticErrorException {
        LP toPropertyLP = findLPByPropertyUsage(toPropertyUsage, toPropertyMapping, newContext);

        LPWithParams toProperty = addScriptedJProp(toPropertyLP, toPropertyMapping);

        return addScriptedAssignAProp(context, fromProperty, whereProperty, toProperty);
    }

    private LAWithParams addScriptedAssignAProp(List<TypedParameter> context, LPWithParams fromProperty, LPWithParams whereProperty, LPWithParams toProperty) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkAssignProperty(fromProperty, toProperty);

        List<Integer> resultInterfaces = getResultInterfaces(context.size(), toProperty, fromProperty, whereProperty);

        List<LAPWithParams> paramsList = new ArrayList<>();
        for (int resI : resultInterfaces) {
            paramsList.add(new LPWithParams(resI));
        }
        paramsList.add(toProperty);
        paramsList.add(fromProperty);
        if (whereProperty != null) {
            paramsList.add(whereProperty);
        }
        List<Object> resultParams = getParamsPlainList(paramsList);
        LA result = addSetPropertyAProp(null, LocalizedString.NONAME, resultInterfaces.size(), whereProperty != null, resultParams.toArray());
        return new LAWithParams(result, resultInterfaces);
    }

    public LAWithParams addScriptedAddObjProp(List<TypedParameter> context, String className, NamedPropertyUsage toPropUsage, List<LPWithParams> toPropMapping, LPWithParams whereProperty, List<TypedParameter> newContext) throws ScriptingErrorLog.SemanticErrorException {
        ValueClass cls = findClass(className);
        checks.checkAddActionsClass(cls);
        checks.checkAddObjTOParams(context.size(), toPropMapping);

        LPWithParams toProperty = null;
        if (toPropUsage != null && toPropMapping != null) {
            toProperty = addScriptedJProp(findLPByPropertyUsage(toPropUsage, toPropMapping, newContext), toPropMapping);
        }

        List<Integer> resultInterfaces = getResultInterfaces(context.size(), toProperty, whereProperty);

        List<LPWithParams> paramsList = new ArrayList<>();
        for (int resI : resultInterfaces) {
            paramsList.add(new LPWithParams(resI));
        }
        if (toProperty != null) {
            paramsList.add(toProperty);
        } else if (whereProperty == null) {
            paramsList.add(new LPWithParams(new LP<>(baseLM.getAddedObjectProperty())));
        }
        if (whereProperty != null) {
            paramsList.add(whereProperty);
        }
        List<Object> resultParams = getParamsPlainList(paramsList);
        LA result = addAddObjAProp((CustomClass) cls, false, resultInterfaces.size(), whereProperty != null, toProperty != null || whereProperty == null, resultParams.toArray());
        return new LAWithParams(result, resultInterfaces);
    }

    public LAWithParams addScriptedDeleteAProp(int oldContextSize, List<TypedParameter> newContext, LPWithParams param, LPWithParams whereProperty) throws ScriptingErrorLog.SemanticErrorException {
        LAWithParams res = addScriptedChangeClassAProp(oldContextSize, newContext, param, getBaseClass().unknown, whereProperty);
        setDeleteActionOptions(res.getLP());
        return res;
    }

    public LAWithParams addScriptedChangeClassAProp(int oldContextSize, List<TypedParameter> newContext, LPWithParams param, String className, LPWithParams whereProperty) throws ScriptingErrorLog.SemanticErrorException {
        ValueClass cls = findClass(className);
        checks.checkChangeClassActionClass(cls);
        return addScriptedChangeClassAProp(oldContextSize, newContext, param, (ConcreteCustomClass) cls, whereProperty);
    }

    private LAWithParams addScriptedChangeClassAProp(int oldContextSize, List<TypedParameter> newContext, LPWithParams param, ConcreteObjectClass cls, LPWithParams whereProperty) throws ScriptingErrorLog.SemanticErrorException {
        List<LAPWithParams> paramList = new ArrayList<>();
        paramList.add(param);
        if (whereProperty != null) {
            paramList.add(whereProperty);
        }
        List<Integer> allParams = mergeAllParams(paramList);
        int changedIndex = allParams.indexOf(param.usedParams.get(0));

        List<Integer> resultInterfaces = new ArrayList<>();
        for (int paramIndex : allParams) {
            if (paramIndex >= oldContextSize) {
                break;
            }
            resultInterfaces.add(paramIndex);
        }
        boolean contextExtended = allParams.size() > resultInterfaces.size();

        checks.checkChangeClassWhere(contextExtended, param, whereProperty, newContext);

        List<LAPWithParams> paramsList = new ArrayList<>();
        for (int resI : resultInterfaces) {
            paramsList.add(new LPWithParams(resI));
        }
        if (whereProperty != null) {
            paramsList.add(whereProperty);
        }
        List<Object> resultParams = getParamsPlainList(paramsList);

        LA<?> res = addChangeClassAProp(cls, resultInterfaces.size(), changedIndex, contextExtended, whereProperty != null, resultParams.toArray());
        return new LAWithParams(res,  resultInterfaces);
    }

    public List<Integer> getResultInterfaces(int contextSize, LAPWithParams... params) {
        List<LAPWithParams> lpList = new ArrayList<>();
        for (LAPWithParams lp : params) {
            if (lp != null) {
                lpList.add(lp);
            }
        }
        List<Integer> allParams = mergeAllParams(lpList);

        //все использованные параметры, которые были в старом контексте, идут на вход результирующего свойства
        List<Integer> resultInterfaces = new ArrayList<>();
        for (int paramIndex : allParams) {
            if (paramIndex >= contextSize) {
                break;
            }
            resultInterfaces.add(paramIndex);
        }
        return resultInterfaces;
    }

    public LAWithParams addScriptedIfAProp(LPWithParams condition, LAWithParams trueAction, LAWithParams falseAction) {
        List<LAPWithParams> propParams = toList(condition, trueAction);
        if (falseAction != null) {
            propParams.add(falseAction);
        }
        List<Integer> allParams = mergeAllParams(propParams);
        LA result = addIfAProp(null, LocalizedString.NONAME, false, getParamsPlainList(propParams).toArray());
        return new LAWithParams(result, allParams);
    }

    public LAWithParams addScriptedTryAProp(LAWithParams tryAction, LAWithParams catchAction, LAWithParams finallyAction) {
        List<LAPWithParams> propParams = new ArrayList<>();
        propParams.add(tryAction);
        if (catchAction != null) {
            propParams.add(catchAction);
        }if (finallyAction != null) {
            propParams.add(finallyAction);
        }

        List<Integer> allParams = mergeAllParams(propParams);
        LA result = addTryAProp(null, LocalizedString.NONAME, catchAction != null, finallyAction != null, getParamsPlainList(propParams).toArray());
        return new LAWithParams(result, allParams);
    }

    public LAWithParams addScriptedCaseAProp(List<LPWithParams> whenProps, List<LAWithParams> thenActions, LAWithParams elseAction, boolean isExclusive) {
        assert whenProps.size() > 0 && whenProps.size() == thenActions.size();

        List<LAPWithParams> caseParams = new ArrayList<>();
        for (int i = 0; i < whenProps.size(); i++) {
            caseParams.add(whenProps.get(i));
            caseParams.add(thenActions.get(i));
        }
        if (elseAction != null) {
            caseParams.add(elseAction);
        }

        List<Integer> allParams = mergeAllParams(caseParams);
        LA result = addCaseAProp(isExclusive, getParamsPlainList(caseParams).toArray());
        return new LAWithParams(result, allParams);
    }

    public LAWithParams addScriptedMultiAProp(List<LAWithParams> actions, boolean isExclusive) {
        List<Integer> allParams = mergeAllParams(actions);
        LA result = addMultiAProp(isExclusive, getParamsPlainList(actions).toArray());
        return new LAWithParams(result, allParams);

    }

    public LAWithParams addScriptedApplyAProp(LAWithParams action, boolean singleApply, List<NamedPropertyUsage> keepSessionProps, boolean keepAllSessionProps, boolean serializable)
            throws ScriptingErrorLog.SemanticErrorException {
        List<LAPWithParams> propParams = Collections.<LAPWithParams>singletonList(action);

        LA result = addApplyAProp(null, LocalizedString.NONAME, (action != null && action.getLP() != null) ? action.getLP() : null, singleApply,
                getMigrateProps(keepSessionProps, keepAllSessionProps), serializable);

        return new LAWithParams(result, mergeAllParams(propParams));
    }

    public LAWithParams addScriptedCancelAProp(List<NamedPropertyUsage> keepSessionProps, boolean keepAllSessionProps)
            throws ScriptingErrorLog.SemanticErrorException {
        scriptLogger.info("addScriptedCancelAProp();");

        LA result = addCancelAProp(null, LocalizedString.NONAME, getMigrateProps(keepSessionProps, keepAllSessionProps));

        return new LAWithParams(result, new ArrayList<Integer>());
    }

    private FunctionSet<SessionDataProperty> getMigrateProps(List<NamedPropertyUsage> keepSessionProps, boolean keepAllSessionProps) throws ScriptingErrorLog.SemanticErrorException {
        FunctionSet<SessionDataProperty> keepProps;
        if(keepAllSessionProps) {
            keepProps = DataSession.keepAllSessionProperties;
        } else {
            MExclSet<SessionDataProperty> mKeepProps = SetFact.mExclSet(keepSessionProps.size());
            for (NamedPropertyUsage migratePropUsage : keepSessionProps) {
                LP<?> prop = findLPByPropertyUsage(migratePropUsage);
                checks.checkSessionProperty(prop);
                mKeepProps.exclAdd((SessionDataProperty) prop.property);
            }
            keepProps = mKeepProps.immutable();
        }
        return keepProps;
    }

    public LAWithParams addScriptedNewAProp(List<TypedParameter> oldContext, LAWithParams action, Integer addNum, String addClassName, Boolean autoSet) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedForAProp(oldContext, null, new ArrayList<LPWithParams>(), action, null, addNum, addClassName, autoSet, false, false, new ArrayList<LPWithParams>(), false);
    }

    public LAWithParams addScriptedForAProp(List<TypedParameter> oldContext, LPWithParams condition, List<LPWithParams> orders, LAWithParams action, LAWithParams elseAction, Integer addNum, String addClassName, Boolean autoSet, boolean recursive, boolean descending, List<LPWithParams> noInline, boolean forceInline) throws ScriptingErrorLog.SemanticErrorException {
        boolean ordersNotNull = (condition != null ? doesExtendContext(oldContext.size(), singletonList(condition), orders) : !orders.isEmpty());

        List<LAPWithParams> creationParams = new ArrayList<>();
        if (condition != null) {
            creationParams.add(condition);
        }
        creationParams.addAll(orders);
        if(addNum != null) {
            creationParams.add(new LPWithParams(addNum));
        }
        if (elseAction != null) {
            creationParams.add(elseAction);
        }
        creationParams.add(action);
        List<Integer> allParams = mergeAllParams(creationParams);

        List<Integer> usedParams = new ArrayList<>();
        List<Integer> extParams = new ArrayList<>();
        for (int paramIndex : allParams) {
            if (paramIndex < oldContext.size()) {
                usedParams.add(paramIndex);
            } else {
                extParams.add(paramIndex);
            }
        }

        if(ActionDebugger.watchHack.get() != null && extParams.size() > 1) {
            ActionDebugger.watchHack.set(true);
        }

        checks.checkForActionConstraints(recursive, usedParams, allParams);

        List<LAPWithParams> allCreationParams = new ArrayList<>();
        for (int usedParam : usedParams) {
            allCreationParams.add(new LPWithParams(usedParam));
        }
        allCreationParams.addAll(creationParams);
        if(noInline==null) { // предполагается надо включить все кроме addNum
            noInline = new ArrayList<>();
            for (int extParam : extParams)
                if(addNum==null || !addNum.equals(extParam)) {
                    noInline.add(new LPWithParams(extParam));
                }
        }
        allCreationParams.addAll(noInline);

        LA result = addForAProp(LocalizedString.NONAME, !descending, ordersNotNull, recursive, elseAction != null, usedParams.size(),
                                addClassName != null ? (CustomClass) findClass(addClassName) : null, autoSet != null ? autoSet : false, condition != null, noInline.size(), forceInline,
                                getParamsPlainList(allCreationParams).toArray());
        return new LAWithParams(result, usedParams);
    }

    public LAWithParams getTerminalFlowAction(boolean isBreak) {
        return new LAWithParams(isBreak ? new LA<>(new BreakAction()) : new LA<>(new ReturnAction()), new ArrayList<Integer>());
    }

    private List<Integer> getParamsAssertList(List<LPWithParams> list) {
        List<Integer> result = new ArrayList<>();
        for(LPWithParams lp : list) {
            assert lp.getLP() == null;
            result.add(BaseUtils.single(lp.usedParams));
        }
        return result;
    }

    @SafeVarargs
    private final List<Object> getParamsPlainList(List<? extends LAPWithParams>... mappedPropLists) {
        List<LAP> props = new ArrayList<>();
        List<List<Integer>> usedParams = new ArrayList<>();
        for (List<? extends LAPWithParams> mappedPropList : mappedPropLists) {
            for (LAPWithParams mappedProp : mappedPropList) {
                props.add(mappedProp.getLP());
                usedParams.add(mappedProp.usedParams);
            }
        }
        return getParamsPlainList(props, usedParams);
    }

    private List<Object> getParamsPlainList(List<LAP> paramProps, List<List<Integer>> usedParams) {
        List<Integer> allUsedParams = mergeIntLists(usedParams);
        List<Object> resultParams = new ArrayList<>();

        for (int i = 0; i < paramProps.size(); i++) {
            LAP property = paramProps.get(i);
            if (property != null) {
                resultParams.add(property);
                for (int paramIndex : usedParams.get(i)) {
                    int localParamIndex = allUsedParams.indexOf(paramIndex);
                    assert localParamIndex >= 0;
                    resultParams.add(localParamIndex + 1);
                }
            } else {
                int localParamIndex = allUsedParams.indexOf(usedParams.get(i).get(0));
                assert localParamIndex >= 0;
                resultParams.add(localParamIndex + 1);
            }
        }
        return resultParams;
    }

    public LP addScriptedGProp(List<LPWithParams> groupProps, GroupingType type, List<LPWithParams> mainProps, List<LPWithParams> orderProps, boolean ascending, LPWithParams whereProp, List<ResolveClassSet> explicitInnerClasses) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkGPropOrderConsistence(type, orderProps.size());
        checks.checkGPropAggrConstraints(type, mainProps, groupProps);
        checks.checkGPropAggregateConsistence(type, mainProps.size());
        checks.checkGPropWhereConsistence(type, whereProp);
        checks.checkGPropSumConstraints(type, mainProps.get(0));

        List<LPWithParams> whereProps = new ArrayList<>();
        if (type == GroupingType.AGGR || type == GroupingType.NAGGR) {
            if (whereProp != null) {
                whereProps.add(whereProp);
            } else {
                whereProps.add(new LPWithParams(mainProps.get(0).usedParams.get(0)));
            }
        }
        if (type == GroupingType.LAST) {
            if (whereProp != null) {
                mainProps.add(0, whereProp);
            } else {
                mainProps.add(mainProps.get(0));
            }
        }
        List<Object> resultParams = getParamsPlainList(mainProps, whereProps, orderProps, groupProps);

        boolean ordersNotNull = doesExtendContext(0, mergeLists(mainProps, groupProps, whereProps), orderProps);

        int groupPropParamCount = mergeAllParams(mergeLists(mainProps, groupProps, orderProps, whereProps)).size();
        assert groupPropParamCount == explicitInnerClasses.size();
        LocalizedString emptyCaption = LocalizedString.NONAME;
        LP resultProp = null;
        if (type == GroupingType.SUM) {
            resultProp = addSGProp(null, false, false, emptyCaption, groupPropParamCount, explicitInnerClasses, resultParams.toArray());
        } else if (type == GroupingType.MAX || type == GroupingType.MIN) {
            resultProp = addMGProp(null, false, emptyCaption, type == GroupingType.MIN, groupPropParamCount, explicitInnerClasses, resultParams.toArray());
        } else if (type == GroupingType.CONCAT) {
            resultProp = addOGProp(null, false, emptyCaption, GroupType.STRING_AGG, orderProps.size(), ordersNotNull, !ascending, groupPropParamCount, explicitInnerClasses, resultParams.toArray());
        } else if (type == GroupingType.AGGR || type == GroupingType.NAGGR) {
            resultProp = addAGProp(null, false, false, emptyCaption, type == GroupingType.NAGGR, groupPropParamCount, explicitInnerClasses, resultParams.toArray());
        } else if (type == GroupingType.EQUAL) {
            resultProp = addCGProp(null, false, false, emptyCaption, null, groupPropParamCount, explicitInnerClasses, resultParams.toArray());
        } else if (type == GroupingType.LAST) {
            resultProp = addOGProp(null, false, emptyCaption, GroupType.LAST, orderProps.size(), ordersNotNull, !ascending, groupPropParamCount, explicitInnerClasses, resultParams.toArray());
        }
        return resultProp;
    }

    public LPContextIndependent addScriptedCIGProp(int oldContextSize, List<LPWithParams> groupProps, GroupingType type, List<LPWithParams> mainProps, List<LPWithParams> orderProps,
                                                   boolean ascending, LPWithParams whereProp, List<TypedParameter> newContext) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedCDIGProp(oldContextSize, groupProps, type, mainProps, orderProps, ascending, whereProp, newContext);
    }

    // ci - надо в дырки вставлять, от использованных, если не ci то в конце
    public List<LPWithParams> getAllGroupProps(List<Integer> resultInterfaces, List<LPWithParams> groupProps, boolean ci) {
        List<LPWithParams> allGroupProps = new ArrayList<>();

        if(ci) {
            Set<Integer> usedInterfaces = new HashSet<>(resultInterfaces);
//        нужно groupProps в дырки вставить для context independent группировки
            int ra = 0, ga = 0;
            int groupSize = groupProps.size();
            for (int i = 0, size = resultInterfaces.size() + groupSize; i < size; i++) {
                LPWithParams add;
                if (ga >= groupSize || usedInterfaces.contains(i))
                    add = new LPWithParams(resultInterfaces.get(ra++));
                else
                    add = groupProps.get(ga++);
                allGroupProps.add(add);
            }
        } else {
            for (int resI : resultInterfaces) {
                allGroupProps.add(new LPWithParams(resI));
            }
            allGroupProps.addAll(groupProps);
        }

        return allGroupProps;
    }

    // второй результат в паре использованные параметры из внешнего контекста (LP на выходе имеет сначала эти использованные параметры, потом группировки)
    public LPContextIndependent addScriptedCDIGProp(int oldContextSize, List<LPWithParams> groupProps, GroupingType type, List<LPWithParams> mainProps, List<LPWithParams> orderProps,
                                                    boolean ascending, LPWithParams whereProp, List<TypedParameter> newContext) throws ScriptingErrorLog.SemanticErrorException {
        List<LPWithParams> lpWithParams = mergeLists(groupProps, mainProps, orderProps, Collections.singletonList(whereProp));
        List<Integer> resultInterfaces = getResultInterfaces(oldContextSize, lpWithParams.toArray(new LAPWithParams[lpWithParams.size()]));

        List<LPWithParams> allGroupProps = getAllGroupProps(resultInterfaces, groupProps, true);

        List<ResolveClassSet> explicitInnerClasses = getClassesFromTypedParams(oldContextSize, resultInterfaces, newContext);

        LP gProp = addScriptedGProp(allGroupProps, type, mainProps, orderProps, ascending, whereProp, explicitInnerClasses);
        return new LPContextIndependent(gProp, getParamClassesByParamProperties(allGroupProps, newContext), resultInterfaces);
    }

    public List<ResolveClassSet> getClassesFromTypedParams(int oldContextSize, List<Integer> resultInterfaces, List<TypedParameter> newContext) {
        List<TypedParameter> usedInnerInterfaces = new ArrayList<>();
        for (int resI : resultInterfaces)
            usedInnerInterfaces.add(newContext.get(resI));
        usedInnerInterfaces.addAll(newContext.subList(oldContextSize, newContext.size()));
        return getClassesFromTypedParams(usedInnerInterfaces);
    }

    public Pair<LPWithParams, LPContextIndependent> addScriptedCDGProp(int oldContextSize, List<LPWithParams> groupProps, GroupingType type, List<LPWithParams> mainProps, List<LPWithParams> orderProps,
                                                                       boolean ascending, LPWithParams whereProp, List<TypedParameter> newContext) throws ScriptingErrorLog.SemanticErrorException {
        if(groupProps == null)
            groupProps = Collections.emptyList();
        LPContextIndependent ci = addScriptedCDIGProp(oldContextSize, groupProps, type, mainProps, orderProps, ascending, whereProp, newContext);
        if(groupProps.size() > 0)
            return new Pair<>(null, ci);
        else
            return new Pair<>(new LPWithParams(ci.property, ci.usedContext), null);
    }

    public LPContextIndependent addScriptedAGProp(List<TypedParameter> context, String aggClassName, LPWithParams whereExpr, DebugInfo.DebugPoint classDebugPoint, DebugInfo.DebugPoint exprDebugPoint, boolean innerPD) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkNoInline(innerPD);

        ValueClass aggClass = findClass(aggClassName);
        checks.checkAggrClass(aggClass);
        checks.checkParamCount(whereExpr.getLP(), context.size());

//        prim1Object = DATA prim1Class (aggrClass) INDEXED;
//        prim2Object = DATA prim2Class (aggrClass) INDEXED;

        List<LPWithParams> groupProps = new ArrayList<>();
        List<ResolveClassSet> resultSignature = new ArrayList<>();
        ResolveClassSet aggSignature = aggClass.getResolveSet();
        for (TypedParameter param : context) {
            LP lp = addDProp(LocalizedString.NONAME, param.cls, aggClass);

            makePropertyPublic(lp, param.paramName, aggSignature);
            ((StoredDataProperty) lp.property).markStored(baseLM.tableFactory);

            groupProps.add(new LPWithParams(lp, 0));
            resultSignature.add(param.cls.getResolveSet());
        }

//        aggrObject (prim1Object, prim2Object) =
//                GROUP AGGR aggrClass aggrObject
//        WHERE aggrObject IS aggrClass BY prim1Object(aggrObject), prim2Object(aggrObject);
        LP lcp = addScriptedGProp(groupProps, GroupingType.AGGR, Collections.singletonList(new LPWithParams(0)), Collections.<LPWithParams>emptyList(), false,
                new LPWithParams(is(aggClass), 0), Collections.singletonList(aggSignature));
        ((AggregateGroupProperty) lcp.property).isFullAggr = true;

//        aggrProperty(prim1Class prim1Object, prim2Class prim2Object) => aggrObject(prim1Object, prim2Object) RESOLVE LEFT; // добавление
        addScriptedFollows(whereExpr.getLP(), new LPWithParams(lcp, whereExpr), Collections.singletonList(new PropertyFollowsDebug(true, classDebugPoint)), Event.APPLY, null);

//        aggrObject IS aggrClass => aggrProperty(prim1Object(aggrObject), prim2Object(aggrObject)) RESOLVE RIGHT; // удаление
        addScriptedFollows(is(aggClass), addScriptedJProp(whereExpr.getLP(), groupProps), Collections.singletonList(new PropertyFollowsDebug(false, exprDebugPoint)), Event.APPLY, null);

        return new LPContextIndependent(lcp, resultSignature, Collections.<Integer>emptyList());
    }

    public LPWithParams addScriptedMaxProp(List<LPWithParams> paramProps, boolean isMin) throws ScriptingErrorLog.SemanticErrorException {
        if (isMin) {
            return addScriptedUProp(Union.MIN, paramProps, "MIN");
        } else {
            return addScriptedUProp(Union.MAX, paramProps, "MAX");
        }
    }

    private LPWithParams addScriptedUProp(Union unionType, List<LPWithParams> paramProps, String errMsgPropType) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkPropertyTypes(paramProps, errMsgPropType);

        int[] coeffs = null;
        if (unionType == Union.SUM) {
            coeffs = new int[paramProps.size()];
            for (int i = 0; i < coeffs.length; i++) {
                coeffs[i] = 1;
            }
        }
        List<Object> resultParams = getParamsPlainList(paramProps);
        LP prop = addUProp(null, LocalizedString.NONAME, unionType, null, coeffs, resultParams.toArray());
        return new LPWithParams(prop, mergeAllParams(paramProps));
    }

    public LPWithParams addScriptedPartitionProp(PartitionType partitionType, NamedPropertyUsage ungroupPropUsage, boolean strict, int precision, boolean isAscending,
                                                 boolean useLast, int groupPropsCnt, List<LPWithParams> paramProps, List<TypedParameter> context) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkPartitionWindowConsistence(partitionType, useLast);
        LP ungroupProp = ungroupPropUsage != null ? findLPByPropertyUsage(ungroupPropUsage, paramProps.subList(0, groupPropsCnt), context) : null;
        checks.checkPartitionUngroupConsistence(ungroupProp, groupPropsCnt);

        boolean ordersNotNull = doesExtendContext(0, paramProps.subList(0, groupPropsCnt + 1), paramProps.subList(groupPropsCnt + 1, paramProps.size()));

        List<Object> resultParams = getParamsPlainList(paramProps);
        List<Integer> usedParams = mergeAllParams(paramProps);
        LP prop;
        if (partitionType == PartitionType.SUM || partitionType == PartitionType.PREVIOUS) {
            prop = addOProp(null, false, LocalizedString.NONAME, partitionType, isAscending, ordersNotNull, useLast, groupPropsCnt, resultParams.toArray());
        } else if (partitionType == PartitionType.DISTR_CUM_PROPORTION) {
            List<ResolveClassSet> contextClasses = getClassesFromTypedParams(context);// для не script - временный хак
            // может быть внешний context
            List<ResolveClassSet> explicitInnerClasses = new ArrayList<>();
            for(int usedParam : usedParams)
                explicitInnerClasses.add(contextClasses.get(usedParam)); // one-based;
            prop = addPGProp(null, false, precision, strict, LocalizedString.NONAME, usedParams.size(), explicitInnerClasses, isAscending, ordersNotNull, ungroupProp, resultParams.toArray());
        } else {
            prop = addUGProp(null, false, strict, LocalizedString.NONAME, usedParams.size(), isAscending, ordersNotNull, ungroupProp, resultParams.toArray());
        }
        return new LPWithParams(prop, usedParams);
    }

    public LPWithParams addScriptedCCProp(List<LPWithParams> params) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(addCCProp(params.size()), params);
    }

    public LPWithParams addScriptedConcatProp(String separator, List<LPWithParams> params) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(addSFUProp(params.size(), separator), params);
    }

    public LPWithParams addScriptedDCCProp(LPWithParams ccProp, int index) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDeconcatenateIndex(ccProp, index);
        return addScriptedJProp(addDCCProp(index - 1), Collections.singletonList(ccProp));
    }

    public LP addScriptedSFProp(String typeName, List<SQLSyntaxType> types, List<String> texts, boolean hasNotNull) throws ScriptingErrorLog.SemanticErrorException {
        assert types.size() == texts.size();
        checks.checkSingleImplementation(types);

        Set<Integer> params = findFormulaParameters(texts.get(0));

        for (String text : texts) {
            Set<Integer> formulaParams = findFormulaParameters(text);
            checks.checkFormulaParameters(formulaParams);
            if (formulaParams.size() != params.size()) {
                errLog.emitFormulaDifferentParamCountError(parser);
            }
        }

        String defaultFormula = "";
        MExclMap<SQLSyntaxType, String> mSyntaxes = MapFact.mExclMap();
        for (int i = 0; i < types.size(); i++) {
            SQLSyntaxType type = types.get(i);
            String text = transformFormulaText(texts.get(i), StringFormulaProperty.getParamName("$1"));
            if (type == null) {
                defaultFormula = text;
            } else {
                mSyntaxes.exclAdd(type, text);
            }
        }
        CustomFormulaSyntax formula = new CustomFormulaSyntax(defaultFormula, mSyntaxes.immutable());
        if (typeName != null) {
            ValueClass cls = findClass(typeName);
            checks.checkFormulaClass(cls);
            return addSFProp(formula, (DataClass) cls, params.size(), hasNotNull);
        } else {
            return addSFProp(formula, params.size(), hasNotNull);
        }
    }

    private Set<Integer> findFormulaParameters(String text) {
        Set<Integer> params = new HashSet<>();
        if(text != null) {
            Pattern pattern = Pattern.compile("\\$\\d+");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String group = matcher.group();
                int paramNumber = Integer.valueOf(group.substring(1));
                params.add(paramNumber);
            }
        }
        return params;
    }

    public static String transformFormulaText(String text, String textTo) { // так как $i не постфиксный (например $1 и $12)
        return text != null ? text.replaceAll("\\$(\\d+)", textTo) : null;
    }

    public LPWithParams addScriptedRProp(List<TypedParameter> context, LPWithParams zeroStep, LPWithParams nextStep, Cycle cycleType) throws ScriptingErrorLog.SemanticErrorException {
        List<Integer> usedParams = mergeAllParams(asList(zeroStep, nextStep));
        checks.checkRecursionContext(getParamNamesFromTypedParams(context), usedParams);

        MOrderExclSet<Integer> mMainParams = SetFact.mOrderExclSetMax(usedParams.size());
        Map<Integer, Integer> usedToResult = new HashMap<>();
        for (int i = 0; i < usedParams.size(); i++) {
            if (!context.get(usedParams.get(i)).paramName.startsWith("$")) {
                mMainParams.exclAdd(i);
                usedToResult.put(usedParams.get(i), i);
            }
        }
        ImOrderSet<Integer> mainParams = mMainParams.immutableOrder();

        Map<Integer, Integer> mapPrev = new HashMap<>();
        for (int i = 0; i < usedParams.size(); i++) {
            String param = context.get(usedParams.get(i)).paramName; // usedParams и context orderSet / revMap'ы
            if (param.startsWith("$")) {
                int index = 0;
                for (TypedParameter tparam : context)  {
                    if (tparam.paramName.equals(param.substring(1))) {
                        break;
                    }
                    ++index;
                }
                mapPrev.put(i, usedToResult.get(index));
            }
        }

        List<Object> resultParams = getParamsPlainList(Arrays.asList(zeroStep, nextStep));
        LP res = addRProp(null, false, LocalizedString.NONAME, cycleType, mainParams, MapFact.fromJavaRevMap(mapPrev), resultParams.toArray());

        List<Integer> resUsedParams = new ArrayList<>();
        for (Integer usedParam : usedParams) {
            if (!context.get(usedParam).paramName.startsWith("$")) {
                resUsedParams.add(usedParam);
            }
        }
        return new LPWithParams(res, resUsedParams);
    }

    private static StringClass getStringConstClass(LocalizedString value) {
        if(value.needToBeLocalized())
            return StringClass.text;
        return StringClass.getv(new ExtInt(value.getSourceString().length()));
    }

    public LP addConstantProp(ConstType type, Object value) throws ScriptingErrorLog.SemanticErrorException {
        switch (type) {
            case INT: return addUnsafeCProp(IntegerClass.instance, value);
            case LONG: return addUnsafeCProp(LongClass.instance, value);
            case NUMERIC: return addNumericConst((String) value);
            case REAL: return addUnsafeCProp(DoubleClass.instance, value);
            case STRING: return addUnsafeCProp(getStringConstClass((LocalizedString)value), value);
            case LOGICAL: return addUnsafeCProp(LogicalClass.instance, value);
            case DATE: return addUnsafeCProp(DateClass.instance, value);
            case DATETIME: return addUnsafeCProp(DateTimeClass.instance, value);
            case TIME: return addUnsafeCProp(TimeClass.instance, value);
            case STATIC: return addStaticClassConst((String) value);
            case COLOR: return addUnsafeCProp(ColorClass.instance, value);
            case NULL: return baseLM.vnull;
        }
        return null;
    }

    private LP addNumericConst(String value) {
        return addUnsafeCProp(NumericClass.get(value.length(), value.length() - value.indexOf('.') - 1), new BigDecimal(value));
    }

    public Color createScriptedColor(int r, int g, int b) throws ScriptingErrorLog.SemanticErrorException {
        if (r > 255 || g > 255 || b > 255) {
            errLog.emitColorComponentValueError(parser);
        }
        return new Color(r, g, b);
    }

    public int createScriptedInteger(String s) throws ScriptingErrorLog.SemanticErrorException {
        int res = 0;
        try {
            res = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            errLog.emitIntegerValueError(parser);
        }
        return res;
    }

    public long createScriptedLong(String s) throws ScriptingErrorLog.SemanticErrorException {
        long res = 0;
        try {
            res = Long.parseLong(s);
        } catch (NumberFormatException e) {
            errLog.emitLongValueError(parser);
        }
        return res;
    }

    public double createScriptedDouble(String s) throws ScriptingErrorLog.SemanticErrorException {
        double res = 0;
        try {
            res = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            errLog.emitDoubleValueError(parser);
        }
        return res;
    }

    private void validateDate(int y, int m, int d) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkRange("year component", y, 1900, 9999);
        checks.checkRange("month component", m, 1, 12);
        checks.checkRange("day component", d, 1, 31);

        final List<Integer> longMonth = Arrays.asList(1, 3, 5, 7, 8, 10, 12);
        if (d == 31 && !longMonth.contains(m) ||
            d == 30 && m == 2 ||
            d == 29 && m == 2 && (y % 4 != 0 || y % 100 == 0 && y % 400 != 0))
        {
            errLog.emitDateDayError(parser, y, m, d);
        }

    }

    private void validateTime(int h, int m) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkRange("hour component", h, 0, 23);
        checks.checkRange("minute component", m, 0, 59);
    }

    private void validateDateTime(int y, int m, int d, int h, int mn) throws ScriptingErrorLog.SemanticErrorException {
        validateDate(y, m, d);
        validateTime(h, mn);
    }

    public java.sql.Date dateLiteralToDate(String text) throws ScriptingErrorLog.SemanticErrorException {
        int y = Integer.parseInt(text.substring(0, 4));
        int m = Integer.parseInt(text.substring(5, 7));
        int d = Integer.parseInt(text.substring(8, 10));
        validateDate(y, m, d);
        return new java.sql.Date(y - 1900, m - 1, d);
    }

    public Timestamp dateTimeLiteralToTimestamp(String text) throws ScriptingErrorLog.SemanticErrorException {
        int y = Integer.parseInt(text.substring(0, 4));
        int m = Integer.parseInt(text.substring(5, 7));
        int d = Integer.parseInt(text.substring(8, 10));
        int h = Integer.parseInt(text.substring(11, 13));
        int mn = Integer.parseInt(text.substring(14, 16));
        validateDateTime(y, m, d, h, mn);
        return new Timestamp(y - 1900, m - 1, d, h, mn, 0, 0);
    }

    public Time timeLiteralToTime(String text) throws ScriptingErrorLog.SemanticErrorException {
        int h = Integer.parseInt(text.substring(0, 2));
        int m = Integer.parseInt(text.substring(3, 5));
        validateTime(h, m);
        return new Time(h, m, 0);
    }

    public <O extends ObjectSelector> LAWithParams addScriptedShowFAProp(MappedForm<O> mapped, List<FormActionProps> allObjectProps,
                                                                         Boolean syncType, WindowFormType windowType, ManageSessionType manageSession, FormSessionScope formSessionScope,
                                                                         boolean checkOnOk, Boolean noCancel, boolean readonly) throws ScriptingErrorLog.SemanticErrorException {
        List<O> allObjects = mapped.objects;
        MList<O> mObjects = ListFact.mList(allObjects.size());
        List<LPWithParams> mapping = new ArrayList<>();
        MList<Boolean> mNulls = ListFact.mList(allObjects.size());
        for (int i = 0; i < allObjects.size(); i++) {
            O object = allObjects.get(i);
            FormActionProps objectProp = allObjectProps.get(i);
            assert objectProp.in != null;
            mObjects.add(object);
            mapping.add(objectProp.in);
            mNulls.add(objectProp.inNull);
            assert !objectProp.out && !objectProp.constraintFilter;
        }

        if(syncType == null)
            syncType = true;
        if(windowType == null) {
            if(syncType)
                windowType = WindowFormType.FLOAT;
            else
                windowType = WindowFormType.DOCKED;
        }

        LA property = addIFAProp(null, LocalizedString.NONAME, mapped.form, mObjects.immutableList(), mNulls.immutableList(),
                                 manageSession, noCancel,
                                 syncType, windowType, false, checkOnOk,
                                 readonly);

        property = addSessionScopeAProp(formSessionScope, property);

        if (mapping.size() > 0) {
            return addScriptedJoinAProp(property, mapping);
        } else {
            return new LAWithParams(property, new ArrayList<Integer>());
        }
    }

    private LP<?> getInputProp(NamedPropertyUsage targetProp, ValueClass valueClass, Set<Property> usedProps) throws ScriptingErrorLog.SemanticErrorException {
        if(targetProp != null) {
            LP<?> result = findLPNoParamsByPropertyUsage(targetProp);
            usedProps.add(result.property);
            return result;
        }

        if(valueClass instanceof DataClass) {
            LP<?> requested = baseLM.getRequestedValueProperty().getLCP((DataClass)valueClass);
            if(usedProps == null || usedProps.add(requested.property))
                return requested;
        }
        // уже был или Object - генерим новое
        return new LP<>(PropertyFact.createInputDataProp(valueClass));
    }

    public <O extends ObjectSelector> LAWithParams addScriptedDialogFAProp(
            MappedForm<O> mapped, List<FormActionProps> allObjectProps,
            WindowFormType windowType, ManageSessionType manageSession, FormSessionScope scope,
            boolean checkOnOk, Boolean noCancel, boolean readonly, LAWithParams doAction, LAWithParams elseAction, List<TypedParameter> oldContext, List<TypedParameter> newContext) throws ScriptingErrorLog.SemanticErrorException {

        List<O> allObjects = mapped.objects;
        MList<O> mInputObjects = ListFact.mListMax(allObjects.size());
        MList<Boolean> mInputNulls = ListFact.mListMax(allObjects.size());
        MList<LP> mInputProps = ListFact.mListMax(allObjects.size());

        MList<O> mObjects = ListFact.mListMax(allObjects.size());
        List<LPWithParams> mapping = new ArrayList<>();
        MList<Boolean> mNulls = ListFact.mListMax(allObjects.size());

        MList<Pair<LPWithParams, DebugInfo.DebugPoint>> mAssignProps = ListFact.mListMax(allObjects.size());

        MList<O> mContextObjects = ListFact.mListMax(allObjects.size() + 1);
        MList<Property> mContextProps = ListFact.mListMax(allObjects.size() + 1);
        List<LPWithParams> contextLPs = new ArrayList<>();

        Set<Property> usedProps = new HashSet<>();

        for (int i = 0; i < allObjects.size(); i++) {
            O object = allObjects.get(i);
            FormActionProps objectProp = allObjectProps.get(i);
            if (objectProp.in != null) {
                mObjects.add(object);
                mapping.add(objectProp.in);
                mNulls.add(objectProp.inNull);
            }
            if (objectProp.out) {
                mInputObjects.add(object);
                mInputNulls.add(objectProp.outNull);
                LP<?> outProp = getInputProp(objectProp.outProp, mapped.form.getBaseClass(object), usedProps);
                mInputProps.add(outProp);

                LPWithParams changeProp = null;
                if(objectProp.constraintFilter || objectProp.assign) {
                    changeProp = objectProp.changeProp;
                    if(changeProp == null)
                        changeProp = objectProp.in;
                    assert changeProp != null;
                }
                if(objectProp.constraintFilter) {
                    mContextObjects.add(object);
                    mContextProps.add(changeProp.getLP().property);
                    contextLPs.add(changeProp);
                }

                Pair<LPWithParams, DebugInfo.DebugPoint> assignProp = null;
                if(objectProp.assign)
                    assignProp = new Pair<>(changeProp, objectProp.assignDebugPoint);
                mAssignProps.add(assignProp);
            }
        }
        ImList<O> inputObjects = mInputObjects.immutableList();
        ImList<Boolean> inputNulls = mInputNulls.immutableList();
        ImList<LP> inputProps = mInputProps.immutableList();

        ImList<Pair<LPWithParams, DebugInfo.DebugPoint>> assignProps = mAssignProps.immutableList();

        ImList<O> contextObjects = mContextObjects.immutableList();
        ImList<Property> contextProps = mContextProps.immutableList();

        if(windowType == null) {
            if (!inputObjects.isEmpty())
                windowType = WindowFormType.DIALOG;
            else
                windowType = WindowFormType.FLOAT;
        }

        List<LPWithParams> propParams = new ArrayList<>();
        propParams.addAll(contextLPs);
        List<Integer> allParams = mergeAllParams(propParams);

        ImList<O> objects = mObjects.immutableList();
        LA property = addIFAProp(null, LocalizedString.NONAME, mapped.form, objects, mNulls.immutableList(),
                                 inputObjects, inputProps, inputNulls,
                                 manageSession, noCancel,
                                 contextObjects, contextProps,
                true, windowType, false, checkOnOk,
                readonly);

        property = addSessionScopeAProp(scope, property, inputProps.addList(baseLM.getRequestCanceledProperty()).getCol());

        LAWithParams formAction;
        if (mapping.size() > 0) { // тут надо contextLPs просто в mapping закинуть по идее сразу
            for(LPWithParams contextLP : contextLPs)
                for (int usedParam : contextLP.usedParams) {
                    mapping.add(new LPWithParams(usedParam));
                }
            formAction = addScriptedJoinAProp(property, mapping);
        } else {
            formAction = new LAWithParams(property, allParams);
        }

        return proceedInputDoClause(doAction, elseAction, oldContext, newContext, inputProps, formAction, assignProps);
    }

    private LAWithParams proceedInputDoClause(LAWithParams doAction, LAWithParams elseAction, List<TypedParameter> oldContext, List<TypedParameter> newContext, ImList<LP> inputParamProps, LAWithParams proceedAction, ImList<Pair<LPWithParams, DebugInfo.DebugPoint>> assignProps) throws ScriptingErrorLog.SemanticErrorException {
        if (doAction != null) {
            doAction = extendDoParams(doAction, newContext, oldContext.size(), false, inputParamProps, null, assignProps);
            return addScriptedRequestAProp(proceedAction, doAction, elseAction);
        }

        return proceedAction;
    }

    private LAWithParams proceedImportDoClause(boolean noParams, LAWithParams doAction, LAWithParams elseAction, List<TypedParameter> oldContext, List<TypedParameter> newContext, LP<?> whereLCP, ImList<LP> importParamProps, ImList<Boolean> nulls, LAWithParams proceedAction) throws ScriptingErrorLog.SemanticErrorException {
        if (doAction != null) {
            assert nulls != null;

            int paramOld = oldContext.size() + (!noParams ? 1 : 0);
            if(paramOld == newContext.size()) // хак, потом можно будет красивее сделать
                importParamProps = SetFact.EMPTYORDER();

            List<LAWithParams> actions = new ArrayList<>();
            actions.add(proceedAction);

            LAWithParams fillNullsAction = fillImportNullsAction(noParams, paramOld, oldContext, newContext, whereLCP, importParamProps, nulls);
            if(fillNullsAction != null)
                actions.add(fillNullsAction);

            actions.add(extendImportDoAction(noParams, paramOld, oldContext, newContext, doAction, elseAction, whereLCP, importParamProps, nulls));

            LAWithParams listAction = addScriptedListAProp(actions, Collections.<LP>emptyList());
            // хак - в ifAProp оборачиваем что delegationType был AFTER_DELEGATE, а не BEFORE или null, вообще по хорошему надо delegationType в момент parsing'а проставлять, а не в самих свойствах
            return addScriptedIfAProp(new LPWithParams(baseLM.vtrue), listAction, null);
        }
        return proceedAction;
    }

    private LAWithParams extendImportDoAction(boolean noParams, int paramOld, List<TypedParameter> oldContext, List<TypedParameter> newContext, LAWithParams doAction, LAWithParams elseAction, LP<?> whereLCP, ImList<LP> importParamProps, ImList<Boolean> nulls) throws ScriptingErrorLog.SemanticErrorException {
        ImList<Pair<LPWithParams, DebugInfo.DebugPoint>> assignProps = ListFact.toList(importParamProps.size(), new GetIndex<Pair<LPWithParams, DebugInfo.DebugPoint>>() {
            public Pair<LPWithParams, DebugInfo.DebugPoint> getMapValue(int i) {
                return null;
            }});
        doAction = extendDoParams(doAction, newContext, paramOld, !noParams, importParamProps, nulls, assignProps); // row parameter consider to be external (it will be proceeded separately)
        if(!noParams) { // adding row parameter
            modifyContextFlowActionDefinitionBodyCreated(doAction, BaseUtils.add(oldContext, newContext.get(oldContext.size())), oldContext, false);

            doAction = addScriptedForAProp(oldContext, new LPWithParams(whereLCP, oldContext.size()), Collections.singletonList(new LPWithParams(oldContext.size())), doAction,
                    elseAction, null, null, false, false, false, Collections.<LPWithParams>emptyList(), false);
        }
        return doAction;
    }

    // filling null values if necessary
    private LAWithParams fillImportNullsAction(boolean noParams, int paramOld, List<TypedParameter> oldContext, List<TypedParameter> newContext, LP<?> whereLCP, ImList<LP> importParamProps, ImList<Boolean> nulls) throws ScriptingErrorLog.SemanticErrorException {
        List<Integer> params = !noParams ? Collections.<Integer>singletonList(oldContext.size()) : Collections.<Integer>emptyList();
        List<TypedParameter> oldAndRowContext = newContext.subList(0, paramOld);
        List<LAWithParams> fillNulls = null;
        for(int i=paramOld;i<newContext.size();i++) {
            int importIndex = i - paramOld;
            if (!nulls.get(importIndex)) { // no null
                if (fillNulls == null)
                    fillNulls = new ArrayList<>();

                LPWithParams importProp = new LPWithParams(importParamProps.get(importIndex), params);
                DataClass cls = (DataClass) newContext.get(i).cls;
                LPWithParams defaultValueProp = new LPWithParams(addCProp(cls, PropertyFact.getValueForProp(cls.getDefaultValue(), cls)));
                // prop(row) <- defvalue WHERE NOT prop(row)
                fillNulls.add(addScriptedAssignAProp(oldAndRowContext, defaultValueProp, addScriptedNotProp(importProp), importProp));
            }
        }
        if(fillNulls != null) {
            LAWithParams fillNullsAction = addScriptedListAProp(fillNulls, Collections.<LP>emptyList());
            if(!noParams) // FOR where(row)
                fillNullsAction = addScriptedForAProp(oldContext, new LPWithParams(noParams ? baseLM.vtrue : whereLCP, params), Collections.<LPWithParams>emptyList(), fillNullsAction,
                    null, null, null, false, false, false, Collections.<LPWithParams>emptyList(), false);
            return fillNullsAction;
        }
        return null;
    }

//    private int findOldParam(List<TypedParameter> params, ImList<Integer> inputParams, Result<ImList<LP>> rInputParamProps) throws ScriptingErrorLog.SemanticErrorException {
//        ImOrderSet<Integer> paramsSet = inputParams.toOrderExclSet();
//        MList<LP> mInputParamProps = ListFact.mList(inputParams.size());
//        int paramOld = params.size() - inputParams.size();
//        for(int i = params.size()-1; i >= paramOld; i--) {
//            int paramIndex = paramsSet.indexOf(i);
//            if(paramIndex < 0) 
//                errLog.emitExtendParamUsage(parser, params.get(i).paramName);
//            
//            mInputParamProps.add(rInputParamProps.result.get(paramIndex));            
//        }
//        rInputParamProps.set(mInputParamProps.immutableList().reverseList());
//        return paramOld;
//    }

    private LAWithParams nullExec(LAWithParams doAction, int param) throws ScriptingErrorLog.SemanticErrorException {
        List<LPWithParams> params = new ArrayList<>();
        boolean found = false;
        for(int usedParam : doAction.usedParams)
            if(usedParam == param){
                found = true;
                params.add(new LPWithParams(baseLM.vnull));
            } else
                params.add(new LPWithParams(usedParam));

        if(!found) // не было использований
            return null;
        return addScriptedJoinAProp(doAction.getLP(), params);
    }

    // recursive
    private LAWithParams extendDoParams(LAWithParams doAction, List<TypedParameter> context, int paramOld, boolean isLastParamRow, ImList<LP> resultProps, ImList<Boolean> nulls, ImList<Pair<LPWithParams, DebugInfo.DebugPoint>> assignProps) throws ScriptingErrorLog.SemanticErrorException {
        assert context.size() - paramOld == resultProps.size();
        assert resultProps.size() == assignProps.size();

        List<TypedParameter> currentContext = new ArrayList<>(context);
        int paramNum;
        while((paramNum = currentContext.size() - 1) >= paramOld) {
            // remove'им параметр
            List<TypedParameter> removedContext = BaseUtils.remove(currentContext, paramNum);

            LPWithParams paramLP = new LPWithParams(paramNum);
            Pair<LPWithParams, DebugInfo.DebugPoint> assignLP = assignProps.get(paramNum - paramOld);
            if(assignLP != null) {
                LAWithParams assignAction = addScriptedAssignAProp(currentContext, paramLP, null, assignLP.first);

                ScriptingLogicsModule.setDebugInfo(null, assignLP.second, assignAction.getLP().property);

                doAction = addScriptedListAProp(Arrays.asList(assignAction, doAction), new ArrayList<LP>());
            }

            boolean paramNoNull = nulls != null && !nulls.get(paramNum - paramOld);
            LAWithParams nullExec = paramNoNull ? null : nullExec(doAction, paramNum); // передает NULL в качестве параметра
            if(paramNoNull || nullExec != null) { // нет параметра нет проблемы
                modifyContextFlowActionDefinitionBodyCreated(doAction, currentContext, removedContext, false);

                LP resultProp = resultProps.get(paramNum - paramOld);
                LPWithParams resultLP = isLastParamRow ? new LPWithParams(resultProp, paramOld - 1) : new LPWithParams(resultProp);

                doAction = addScriptedForAProp(removedContext, addScriptedEqualityProp("==", paramLP, resultLP, currentContext), new ArrayList<LPWithParams>(), doAction,
                        nullExec, null, null, false, false, false, isLastParamRow ? Collections.<LPWithParams>emptyList() : null, false);
            }

            currentContext = removedContext;
        }

        return doAction;
    }

    public <O extends ObjectSelector> LAWithParams addScriptedPrintFAProp(MappedForm<O> mapped, List<FormActionProps> allObjectProps,
                                                                          LPWithParams printerProperty, FormPrintType printType, NamedPropertyUsage propUsage,
                                                                          Boolean syncType, Integer selectTop, NamedPropertyUsage sheetNamePropUsage, LPWithParams passwordProperty) throws ScriptingErrorLog.SemanticErrorException {
        assert printType != null;
        List<O> allObjects = mapped.objects;
        MList<O> mObjects = ListFact.mList(allObjects.size());
        List<LPWithParams> mapping = new ArrayList<>();
        MList<Boolean> mNulls = ListFact.mList(allObjects.size());
        for (int i = 0; i < allObjects.size(); i++) {
            O object = allObjects.get(i);
            FormActionProps objectProp = allObjectProps.get(i);
            assert objectProp.in != null;
            mObjects.add(object);
            mapping.add(objectProp.in);
            mNulls.add(objectProp.inNull);
            assert !objectProp.out && !objectProp.constraintFilter;
        }

        if(syncType == null)
            syncType = false;

        //использования printerProperty и passwordProperty не пересекаются, поэтому параметры не разделяем
        List<LPWithParams> propParams = new ArrayList<>();
        if(printerProperty != null) {
            propParams.add(printerProperty);
        }
        if(passwordProperty != null) {
            propParams.add(passwordProperty);
        }
        List<Integer> allParams = mergeAllParams(propParams);

        LP<?> targetProp = null;
        if(propUsage != null)
            targetProp = findLPNoParamsByPropertyUsage(propUsage);

        LP<?> sheetNameProperty = null;
        if(sheetNamePropUsage != null)
            sheetNameProperty = findLPNoParamsByPropertyUsage(sheetNamePropUsage);

        LA property = addPFAProp(null, LocalizedString.NONAME, mapped.form, mObjects.immutableList(), mNulls.immutableList(),
                printerProperty != null ? printerProperty.getLP().property : null, sheetNameProperty, printType, syncType, selectTop,
                passwordProperty != null ? passwordProperty.getLP().property : null, targetProp, false);

        if (mapping.size() > 0)  { // тут надо printerProperty просто в mapping закинуть по идее сразу
            if(printerProperty != null) {
                for (int usedParam : printerProperty.usedParams) {
                    mapping.add(new LPWithParams(usedParam));
                }
            }
            if(passwordProperty != null) {
                for (int usedParam : passwordProperty.usedParams) {
                    mapping.add(new LPWithParams(usedParam));
                }
            }
            return addScriptedJoinAProp(property, mapping);
        } else {
            return new LAWithParams(property, allParams);
        }
    }

    public <O extends ObjectSelector> LAWithParams addScriptedExportFAProp(MappedForm<O> mapped, List<FormActionProps> allObjectProps, FormIntegrationType exportType,
                                                                           LPWithParams rootProperty, LPWithParams tagProperty, boolean attr, boolean noHeader,
                                                                           String separator, boolean noEscape, Integer selectTop, String charset, NamedPropertyUsage propUsage,
                                                                           OrderedMap<GroupObjectEntity, NamedPropertyUsage> propUsages) throws ScriptingErrorLog.SemanticErrorException {
        if(exportType == null)
            exportType = FormIntegrationType.JSON;

        List<O> allObjects = mapped.objects;
        MList<O> mObjects = ListFact.mList(allObjects.size());
        List<LPWithParams> mapping = new ArrayList<>();
        MList<Boolean> mNulls = ListFact.mList(allObjects.size());
        for (int i = 0; i < allObjects.size(); i++) {
            O object = allObjects.get(i);
            FormActionProps objectProp = allObjectProps.get(i);
            assert objectProp.in != null;
            mObjects.add(object);
            mapping.add(objectProp.in);
            mNulls.add(objectProp.inNull);
            assert !objectProp.out && !objectProp.constraintFilter;
        }


        List<LAPWithParams> propParams = new ArrayList<>();
        List<Integer> allParams = mergeAllParams(propParams);

        LP<?> singleExportFile = null;
        MExclMap<GroupObjectEntity, LP> exportFiles = MapFact.mExclMap();
        if(exportType.isPlain()) {
            if(propUsages != null) {
                for (Map.Entry<GroupObjectEntity, NamedPropertyUsage> entry : propUsages.entrySet()) {
                    exportFiles.exclAdd(entry.getKey(), findLPNoParamsByPropertyUsage(entry.getValue()));
                }
            } else if (propUsage != null) {
                errLog.emitSimpleError(parser, String.format("EXPORT %s TO single file not supported", exportType));
            } else {
                errLog.emitSimpleError(parser, "Output file(s) for export not specified");
            }
        } else {
            if(propUsages != null) {
                errLog.emitSimpleError(parser, String.format("EXPORT %s TO multiple files not supported", exportType));
            } else {
                singleExportFile = propUsage != null ? findLPNoParamsByPropertyUsage(propUsage) : baseLM.exportFile;
            }
        }

        if(rootProperty != null) {
            errLog.emitSimpleError(parser, "EXPORT form with ROOT not supported");
        }
        if(tagProperty != null) {
            errLog.emitSimpleError(parser, "EXPORT form with TAG not supported");
        }
        if(attr) {
            errLog.emitSimpleError(parser, "EXPORT form with ATTR not supported");
        }

        LA property = addEFAProp(null, LocalizedString.NONAME, mapped.form, mObjects.immutableList(), mNulls.immutableList(),
                exportType, noHeader, separator, noEscape, selectTop, charset, null, null, singleExportFile, exportFiles.immutable());

        if (mapping.size() > 0) {
            return addScriptedJoinAProp(property, mapping);
        } else {
            return new LAWithParams(property, allParams);
        }
    }

    public GroupObjectEntity findGroupObjectEntity(FormEntity form, String objectName) throws ScriptingErrorLog.SemanticErrorException {
        GroupObjectEntity result = form.getNFGroupObject(objectName, getVersion());
        if (result == null) {
            errLog.emitGroupObjectNotFoundError(parser, objectName);
        }
        return result;
    }

    public ObjectEntity findObjectEntity(FormEntity form, String objectName) throws ScriptingErrorLog.SemanticErrorException {
        ObjectEntity result = form.getNFObject(objectName, getVersion());
        if (result == null) {
            errLog.emitObjectNotFoundError(parser, objectName);
        }
        return result;
    }

    public void addScriptedMetaCodeFragment(String name, List<String> params, List<String> tokens, List<Pair<Integer, Boolean>> metaTokens, String code, int lineNumber) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDuplicateMetaCodeFragment(name, params.size());
        checks.checkDistinctParameters(params);

        MetaCodeFragment fragment = new MetaCodeFragment(elementCanonicalName(name), params, tokens, metaTokens, code, getName(), lineNumber);
        addMetaCodeFragment(fragment);
    }

    public void runMetaCode(String name, List<String> params, int lineNumber, boolean enabledMeta) throws RecognitionException {
        MetaCodeFragment metaCode = findMetaCodeFragment(name, params.size());
        checks.checkMetaCodeParamCount(metaCode, params.size());

        String code = metaCode.getCode(params);
        parser.runMetaCode(this, code, metaCode, MetaCodeFragment.metaCodeCallString(name, metaCode, params), lineNumber, enabledMeta);
    }

    public Pair<List<String>, List<Pair<Integer, Boolean>>> grabMetaCode(String metaCodeName) throws ScriptingErrorLog.SemanticErrorException {
        return parser.grabMetaCode(metaCodeName);
    }

    public List<String> grabJavaCode() throws ScriptingErrorLog.SemanticErrorException {
        return parser.grabJavaCode();
    }

    private LP addStaticClassConst(String name) throws ScriptingErrorLog.SemanticErrorException {
        int pointPos = name.lastIndexOf('.');
        assert pointPos > 0;

        String className = name.substring(0, pointPos);
        String instanceName = name.substring(pointPos + 1);
        LP resultProp = null;

        ValueClass cls = findClass(className);
        if (cls instanceof ConcreteCustomClass) {
            ConcreteCustomClass concreteClass = (ConcreteCustomClass) cls;
            if (concreteClass.hasStaticObject(instanceName)) { //, versionб так как отдельным шагом парсится
                resultProp = addCProp(concreteClass, instanceName);
            } else {
                errLog.emitNotFoundError(parser, "static оbject", instanceName);
            }
        } else {
            errLog.emitAbstractClassInstancesUseError(parser, className, instanceName);
        }
        return resultProp;
    }

    public void throwAlreadyDefinePropertyDraw(FormEntity.AlreadyDefined alreadyDefined) throws ScriptingErrorLog.SemanticErrorException {
        getErrLog().emitAlreadyDefinedPropertyDraw(getParser(), alreadyDefined.formCanonicalName, alreadyDefined.newSID, alreadyDefined.formPath);
    }

    public LP addScriptedGroupObjectProp(String name, GroupObjectProp prop, List<ResolveClassSet> outClasses) throws ScriptingErrorLog.SemanticErrorException {
        int pointPos = name.lastIndexOf('.');
        assert pointPos > 0;

        String formName = name.substring(0, pointPos);
        String objectName = name.substring(pointPos+1);
        LP resultProp = null;

        FormEntity form = findForm(formName);

        GroupObjectEntity groupObject = form.getNFGroupObject(objectName, getVersion());
        if (groupObject != null) {
            for (ObjectEntity obj : groupObject.getOrderObjects()) {
                outClasses.add(obj.getResolveClassSet());
            }
            resultProp = addGroupObjectProp(groupObject, prop);
        } else {
            errLog.emitNotFoundError(parser, "group оbject", objectName);
        }
        return resultProp;
    }


    public LP addScriptedReflectionProperty(ReflectionPropertyType type, ActionOrPropertyUsage propertyUsage, List<ResolveClassSet> outClasses) throws ScriptingErrorLog.SemanticErrorException {
        switch (type) {
            case CANONICAL_NAME:
            default: return addCanonicalNameProp(propertyUsage);
        }
    }

    public LP addCanonicalNameProp(ActionOrPropertyUsage propertyUsage) throws ScriptingErrorLog.SemanticErrorException {
        return new LP<>(new CanonicalNameProperty(findLPByActionOrPropertyUsage(propertyUsage)));
    }

    public LAWithParams addScriptedFocusAction(PropertyDrawEntity property) {
        return new LAWithParams(addFocusAction(property), new ArrayList<Integer>());
    }

    public LAWithParams addScriptedReadAction(LPWithParams sourcePathProp, NamedPropertyUsage propUsage, List<TypedParameter> params, boolean clientAction, boolean dialog) throws ScriptingErrorLog.SemanticErrorException {
        ValueClass sourceProp = getValueClassByParamProperty(sourcePathProp, params);
        LP<?> targetProp = propUsage == null ? baseLM.readFile : findLPNoParamsByPropertyUsage(propUsage);
        return addScriptedJoinAProp(addAProp(new ReadAction(sourceProp, targetProp, clientAction, dialog)), Collections.singletonList(sourcePathProp));
    }

    public LAWithParams addScriptedWriteAction(LPWithParams sourceProp, LPWithParams pathProp, List<TypedParameter> params, boolean clientAction, boolean dialog, boolean append) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJoinAProp(addAProp(new WriteAction(getTypeByParamProperty(sourceProp, params),
                clientAction, dialog, append, getValueClassByParamProperty(sourceProp, params), getValueClassByParamProperty(pathProp, params))),
                Arrays.asList(sourceProp, pathProp));
    }

    public ImList<Type> getTypesForExportProp(List<LPWithParams> paramProps, List<TypedParameter> params) {
        return getTypesByParamProperties(paramProps, params);
    }

    public LAWithParams addScriptedExportAction(List<TypedParameter> oldContext, FormIntegrationType type, final List<String> ids, List<Boolean> literals,
                                                List<LPWithParams> exprs, LPWithParams whereProperty, NamedPropertyUsage fileProp, LPWithParams rootProperty, LPWithParams tagProperty,
                                                String separator, boolean noHeader, boolean noEscape, Integer selectTop, String charset, boolean attr,
                                                List<LPWithParams> orderProperties, List<Boolean> orderDirections) throws ScriptingErrorLog.SemanticErrorException {

        LP<?> targetProp = fileProp != null ? findLPNoParamsByPropertyUsage(fileProp) : null;
        if(targetProp == null)
            targetProp = baseLM.exportFile;

        List<String> exIds = new ArrayList<>(ids);
        List<Boolean> exLiterals = new ArrayList<Boolean>(literals);

        MOrderExclMap<String, Boolean> mOrders = MapFact.mOrderExclMap(orderProperties.size());
        for (int i = 0; i < orderProperties.size(); i++) {
            LPWithParams orderProperty = orderProperties.get(i);
            exprs.add(orderProperty);
            String orderId = "order" + exIds.size();
            exIds.add(orderId);
            exLiterals.add(false);
            mOrders.exclAdd(orderId, orderDirections.get(i));
        }
        ImOrderMap<String, Boolean> orders = mOrders.immutableOrder();

        List<LPWithParams> props = exprs;
        if(whereProperty != null)
            props = BaseUtils.add(exprs, whereProperty);

        List<Integer> resultInterfaces = getResultInterfaces(oldContext.size(), props.toArray(new LAPWithParams[exprs.size()+1]));

        if(type == null)
            type = FormIntegrationType.JSON;
//            type = doesExtendContext(oldContext.size(), new ArrayList<LAPWithParams>(), props) ? FormIntegrationType.JSON : FormIntegrationType.TABLE;

        List<LAPWithParams> paramsList = new ArrayList<>();
        for (int resI : resultInterfaces) {
            paramsList.add(new LPWithParams(resI));
        }
        paramsList.addAll(exprs);
        if (whereProperty != null) {
            paramsList.add(whereProperty);
        }
        if(rootProperty != null) {
            paramsList.add(rootProperty);
        }
        if(tagProperty != null) {
            paramsList.add(tagProperty);
        }

//        ImList<Type> exprTypes = getTypesForExportProp(exprs, newContext);

        List<Object> resultParams = getParamsPlainList(paramsList);
        LA result = null;
        try {
            result = addExportPropertyAProp(LocalizedString.NONAME, type, resultInterfaces.size(), exIds, exLiterals, orders, targetProp,
                    whereProperty != null, rootProperty != null ? rootProperty.getLP().property : null, tagProperty != null ? tagProperty.getLP().property : null,
                    separator, noHeader, noEscape, selectTop, charset, attr, resultParams.toArray());
        } catch (FormEntity.AlreadyDefined alreadyDefined) {
            throwAlreadyDefinePropertyDraw(alreadyDefined);
        }
        return new LAWithParams(result, resultInterfaces);
    }

    // always from LAPWithParams where usedParams is ordered set
    public static ImOrderSet<String> getUsedNames(List<TypedParameter> context, List<Integer> usedParams) {
        MOrderExclSet<String> mResult = SetFact.mOrderExclSet(usedParams.size());
        for (int usedIndex : usedParams) {
            mResult.exclAdd(context.get(usedIndex).paramName);
        }
        return mResult.immutableOrder();
    }

    public static List<ResolveClassSet> getUsedClasses(List<TypedParameter> context, List<Integer> usedParams) {
        List<ResolveClassSet> usedClasses = new ArrayList<>();
        for (int usedIndex : usedParams) {
            ValueClass cls = context.get(usedIndex).cls;
            if(cls == null)
                usedClasses.add(null);
            else
                usedClasses.add(cls.getResolveSet());
        }
        return usedClasses;
    }

    public LAWithParams addScriptedNewThreadAction(LAWithParams action, LPWithParams connectionProp, LPWithParams periodProp, LPWithParams delayProp) throws ScriptingErrorLog.SemanticErrorException {
        List<LAPWithParams> propParams = BaseUtils.<LAPWithParams>toList(action);
        if (periodProp != null) {
            propParams.add(periodProp);
        }
        if (delayProp != null) {
            propParams.add(delayProp);
        }
        if (connectionProp != null) {
            propParams.add(connectionProp);
        }
        List<Integer> allParams = mergeAllParams(propParams);
        LA<?> property = addNewThreadAProp(null, LocalizedString.NONAME, connectionProp != null, periodProp != null, delayProp != null, getParamsPlainList(propParams).toArray());
        return new LAWithParams(property, allParams);
    }

    public LAWithParams addScriptedNewExecutorAction(LAWithParams action, LPWithParams threadsProp) throws ScriptingErrorLog.SemanticErrorException {
        List<LAPWithParams> propParams = Arrays.asList(action, threadsProp);
        List<Integer> allParams = mergeAllParams(propParams);
        LA<?> property = addNewExecutorAProp(null, LocalizedString.NONAME, getParamsPlainList(propParams).toArray());
        return new LAWithParams(property, allParams);
    }

    private ImList<LP> findLPsForImport(List<NamedPropertyUsage> propUsages, ImList<ValueClass> paramClasses) throws ScriptingErrorLog.SemanticErrorException {
        MList<LP> mProps = ListFact.mList(propUsages.size());
        for (NamedPropertyUsage propUsage : propUsages) {
            mProps.add(findLPParamByPropertyUsage(propUsage, paramClasses));
        }
        return mProps.immutableList();
    }

    private ImList<LP> genLPsForImport(List<TypedParameter> oldContext, List<TypedParameter> newContext, ImList<ValueClass> paramClasses) throws ScriptingErrorLog.SemanticErrorException {
        int size=newContext.size() - oldContext.size() - paramClasses.size();

        MList<LP> mResult = ListFact.mList(size);
        for(int i=size-1;i>=0;i--)
            mResult.add(new LP<>(PropertyFact.createImportDataProp(newContext.get(newContext.size() - 1 - i).cls, paramClasses)));
        return mResult.immutableList();
    }

    private ImList<LP> findLPsIntegerParamByPropertyUsage(List<NamedPropertyUsage> propUsages) throws ScriptingErrorLog.SemanticErrorException {
        MList<LP> mProps = ListFact.mList(propUsages.size());
        for (NamedPropertyUsage propUsage : propUsages) {
            mProps.add(findLPIntegerParamByPropertyUsage(propUsage));
        }
        return mProps.immutableList();
    }

    private LP findLPIntegerParamByPropertyUsage(NamedPropertyUsage propUsage) throws ScriptingErrorLog.SemanticErrorException {
        return findLPParamByPropertyUsage(propUsage, ListFact.singleton((ValueClass) IntegerClass.instance));
    }

    private LP findLPStringParamByPropertyUsage(NamedPropertyUsage propUsage) throws ScriptingErrorLog.SemanticErrorException {
        return findLPParamByPropertyUsage(propUsage, ListFact.singleton((ValueClass) StringClass.text));
    }

    private LP findLPParamByPropertyUsage(NamedPropertyUsage propUsage, ImList<ValueClass> valueClasses) throws ScriptingErrorLog.SemanticErrorException {
        if (propUsage.classNames == null) {
            propUsage.classNames = new ArrayList<>();
            for (ValueClass valueClass : valueClasses) {
                propUsage.classNames.add(valueClass.getParsedName());
            }
        }
        LP<?> lcp = findLPByPropertyUsage(propUsage);
        ValueClass[] paramClasses = lcp.getInterfaceClasses(ClassType.signaturePolicy);
        if (paramClasses.length != valueClasses.size()) {
            errLog.emitPropertyWithParamsExpectedError(getParser(), propUsage.name, getParamClasses(valueClasses));
        } else {
            for (int i = 0; i < paramClasses.length; i++) {
                ValueClass paramClass = paramClasses[i];
                ValueClass valueClass = valueClasses.get(i);
                if (!valueClass.isCompatibleParent(paramClass) && !paramClass.isCompatibleParent(valueClass)) {
                    errLog.emitPropertyWithParamsExpectedError(getParser(), propUsage.name, getParamClasses(valueClasses));
                }
            }
        }
        return lcp;
    }

    private String getParamClasses(ImList<ValueClass> valueClasses) {
        List<ResolveClassSet> signature = new ArrayList<>();
        for(ValueClass valueClass : valueClasses) {
            signature.add(valueClass.getResolveSet());
        }
        return PropertyCanonicalNameUtils.createSignature(signature);
    }

    private FormIntegrationType adjustImportFormatFromFileType(FormIntegrationType format, LPWithParams fileProp, OrderedMap<GroupObjectEntity, LPWithParams> fileProps, List<TypedParameter> context) {
        if(format == null) {
            if(fileProps != null && !fileProps.isEmpty())
                fileProp = fileProps.values().iterator().next();
            Type type = getTypeByParamProperty(fileProp, context);
            if(type instanceof StaticFormatFileClass)
                return ((StaticFormatFileClass)type).getIntegrationType();
        }
        return format;
    }

    public LAWithParams addScriptedImportAction(FormIntegrationType format, LPWithParams fileProp, List<String> ids, List<Boolean> literals, List<NamedPropertyUsage> propUsages,
                                                List<Boolean> nulls, LAWithParams doAction, LAWithParams elseAction, List<TypedParameter> context, List<TypedParameter> newContext,
                                                NamedPropertyUsage wherePropertyUsage, LPWithParams sheet, boolean sheetAll, String separator, boolean noHeader, boolean noEscape,
                                                String charset, LPWithParams root, List<TypedParameter> fieldParams, List<String> toParamClasses, boolean attr,
                                                LPWithParams whereProp, LPWithParams memoProp) throws ScriptingErrorLog.SemanticErrorException {

        if(fileProp == null)
            fileProp = new LPWithParams(baseLM.importFile);

        if(toParamClasses != null && toParamClasses.size() > 1) {
            errLog.emitSimpleError(parser, "IMPORT TO/FIELDS params with multiple classes not supported");
        }

        format = adjustImportFormatFromFileType(format, fileProp, null, context);

        ImList<LP> props;
        ImList<ValueClass> paramClasses;
        if(fieldParams != null) { // FIELDS
            paramClasses = getValueClassesFromTypedParams(fieldParams);
            props = genLPsForImport(context, newContext, paramClasses);
        } else { // TO
            paramClasses = findClasses(toParamClasses);
            props = findLPsForImport(propUsages, paramClasses);
        }

        boolean noParams = paramClasses.isEmpty();

        LP<?> whereLCP;
        if(fieldParams != null) { // FIELDS
            assert wherePropertyUsage == null;
            whereLCP = !noParams ? new LP<>(PropertyFact.createImportDataProp(LogicalClass.instance, paramClasses)) : null;
        } else { // TO
            if(wherePropertyUsage != null)
                whereLCP = findLPByPropertyUsage(wherePropertyUsage);
            else
                whereLCP = findLPByPropertyUsage(new NamedPropertyUsage("imported", toParamClasses), false, true);
        }

        List<LPWithParams> params = new ArrayList<>();
        params.add(fileProp);
        if(root != null)
            params.add(root);
        if(whereProp != null)
            params.add(whereProp);
        if(memoProp != null)
            params.add(memoProp);
        if(sheet != null)
            params.add(sheet);

        LA importAction = null;
        try {
            importAction = addImportPropertyAProp(format, params.size(), ids, literals, paramClasses, whereLCP, separator, noHeader, noEscape, charset, sheetAll, attr, whereProp != null, getUParams(props.toArray(new LP[props.size()])));
        } catch (FormEntity.AlreadyDefined alreadyDefined) {
            throwAlreadyDefinePropertyDraw(alreadyDefined);
        }
        return proceedImportDoClause(noParams, doAction, elseAction, context, newContext, whereLCP, props, nulls != null ? ListFact.fromJavaList(nulls) : null, addScriptedJoinAProp(importAction, params));
    }

    public ImList<ValueClass> findClasses(List<String> classNames) throws ScriptingErrorLog.SemanticErrorException {
        MList<ValueClass> mResult = ListFact.mList(classNames.size()); // exception 
        for(String className : classNames)
            mResult.add(findClass(className));
        return mResult.immutableList();
    }

    public LAWithParams addScriptedImportFormAction(FormIntegrationType format, List<TypedParameter> context, LPWithParams fileProp, OrderedMap<GroupObjectEntity, LPWithParams> fileProps,
                                                    FormEntity formEntity, LPWithParams sheet, boolean sheetAll, boolean noHeader, boolean noEscape, boolean attr, String charset, String separator,
                                                    LPWithParams rootProp, LPWithParams whereProp, LPWithParams memoProp) throws ScriptingErrorLog.SemanticErrorException {
        format = adjustImportFormatFromFileType(format, fileProp, fileProps, context);

        List<LPWithParams> params = new ArrayList<>();
        boolean hasFileProps = fileProps != null && !fileProps.isEmpty();
        boolean isPlain = format != null ? format.isPlain() : hasFileProps;
        if(isPlain) {
            if(hasFileProps) {
                for(LPWithParams fProp : fileProps.values()) {
                    checks.checkImportFromFileExpression(fProp);
                    params.add(fProp);
                }
            } else if(fileProp != null) {
                errLog.emitSimpleError(parser, String.format("IMPORT %s FROM single file not supported", format));
            } else {
                errLog.emitSimpleError(parser, "Input file(s) for import not specified");
            }
        } else {
            if(hasFileProps) {
                errLog.emitSimpleError(parser, String.format("IMPORT %s FROM multiple files not supported", format));
            } else {
                if (fileProp == null) fileProp = new LPWithParams(baseLM.importFile);
                checks.checkImportFromFileExpression(fileProp);
                params.add(fileProp);
            }
        }

        if(attr)
            errLog.emitSimpleError(parser, "IMPORT form with ATTR not supported");
        if(whereProp != null)
            errLog.emitSimpleError(parser, "IMPORT form with WHERE not supported");
        if(memoProp != null)
            errLog.emitSimpleError(parser, "IMPORT form with MEMO not supported");

        if(rootProp != null)
            params.add(rootProp);
        if(sheet != null)
            params.add(sheet);

        ImOrderSet<GroupObjectEntity> groupFiles = fileProps != null ? SetFact.fromJavaOrderSet(fileProps.keyList()) : SetFact.<GroupObjectEntity>EMPTYORDER();
        return addScriptedJoinAProp(addImportFAProp(format, formEntity, params.size(), groupFiles, sheetAll, separator, noHeader, noEscape, charset, whereProp != null), params);
    }

    public LP addTypeProp(ValueClass valueClass, boolean bIs) throws ScriptingErrorLog.SemanticErrorException {
        if (bIs) {
            return is(valueClass);
        } else {
            return object(valueClass);
        }
    }

    public LPWithParams addScriptedTypeProp(LPWithParams ccProp, String className, boolean bIs) throws ScriptingErrorLog.SemanticErrorException {
        return addScriptedJProp(addTypeProp(findClass(className), bIs), Collections.singletonList(ccProp));
    }

    public void addScriptedConstraint(LP<?> property, Event event, boolean checked, List<NamedPropertyUsage> propUsages, LP<?> messageProperty, DebugInfo.DebugPoint debugPoint) throws ScriptingErrorLog.SemanticErrorException {
        if (!property.property.checkAlwaysNull(true)) {
            errLog.emitConstraintPropertyAlwaysNullError(parser);
        }
        ImSet<Property<?>> checkedProps = null;
        Property.CheckType type = (checked ? Property.CheckType.CHECK_ALL : Property.CheckType.CHECK_NO);
        if (checked && propUsages != null) {
            MSet<Property<?>> mCheckedProps = SetFact.mSet();
            for (NamedPropertyUsage propUsage : propUsages) {
                LP<?> lcp = findLPByPropertyUsage(propUsage);
                mCheckedProps.add(lcp.property);
            }
            type = Property.CheckType.CHECK_SOME;
            checkedProps = mCheckedProps.immutable();
        }
        addConstraint(property, messageProperty, type, checkedProps, event, this, debugPoint);
    }

    private PrevScope prevScope = null;
    public void setPrevScope(Event event) {
        setPrevScope(event.getScope());
    }

    public void dropPrevScope(Event event) {
        dropPrevScope(event.getScope());
    }

    // по сути оптимизация - когда контекст глобального события использовать в операторах изменений PrevScope.DB
    public void setPrevScope(PrevScope scope) {
        assert prevScope == null;
        prevScope = scope;
    }

    public void dropPrevScope(PrevScope scope) {
        assert prevScope.equals(scope);
        prevScope = null;
    }

    public LPWithParams addScriptedSessionProp(IncrementType type, LPWithParams property) {
        LP newProp;
        PrevScope scope = (type == null ? PrevScope.DB : (prevScope != null ? prevScope : PrevScope.EVENT)); // по сути оптимизация если scope известен использовать его
        if (type == null) {
            newProp = addOldProp(property.getLP(), scope);
        } else {
            newProp = addCHProp(property.getLP(), type, scope);
        }
        return new LPWithParams(newProp, property);
    }

    public LPWithParams addScriptedSignatureProp(LPWithParams property) throws ScriptingErrorLog.SemanticErrorException {
        LP newProp = addClassProp(property.getLP());
        return new LPWithParams(newProp, property);
    }

    public LPWithParams addScriptedActiveTabProp(ComponentView component) throws ScriptingErrorLog.SemanticErrorException {
        return new LPWithParams(new LP<>(component.getActiveTab().property));
    }

    public void addScriptedFollows(NamedPropertyUsage mainPropUsage, List<TypedParameter> namedParams, List<PropertyFollowsDebug> resolveOptions, LPWithParams rightProp, Event event, DebugInfo.DebugPoint debugPoint) throws ScriptingErrorLog.SemanticErrorException {
        LP mainProp = findLPByPropertyUsage(mainPropUsage, namedParams);
        checks.checkParamCount(mainProp, namedParams.size());
        checks.checkDistinctParameters(getParamNamesFromTypedParams(namedParams));

        addScriptedFollows(mainProp, rightProp, resolveOptions, event, debugPoint);
    }

    private void addScriptedFollows(LP mainProp, LPWithParams rightProp, List<PropertyFollowsDebug> resolveOptions, Event event, DebugInfo.DebugPoint debugPoint) {
        Integer[] params = new Integer[rightProp.usedParams.size()];
        for (int j = 0; j < params.length; j++) {
            params[j] = rightProp.usedParams.get(j) + 1;
        }
        follows(mainProp, debugPoint, ListFact.fromJavaList(resolveOptions), event, rightProp.getLP(), params);
    }

    public void addScriptedWriteWhen(NamedPropertyUsage mainPropUsage, List<TypedParameter> namedParams, LPWithParams valueProp, LPWithParams whenProp, boolean action) throws ScriptingErrorLog.SemanticErrorException {
        LP mainProp = findLPByPropertyUsage(mainPropUsage, namedParams);
        if (!(mainProp.property instanceof DataProperty)) {
            errLog.emitOnlyDataPropertyIsAllowedError(parser, mainPropUsage.name);
        }
        checks.checkParamCount(mainProp, namedParams.size());
        checks.checkDistinctParameters(getParamNamesFromTypedParams(namedParams));

        List<Object> params = getParamsPlainList(asList(valueProp, whenProp));
        mainProp.setEventChange(this, action, params.toArray());
    }

    public Set<Property> findPropsByPropertyUsages(List<NamedPropertyUsage> propUsages) throws ScriptingErrorLog.SemanticErrorException {
        if(propUsages==null)
            return null;

        Set<Property> props = new HashSet<>(); // функционально из-за exception'а не сделаешь
        for (NamedPropertyUsage usage : propUsages) {
            LP<?> lp = findLPByPropertyUsage(usage);
            props.add(lp.property);
        }
        return props;
    }

    public void addScriptedEvent(LPWithParams whenProp, LAWithParams event, List<LPWithParams> orders, boolean descending, Event baseEvent, List<LPWithParams> noInline, boolean forceInline, DebugInfo.DebugPoint debugPoint) throws ScriptingErrorLog.SemanticErrorException {
        if(noInline==null) {
            noInline = new ArrayList<>();
            for(Integer usedParam : whenProp.usedParams)
                noInline.add(new LPWithParams(usedParam));
        }
        List<Object> params = getParamsPlainList(asList(event, whenProp), orders, noInline);
        addEventAction(baseEvent, descending, false, noInline.size(), forceInline, debugPoint, params.toArray());
    }

    public void addScriptedGlobalEvent(LAWithParams event, Event baseEvent, boolean single, ActionOrPropertyUsage showDep) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkEventNoParameters(event.getLP());
        Action action = event.getLP().property;
        if(showDep!=null)
            action.showDep = findLPByActionOrPropertyUsage(showDep).property;
        addBaseEvent(action, baseEvent, false, single);
    }

    public void addScriptedShowDep(ActionOrPropertyUsage property, ActionOrPropertyUsage propFrom) throws ScriptingErrorLog.SemanticErrorException {
        findLPByActionOrPropertyUsage(property).property.showDep = findLPByActionOrPropertyUsage(propFrom).property;
    }

    public void addScriptedAspect(NamedPropertyUsage mainPropUsage, List<TypedParameter> mainPropParams, LAWithParams action, boolean before) throws ScriptingErrorLog.SemanticErrorException {
        LA mainProp = findLAByPropertyUsage(mainPropUsage, mainPropParams);
        checks.checkParamCount(mainProp, mainPropParams.size());
        checks.checkDistinctParameters(getParamNamesFromTypedParams(mainPropParams));

        LA<PropertyInterface> mainActionLP = (LA<PropertyInterface>) mainProp;

        List<Object> params = getParamsPlainList(Collections.singletonList(action));
        ImList<ActionMapImplement<?, PropertyInterface>> actionImplements = readActionImplements(mainActionLP.listInterfaces, params.toArray());
        addAspectEvent(mainActionLP.property, actionImplements.get(0), before);
    }

    public void addScriptedTable(String name, List<String> classIds, boolean isFull, boolean isExplicit) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDuplicateTable(name);

        // todo [dale]: Hack. Class CustomObjectClass is created after all in InitObjectClassTask 
        boolean isCustomObjectClassTable = isCustomObjectClassTable(name, classIds);

        ValueClass[] classes = new ValueClass[classIds.size()];
        if (!isCustomObjectClassTable(name, classIds)) {
            for (int i = 0; i < classIds.size(); i++) {
                classes[i] = findClass(classIds.get(i));
            }
        }
        
        tempTables.add(new TemporaryTableInfo(name, classes, isFull, isExplicit, isCustomObjectClassTable));
    }

    private boolean isCustomObjectClassTable(String name, List<String> classIds) {
        return classIds.size() == 1 && classIds.get(0).equals("CustomObjectClass");
    }
    
    private void addScriptedTables() {
        for (TemporaryTableInfo info : tempTables) {
            ValueClass[] classes = info.classes;
            if (info.isCustomObjectClassTable) {
                classes = new ValueClass[] {baseLM.baseClass.objectClass};
            }
            addTable(info.name, info.isFull, info.isExplicit, classes);
        }
        tempTables.clear(); 
    } 
    
    private List<TemporaryTableInfo> tempTables = new ArrayList<>();
    
    private static class TemporaryTableInfo {
        public final String name;
        public final ValueClass[] classes;
        public final boolean isFull, isExplicit;
        public final boolean isCustomObjectClassTable;
        
        public TemporaryTableInfo(String name, ValueClass[] classes, boolean isFull, boolean isExplicit, boolean isCustomObjectClassTable) {
            this.name = name;
            this.classes = classes;
            this.isFull = isFull;
            this.isExplicit = isExplicit;
            this.isCustomObjectClassTable = isCustomObjectClassTable;  
        }
    }  
    
    private List<LP> indexedProperties = new ArrayList<>();
    private List<TemporaryIndexInfo> tempIndicies = new ArrayList<>();
            
    public void addScriptedIndex(LP lp) {
        indexedProperties.add(lp);

        ImSet<StoredDataProperty> fullAggrProps;
        if(lp.property instanceof AggregateGroupProperty && (fullAggrProps = ((AggregateGroupProperty) lp.property).getFullAggrProps()) != null) {
            for(StoredDataProperty fullAggrProp : fullAggrProps)
                indexedProperties.add(new LP<>(fullAggrProp));
        }
    }

    public LPWithParams findIndexProp(NamedPropertyUsage toPropertyUsage, List<LPWithParams> toPropertyMapping, List<TypedParameter> context) throws ScriptingErrorLog.SemanticErrorException {
        LP toPropertyLP = findLPByPropertyUsage(toPropertyUsage, toPropertyMapping, context);
        return new LPWithParams(toPropertyLP, getParamsAssertList(toPropertyMapping));
    }

    public void addScriptedIndex(List<TypedParameter> params, List<LPWithParams> lps) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkIndexNecessaryProperty(lps);
        checks.checkStoredProperties(lps);
        checks.checkDistinctParametersList(lps);
        checks.checkIndexNumberOfParameters(params.size(), lps);
        ImOrderSet<String> keyNames = ListFact.fromJavaList(params).toOrderExclSet().mapOrderSetValues(new GetValue<String, TypedParameter>() {
            public String getMapValue(TypedParameter value) {
                return value.paramName;
            }});
        tempIndicies.add(new TemporaryIndexInfo(keyNames, getParamsPlainList(lps).toArray()));
    }

    private static class TemporaryIndexInfo {
        public ImOrderSet<String> keyNames;
        public Object[] params;
        
        public TemporaryIndexInfo(ImOrderSet<String> keyNames, Object[] params) {
            this.keyNames = keyNames;
            this.params = params;
        }
    }

    public void addScriptedLoggable(List<NamedPropertyUsage> propUsages) throws ScriptingErrorLog.SemanticErrorException {
        for (NamedPropertyUsage propUsage : propUsages) {
            LP lp = findLPByPropertyUsage(propUsage);
            lp.makeLoggable(this, BL.systemEventsLM);
        }
    }

    public void addScriptedWindow(WindowType type, String name, LocalizedString captionStr, NavigatorWindowOptions options) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkDuplicateWindow(name);

        LocalizedString caption = (captionStr == null ? LocalizedString.create(name) : captionStr);
        NavigatorWindow window = null;

        switch (type) {
            case MENU:
                window = createMenuWindow(name, caption, options);
                break;
            case PANEL:
                window = createPanelWindow(name, caption, options);
                break;
            case TOOLBAR:
                window = createToolbarWindow(name, caption, options);
                break;
            case TREE:
                window = createTreeWindow(name, caption, options);
                break;
        }

        window.drawRoot = nvl(options.getDrawRoot(), false);
        window.drawScrollBars = nvl(options.getDrawScrollBars(), true);
        window.titleShown = nvl(options.getDrawTitle(), true);

        addWindow(window);
    }

    private MenuNavigatorWindow createMenuWindow(String name, LocalizedString caption, NavigatorWindowOptions options) throws ScriptingErrorLog.SemanticErrorException {
        Orientation orientation = options.getOrientation();
        DockPosition dp = options.getDockPosition();
        if (dp == null) {
            errLog.emitWindowPositionNotSpecified(parser, name);
        }
        assert dp != null;
        MenuNavigatorWindow window = new MenuNavigatorWindow(elementCanonicalName(name), caption, dp.x, dp.y, dp.width, dp.height);
        window.orientation = orientation.asMenuOrientation();

        return window;
    }

    private PanelNavigatorWindow createPanelWindow(String name, LocalizedString caption, NavigatorWindowOptions options) {
        Orientation orientation = options.getOrientation();
        DockPosition dockPosition = options.getDockPosition();

        if (orientation == null) {
            orientation = Orientation.VERTICAL;
        }

        PanelNavigatorWindow window = new PanelNavigatorWindow(elementCanonicalName(name), caption, orientation.asToolbarOrientation());
        if (dockPosition != null) {
            window.setDockPosition(dockPosition.x, dockPosition.y, dockPosition.width, dockPosition.height);
        }
        return window;
    }

    private ToolBarNavigatorWindow createToolbarWindow(String name, LocalizedString caption, NavigatorWindowOptions options) throws ScriptingErrorLog.SemanticErrorException {
        Orientation orientation = options.getOrientation();
        BorderPosition borderPosition = options.getBorderPosition();
        DockPosition dockPosition = options.getDockPosition();

        if (orientation == null) {
            orientation = Orientation.VERTICAL;
        }

        if (borderPosition != null && dockPosition != null) {
            errLog.emitWindowPositionConflict(parser, name);
        }

        ToolBarNavigatorWindow window;
        if (borderPosition != null) {
            window = new ToolBarNavigatorWindow(orientation.asToolbarOrientation(), elementCanonicalName(name), caption, borderPosition.asLayoutConstraint());
        } else if (dockPosition != null) {
            window = new ToolBarNavigatorWindow(orientation.asToolbarOrientation(), elementCanonicalName(name), caption, dockPosition.x, dockPosition.y, dockPosition.width, dockPosition.height);
        } else {
            window = new ToolBarNavigatorWindow(orientation.asToolbarOrientation(), elementCanonicalName(name), caption);
        }

        Alignment hAlign = options.getHAlign();
        Alignment vAlign = options.getVAlign();
        Alignment thAlign = options.getTextHAlign();
        Alignment tvAlign = options.getTextVAlign();
        if (hAlign != null) {
            window.alignmentX = asHorizontalToolbarAlign(hAlign);
        }
        if (vAlign != null) {
            window.alignmentY = asVerticalToolbarAlign(vAlign);
        }
        if (thAlign != null) {
            window.horizontalTextPosition = asHorizontalTextPosition(thAlign);
        }
        if (tvAlign != null) {
            window.verticalTextPosition = asVerticalTextPosition(tvAlign);
        }
        return window;
    }

    private TreeNavigatorWindow createTreeWindow(String name, LocalizedString caption, NavigatorWindowOptions options) {
        TreeNavigatorWindow window = new TreeNavigatorWindow(elementCanonicalName(name), caption);
        DockPosition dp = options.getDockPosition();
        if (dp != null) {
            window.setDockPosition(dp.x, dp.y, dp.width, dp.height);
        }
        return window;
    }


    public void hideWindow(String name) throws ScriptingErrorLog.SemanticErrorException {
        findWindow(name).visible = false;
    }

    public static class NavigatorElementOptions {
        public String imagePath;
        public NavigatorElement anchor;
        public InsertPosition position;
        public String windowName;
    }

    public NavigatorElement createScriptedNavigatorElement(String name, LocalizedString caption, DebugInfo.DebugPoint point,
                                                           NamedPropertyUsage actionUsage, String formName, boolean isAction) throws ScriptingErrorLog.SemanticErrorException {
        LA<?> action = null;
        FormEntity form = null;
        if (formName != null) {
            form = findForm(formName);
        }
        if (actionUsage != null) {
            if(!isAction)
                form = findForm(actionUsage.name, true);
            if(form == null)
                action = findNavigatorAction(actionUsage);
        }

        if (name == null) {
            name = createDefaultNavigatorElementName(action, form);
        }

        checks.checkNavigatorElementName(name);
        checks.checkDuplicateNavigatorElement(name);

        if (caption == null) {
            caption = createDefaultNavigatorElementCaption(action, form);
            if (caption == null) {
                caption = LocalizedString.create(name);
            }
        }

        return createNavigatorElement(elementCanonicalName(name), caption, point, action, form);
    }

    private String createDefaultNavigatorElementName(LA<?> action, FormEntity form) {
        if (action != null) {
            return action.property.getName();
        } else if (form != null) {
            return form.getName();
        }
        return null;
    }

    private LocalizedString createDefaultNavigatorElementCaption(LA<?> action, FormEntity form) {
        if (action != null) {
            return action.property.caption;
        } else if (form != null) {
            return form.getCaption();
        }
        return null;
    }

    private NavigatorElement createNavigatorElement(String canonicalName, LocalizedString caption, DebugInfo.DebugPoint point, LA<?> action, FormEntity form) throws ScriptingErrorLog.SemanticErrorException {
        NavigatorElement newElement;
        if (form != null) {
            newElement = addNavigatorForm(form, canonicalName, caption);
        } else if (action != null) {
            newElement = addNavigatorAction(action, canonicalName, caption);
        } else {
            newElement = addNavigatorFolder(canonicalName, caption);
        }
        newElement.setDebugPoint(point);
        return newElement;
    }

    private LA<?> findNavigatorAction(NamedPropertyUsage actionUsage) throws ScriptingErrorLog.SemanticErrorException {
        assert actionUsage != null;
        if (actionUsage.classNames == null) {
            actionUsage.classNames = Collections.emptyList();
        }
        LA<?> action = findLANoParamsByPropertyUsage(actionUsage);
        checks.checkNavigatorAction(action);
        return action;
    }

    public void setupNavigatorElement(NavigatorElement element, LocalizedString caption, NavigatorElement parentElement, NavigatorElementOptions options, boolean isEditOperation) throws ScriptingErrorLog.SemanticErrorException {
        if (caption != null) {
            element.caption = caption;
        }

        applyNavigatorElementOptions(element, parentElement, options, isEditOperation);
    }

    public void applyNavigatorElementOptions(NavigatorElement element, NavigatorElement parent, NavigatorElementOptions options, boolean isEditOperation) throws ScriptingErrorLog.SemanticErrorException {
        setNavigatorElementWindow(element, options.windowName);
        setNavigatorElementImage(element, parent, options.imagePath);

        if (parent != null && (!isEditOperation || options.position != InsertPosition.IN)) {
            moveElement(element, parent, options.position, options.anchor, isEditOperation);
        }
    }

    private void moveElement(NavigatorElement element, NavigatorElement parentElement, InsertPosition pos, NavigatorElement anchorElement, boolean isEditOperation) throws ScriptingErrorLog.SemanticErrorException {
        Version version = getVersion();
        checks.checkNavigatorElementMoveOperation(element, parentElement, anchorElement, isEditOperation, version);

        switch (pos) {
            case IN:    parentElement.add(element, version); break;
            case BEFORE:parentElement.addBefore(element, anchorElement, version); break;
            case AFTER: parentElement.addAfter(element, anchorElement, version); break;
            case FIRST: parentElement.addFirst(element, version); break;
        }
    }

    public void setNavigatorElementWindow(NavigatorElement element, String windowName) throws ScriptingErrorLog.SemanticErrorException {
        assert element != null;

        if (windowName != null) {
            AbstractWindow window = findWindow(windowName);

            if (window instanceof NavigatorWindow) {
                element.window = (NavigatorWindow) window;
            } else {
                errLog.emitAddToSystemWindowError(parser, windowName);
            }
        }
    }

    public void setNavigatorElementImage(NavigatorElement element, NavigatorElement parent, String imagePath) throws ScriptingErrorLog.SemanticErrorException {
        if (imagePath != null) {
            element.setImage(imagePath);
        } else if (element.defaultIcon != null) {
            if (baseLM.root != null && parent != null && baseLM.root.equals(parent)) {
                element.setImage(element.defaultIcon == DefaultIcon.ACTION ? "/images/actionTop.png" :
                        element.defaultIcon == DefaultIcon.OPEN ? "/images/openTop.png" : "/images/formTop.png");
            }
            element.defaultIcon = null;
        }
    }

    public LPWithParams propertyExpressionCreated(LPWithParams property, List<TypedParameter> context, boolean needFullContext) {
        if (needFullContext) 
            property = patchExtendParams(property, context, Collections.<TypedParameter>emptyList());
        return property;
    } 
    public void propertyDefinitionCreated(LP<?> property, DebugInfo.DebugPoint point) {
        if(property != null) { // can be null if property is param
            Property prop = property.property;
            boolean needToCreateDelegate = debugger.isEnabled() && point.needToCreateDelegate() && prop instanceof DataProperty;
            if (prop.getDebugInfo() == null) { // при использовании в propertyExpression оптимизированных join свойств, не нужно им переустанавливать DebugInfo
                PropertyDebugInfo debugInfo = new PropertyDebugInfo(point, needToCreateDelegate);
                if (needToCreateDelegate) {
                    debugger.addDelegate(debugInfo);
                }
                prop.setDebugInfo(debugInfo);
            }
        }
    }

    public void actionDefinitionBodyCreated(LAWithParams lpWithParams, DebugInfo.DebugPoint startPoint, DebugInfo.DebugPoint endPoint, boolean modifyContext, Boolean needToCreateDelegate) throws ScriptingErrorLog.SemanticErrorException {
        if (lpWithParams.getLP() != null) {
            setDebugInfo(lpWithParams, startPoint, endPoint, modifyContext, needToCreateDelegate);
        }
    }

    public static void setDebugInfo(LAWithParams lpWithParams, DebugInfo.DebugPoint startPoint, DebugInfo.DebugPoint endPoint, boolean modifyContext, Boolean needToCreateDelegate) {
        //noinspection unchecked
        LA<PropertyInterface> lAction = (LA<PropertyInterface>) lpWithParams.getLP();
        Action property = lAction.property;
        setDebugInfo(needToCreateDelegate, startPoint, endPoint, modifyContext, property);
    }

    public static void setDebugInfo(Boolean needToCreateDelegate, DebugInfo.DebugPoint point, Action property) {
        setDebugInfo(needToCreateDelegate, point, point, false, property);
    }

    private static void setDebugInfo(Boolean needToCreateDelegate, DebugInfo.DebugPoint startPoint, DebugInfo.DebugPoint endPoint, boolean modifyContext, Action property) {
        ActionDelegationType delegationType = property.getDelegationType(modifyContext);

        if(needToCreateDelegate == null)
            needToCreateDelegate = startPoint.needToCreateDelegate();

        if (debugger.isEnabled() && needToCreateDelegate && delegationType != null) {
            DebugInfo.DebugPoint typePoint = delegationType.getDebugPoint(startPoint, endPoint);
            ActionDebugInfo info = new ActionDebugInfo(startPoint, typePoint.line, typePoint.offset, delegationType);
            debugger.addDelegate(info);
            property.setDebugInfo(info);
        } else {
            property.setDebugInfo(new ActionDebugInfo(startPoint, delegationType, false));
        }
    }

    public void topContextActionDefinitionBodyCreated(LAWithParams lpWithParams) throws ScriptingErrorLog.SemanticErrorException {
        boolean isDebug = debugger.isEnabled();

        if(isDebug) {
            //noinspection unchecked
            LA<PropertyInterface> lAction = (LA<PropertyInterface>) lpWithParams.getLP();

            Action property = lAction.property;

            debugger.setNewDebugStack(property);
        }
    }

    public LAWithParams modifyContextFlowActionDefinitionBodyCreated(LAWithParams lpWithParams,
                                                                     List<TypedParameter> newContext, List<TypedParameter> oldContext,
                                                                     boolean needFullContext) throws ScriptingErrorLog.SemanticErrorException {
        boolean isDebug = debugger.isEnabled();

        if(isDebug || needFullContext) {
            lpWithParams = patchExtendParams(lpWithParams, newContext, oldContext);
        }

        if (isDebug) {
            //noinspection unchecked
            LA<PropertyInterface> lAction = (LA<PropertyInterface>) lpWithParams.getLP();

            Action property = lAction.property;

            Map<String, PropertyInterface> paramsToInterfaces = new HashMap<>();
            Map<String, String> paramsToClassFQN = new HashMap<>();

            for (int i = 0; i < lpWithParams.usedParams.size(); i++) {
                int usedParam = lpWithParams.usedParams.get(i);
                if(usedParam >= oldContext.size()) { // если новый параметр
                    TypedParameter param = newContext.get(usedParam);

                    paramsToInterfaces.put(param.paramName, lAction.listInterfaces.get(i));
                    paramsToClassFQN.put(param.paramName, param.getParsedName());
                }
            }

            debugger.addParamInfo(property, paramsToInterfaces, paramsToClassFQN);
        }

        return lpWithParams;
    }

    // assert that newContext extends oldContext (at least there is such assertion in addScriptedForAProp)
    private LAWithParams patchExtendParams(LAWithParams lpWithParams, List<TypedParameter> newContext, List<TypedParameter> oldContext) {

        if(!lpWithParams.getLP().listInterfaces.isEmpty() && lpWithParams.usedParams.isEmpty()) {
            return lpWithParams;
        }

        Set<Integer> usedExtendParams = new HashSet<>();
        for (int i = 0; i < lpWithParams.usedParams.size(); i++) {
            Integer usedParam = lpWithParams.usedParams.get(i);
            if(usedParam >= oldContext.size()) {
                usedExtendParams.add(usedParam);
            }
        }

        if(usedExtendParams.size() == (newContext.size() - oldContext.size())) { // все использованы
            return lpWithParams;
        }

        // по сути этот алгоритм эмулирует создание ListAction, с докидыванием в конец виртуального action'а который использует все extend параметры, однако само действие при этом не создает 
        List<LAPWithParams> allCreationParams = new ArrayList<>();
        allCreationParams.add(lpWithParams);
        for (int i = oldContext.size(); i < newContext.size(); i++) { // докидываем 
            allCreationParams.add(new LPWithParams(i));
        }

        List<Object> resultParams = getParamsPlainList(allCreationParams);
        List<Integer> wrappedUsed = mergeAllParams(allCreationParams);

        LA wrappedLA = addListAProp(newContext.size() - oldContext.size(), resultParams.toArray());
        return new LAWithParams(wrappedLA, wrappedUsed);
    }

    // assert that newContext extends oldContext (at least there is such assertion in addScriptedForAProp)
    private LPWithParams patchExtendParams(LPWithParams lpWithParams, List<TypedParameter> newContext, List<TypedParameter> oldContext) {
        if(lpWithParams.getLP() != null && !lpWithParams.getLP().listInterfaces.isEmpty() && lpWithParams.usedParams.isEmpty()) {
            return lpWithParams;
        }

        Set<Integer> usedExtendParams = new HashSet<>();
        for (int i = 0; i < lpWithParams.usedParams.size(); i++) {
            Integer usedParam = lpWithParams.usedParams.get(i);
            if(usedParam >= oldContext.size()) {
                usedExtendParams.add(usedParam);
            }
        }

        if(usedExtendParams.size() == (newContext.size() - oldContext.size())) { // все использованы
            return lpWithParams;
        }
        
        // по сути этот алгоритм эмулирует создание ListAction, с докидыванием в конец виртуального action'а который использует все extend параметры, однако само действие при этом не создает 
        List<LPWithParams> allCreationParams = new ArrayList<>();
        allCreationParams.add(lpWithParams);
        for (int i = oldContext.size(); i < newContext.size(); i++) { // докидываем 
            allCreationParams.add(new LPWithParams(i));
        }
        
        List<Object> resultParams = getParamsPlainList(allCreationParams);
        List<Integer> wrappedUsed = mergeAllParams(allCreationParams);
        
        LP wrappedLCP = addJProp(false, newContext.size() - oldContext.size(), (LP)resultParams.get(0), resultParams.subList(1, resultParams.size()).toArray());
        return new LPWithParams(wrappedLCP, wrappedUsed);
    }

    public void checkPropertyValue(LP property) {
        checks.checkPropertyValue(property, alwaysNullProperties);
    }

    public void checkCIInExpr(LPContextIndependent ci) throws ScriptingErrorLog.SemanticErrorException {
        checks.checkCIInExpr(ci);
    }

    public void initModulesAndNamespaces(List<String> requiredModules, List<String> namespacePriority) throws ScriptingErrorLog.SemanticErrorException {
        initNamespacesToModules(this, new HashSet<LogicsModule>());

        if (getNamespace().contains("_")) {
            errLog.emitNamespaceNameError(parser, getNamespace());
        }

        if (namespacePriority.contains(getNamespace())) {
            errLog.emitOwnNamespacePriorityError(parser, getNamespace());
        }

        for (String namespaceName : namespacePriority) {
            checks.checkNamespace(namespaceName);
        }

        for (String moduleName : requiredModules) {
            checks.checkModule(BL.getSysModule(moduleName), moduleName);
        }

        Set<String> prioritySet = new HashSet<>();
        for (String namespaceName : namespacePriority) {
            if (prioritySet.contains(namespaceName)) {
                errLog.emitNonUniquePriorityListError(parser, namespaceName);
            }
            prioritySet.add(namespaceName);
        }
    }

    public void setPropertyScriptInfo(LAP property, String script, DebugInfo.DebugPoint point) {
        property.setCreationScript(script);
        property.setCreationPath(point.toString());
    }

    private void parseStep(ScriptParser.State state) throws RecognitionException {
        try {
            parser.initParseStep(this, createStream(), state);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initNamespacesToModules(LogicsModule module, Set<LogicsModule> visitedModules) {
        visitedModules.add(module);
        String namespaceName = module.getNamespace();
        if (!namespaceToModules.containsKey(namespaceName)) {
            namespaceToModules.put(namespaceName, BaseUtils.toList(module));
        } else {
            namespaceToModules.get(namespaceName).add(module);
        }
        for (String requiredModuleName : module.getRequiredNames()) {
            LogicsModule requiredModule = BL.getSysModule(requiredModuleName);
            assert requiredModule != null;
            if (!visitedModules.contains(requiredModule)) {
                initNamespacesToModules(requiredModule, visitedModules);
            }
        }
    }

    private void showWarnings() {
        for (String warningText : warningList) {
            scriptLogger.warn("WARNING!" + warningText);
        }
    }

    public interface AbstractFormActionOrPropertyUsage {
    }

    public static abstract class BaseFormActionOrPropertyUsage implements AbstractFormActionOrPropertyUsage {
        public List<String> mapping;

        public BaseFormActionOrPropertyUsage(List<String> mapping) {
            this.mapping = mapping;
        }
        
        public void setMapping(List<String> mapping) { // need this because in formMappedProperty mapping is parsed after usage
            this.mapping = mapping;
        }
    }

    public interface AbstractFormPropertyUsage extends AbstractFormActionOrPropertyUsage { // lcp or calc
    }

    public interface AbstractFormActionUsage extends AbstractFormActionOrPropertyUsage { // LA or calc
    }

    public static abstract class FormLAPUsage<L extends LAP> implements AbstractFormActionOrPropertyUsage {
        public final L lp;
        public final List<ResolveClassSet> signature;
        public final ImOrderSet<String> mapping;

        public FormLAPUsage(L lp, ImOrderSet<String> mapping) {
            this(lp, mapping, null);            
        }
        public FormLAPUsage(L lp, ImOrderSet<String> mapping, List<ResolveClassSet> signature) {
            this.lp = lp;
            this.signature = signature;
            this.mapping = mapping;
        }
    }

    public static class FormLPUsage extends FormLAPUsage<LP> implements AbstractFormPropertyUsage {
        public FormLPUsage(LP lp, ImOrderSet<String> mapping) {
            super(lp, mapping);
        }

        public FormLPUsage(LP lp, ImOrderSet<String> mapping, List<ResolveClassSet> signature) {
            super(lp, mapping, signature);
        }
    }

    public static class FormLAUsage extends FormLAPUsage<LA> implements AbstractFormActionUsage {
        public FormLAUsage(LA lp, ImOrderSet<String> mapping) {
            super(lp, mapping);
        }

        public FormLAUsage(LA lp, ImOrderSet<String> mapping, List<ResolveClassSet> signature) {
            super(lp, mapping, signature);
        }
    }

    public static class NamedPropertyUsage {
        public String name;
        public List<String> classNames;

        public NamedPropertyUsage(String name) {
            this(name, null);
        }

        public NamedPropertyUsage(String name, List<String> classNames) {
            this.name = name;
            this.classNames = classNames;
        }

        public String getSourceName() {
            String result = null;
            if (name != null) {
                result = name;
                if (classNames != null) {
                    result += "[";
                    for (String className : classNames) {
                        if (!result.endsWith("[")) {
                            result += ", ";
                        }
                        result += className;
                    }
                    result += "]";
                }
            }
            return result;
        }
    }

    public static class FormPredefinedUsage extends BaseFormActionOrPropertyUsage {
        public final NamedPropertyUsage property;

        public FormPredefinedUsage(NamedPropertyUsage property, List<String> mapping) {
            super(mapping);
            this.property = property;
        }
    }
    
    public abstract static class FormActionOrPropertyUsage<U extends ActionOrPropertyUsage> extends BaseFormActionOrPropertyUsage {
        public final U usage;

        public FormActionOrPropertyUsage(U usage, List<String> mapping) {
            super(mapping);
            this.usage = usage;
        }
    }

    public static class FormPropertyUsage extends FormActionOrPropertyUsage implements AbstractFormPropertyUsage {
        public FormPropertyUsage(NamedPropertyUsage property, List<String> mapping) {
            this(new PropertyUsage(property), mapping);
        }
        public FormPropertyUsage(PropertyUsage property, List<String> mapping) {
            super(property, mapping);
        }
    }

    public static class FormPropertyElseActionUsage extends FormActionOrPropertyUsage {
        public FormPropertyElseActionUsage(PropertyElseActionUsage property, List<String> mapping) {
            super(property, mapping);
        }
    }

    public static class FormActionUsage extends FormActionOrPropertyUsage implements AbstractFormActionUsage {
        public FormActionUsage(NamedPropertyUsage property, List<String> mapping) {
            this(new ActionUsage(property), mapping);
        }
        public FormActionUsage(ActionUsage property, List<String> mapping) {
            super(property, mapping);
        }
    }

    public abstract static class ActionOrPropertyUsage {
        public final NamedPropertyUsage property;

        public ActionOrPropertyUsage(NamedPropertyUsage property) {
            this.property = property;
        }
        
        public abstract FormActionOrPropertyUsage createFormUsage(List<String> mapping);
    }

    public static class PropertyUsage extends ActionOrPropertyUsage {
        public PropertyUsage(NamedPropertyUsage property) {
            super(property);
        }

        public FormActionOrPropertyUsage createFormUsage(List<String> mapping) {
            return new FormPropertyUsage(this, mapping);
        }
    }

    public static class PropertyElseActionUsage extends ActionOrPropertyUsage {
        public PropertyElseActionUsage(NamedPropertyUsage property) {
            super(property);
        }

        public FormActionOrPropertyUsage createFormUsage(List<String> mapping) {
            return new FormPropertyElseActionUsage(this, mapping);
        }
    }

    public static class ActionUsage extends ActionOrPropertyUsage {
        public ActionUsage(NamedPropertyUsage property) {
            super(property);
        }

        @Override
        public FormActionOrPropertyUsage createFormUsage(List<String> mapping) {
            return new FormActionUsage(this, mapping);
        }
    }

    public class TypedParameter {
        public ValueClass cls;
        public String paramName;

        public TypedParameter(ValueClass cls, String name) {
            this.cls = cls;
            paramName = name;
        }

        public TypedParameter(String cName, String pName) throws ScriptingErrorLog.SemanticErrorException {
            if (cName != null) {
                cls = findClass(cName);
            } else {
                cls = null;
            }
            paramName = pName;
        }

        public String getParsedName() {
            if(cls != null)
                return cls.getParsedName();
            return null;
        }
    }

    public static class FormActionProps {
        public final LPWithParams in;
        public final Boolean inNull;

        public final boolean out;
        public final Integer outParamNum;
        public final Boolean outNull;
        public final NamedPropertyUsage outProp;

        public final LPWithParams changeProp;

        public final boolean assign;
        public final DebugInfo.DebugPoint assignDebugPoint;
        public final boolean constraintFilter;


        public FormActionProps(LPWithParams in, Boolean inNull, boolean out, Integer outParamNum, Boolean outNull, NamedPropertyUsage outProp, boolean constraintFilter, boolean assign, LPWithParams changeProp, DebugInfo.DebugPoint changeDebugPoint) {
            assert outProp == null;
            this.in = in;
            this.inNull = inNull;
            this.out = out;
            this.outParamNum = outParamNum;
            this.outNull = outNull;
            this.outProp = outProp;
            this.constraintFilter = constraintFilter;
            this.assign = assign;
            this.changeProp = changeProp;
            this.assignDebugPoint = changeDebugPoint;
        }
    }

}
