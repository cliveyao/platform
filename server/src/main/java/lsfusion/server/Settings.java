package lsfusion.server;

import lsfusion.server.context.ThreadLocalContext;
import lsfusion.server.logics.property.AlgType;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public class Settings implements Cloneable {

    public int packOnCacheComplexity = 300000;

    public boolean noApplyIncrement = false;

    public boolean applyVolatileStats = false;

    public boolean defaultOrdersNotNull = true; // временно

    // будет ли компилятор вместо UNION (когда UNION ALL не удается построить) использовать FULL JOIN
    boolean useFJInsteadOfUnion = false;

    private int innerGroupExprs = 0; // использовать Subquery Expressions

    private int LRUOftenCleanPeriod = 2;

    private int LRUOftenExpireSecond = 5;

    private int LRUOftenProceedBucket = 10000;

    private int LRURareCleanPeriod = 20;

    private int LRURareExpireSecond = 3600;

    private int LRURareProceedBucket = 10000;

    private boolean cacheInnerHashes = true;

    private int mapInnerMaxIterations = 24;

    // обозначает что если компилятор видет включающие join'ы (J1, J2, ... Jk) (J1,...J2, ... Jk,.. Jn) он будет выполнять все в первом подмножестве, предполагая что возникающий OR разберет SQL сервер что мягко говоря не так
    private boolean compileMeans = true;

    // обозначает что при проверке условия на TRUE не будет преобразовывать A cmp B в 3 противоположных NOT'а как правильно, а будет использовать эвристику
    private boolean simpleCheckCompare = true;

    // обозначает что на следствия (и отрицания) условия будет проверять когда остались только термы, не делая этого на промежуточных уровнях
    private boolean checkFollowsWhenObjects = false;

    // будет ли оптимизатор пытаться перестраивать условия по правилу X OR (Y AND Z) и X=>Y, то Y AND (X OR Z)
    private boolean restructWhereOnMeans = false;

    // будет ли оптимизатор разбивать группирующие выражения, чтобы не было FULL JOIN и UNION ALL
    private boolean splitSelectGroupInnerJoins = false;

    // при включенных нижних двух, дальнейшая настройка limitGroup* константы
    // групировать ли inner join'ы в группировочном выражении по статистике (чем больше группируем, тем теоретически меньше точность проталкивания, с другой стороны лучше группируются вычисления)
    private boolean groupStatExprWhereJoins = true; // сначала выключать эту опцию 

    // будет ли оптимизатор разбивать inner join'ы по статистике в группировке (чем меньше разбиений, тем больше группируются вычисления одного показателя, но меньшая вероятность сгруппировать разные показатели)
    // при включенном может быть проблема с GROUP MAX по объектному типу, для них при разбиении делаются IF ELSE ( а не аналог (+), так как тогда нужен вывод классов а он требует булеву логику), что может приводить к экспоненте
    // для борьбы с этой проблемой пока выключается нижняя опция
    private boolean splitGroupStatInnerJoins = true; // потом эту

    // смотри верхнюю опцию
    private boolean splitGroupStatMaxMinObjectType = false;

    // будет ли оптимизатор разбивать группирующие выражения на максимум, так чтобы в группируемом выражении не было бы Case'ов
    private boolean splitGroupSelectExprcases = false;

    // будет ли высчитываться что именно изменилось в группирующих свойствах или же будет считаться что изменилось все
    private boolean calculateGroupDataChanged = false;

    // не использовать инкрементную логику в группирующем свойстве на максимум
    private boolean noIncrementMaxGroupProperty = true;

    // использовать применение изменений "по одному"
    private boolean enableApplySingleStored = true;
    private boolean enableApplySingleRemoveClasses = true;

    private boolean editLogicalOnSingleClick = false;

    private boolean editActionOnSingleClick = false;

    private int freeConnections = 12;

    private boolean commonUnique = true; // потому как в таком случае все common connection'ы начинают блокировать друг друга, поэтому схема с private pool'ом правильней

    private boolean disablePoolConnections = false;

    private boolean disablePoolPreparedStatements = true;

    private boolean disableWhenCalcDo = true;

    private boolean disablePrereadValues = false;

    private boolean disableSumGroupNotZero = false;

    private int usedChangesCacheLimit = 20;

    // максимум сколько свойств вместе будет применяться в базу
    private int splitIncrementApply = 10;

    private int statDegree = 5;

    private int averageIntervalStat = 1;

    private int barcodeLength = 13;

    private boolean useUniPass;

    private boolean useSingleJoins = false;

    private boolean useQueryExpr = true;

    private int limitWhereJoinsCount = 15;
    private int limitWhereJoinsDegree = 2;

    private int limitIgnoreSaveStatsCount = 999999;

    // имеет смысл когда включены или groupStatExprWhereJoins или splitGroupStatInnerJoins
    private int limitGroupWhereJoinsCount = 1; // сворачиваем до конца пока сворачивается
    private int limitGroupIgnoreSaveStatsCount = 5; // оставлчем не больше 5, иначе сложность создаваемого GroupExpr растет экспоненциально 

    private boolean singleInstance;

    private boolean busyDialog = true;

    private long busyDialogTimeout = 1000;

    private boolean safeCheckFileExistence = true;

    public int getLimitWhereJoinsDegree() {
        return limitWhereJoinsDegree;
    }

    public void setLimitWhereJoinsDegree(int limitWhereJoinsDegree) {
        this.limitWhereJoinsDegree = limitWhereJoinsDegree;
    }

    private int limitWhereJoinsComplexity = 300;

    // очень опасная эвристика - может в определенных случаях "потерять ключ", то есть образуется And в котором не хватает KeyExpr'а
    private int limitClassWhereCount = 40;

    private int limitClassWhereComplexity = 4000;

    private int limitWhereJoinPack = 300;

    private boolean noExclusiveCompile = true;

    private int limitExclusiveCount = 7; // когда вообще не пытаться строить exclusive (count)

    private int limitExclusiveComplexity = 100; // когда вообще не пытаться строить exclusive (complexity)

    private int limitExclusiveSimpleCount = 10; // когда строить exclusive без рекурсии (count)

    private int limitExclusiveSimpleComplexity = 100; // когда строить exclusive без рекурсии (complexity)

    private int limitIncrementCoeff = 1;

    private int limitHintIncrementComplexity = 50; // есть проблема когда идет G(очень большого числа данных) = значение, статистика нормальная, сложность большая, начинает hint'ить что мешает проталкиванию

    private int limitHintIncrementValueComplexity = 1000; // есть проблема когда идет G(очень большого числа данных) = значение, статистика нормальная, сложность большая, начинает hint'ить что мешает проталкиванию

    private double limitComplexityGrowthCoeff = 1.5;

    private long limitHintIncrementStat = 200;

    private int limitHintNoUpdateComplexity = 10000;

    private int limitWrapComplexity = 200;

    private int limitMaterializeComplexity = 20;

    private int limitApplyHintIncrementComplexity = 100;

    private long limitApplyHintIncrementStat = 1000;

    private int updateFormCountPeriod = 30;
    
    private int updateUserLastActivity = 30;

    private int updatePingInfo = 3600;

    private int checkCurrentDate = 30;

    private boolean autoAnalyzeTempStats = true; // автоматически анализировать статистику после каждого заполнения временной таблицы (прикол в том что после удаления таблицы и добавления новых записей статистика сама увеличивается)

    private boolean useGreaterEquals = true;

    private boolean disableAutoHints = false;

    private boolean disableAutoHintCaches = true;

    private boolean disableWrapComplexity = true;

    private boolean enablePrevWrapComplexity = false;

    private int groupJoinLevel = 1; // -1 отключить

    // если prev идет в value, то использовать то значение которое есть сейчас после singleapply,
    // а не высчитывать на начало транзакции потому как все равно "временнОй" целостности не будет
    private boolean useEventValuePrevHeuristic = true;

    // отключает оптимизацию с вкладками
    private boolean disableTabbedOptimization = false;

    private boolean checkUniqueEvent = false; // проверять на то что для одного свойства один event

    private boolean disableChangeModifierAllHints = true; // если есть change modifier то disable'ить hint'ы - временное решение

    private boolean disableValueAllHints = true; // если есть change modifier то disable'ить hint'ы - временное решение

    private int commandLengthVolatileStats = 100000000; // определяет при какой длине команды, включать работу с волатильной статистикой

    private boolean disableReadSingleValues = false; // определять ли конкретные значения при записи запроса в таблицы

    private boolean disableReadClasses = false; // определять ли конкретные кдассы при записи запроса в таблицы

    private int reserveIDStep = 50; // по сколько ID'ков будут резервировать себе сервера приложений у сервера БД

    private boolean mergeUpClassSets = AlgType.useInfer; // проблема в том что с false детерменированность не гарантирована

    private int queryPrepareLength = 1000; // длина запроса, при которой необходимо pool'ить preparedStatement'ы

    private int queryPrepareRunTime = 40; // время выполнения запроса, при которой необходимо pool'ить preparedStatement'ы

    private boolean disableSimpleAddRemoveInNonExclCase = true;

    private boolean modifySessionTableInsteadOfRewrite = true;

    private boolean checkAlwaysNull = true;

    private boolean checkClassWhere = true;

    private int dialogTransactionTimeout = 5000;

    private int tooMuchRetryAttempts = 3;

    public int getTooMuchRetryAttempts() {
        return tooMuchRetryAttempts;
    }

    public void setTooMuchRetryAttempts(int tooMuchRetryAttempts) {
        this.tooMuchRetryAttempts = tooMuchRetryAttempts;
    }

    private String disableExplicitVolatileStats = "";
    private int tooMuchAttempts = 15;
    private boolean enableAdjustSelectivity = false; // включает повышение selectivity при volatile stats для операторов >, пока были непостоянные случаи, поэтому выключен

    private long maxRecalculateTime = 300000;//300000; //5 minutes

    private boolean groupByTables = true; //для recalculate

    private int profilerBatchSize = 10000;

    private int threadAllocatedMemoryPeriod = 180; //every 3 minutes
    
    private boolean readAllocatedBytes = true;

    private long maxThreadAllocatedBytes = 500048576; //500MB

    private long excessThreadAllocatedBytes = 5368709120L; //5GB

    private int accessInterruptCount = 4;
    
    private boolean logSqlProcesses = false;

    private boolean useShowIfInReports = true;

    private int minSizeForExcelStreamingReader = -1; //-1 disabled, to enable use for example 50000000 (50 MB)

    public long getMaxRecalculateTime() {
        return maxRecalculateTime;
    }

    private long maxPrereadCachesTime = 10000;// 10 seconds

    public long getMaxPrereadCachesTime() {
        return maxPrereadCachesTime;
    }

    public void setMaxPrereadCachesTime(long maxPrereadCachesTime) {
        this.maxPrereadCachesTime = maxPrereadCachesTime;
    }

    public void setMaxRecalculateTime(long maxRecalculateTime) {
        this.maxRecalculateTime = maxRecalculateTime;
    }
    
    public static Settings get() {
        return ThreadLocalContext.getSettings();
    }

    public static Settings copy() throws CloneNotSupportedException {
        return (Settings) get().clone();
    }

    public Settings cloneSettings() throws CloneNotSupportedException {
        return (Settings) clone();
    }

    public int getInnerGroupExprs() {
        return innerGroupExprs;
    }

    public void setInnerGroupExprs(int innerGroupExprs) {
        this.innerGroupExprs = innerGroupExprs;
    }

    public int getPackOnCacheComplexity() {
        return packOnCacheComplexity;
    }

    public void setPackOnCacheComplexity(int packOnCacheComplexity) {
        this.packOnCacheComplexity = packOnCacheComplexity;
    }

    public int getLRUOftenCleanPeriod() {
        return LRUOftenCleanPeriod;
    }

    public void setLRUOftenCleanPeriod(int LRUOftenCleanPeriod) {
        this.LRUOftenCleanPeriod = LRUOftenCleanPeriod;
    }

    public int getLRUOftenExpireSecond() {
        return LRUOftenExpireSecond;
    }

    public void setLRUOftenExpireSecond(int LRUOftenExpireSecond) {
        this.LRUOftenExpireSecond = LRUOftenExpireSecond;
    }

    public int getLRUOftenProceedBucket() {
        return LRUOftenProceedBucket;
    }

    public void setLRUOftenProceedBucket(int LRUOftenProceedBucket) {
        this.LRUOftenProceedBucket = LRUOftenProceedBucket;
    }

    public int getLRURareCleanPeriod() {
        return LRURareCleanPeriod;
    }

    public void setLRURareCleanPeriod(int LRURareCleanPeriod) {
        this.LRURareCleanPeriod = LRURareCleanPeriod;
    }

    public int getLRURareExpireSecond() {
        return LRURareExpireSecond;
    }

    public void setLRURareExpireSecond(int LRURareExpireSecond) {
        this.LRURareExpireSecond = LRURareExpireSecond;
    }

    public int getLRURareProceedBucket() {
        return LRURareProceedBucket;
    }

    public void setLRURareProceedBucket(int LRURareProceedBucket) {
        this.LRURareProceedBucket = LRURareProceedBucket;
    }

    public boolean isCacheInnerHashes() {
        return cacheInnerHashes;
    }

    public void setCacheInnerHashes(boolean cacheInnerHashes) {
        this.cacheInnerHashes = cacheInnerHashes;
    }

    public int getMapInnerMaxIterations() {
        return mapInnerMaxIterations;
    }

    public void setMapInnerMaxIterations(int mapInnerMaxIterations) {
        this.mapInnerMaxIterations = mapInnerMaxIterations;
    }

    public boolean isEnableApplySingleRemoveClasses() {
        return enableApplySingleRemoveClasses;
    }

    public void setEnableApplySingleRemoveClasses(boolean enableApplySingleRemoveClasses) {
        this.enableApplySingleRemoveClasses = enableApplySingleRemoveClasses;
    }

    public boolean isEnableApplySingleStored() {
        return enableApplySingleStored;
    }

    public void setEnableApplySingleStored(boolean enableApplySingleStored) {
        this.enableApplySingleStored = enableApplySingleStored;
    }

    public boolean isSplitSelectGroupInnerJoins() {
        return splitSelectGroupInnerJoins;
    }

    public void setSplitSelectGroupInnerJoins(boolean splitSelectGroupInnerJoins) {
        this.splitSelectGroupInnerJoins = splitSelectGroupInnerJoins;
    }

    public boolean isSplitGroupStatInnerJoins() {
        return splitGroupStatInnerJoins;
    }

    public void setSplitGroupStatInnerJoins(boolean splitGroupStatInnerJoins) {
        this.splitGroupStatInnerJoins = splitGroupStatInnerJoins;
    }

    public boolean isSplitGroupStatMaxMinObjectType() {
        return splitGroupStatMaxMinObjectType;
    }

    public void setSplitGroupStatMaxMinObjectType(boolean splitGroupStatMaxMinObjectType) {
        this.splitGroupStatMaxMinObjectType = splitGroupStatMaxMinObjectType;
    }

    public boolean isGroupStatExprWhereJoins() {
        return groupStatExprWhereJoins;
    }

    public void setGroupStatExprWhereJoins(boolean groupStatExprWhereJoins) {
        this.groupStatExprWhereJoins = groupStatExprWhereJoins;
    }
    
    private int sessionRowsToTable = 4;

    public int getSessionRowsToTable() {
        return sessionRowsToTable;
    }

    public void setSessionRowsToTable(int sessionRowsToTable) {
        this.sessionRowsToTable = sessionRowsToTable;
    }

    public boolean isUseFJInsteadOfUnion() {
        return useFJInsteadOfUnion;
    }

    public void setUseFJInsteadOfUnion(boolean useFJInsteadOfUnion) {
        this.useFJInsteadOfUnion = useFJInsteadOfUnion;
    }

    public boolean isSimpleCheckCompare() {
        return simpleCheckCompare;
    }

    public void setSimpleCheckCompare(boolean simpleCheckCompare) {
        this.simpleCheckCompare = simpleCheckCompare;
    }

    public boolean getEditLogicalOnSingleClick() {
        return editLogicalOnSingleClick;
    }

    public void setEditLogicalOnSingleClick(boolean editLogicalOnSingleClick) {
        this.editLogicalOnSingleClick = editLogicalOnSingleClick;
    }

    public boolean getEditActionOnSingleClick() {
        return editActionOnSingleClick;
    }

    public void setEditActionOnSingleClick(boolean editActionOnSingleClick) {
        this.editActionOnSingleClick = editActionOnSingleClick;
    }

    public boolean isCheckFollowsWhenObjects() {
        return checkFollowsWhenObjects;
    }

    public void setCheckFollowsWhenObjects(boolean checkFollowsWhenObjects) {
        this.checkFollowsWhenObjects = checkFollowsWhenObjects;
    }

    public boolean isRestructWhereOnMeans() {
        return restructWhereOnMeans;
    }

    public void setRestructWhereOnMeans(boolean restructWhereOnMeans) {
        this.restructWhereOnMeans = restructWhereOnMeans;
    }

    public boolean isSplitGroupSelectExprcases() {
        return splitGroupSelectExprcases;
    }

    public void setSplitGroupSelectExprcases(boolean splitGroupSelectExprcases) {
        this.splitGroupSelectExprcases = splitGroupSelectExprcases;
    }

    public boolean isCalculateGroupDataChanged() {
        return calculateGroupDataChanged;
    }

    public void setCalculateGroupDataChanged(boolean calculateGroupDataChanged) {
        this.calculateGroupDataChanged = calculateGroupDataChanged;
    }

    public boolean isNoIncrementMaxGroupProperty() {
        return noIncrementMaxGroupProperty;
    }

    public void setNoIncrementMaxGroupProperty(boolean noIncrementMaxGroupProperty) {
        this.noIncrementMaxGroupProperty = noIncrementMaxGroupProperty;
    }

    public boolean isCompileMeans() {
        return compileMeans;
    }

    public void setCompileMeans(boolean compileMeans) {
        this.compileMeans = compileMeans;
    }

    public int getFreeConnections() {
        return freeConnections;
    }

    public void setFreeConnections(int freeConnections) {
        this.freeConnections = freeConnections;
    }

    public boolean isCommonUnique() {
        return commonUnique;
    }

    public void setCommonUnique(boolean commonUnique) {
        this.commonUnique = commonUnique;
    }

    public boolean isDisablePoolConnections() {
        return disablePoolConnections;
    }

    public void setDisablePoolConnections(boolean disablePoolConnections) {
        this.disablePoolConnections = disablePoolConnections;
    }

    public boolean isDisablePoolPreparedStatements() {
        return disablePoolPreparedStatements;
    }

    public void setDisablePoolPreparedStatements(boolean disablePoolPreparedStatements) {
        this.disablePoolPreparedStatements = disablePoolPreparedStatements;
    }

    public boolean isDisableWhenCalcDo() {
        return disableWhenCalcDo;
    }

    public void setDisableWhenCalcDo(boolean disableWhenCalcDo) {
        this.disableWhenCalcDo = disableWhenCalcDo;
    }

    public boolean isDisablePrereadValues() {
        return disablePrereadValues;
    }

    public void setDisablePrereadValues(boolean disablePrereadValues) {
        this.disablePrereadValues = disablePrereadValues;
    }

    public boolean isDisableSumGroupNotZero() {
        return disableSumGroupNotZero;
    }

    public void setDisableSumGroupNotZero(boolean disableSumGroupNotZero) {
        this.disableSumGroupNotZero = disableSumGroupNotZero;
    }

    public int getUsedChangesCacheLimit() {
        return usedChangesCacheLimit;
    }

    public void setUsedChangesCacheLimit(int usedChangesCacheLimit) {
        this.usedChangesCacheLimit = usedChangesCacheLimit;
    }

    public int getSplitIncrementApply() {
        return splitIncrementApply;
    }

    public void setSplitIncrementApply(int splitIncrementApply) {
        this.splitIncrementApply = splitIncrementApply;
    }

    public int getStatDegree() {
        return statDegree;
    }

    public void setStatDegree(int statDegree) {
        this.statDegree = statDegree;
    }

    public int getAverageIntervalStat() {
        return averageIntervalStat;
    }

    public boolean isSingleInstance() {
        return singleInstance;
    }

    public void setSingleInstance(boolean singleInstance) {
        this.singleInstance = singleInstance;
    }

    public boolean isBusyDialog() {
        return busyDialog;
    }

    public void setBusyDialog(boolean busyDialog) {
        this.busyDialog = busyDialog;
    }

    public long getBusyDialogTimeout() {
        return busyDialogTimeout;
    }

    public void setBusyDialogTimeout(long busyDialogTimeout) {
        this.busyDialogTimeout = busyDialogTimeout;
    }

    public boolean isSafeCheckFileExistence() {
        return safeCheckFileExistence;
    }

    public void setSafeCheckFileExistence(boolean safeCheckFileExistence) {
        this.safeCheckFileExistence = safeCheckFileExistence;
    }

    public void setAverageIntervalStat(int averageIntervalStat) {
        this.averageIntervalStat = averageIntervalStat;
    }

    public int getBarcodeLength() {
        return barcodeLength;
    }

    public void setBarcodeLength(int barcodeLength) {
        this.barcodeLength = barcodeLength;
    }

    public boolean getUseUniPass() {
        return useUniPass;
    }

    public void setUseUniPass(boolean useUniPass) {
        this.useUniPass = useUniPass;
    }

    public boolean isUseSingleJoins() {
        return useSingleJoins;
    }

    public void setUseSingleJoins(boolean useSingleJoins) {
        this.useSingleJoins = useSingleJoins;
    }

    public boolean isUseQueryExpr() {
        return useQueryExpr;
    }

    public void setUseQueryExpr(boolean useQueryExpr) {
        this.useQueryExpr = useQueryExpr;
    }

    public boolean isNoExclusiveCompile() {
        return noExclusiveCompile;
    }

    public void setNoExclusiveCompile(boolean noExclusiveCompile) {
        this.noExclusiveCompile = noExclusiveCompile;
    }

    public int getLimitWhereJoinsCount() {
        return limitWhereJoinsCount;
    }

    public void setLimitWhereJoinsCount(int limitWhereJoinsCount) {
        this.limitWhereJoinsCount = limitWhereJoinsCount;
    }

    public int getLimitIgnoreSaveStatsCount() {
        return limitIgnoreSaveStatsCount;
    }

    public void setLimitIgnoreSaveStatsCount(int limitIgnoreSaveStatsCount) {
        this.limitIgnoreSaveStatsCount = limitIgnoreSaveStatsCount;
    }

    public int getLimitGroupWhereJoinsCount() {
        return limitGroupWhereJoinsCount;
    }

    public void setLimitGroupWhereJoinsCount(int limitGroupWhereJoinsCount) {
        this.limitGroupWhereJoinsCount = limitGroupWhereJoinsCount;
    }

    public int getLimitGroupIgnoreSaveStatsCount() {
        return limitGroupIgnoreSaveStatsCount;
    }

    public void setLimitGroupIgnoreSaveStatsCount(int limitGroupIgnoreSaveStatsCount) {
        this.limitGroupIgnoreSaveStatsCount = limitGroupIgnoreSaveStatsCount;
    }

    public int getLimitWhereJoinsComplexity() {
        return limitWhereJoinsComplexity;
    }

    public void setLimitWhereJoinsComplexity(int limitWhereJoinsComplexity) {
        this.limitWhereJoinsComplexity = limitWhereJoinsComplexity;
    }

    public int getLimitClassWhereCount() {
        return limitClassWhereCount;
    }

    public void setLimitClassWhereCount(int limitClassWhereCount) {
        this.limitClassWhereCount = limitClassWhereCount;
    }

    public int getLimitClassWhereComplexity() {
        return limitClassWhereComplexity;
    }

    public void setLimitClassWhereComplexity(int limitClassWhereComplexity) {
        this.limitClassWhereComplexity = limitClassWhereComplexity;
    }

    public int getLimitWhereJoinPack() {
        return limitWhereJoinPack;
    }

    public void setLimitWhereJoinPack(int limitWhereJoinPack) {
        this.limitWhereJoinPack = limitWhereJoinPack;
    }

    public int getLimitIncrementCoeff() {
        return limitIncrementCoeff;
    }

    public void setLimitIncrementCoeff(int limitIncrementCoeff) {
        this.limitIncrementCoeff = limitIncrementCoeff;
    }

    public int getLimitHintIncrementComplexity() {
        return limitHintIncrementComplexity;
    }

    public void setLimitHintIncrementComplexity(int limitHintIncrementComplexity) {
        this.limitHintIncrementComplexity = limitHintIncrementComplexity;
    }

    public int getLimitHintIncrementComplexityCoeff() {
        return limitHintIncrementComplexity * limitIncrementCoeff;
    }

    public void setLimitHintIncrementComplexityCoeff(int limitHintIncrementComplexity) {
        this.limitHintIncrementComplexity = limitHintIncrementComplexity;
    }

    public int getLimitHintIncrementValueComplexity() {
        return limitHintIncrementValueComplexity;
    }

    public void setLimitHintIncrementValueComplexity(int limitHintIncrementValueComplexity) {
        this.limitHintIncrementValueComplexity = limitHintIncrementValueComplexity;
    }

    public int getLimitHintIncrementValueComplexityCoeff() {
        return limitHintIncrementValueComplexity * limitIncrementCoeff;
    }

    public boolean isNoApplyIncrement() {
        return noApplyIncrement;
    }

    public void setNoApplyIncrement(boolean noApplyIncrement) {
        this.noApplyIncrement = noApplyIncrement;
    }

    public int getLimitApplyHintIncrementComplexity() {
        return limitApplyHintIncrementComplexity * limitIncrementCoeff;
    }

    public void setLimitApplyHintIncrementComplexity(int limitApplyHintIncrementComplexity) {
        this.limitApplyHintIncrementComplexity = limitApplyHintIncrementComplexity;
    }

    public long getLimitHintIncrementStat() {
        return limitHintIncrementStat;
    }

    public void setLimitHintIncrementStat(long limitHintIncrementStat) {
        this.limitHintIncrementStat = limitHintIncrementStat;
    }

    private int adjustRecursionStat = 1000;

    public int getAdjustRecursionStat() {
        return adjustRecursionStat;
    }

    public void setAdjustRecursionStat(int adjustRecursionStat) {
        this.adjustRecursionStat = adjustRecursionStat;
    }

    public long getLimitApplyHintIncrementStat() {
        return limitApplyHintIncrementStat;
    }

    public void setLimitApplyHintIncrementStat(int limitApplyHintIncrementStat) {
        this.limitApplyHintIncrementStat = limitApplyHintIncrementStat;
    }

    public int getLimitHintNoUpdateComplexity() {
        return limitHintNoUpdateComplexity * limitIncrementCoeff;
    }

    public void setLimitHintNoUpdateComplexity(int limitHintNoUpdateComplexity) {
        this.limitHintNoUpdateComplexity = limitHintNoUpdateComplexity;
    }

    public int getLimitWrapComplexity() {
        return limitWrapComplexity * limitIncrementCoeff;
    }

    public void setLimitWrapComplexity(int limitWrapComplexity) {
        this.limitWrapComplexity = limitWrapComplexity;
    }

    public double getLimitComplexityGrowthCoeff() {
        return limitComplexityGrowthCoeff;
    }

    public void setLimitComplexityGrowthCoeff(double limitComplexityGrowthCoeff) {
        this.limitComplexityGrowthCoeff = limitComplexityGrowthCoeff;
    }

    public int getLimitExclusiveCount() {
        return limitExclusiveCount;
    }

    public void setLimitExclusiveCount(int limitExclusiveCount) {
        this.limitExclusiveCount = limitExclusiveCount;
    }

    public int getLimitExclusiveSimpleCount() {
        return limitExclusiveSimpleCount;
    }

    public void setLimitExclusiveSimpleCount(int limitExclusiveSimpleCount) {
        this.limitExclusiveSimpleCount = limitExclusiveSimpleCount;
    }

    public int getLimitExclusiveSimpleComplexity() {
        return limitExclusiveSimpleComplexity;
    }

    public void setLimitExclusiveSimpleComplexity(int limitExclusiveSimpleComplexity) {
        this.limitExclusiveSimpleComplexity = limitExclusiveSimpleComplexity;
    }

    public int getLimitExclusiveComplexity() {
        return limitExclusiveComplexity;
    }

    public void setLimitExclusiveComplexity(int limitExclusiveComplexity) {
        this.limitExclusiveComplexity = limitExclusiveComplexity;
    }

    public int getLimitMaterializeComplexity() {
        return limitMaterializeComplexity;
    }

    public void setLimitMaterializeComplexity(int limitMaterializeComplexity) {
        this.limitMaterializeComplexity = limitMaterializeComplexity;
    }

    public int getUpdateFormCountPeriod() {
        return updateFormCountPeriod;
    }

    public void setUpdateFormCountPeriod(int updateFormCountPeriod) {
        this.updateFormCountPeriod = updateFormCountPeriod;
    }

    public int getUpdateUserLastActivity() {
        return updateUserLastActivity;
    }

    public void setUpdateUserLastActivity(int updateUserLastActivity) {
        this.updateUserLastActivity = updateUserLastActivity;
    }

    public int getCheckCurrentDate() {
        return checkCurrentDate;
    }

    public void setCheckCurrentDate(int checkCurrentDate) {
        this.checkCurrentDate = checkCurrentDate;
    }

    public int getUpdatePingInfo() {
        return updatePingInfo;
    }

    public void setUpdatePingInfo(int updatePingInfo) {
        this.updatePingInfo = updatePingInfo;
    }

    public boolean isAutoAnalyzeTempStats() {
        return autoAnalyzeTempStats;
    }

    public void setAutoAnalyzeTempStats(boolean autoAnalyzeTempStats) {
        this.autoAnalyzeTempStats = autoAnalyzeTempStats;
    }

    public boolean isUseGreaterEquals() {
        return useGreaterEquals;
    }

    public void setUseGreaterEquals(boolean useGreaterEquals) {
        this.useGreaterEquals = useGreaterEquals;
    }

    public boolean isDisableAutoHints() {
        return disableAutoHints;
    }

    public void setDisableAutoHints(boolean disableAutoHints) {
        this.disableAutoHints = disableAutoHints;
    }

    public boolean isDisableAutoHintCaches() {
        return disableAutoHintCaches;
    }

    public void setDisableAutoHintCaches(boolean disableAutoHintCaches) {
        this.disableAutoHintCaches = disableAutoHintCaches;
    }

    public boolean isDisableWrapComplexity() {
        return disableWrapComplexity;
    }

    public void setDisableWrapComplexity(boolean disableWrapComplexity) {
        this.disableWrapComplexity = disableWrapComplexity;
    }

    public boolean isEnablePrevWrapComplexity() {
        return enablePrevWrapComplexity;
    }

    public void setEnablePrevWrapComplexity(boolean enablePrevWrapComplexity) {
        this.enablePrevWrapComplexity = enablePrevWrapComplexity;
    }

    public int getGroupJoinLevel() {
        return groupJoinLevel;
    }

    public void setGroupJoinLevel(int groupJoinLevel) {
        this.groupJoinLevel = groupJoinLevel;
    }

    public boolean isApplyVolatileStats() {
        return applyVolatileStats;
    }

    public void setApplyVolatileStats(boolean applyVolatileStats) {
        this.applyVolatileStats = applyVolatileStats;
    }

    public boolean isUseEventValuePrevHeuristic() {
        return useEventValuePrevHeuristic;
    }

    public void setUseEventValuePrevHeuristic(boolean useEventValuePrevHeuristic) {
        this.useEventValuePrevHeuristic = useEventValuePrevHeuristic;
    }

    public boolean isDisableTabbedOptimization() {
        return disableTabbedOptimization;
    }

    public void setDisableTabbedOptimization(boolean disableTabbedOptimization) {
        this.disableTabbedOptimization = disableTabbedOptimization;
    }

    public boolean isCheckUniqueEvent() {
        return checkUniqueEvent;
    }

    public void setCheckUniqueEvent(boolean checkUniqueEvent) {
        this.checkUniqueEvent = checkUniqueEvent;
    }

    public boolean isDisableChangeModifierAllHints() {
        return disableChangeModifierAllHints;
    }

    public void setDisableChangeModifierAllHints(boolean disableChangeModifierAllHints) {
        this.disableChangeModifierAllHints = disableChangeModifierAllHints;
    }

    public boolean isDisableValueAllHints() {
        return disableValueAllHints;
    }

    public void setDisableValueAllHints(boolean disableValueAllHints) {
        this.disableValueAllHints = disableValueAllHints;
    }

    public boolean isDefaultOrdersNotNull() {
        return defaultOrdersNotNull;
    }

    public void setDefaultOrdersNotNull(boolean defaultOrdersNotNull) {
        this.defaultOrdersNotNull = defaultOrdersNotNull;
    }

    public int getCommandLengthVolatileStats() {
        return commandLengthVolatileStats;
    }

    public void setCommandLengthVolatileStats(int commandLengthVolatileStats) {
        this.commandLengthVolatileStats = commandLengthVolatileStats;
    }

    public boolean isDisableReadSingleValues() {
        return disableReadSingleValues;
    }

    public void setDisableReadSingleValues(boolean disableReadSingleValues) {
        this.disableReadSingleValues = disableReadSingleValues;
    }

    public boolean isDisableReadClasses() {
        return disableReadClasses;
    }

    public void setDisableReadClasses(boolean disableReadClasses) {
        this.disableReadClasses = disableReadClasses;
    }

    public int getReserveIDStep() {
        return reserveIDStep;
    }

    public void setReserveIDStep(int reserveIDStep) {
        this.reserveIDStep = reserveIDStep;
    }

    public boolean isMergeUpClassSets() {
        return mergeUpClassSets;
    }

    public void setMergeUpClassSets(boolean mergeUpClassSets) {
        this.mergeUpClassSets = mergeUpClassSets;
    }

    public int getQueryPrepareLength() {
        return queryPrepareLength;
    }

    public void setQueryPrepareLength(int queryPrepareLength) {
        this.queryPrepareLength = queryPrepareLength;
    }

    public int getQueryPrepareRunTime() {
        return queryPrepareRunTime;
    }

    public void setQueryPrepareRunTime(int queryPrepareRunTime) {
        this.queryPrepareRunTime = queryPrepareRunTime;
    }

    public boolean isDisableSimpleAddRemoveInNonExclCase() {
        return disableSimpleAddRemoveInNonExclCase;
    }

    public void setDisableSimpleAddRemoveInNonExclCase(boolean disableSimpleAddRemoveInNonExclCase) {
        this.disableSimpleAddRemoveInNonExclCase = disableSimpleAddRemoveInNonExclCase;
    }

    public boolean isModifySessionTableInsteadOfRewrite() {
        return modifySessionTableInsteadOfRewrite;
    }

    public void setModifySessionTableInsteadOfRewrite(boolean modifySessionTableInsteadOfRewrite) {
        this.modifySessionTableInsteadOfRewrite = modifySessionTableInsteadOfRewrite;
    }

    public boolean isCheckAlwaysNull() {
        return checkAlwaysNull;
    }

    public void setCheckAlwaysNull(boolean checkAlwaysNull) {
        this.checkAlwaysNull = checkAlwaysNull;
    }

    public boolean isCheckClassWhere() {
        return checkClassWhere;
    }

    public int getDialogTransactionTimeout() {
        return dialogTransactionTimeout;
    }

    public void setDialogTransactionTimeout(int dialogTransactionTimeout) {
        this.dialogTransactionTimeout = dialogTransactionTimeout;
    }

    public String getDisableExplicitVolatileStats() {
        return disableExplicitVolatileStats;
    }

    public void setDisableExplicitVolatileStats(String disableExplicitVolatileStats) {
        this.disableExplicitVolatileStats = disableExplicitVolatileStats;
    }

    public void setCheckClassWhere(boolean checkClassWhere) {
        this.checkClassWhere = checkClassWhere;
    }

    public void setProperties(Map<String, String> properties) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            String propertyName = property.getKey();
            String propertyValue = property.getValue();

            if (propertyName != null && propertyValue != null && !propertyValue.trim().isEmpty()) {
                if (!PropertyUtils.isWriteable(this, propertyName)) {
                    throw new RuntimeException("Property '" + propertyName + "' isn't writable on Settings");
                }

                try {
                    BeanUtils.setProperty(this, propertyName, propertyValue);
                } catch (Exception e) {
                    throw new RuntimeException("Property '" + propertyName + "' can't be set: " + e.getMessage());
                }
            }
        }
    }
    
    private int queryLengthTimeout = 1000; // после какой длины запроса использовать timeout'ы, сильно маленький не должен быть так как простые UPDATE'ы с блокировками будут timeout'ся

    public int getQueryLengthTimeout() {
        return queryLengthTimeout;
    }

    public void setQueryLengthTimeout(int queryLengthTimeout) {
        this.queryLengthTimeout = queryLengthTimeout;
    }
    
    private int timeoutDegree = 5; // cтепень с которой растить timeout

    public int getTimeoutDegree() {
        return timeoutDegree;
    }

    public void setTimeoutDegree(int timeoutDegree) {
        this.timeoutDegree = timeoutDegree;
    }
    
    private int timeoutStart = 3; // со скольки секунд начинать timeout

    public int getTimeoutStart() {
        return timeoutStart;
    }

    public void setTimeoutStart(int timeoutStart) {
        this.timeoutStart = timeoutStart;
    }
    
    private int applyAutoAttemptCountLimit = 3; // количество попыток провести транзакцию, при отсутствии взаимодействия с пользователем

    public int getApplyAutoAttemptCountLimit() {
        return applyAutoAttemptCountLimit;
    }

    public void setApplyAutoAttemptCountLimit(int applyAutoAttemptCountLimit) {
        this.applyAutoAttemptCountLimit = applyAutoAttemptCountLimit;
    }

    private boolean alwaysDropSessionTableAfter = true; // если false то может использовать меньше таблиц, но есть риск что при exception'е не востановится таблица, что может привести к непредсказуемым последствиям 

    public boolean isAlwaysDropSessionTableAfter() {
        return alwaysDropSessionTableAfter;
    }

    public void setAlwaysDropSessionTableAfter(boolean alwaysDropSessionTableAfter) {
        this.alwaysDropSessionTableAfter = alwaysDropSessionTableAfter;
    }

    private int disablePropertyReupdateCount = 10; // при записи свойств в базу после какого количества свойств включать REUPDATE (чтобы исключить избыточную проверку так как с большой вероятностью поля все равно придется обновить) 

    public int getDisablePropertyReupdateCount() {
        return disablePropertyReupdateCount;
    }

    public void setDisablePropertyReupdateCount(int disablePropertyReupdateCount) {
        this.disablePropertyReupdateCount = disablePropertyReupdateCount;
    }

    private int flushPendingTransactionCleanersThreshold = 10; // время как часто будет обрабатываться пул асинхронных очисток ресурсов сессии (в секундах), должно быть работать быстрее сборок мусора

    public int getFlushPendingTransactionCleanersThreshold() {
        return flushPendingTransactionCleanersThreshold;
    }

    public void setFlushPendingTransactionCleanersThreshold(int flushPendingTransactionCleanersThreshold) {
        this.flushPendingTransactionCleanersThreshold = flushPendingTransactionCleanersThreshold;
    }

    private int tempTablesTimeThreshold = 240; // время сколько будет гарантированно жить таблица (в секундах), нужно для предотвращения ротации кэшей, должно быть соизмеримо со стандартным временем использования
    private int tempTablesCountThreshold = 40; // очищать таблицы, когда их общее количество превысило данный порог * количество соединений

    public int getTempTablesTimeThreshold() {
        return tempTablesTimeThreshold;
    }

    public void setTempTablesTimeThreshold(int tempTablesTimeThreshold) {
        this.tempTablesTimeThreshold = tempTablesTimeThreshold;
    }

    public int getTempTablesCountThreshold() {
        return tempTablesCountThreshold;
    }

    public void setTempTablesCountThreshold(int tempTablesCountThreshold) {
        this.tempTablesCountThreshold = tempTablesCountThreshold;
    }

    private int tempStatisticsTarget = 10;

    public int getTempStatisticsTarget() {
        return tempStatisticsTarget;
    }

    public void setTempStatisticsTarget(int tempStatisticsTarget) {
        this.tempStatisticsTarget = tempStatisticsTarget;
    }

    private int queryRowCountPessLimit = 1000; // пессимистичная оценка, чтобы отсекать совсем маленькие запросы

    public int getQueryRowCountPessLimit() {
        return queryRowCountPessLimit;
    }

    public void setQueryRowCountPessLimit(int queryRowCountPessLimit) {
        this.queryRowCountPessLimit = queryRowCountPessLimit;
    }

    private int queryRowCountOptDivider = 50; // когда предполагаемый объем потребления памяти, превысит заданную часть, кидать exception

    public int getQueryRowCountOptDivider() {
        return queryRowCountOptDivider;
    }

    public void setQueryRowCountOptDivider(int queryRowCountOptDivider) {
        this.queryRowCountOptDivider = queryRowCountOptDivider;
    }

    private int queryLengthLimit = 2000 * 1000;

    public int getQueryLengthLimit() {
        return queryLengthLimit;
    }

    public void setQueryLengthLimit(int queryLengthLimit) {
        this.queryLengthLimit = queryLengthLimit;
    }

    private boolean enableHacks = true;

    public boolean isEnableHacks() {
        return enableHacks;
    }

    public void setEnableHacks(boolean enableHacks) {
        this.enableHacks = enableHacks;
    }
    
    private int logLevelJDBC = 0;

    public int getLogLevelJDBC() {
        return logLevelJDBC;
    }

    public void setLogLevelJDBC(int logLevelJDBC) {
        this.logLevelJDBC = logLevelJDBC;
    }
    
    private boolean useSafeStringAgg = false; // temporary

    public boolean isUseSafeStringAgg() {
        return useSafeStringAgg;
    }

    public void setUseSafeStringAgg(boolean useSafeStringAgg) {
        this.useSafeStringAgg = useSafeStringAgg;
    }
    
    private int remoteLogTime = 3000; // millisectonds

    public int getRemoteLogTime() {
        return remoteLogTime;
    }

    public void setRemoteLogTime(int remoteLogTime) {
        this.remoteLogTime = remoteLogTime;
    }

    public int getTooMuchAttempts() {
        return tooMuchAttempts;
    }

    public void setTooMuchAttempts(int tooMuchAttempts) {
        this.tooMuchAttempts = tooMuchAttempts;
    }


    public boolean isEnableAdjustSelectivity() {
        return enableAdjustSelectivity;
    }

    public void setEnableAdjustSelectivity(boolean enableAdjustSelectivity) {
        this.enableAdjustSelectivity = enableAdjustSelectivity;
    }
    
    private boolean useMSSQLFuncWrapper = false; // в ms sql оборачивать CASE WHEN'ы (в основном CASE WHEN ... NULL END) в функции из-за проблем со статистикой в SQL Server <= 2012 

    public boolean isUseMSSQLFuncWrapper() {
        return useMSSQLFuncWrapper;
    }

    public void setUseMSSQLFuncWrapper(boolean useMSSQLFuncWrapper) {
        this.useMSSQLFuncWrapper = useMSSQLFuncWrapper;
    }
    
    private String logTimeFilter = "";

    public String getLogTimeFilter() {
        return logTimeFilter;
    }

    public void setLogTimeFilter(String logTimeFilter) {
        this.logTimeFilter = logTimeFilter;
    }
    
    private int logTimeThreshold = 60;

    public int getLogTimeThreshold() {
        return logTimeThreshold;
    }

    public void setLogTimeThreshold(int logTimeThreshold) {
        this.logTimeThreshold = logTimeThreshold;
    }
    
    // 0 - no adjustment
    // 1 - multi tree
    // 2 - (multi tree + spanning tree) / 2
    // 3 - spanning tree
    // 1 и 2 используют переборный механизм, поэтому туда надо еще отсечение вставить, если понадобится использовать
    private int pessStatType = 3;

    public int getPessStatType() {
        return pessStatType;
    }

    public void setPessStatType(int pessStatType) {
        this.pessStatType = pessStatType;
    }

    // в перерасчете / проверке агрегаций можно использовать InconsistentExpr, но тогда появляются лишние join'ы (а значит нужно еще больше памяти)
    private boolean useRecalculateClassesInsteadOfInconsisentExpr = true;

    public boolean isUseRecalculateClassesInsteadOfInconsisentExpr() {
        return useRecalculateClassesInsteadOfInconsisentExpr;
    }

    public void setUseRecalculateClassesInsteadOfInconsisentExpr(boolean useRecalculateClassesInsteadOfInconsisentExpr) {
        this.useRecalculateClassesInsteadOfInconsisentExpr = useRecalculateClassesInsteadOfInconsisentExpr;
    }

    private int pageSizeDefaultValue = 50;

    public int getPageSizeDefaultValue() {
        return pageSizeDefaultValue;
    }

    public void setPageSizeDefaultValue(int pageSizeDefaultValue) {
        this.pageSizeDefaultValue = pageSizeDefaultValue;
    }

    private boolean disableInnerFollows = false;

    public boolean isDisableInnerFollows() {
        return disableInnerFollows;
    }

    public void setDisableInnerFollows(boolean disableInnerFollows) {
        this.disableInnerFollows = disableInnerFollows;
    }

    private boolean disableGroupNotJoinsWheres = false;

    public boolean isDisableGroupNotJoinsWheres() {
        return disableGroupNotJoinsWheres;
    }

    public void setDisableGroupNotJoinsWheres(boolean disableGroupNotJoinsWheres) {
        this.disableGroupNotJoinsWheres = disableGroupNotJoinsWheres;
    }

    private int defaultTypeExecuteEnvironment = 2;

    public int getDefaultTypeExecuteEnvironment() {
        return defaultTypeExecuteEnvironment;
    }

    public void setDefaultTypeExecuteEnvironment(int defaultTypeExecuteEnvironment) {
        this.defaultTypeExecuteEnvironment = defaultTypeExecuteEnvironment;
    }

    private int timeoutNanosPerRow = 20;

    public int getTimeoutNanosPerRow() {
        return timeoutNanosPerRow;
    }

    public void setTimeoutNanosPerRow(int timeoutNanosPerRow) {
        this.timeoutNanosPerRow = timeoutNanosPerRow;
    }
    
    private boolean noDisablingNestedLoop = true;

    public boolean isNoDisablingNestedLoop() {
        return noDisablingNestedLoop;
    }

    public void setNoDisablingNestedLoop(boolean noDisablingNestedLoop) {
        this.noDisablingNestedLoop = noDisablingNestedLoop;
    }

    private int lastStepCoeff = 5; // для больших баз переходить на disableNestedLoop опасно

    public int getLastStepCoeff() {
        return lastStepCoeff;
    }

    public void setLastStepCoeff(int lastStepCoeff) {
        this.lastStepCoeff = lastStepCoeff;
    }

    private int subQueriesSplit = 4;

    public int getSubQueriesSplit() {
        return subQueriesSplit;
    }

    public void setSubQueriesSplit(int subQueriesSplit) {
        this.subQueriesSplit = subQueriesSplit;
    }

    private int subQueriesRowsThreshold = 1000; // до какого числа рядов не обращать внимание на статистику

    public int getSubQueriesRowsThreshold() {
        return subQueriesRowsThreshold;
    }

    public void setSubQueriesRowsThreshold(int subQueriesRowsThreshold) {
        this.subQueriesRowsThreshold = subQueriesRowsThreshold;
    }

    private int subQueriesRowsMax = 100000; // какое число записей не материализовать никогда

    public int getSubQueriesRowsMax() {
        return subQueriesRowsMax;
    }

    public void setSubQueriesRowsMax(int subQueriesRowsMax) {
        this.subQueriesRowsMax = subQueriesRowsMax;
    }

    private int subQueriesRowCountCoeff = 2; // коэффициент, для оценки  - определяет баланс между размером таблицы, количеством подзапросов
    private int subQueriesParentCoeff = 2; // коэффициент количество путей до вершины (по сути сколько раз подзапрос будет выполняться), для оценки  - определяет баланс между размером таблицы, количеством подзапросов
    private int subQueriesPessQueryCoeff = 2; // коэффициент для оценки - на сколько ее увеличивать если есть пессимистичный вариант выполнения запроса, нужно чтобы сначала больше оптимистичных выполнилось  

    public int getSubQueriesRowCountCoeff() {
        return subQueriesRowCountCoeff;
    }

    public void setSubQueriesRowCountCoeff(int subQueriesRowCountCoeff) {
        this.subQueriesRowCountCoeff = subQueriesRowCountCoeff;
    }

    public int getSubQueriesParentCoeff() {
        return subQueriesParentCoeff;
    }

    public void setSubQueriesParentCoeff(int subQueriesParentCoeff) {
        this.subQueriesParentCoeff = subQueriesParentCoeff;
    }

    public int getSubQueriesPessQueryCoeff() {
        return subQueriesPessQueryCoeff;
    }

    public void setSubQueriesPessQueryCoeff(int subQueriesPessQueryCoeff) {
        this.subQueriesPessQueryCoeff = subQueriesPessQueryCoeff;
    }

    private int explainThreshold = 100;

    public int getExplainThreshold() {
        return explainThreshold;
    }

    public void setExplainThreshold(int explainThreshold) {
        this.explainThreshold = explainThreshold;
    }

    private boolean useCastDivisionOperands = true;

    public boolean isUseCastDivisionOperands() {
        return useCastDivisionOperands;
    }

    public void setUseCastDivisionOperands(boolean useCastDivisionOperands) {
        this.useCastDivisionOperands = useCastDivisionOperands;
    }

    private boolean useSafeDivision = true;

    public boolean isUseSafeDivision() {
        return useSafeDivision;
    }

    public void setUseSafeDivision(boolean useSafeDivision) {
        this.useSafeDivision = useSafeDivision;
    }

    private boolean useSafeScaleCast = false; // использовать safeCast, вместо cast (актуально только для useScaleOpType - 1

    public boolean isUseSafeScaleCast() {
        return useSafeScaleCast;
    }

    public void setUseSafeScaleCast(boolean useSafeScaleCast) {
        this.useSafeScaleCast = useSafeScaleCast;
    }

    private int useScaleOpType = 1; // целая и дробная часть : MAX (0+1), SUM (2), соотвественно при 1 - идет CAST к типу (SAFE или не SAFE) определяется useSafeScaleCast, 2 - пока не реализовано поэтому работает как 0

    public int getUseScaleOpType() {
        return useScaleOpType;
    }

    public void setUseScaleOpType(int useScaleOpType) {
        this.useScaleOpType = useScaleOpType;
    }

    public boolean disableCompiledSubQueries = false;

    public boolean isDisableCompiledSubQueries() {
        return disableCompiledSubQueries;
    }

    public void setDisableCompiledSubQueries(boolean disableCompiledSubQueries) {
        this.disableCompiledSubQueries = disableCompiledSubQueries;
    }

    private boolean disableSetDroppedOptimization = false; // вообще не сильно полезная оптимизация, но раз сделали

    public boolean isDisableSetDroppedOptimization() {
        return disableSetDroppedOptimization;
    }

    public void setDisableSetDroppedOptimization(boolean disableSetDroppedOptimization) {
        this.disableSetDroppedOptimization = disableSetDroppedOptimization;
    }

    private boolean disableFirstChangesOptimization = false;

    public boolean isDisableFirstChangesOptimization() {
        return disableFirstChangesOptimization;
    }

    public void setDisableFirstChangesOptimization(boolean disableFirstChangesOptimization) {
        this.disableFirstChangesOptimization = disableFirstChangesOptimization;
    }

    private boolean useDeleteNoInline = true;

    public boolean isUseDeleteNoInline() {
        return useDeleteNoInline;
    }

    public void setUseDeleteNoInline(boolean useDeleteNoInline) {
        this.useDeleteNoInline = useDeleteNoInline;
    }

    private boolean disableUpdateTypeHeur = false;

    public boolean isDisableUpdateTypeHeur() {
        return disableUpdateTypeHeur;
    }

    public void setDisableUpdateTypeHeur(boolean disableUpdateTypeHeur) {
        this.disableUpdateTypeHeur = disableUpdateTypeHeur;
    }

    private int divStatUpdateTypeHeur = 100; // во сколько раз должна уменьшатся статистика фильтра, чтобы отключать поиск предыдущего объекта

    public int getDivStatUpdateTypeHeur() {
        return divStatUpdateTypeHeur;
    }

    public void setDivStatUpdateTypeHeur(int divStatUpdateTypeHeur) {
        this.divStatUpdateTypeHeur = divStatUpdateTypeHeur;
    }

    private boolean useUserChangesSync = true; // использовать для пользователя синхронизацию изменений

    public boolean getUseUserChangesSync() {
        return useUserChangesSync;
    }

    public void setUseUserChangesSync(boolean useUserChangesSync) {
        this.useUserChangesSync = useUserChangesSync;
    }

    private boolean packStatBackwardCompatibility = false;

    public boolean isPackStatBackwardCompatibility() {
        return packStatBackwardCompatibility;
    }

    public void setPackStatBackwardCompatibility(boolean packStatBackwardCompatibility) {
        this.packStatBackwardCompatibility = packStatBackwardCompatibility;
    }

    private boolean noTransSyncDB = false;

    public boolean isNoTransSyncDB() {
        return noTransSyncDB;
    }

    public void setNoTransSyncDB(boolean noTransSyncDB) {
        this.noTransSyncDB = noTransSyncDB;
    }

    private boolean startServerAnyWay = false;

    public boolean isStartServerAnyWay() {
        return startServerAnyWay;
    }

    public void setStartServerAnyWay(boolean startServerAnyWay) {
        this.startServerAnyWay = startServerAnyWay;
    }

    private boolean disableAntiJoinOptimization = false;

    public boolean isDisableAntiJoinOptimization() {
        return disableAntiJoinOptimization;
    }

    public void setDisableAntiJoinOptimization(boolean disableAntiJoinOptimization) {
        this.disableAntiJoinOptimization = disableAntiJoinOptimization;
    }

    private boolean disableHiddenHintReallyChanged = true;

    public boolean isDisableHiddenHintReallyChanged() {
        return disableHiddenHintReallyChanged;
    }

    public void setDisableHiddenHintReallyChanged(boolean disableHiddenHintReallyChanged) {
        this.disableHiddenHintReallyChanged = disableHiddenHintReallyChanged;
    }

    public boolean isGroupByTables() {
        return groupByTables;
    }

    public void setGroupByTables(boolean groupByTables) {
        this.groupByTables = groupByTables;
    }

    public int getProfilerBatchSize() {
        return profilerBatchSize;
    }

    public void setProfilerBatchSize(int profilerBatchSize) {
        this.profilerBatchSize = profilerBatchSize;
    }

    public int getThreadAllocatedMemoryPeriod() {
        return threadAllocatedMemoryPeriod;
    }

    public void setThreadAllocatedMemoryPeriod(int threadAllocatedMemoryPeriod) {
        this.threadAllocatedMemoryPeriod = threadAllocatedMemoryPeriod;
    }

    public long getMaxThreadAllocatedBytes() {
        return maxThreadAllocatedBytes;
    }

    public void setMaxThreadAllocatedBytes(long maxThreadAllocatedBytes) {
        this.maxThreadAllocatedBytes = maxThreadAllocatedBytes;
    }

    public long getExcessThreadAllocatedBytes() {
        return excessThreadAllocatedBytes;
    }

    public void setExcessThreadAllocatedBytes(long excessThreadAllocatedBytes) {
        this.excessThreadAllocatedBytes = excessThreadAllocatedBytes;
    }

    public int getAccessInterruptCount() {
        return accessInterruptCount;
    }

    public void setAccessInterruptCount(int accessInterruptCount) {
        this.accessInterruptCount = accessInterruptCount;
    }

    public boolean isLogSqlProcesses() {
        return logSqlProcesses;
    }

    public void setLogSqlProcesses(boolean logSqlProcesses) {
        this.logSqlProcesses = logSqlProcesses;
    }

    private int cacheMissesStatsLimit = 10000;

    public int getCacheMissesStatsLimit() {
        return cacheMissesStatsLimit;
    }

    public void setCacheMissesStatsLimit(int cacheMissesStatsLimit) {
        this.cacheMissesStatsLimit = cacheMissesStatsLimit;
    }

    public boolean isReadAllocatedBytes() {
        return readAllocatedBytes;
    }

    public void setReadAllocatedBytes(boolean readAllocatedBytes) {
        this.readAllocatedBytes = readAllocatedBytes;
    }

    private int updateStatisticsLimit = 300; // при изменении какого количества записей будет принудительный ANALYZE таблицы делаться

    public int getUpdateStatisticsLimit() {
        return updateStatisticsLimit;
    }

    public void setUpdateStatisticsLimit(int updateStatisticsLimit) {
        this.updateStatisticsLimit = updateStatisticsLimit;
    }

    private int maxRecursionStatsIterations = 3;

    public int getMaxRecursionStatsIterations() {
        return maxRecursionStatsIterations;
    }

    public void setMaxRecursionStatsIterations(int maxRecursionStatsIterations) {
        this.maxRecursionStatsIterations = maxRecursionStatsIterations;
    }

    private boolean useSavepointsForExceptions = true;

    public boolean isUseSavepointsForExceptions() {
        return useSavepointsForExceptions;
    }

    public void setUseSavepointsForExceptions(boolean useSavepointsForExceptions) {
        this.useSavepointsForExceptions = useSavepointsForExceptions;
    }

    private int maxLength = 127;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    private int maxPrecision = 32;

    public int getMaxPrecision() {
        return maxPrecision;
    }

    public void setMaxPrecision(int maxPrecision) {
        this.maxPrecision = maxPrecision;
    }

    private int maxEdgeIterations = 100;

    public int getMaxEdgeIterations() {
        return maxEdgeIterations;
    }

    public void setMaxEdgeIterations(int maxEdgeIterations) {
        this.maxEdgeIterations = maxEdgeIterations;
    }

//    private int minClassDataIndexCount = 1000; // при превышении какого количества записей строить индексы
//
//    public int getMinClassDataIndexCount() {
//        return minClassDataIndexCount;
//    }
//
//    public void setMinClassDataIndexCount(int minClassDataIndexCount) {
//        this.minClassDataIndexCount = minClassDataIndexCount;
//    }

    private boolean explainNoAnalyze = false;

    public boolean isExplainNoAnalyze() {
        return explainNoAnalyze;
    }

    public void setExplainNoAnalyze(boolean explainNoAnalyze) {
        this.explainNoAnalyze = explainNoAnalyze;
    }

    private boolean logConflictStack = false;

    public boolean isLogConflictStack() {
        return logConflictStack;
    }

    public void setLogConflictStack(boolean logConflictStack) {
        this.logConflictStack = logConflictStack;
    }

    private boolean explainJavaStack = false;

    public boolean isExplainJavaStack() {
        return explainJavaStack;
    }

    public void setExplainJavaStack(boolean explainJavaStack) {
        this.explainJavaStack = explainJavaStack;
    }
    
    private boolean explainCompile = false;

    public boolean isExplainCompile() {
        if(SystemProperties.inTestMode)
            return true;

        return explainCompile;
    }

    public void setExplainCompile(boolean explainCompile) {
        this.explainCompile = explainCompile;
    }

    private boolean disableSyncStatProps = false;

    public boolean isDisableSyncStatProps() {
        return disableSyncStatProps;
    }

    public void setDisableSyncStatProps(boolean disableSyncStatProps) {
        this.disableSyncStatProps = disableSyncStatProps;
    }

    // если сильно "наобъединяться" бОльшая вероятность, что не слишком умная СУБД сделает неправильный план (впрочем после materialized subqueries должно уйти)
    // в запросе выполнения всегда объединять join'ы с одинаковой статистикой, а не только при превышении порога
    // проблема в "орел-решка", когда есть очень волатильная статистика (0 или очень большая статистика), сейчас берется пессимистичный вариант основным, к нему присоединяются "хорошие" варианты с индексом что не очень хорошо
    // до разделения пессимистично - оптимистичной статистики нельзя включать (примеры : WHEN value THEN HugeStat ELSE SmallStat, WHEN isParent - где IsParent рекурсивное свойство, еще один яркий пример неравномерной статистики)
    private int collapseStatsCount = 100;
    private int collapseStatsComplexity = 5000; // 100 - лучше в несколько раз на очень больших запросов, и даже 200, но все же иногда попадает в "орел-решку" и вообще в неравномерную статистику

    public int getCollapseStatsCount() {
        return collapseStatsCount;
    }

    public void setCollapseStatsCount(int collapseStatsCount) {
        this.collapseStatsCount = collapseStatsCount;
    }

    public int getCollapseStatsComplexity() {
        return collapseStatsComplexity;
    }

    public void setCollapseStatsComplexity(int collapseStatsComplexity) {
        this.collapseStatsComplexity = collapseStatsComplexity;
    }

    private boolean useSafeMonitorProcess = false;

    public boolean isUseSafeMonitorProcess() {
        return useSafeMonitorProcess;
    }

    public void setUseSafeMonitorProcess(boolean useSafeMonitorProcess) {
        this.useSafeMonitorProcess = useSafeMonitorProcess;
    }

    private boolean enableSingleReadObjectsOptimization = true;

    public boolean isEnableSingleReadObjectsOptimization() {
        return enableSingleReadObjectsOptimization;
    }

    public void setEnableSingleReadObjectsOptimization(boolean enableSingleReadObjectsOptimization) {
        this.enableSingleReadObjectsOptimization = enableSingleReadObjectsOptimization;
    }

    private int conflictSleepThreshold = 3; // начинать засыпать после попытки включительно
    private double conflictSleepTimeDegree = 2; // кол-во попыток в степени этого времени (порог ожидания)

    public int getConflictSleepThreshold() {
        return conflictSleepThreshold;
    }

    public void setConflictSleepThreshold(int conflictSleepThreshold) {
        this.conflictSleepThreshold = conflictSleepThreshold;
    }

    public double getConflictSleepTimeDegree() {
        return conflictSleepTimeDegree;
    }

    public void setConflictSleepTimeDegree(double conflictSleepTimeDegree) {
        this.conflictSleepTimeDegree = conflictSleepTimeDegree;
    }

    private int deadLockThreshold = 0; // после какой попытки начинать управлять deadLock приоритетом

    public int getDeadLockThreshold() {
        return deadLockThreshold;
    }

    public void setDeadLockThreshold(int deadLockThreshold) {
        this.deadLockThreshold = deadLockThreshold;
    }

    // блок average - "нормальные" значения для запроса \ соединения
    private int queryLengthAverageMax = 10000; // 10 кб
    private int queryTimeAverageMax = 10000; // 10 секунд
    private int usedTempRowsAverageMax = 500; // 500 записей всего (на 1000 перестарт уже близок к секунде может быть)
    private int maxUsedTempRowsAverageMax = 5000; // сколько в моменте использовано, по хорошему должен быть больше usedTempRows, а степень наоборот

    private int lastTempTablesActivityAverageMax = 180000; // 3 минуты отсуствует активность

    private double timeStartedAverageMaxCoeff = 1.25; // какой коэффициент от среднего времени жизни connection'а брать, меньше 1 нет смысла брать, так как соединения будут просто перестартовывать по очереди

    // степени - влияния
    private int queryExecuteDegree = 2;
    private double usedTempRowsDegree = 4;
    private double maxUsedTempRowsDegree = 2; // по хорошему должен быть меньше usedTempRows, иначе может начать постоянно перестартовывать соединение с большим used
    private double timeStartedDegree = 8; // по хорошему должен быть больше usedTempRowsDegree, чтобы даже те кто использует много таблиц когда-нибудь перестартовывались

    private int periodRestartConnections = 60; // 1 минута
    private double percentRestartConnections = 1; // 1% соединений (соотвествено 1,5 часа среднее время жизни соединения при равномерной загрузке)

    private int periodProcessDump = 60; //1 минута

    public int getQueryLengthAverageMax() {
        return queryLengthAverageMax;
    }

    public void setQueryLengthAverageMax(int queryLengthAverageMax) {
        this.queryLengthAverageMax = queryLengthAverageMax;
    }
    
    private int outSelectLengthThreshold = 100000;

    public int getOutSelectLengthThreshold() {
        return outSelectLengthThreshold;
    }

    public void setOutSelectLengthThreshold(int outSelectLengthThreshold) {
        this.outSelectLengthThreshold = outSelectLengthThreshold;
    }

    public int getQueryTimeAverageMax() {
        return queryTimeAverageMax;
    }

    public void setQueryTimeAverageMax(int queryTimeAverageMax) {
        this.queryTimeAverageMax = queryTimeAverageMax;
    }

    public int getUsedTempRowsAverageMax() {
        return usedTempRowsAverageMax;
    }

    public void setUsedTempRowsAverageMax(int usedTempRowsAverageMax) {
        this.usedTempRowsAverageMax = usedTempRowsAverageMax;
    }

    public double getTimeStartedAverageMaxCoeff() {
        return timeStartedAverageMaxCoeff;
    }

    public int getLastTempTablesActivityAverageMax() {
        return lastTempTablesActivityAverageMax;
    }

    public void setLastTempTablesActivityAverageMax(int lastTempTablesActivityAverageMax) {
        this.lastTempTablesActivityAverageMax = lastTempTablesActivityAverageMax;
    }

    public void setTimeStartedAverageMaxCoeff(double timeStartedAverageMaxCoeff) {
        this.timeStartedAverageMaxCoeff = timeStartedAverageMaxCoeff;
    }

    public int getQueryExecuteDegree() {
        return queryExecuteDegree;
    }

    public void setQueryExecuteDegree(int queryExecuteDegree) {
        this.queryExecuteDegree = queryExecuteDegree;
    }

    public double getUsedTempRowsDegree() {
        return usedTempRowsDegree;
    }

    public void setUsedTempRowsDegree(double usedTempRowsDegree) {
        this.usedTempRowsDegree = usedTempRowsDegree;
    }

    public double getMaxUsedTempRowsDegree() {
        return maxUsedTempRowsDegree;
    }

    public void setMaxUsedTempRowsDegree(double maxUsedTempRowsDegree) {
        this.maxUsedTempRowsDegree = maxUsedTempRowsDegree;
    }

    public double getTimeStartedDegree() {
        return timeStartedDegree;
    }

    public void setTimeStartedDegree(double timeStartedDegree) {
        this.timeStartedDegree = timeStartedDegree;
    }

    public double getPercentRestartConnections() {
        return percentRestartConnections;
    }

    public void setPercentRestartConnections(double percentRestartConnections) {
        this.percentRestartConnections = percentRestartConnections;
    }

    public int getPeriodRestartConnections() {
        return periodRestartConnections;
    }

    public void setPeriodRestartConnections(int periodRestartConnections) {
        this.periodRestartConnections = periodRestartConnections;
    }

    public int getPeriodProcessDump() {
        return periodProcessDump;
    }

    public void setPeriodProcessDump(int periodProcessDump) {
        this.periodProcessDump = periodProcessDump;
    }

    public int getMaxUsedTempRowsAverageMax() {
        return maxUsedTempRowsAverageMax;
    }

    public void setMaxUsedTempRowsAverageMax(int maxUsedTempRowsAverageMax) {
        this.maxUsedTempRowsAverageMax = maxUsedTempRowsAverageMax;
    }

    private boolean disableRegisterChanges = false; // временно, потом убрать

    public boolean isDisableRegisterChanges() {
        return disableRegisterChanges;
    }

    public void setDisableRegisterChanges(boolean disableRegisterChanges) {
        this.disableRegisterChanges = disableRegisterChanges;
    }
    
    private int classOptimizationActionCasesCount = 3;

    public int getClassOptimizationActionCasesCount() {
        return classOptimizationActionCasesCount;
    }

    public void setClassOptimizationActionCasesCount(int classOptimizationActionCasesCount) {
        this.classOptimizationActionCasesCount = classOptimizationActionCasesCount;
    }
    
    private boolean extendedSQLConnectionLog = false;

    public boolean isExtendedSQLConnectionLog() {
        return extendedSQLConnectionLog;
    }

    public void setExtendedSQLConnectionLog(boolean extendedSQLConnectionLog) {
        this.extendedSQLConnectionLog = extendedSQLConnectionLog;
    }

    private int closeFormDelay = 5000;

    public int getCloseFormDelay() {
        return closeFormDelay;
    }

    public void setCloseFormDelay(int closeFormDelay) {
        this.closeFormDelay = closeFormDelay;
    }

    private int waitSchedulerCanceledDelay = 5000;

    public int getWaitSchedulerCanceledDelay() {
        return waitSchedulerCanceledDelay;
    }

    public void setWaitSchedulerCanceledDelay(int waitSchedulerCanceledDelay) {
        this.waitSchedulerCanceledDelay = waitSchedulerCanceledDelay;
    }

    private boolean disableAsyncClose = false; // проблема в том что DataSession не синхронизирована (assertion, что синхронизация обеспечивается модальностью вызовов), а unreferenced нарушает этот assertion (и непонятно как ее обойти, да и не понятно имеет ли смысл)
    // та же проблема по идее с unreferenced

    public boolean isDisableAsyncClose() {
        return disableAsyncClose;
    }

    public void setDisableAsyncClose(boolean disableAsyncClose) {
        this.disableAsyncClose = disableAsyncClose;
    }

    private boolean disableFinalized = false; // есть вопрос с синхронизацией explicitClose FormInstance

    public boolean isDisableFinalized() {
        return disableFinalized;
    }

    public void setDisableFinalized(boolean disableFinalized) {
        this.disableFinalized = disableFinalized;
    }

    private boolean checkSessionCount = false;

    public boolean isCheckSessionCount() {
        return checkSessionCount;
    }

    public void setCheckSessionCount(boolean checkSessionCount) {
        this.checkSessionCount = checkSessionCount;
    }

    private boolean disablePrereadCaches = true;

    public boolean isDisablePrereadCaches() {
        return disablePrereadCaches;
    }

    public void setDisablePrereadCaches(boolean disablePrereadCaches) {
        this.disablePrereadCaches = disablePrereadCaches;
    }
    
    private int subQueryLargeDepth = 6; // высокая глубина, подозрение на бесконечное проталкивание (смотрим только на cost при проталкивании)
    private int subQueryInfiniteDepth = 12; //  бесконечная глубина, считаем что бесконечное проталкивание (ничего не проталкиваем)

    public int getSubQueryLargeDepth() {
        return subQueryLargeDepth;
    }

    public void setSubQueryLargeDepth(int subQueryLargeDepth) {
        this.subQueryLargeDepth = subQueryLargeDepth;
    }

    public int getSubQueryInfiniteDepth() {
        return subQueryInfiniteDepth;
    }

    public void setSubQueryInfiniteDepth(int subQueryInfiniteDepth) {
        this.subQueryInfiniteDepth = subQueryInfiniteDepth;
    }
    
    // преобразует все partition'ы в ключи и "выносит их наружу" (добавляет избыточное связывание) - так улучшается push down (избыточное связывание может быть например по выражение vk=e, на которое есть предикат равенства снаружи e=0, в этом случае именно этот предикат и протолкнется) ну и теоретически статистика точнее, насчет sql пока не понятно (хотя с другой стороны больше не меньше, хотя с константами в partition у sql были вопросы) 
    private boolean transformPartitionExprsToKeys = true;

    public boolean isTransformPartitionExprsToKeys() {
        return transformPartitionExprsToKeys;
    }

    public void setTransformPartitionExprsToKeys(boolean transformPartitionExprsToKeys) {
        this.transformPartitionExprsToKeys = transformPartitionExprsToKeys;
    }

    private int logHeurStackSize = 1;

    public int getLogHeurStackSize() {
        return logHeurStackSize;
    }

    public void setLogHeurStackSize(int logHeurStackSize) {
        this.logHeurStackSize = logHeurStackSize;
    }

    private int constraintRowsLimit = 30;

    public int getConstraintRowsLimit() {
        return constraintRowsLimit;
    }

    public void setConstraintRowsLimit(int constraintRowsLimit) {
        this.constraintRowsLimit = constraintRowsLimit;
    }
    
    private boolean disableCheckDataClasses = false; // проверка на целостность изменений свойств перед применением транзакции (без SERIALIZABLE не имеет особого смысла)

    public boolean isDisableCheckDataClasses() {
        return disableCheckDataClasses;
    }

    public void setDisableCheckDataClasses(boolean disableCheckDataClasses) {
        this.disableCheckDataClasses = disableCheckDataClasses;
    }
    
    private boolean isClustered = false;

    public boolean isIsClustered() {
        return isClustered;
    }

    public void setIsClustered(boolean isClustered) {
        this.isClustered = isClustered;
    }
    
    private boolean disableAdjustLimitHeur = false;

    public boolean isDisableAdjustLimitHeur() {
        return disableAdjustLimitHeur;
    }

    public void setDisableAdjustLimitHeur(boolean disableAdjustLimitHeur) {
        this.disableAdjustLimitHeur = disableAdjustLimitHeur;
    }

    private int usePessQueryHeurWhenReducedMore = 15;

    public int getUsePessQueryHeurWhenReducedMore() {
        return usePessQueryHeurWhenReducedMore;
    }

    public void setUsePessQueryHeurWhenReducedMore(int usePessQueryHeurWhenReducedMore) {
        this.usePessQueryHeurWhenReducedMore = usePessQueryHeurWhenReducedMore;
    }

    // выключен так как обычно выталкиваемы предикаты в GroupLast не делают и они как правило приходят извне, во всяком случае пока подтвержденных случаев (кроме одного когда только 3 помогает не видели)
    private int useGroupLastOpt = 1; // 0 (no) - не используем, 1 (pushedIn) - используем только полный pushedInWhere, 2 (mixed) - используем полный pushedInWhere если есть иначе pushedOutWhere, 3 (pushedOut) - всегда pushedOut

    public int getUseGroupLastOpt() {
        if(SystemProperties.inTestMode)
            return 3;
        return useGroupLastOpt;
    }

    public void setUseGroupLastOpt(int useGroupLastOpt) {
        this.useGroupLastOpt = useGroupLastOpt;
    }

    private boolean defaultCompareForStringContains = false;

    public boolean isDefaultCompareForStringContains() {
        return defaultCompareForStringContains;
    }

    public void setDefaultCompareForStringContains(boolean defaultCompareForStringContains) {
        this.defaultCompareForStringContains = defaultCompareForStringContains;
    }
    
    private boolean disableCorrelations = true; // enable'ить только при включенном sinpleApplyRemoveClasses (если включен singleApply) иначе при включенном singleApply изменения корреляции применятся, а в изменениях где корреляция используется ее никто не обновит

    public boolean isDisableCorrelations() {
        if(SystemProperties.inTestMode)
            return false;
        return disableCorrelations;
    }
    
    public void setDisableCorrelations(boolean disableCorrelations) {
        this.disableCorrelations = disableCorrelations;
    }
    
    private boolean enableCloseThreadLocalSqlInNativeThreads = true;

    public boolean isEnableCloseThreadLocalSqlInNativeThreads() {
        return enableCloseThreadLocalSqlInNativeThreads;
    }

    public void setEnableCloseThreadLocalSqlInNativeThreads(boolean enableCloseThreadLocalSqlInNativeThreads) {
        this.enableCloseThreadLocalSqlInNativeThreads = enableCloseThreadLocalSqlInNativeThreads;
    }
    
    private boolean useHeurCanBeChanged = true;

    public boolean isUseHeurCanBeChanged() {
        return useHeurCanBeChanged;
    }

    public void setUseHeurCanBeChanged(boolean useHeurCanBeChanged) {
        this.useHeurCanBeChanged = useHeurCanBeChanged;
    }

    private boolean enableInteractiveAssertLog = false; // temporary

    public boolean isEnableInteractiveAssertLog() {
        return enableInteractiveAssertLog;
    }

    public void setEnableInteractiveAssertLog(boolean enableInteractiveAssertLog) {
        this.enableInteractiveAssertLog = enableInteractiveAssertLog;
    }
    
    private boolean disablePessQueries = false;

    public boolean isDisablePessQueries() {
        return disablePessQueries;
    }

    public void setDisablePessQueries(boolean disablePessQueries) {
        this.disablePessQueries = disablePessQueries;
    }

    //для записи stackTrace java потока в конструкторе SQLSession. Используется в мониторе процессов
    private boolean stacktraceInSQLSession = false;

    public boolean isStacktraceInSQLSession() {
        return stacktraceInSQLSession;
    }

    public void setStacktraceInSQLSession(boolean stacktraceInSQLSession) {
        this.stacktraceInSQLSession = stacktraceInSQLSession;
    }
    
    private boolean useRequestTimeout = true;

    public boolean isUseRequestTimeout() {
        return useRequestTimeout;
    }

    public void setUseRequestTimeout(boolean useRequestTimeout) {
        this.useRequestTimeout = useRequestTimeout;
    }

    //для блокирующего чтения в операторе READ.
    private boolean blockingFileRead = false;

    public boolean isBlockingFileRead() {
        return blockingFileRead;
    }
    
    public void setBlockingFileRead(boolean blockingFileRead) {
        this.blockingFileRead = blockingFileRead;
    }

    public boolean isUseShowIfInReports() {
        return useShowIfInReports;
    }

    public void setUseShowIfInReports(boolean useShowIfInReports) {
        this.useShowIfInReports = useShowIfInReports;
    }

    public int getMinSizeForExcelStreamingReader() {
        return minSizeForExcelStreamingReader;
    }

    public void setMinSizeForExcelStreamingReader(int minSizeForExcelStreamingReader) {
        this.minSizeForExcelStreamingReader = minSizeForExcelStreamingReader;
    }
}
