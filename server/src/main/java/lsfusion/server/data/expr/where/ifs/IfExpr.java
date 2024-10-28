package lsfusion.server.data.expr.where.ifs;

import lsfusion.base.BaseUtils;
import lsfusion.base.col.ListFact;
import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.col.interfaces.mutable.MMap;
import lsfusion.base.mutability.TwinImmutableObject;
import lsfusion.interop.form.property.Compare;
import lsfusion.server.base.caches.ManualLazy;
import lsfusion.server.data.caches.OuterContext;
import lsfusion.server.data.caches.hash.HashContext;
import lsfusion.server.data.expr.BaseExpr;
import lsfusion.server.data.expr.Expr;
import lsfusion.server.data.expr.classes.IsClassType;
import lsfusion.server.data.expr.join.classes.ObjectClassField;
import lsfusion.server.data.expr.key.KeyType;
import lsfusion.server.data.expr.where.cases.ExprCase;
import lsfusion.server.data.expr.where.cases.ExprCaseList;
import lsfusion.server.data.query.compile.CompileSource;
import lsfusion.server.data.query.compile.FJData;
import lsfusion.server.data.stat.Stat;
import lsfusion.server.data.translate.ExprTranslator;
import lsfusion.server.data.translate.MapTranslate;
import lsfusion.server.data.type.Type;
import lsfusion.server.data.type.reader.ClassReader;
import lsfusion.server.data.where.Where;
import lsfusion.server.logics.classes.ConcreteClass;
import lsfusion.server.logics.classes.ValueClassSet;
import lsfusion.server.logics.classes.data.DataClass;

public class IfExpr extends Expr {

    public final Where ifWhere;
    public final Expr trueExpr;
    public final Expr falseExpr;

    public IfExpr(Where ifWhere, Expr trueExpr, Expr falseExpr) {
        this.ifWhere = ifWhere;
        this.trueExpr = trueExpr;
        this.falseExpr = falseExpr;

        assert assertOrder(ifWhere, trueExpr, falseExpr);
    }

    private static boolean assertOrder(Where where, Expr trueExpr, Expr falseExpr) {
        int trueDepth = trueExpr.getWhereDepth(); int falseDepth = falseExpr.getWhereDepth();
        return trueDepth > falseDepth || (trueDepth==falseDepth && !where.isNot());
    }

    private int whereDepth = -1;
    @ManualLazy
    public int getWhereDepth() {
        if(whereDepth<0)
            whereDepth = trueExpr.getWhereDepth() + 1;
        return whereDepth;
    }

    private static Expr createPack(Where where, Expr trueExpr, Expr falseExpr, Where falseWhere, boolean pack) {
        trueExpr = trueExpr.followFalse(orExprCheck(falseWhere, where.not()), pack);
        falseExpr = falseExpr.followFalse(orExprCheck(falseWhere, where), pack);

        if(BaseUtils.hashEquals(trueExpr, falseExpr))
            return trueExpr;

       // вообще должен был быть equals (и верхняя проверка не нужна), но будет слишком сложное условие
        where = where.followFalse(orExprCheck(falseWhere, andExprCheck(trueExpr.getWhere().not(), falseExpr.getWhere().not())), pack);
        if(where.isTrue())
            return trueExpr;
        if(where.isFalse())
            return falseExpr;
        // вопрос гнать рекурсию или нет???

        return createMean(where, trueExpr, falseExpr, pack, false);
    }

    // проверяем на следствия как в Where, если B=>A и A.FF(B) упрощается меняем местами
    // приходится проверять в обе стороны так как лексикографику по следствиям невозможно построить
    private static Expr createMean(Where where, Expr trueExpr, Expr falseExpr, boolean pack, boolean falseChecked) {
        if(trueExpr instanceof IfExpr) {
            IfExpr trueIfExpr = ((IfExpr)trueExpr);
            if(trueIfExpr.ifWhere.means(where)) {
                Where.FollowChange change = new Where.FollowChange();
                Where meanWhere = where.followFalseChange(trueIfExpr.ifWhere, pack, change);
                if(change.type!= Where.FollowType.EQUALS)
                    return createOrderMerge(trueIfExpr.ifWhere, trueIfExpr.trueExpr, createMean(meanWhere, trueIfExpr.falseExpr, falseExpr, pack, falseChecked));
            }
        }

        if(falseChecked)
            return createOrderMerge(where, trueExpr, falseExpr);
        else
            return createMean(where.not(), falseExpr, trueExpr, pack, true);
    }

    private static Expr createOrderMerge(Where where, Expr trueExpr, Expr falseExpr) {
        if(trueExpr.getWhereDepth() >= falseExpr.getWhereDepth())
            return createMerge(where, trueExpr, falseExpr);
        else
            return createMerge(where.not(), falseExpr, trueExpr);
    }

    private static Expr createMerge(Where where, Expr trueExpr, Expr falseExpr) {
        assert trueExpr.getWhereDepth() >= falseExpr.getWhereDepth();
        // нужно найти в if'е равенства
        if(trueExpr instanceof IfExpr) { // рекурсию гнать смысла нет, так как внутри уже целостная структура и повторений не будет
            IfExpr trueIfExpr = ((IfExpr)trueExpr);
            if(BaseUtils.hashEquals(trueIfExpr.falseExpr, falseExpr))
                return createFinalOrder(where.and(trueIfExpr.ifWhere), trueIfExpr.trueExpr, falseExpr);
            if(BaseUtils.hashEquals(trueIfExpr.trueExpr, falseExpr))
                return createFinalOrder(trueIfExpr.ifWhere.or(where.not()), falseExpr, trueIfExpr.falseExpr);
        }
        return createFinalOrder(where, trueExpr, falseExpr);
    }

    private static Expr createFinalOrder(Where where, Expr trueExpr, Expr falseExpr) {
        if(trueExpr.getWhereDepth()==falseExpr.getWhereDepth() && where.isNot())
            return new IfExpr(where.not(), falseExpr, trueExpr);
        else
            return new IfExpr(where, trueExpr, falseExpr);
    }

    public static Expr create(Where where, Expr trueExpr, Expr falseExpr) {
        return createPack(where, trueExpr, falseExpr, Where.FALSE(), false);
    }

    public Expr followFalse(Where where, boolean pack) {
        return createPack(ifWhere, trueExpr, falseExpr, where, pack);
    }

    public ConcreteClass getStaticClass() {
        ConcreteClass trueClass = trueExpr.getStaticClass();
        if(trueClass==null)
            return null;
        ConcreteClass falseClass = falseExpr.getStaticClass();
        if(falseClass==null)
            return null;
        if(BaseUtils.hashEquals(trueClass, falseClass))
            return trueClass;
        return null;
    }

    public Type getType(KeyType keyType) { // порядок высот
        Type trueType = trueExpr.getType(keyType);
        if (trueType instanceof DataClass) {
            Type falseType = falseExpr.getType(keyType);
            return falseType != null ? trueType.getCompatible(falseType) : trueType;
        } else {
            return trueType;
        }
    }
    public Stat getTypeStat(Where fullWhere, boolean forJoin) {
        return trueExpr.getTypeStat(fullWhere.and(ifWhere), forJoin);
    }

    public boolean calcTwins(TwinImmutableObject o) { // порядок высот / общий
        return ifWhere.equals(((IfExpr)o).ifWhere) && trueExpr.equals(((IfExpr)o).trueExpr) && falseExpr.equals(((IfExpr) o).falseExpr);
    }

    public int hash(HashContext hashContext) { // порядок высот / общий
        return 31 * (31 * ifWhere.hashOuter(hashContext) + trueExpr.hashOuter(hashContext)) + falseExpr.hashOuter(hashContext);
    }

    public Where getBaseWhere() {
        return ifWhere;
    }

    public ClassReader getReader(KeyType keyType) {
        return getType(keyType);
    }

    public Where calculateWhere() {
        return ifWhere.ifElse(trueExpr.getWhere(), falseExpr.getWhere());
    }

    public ExprCaseList getCases() {
        return new ExprCaseList(ListFact.toList(new ExprCase(ifWhere, trueExpr), new ExprCase(Where.TRUE(), falseExpr)));
    }

    public Expr classExpr(ImSet<ObjectClassField> classes, IsClassType type) {
        return trueExpr.classExpr(classes, type).ifElse(ifWhere, falseExpr.classExpr(classes, type));
    }

    public Where isClass(ValueClassSet set, IsClassType type) {
        return ifWhere.ifElse(trueExpr.isClass(set, type), falseExpr.isClass(set, type));
    }

    public Where compareBase(BaseExpr expr, Compare compareBack) {
        return ifWhere.ifElse(trueExpr.compareBase(expr, compareBack), falseExpr.compareBase(expr, compareBack));
    }

    public Where compare(Expr expr, Compare compare) {
        return ifWhere.ifElse(trueExpr.compare(expr, compare), falseExpr.compare(expr, compare));
    }

    public Expr translate(ExprTranslator translator) {
        return IfExpr.create(ifWhere.translateExpr(translator), trueExpr.translateExpr(translator), falseExpr.translateExpr(translator));
    }

    protected Expr translate(MapTranslate translator) {
        return new IfExpr(ifWhere.translateOuter(translator), trueExpr.translateOuter(translator), falseExpr.translateOuter(translator));
    }

    public String getSource(CompileSource compile, boolean needValue) {
        if (compile instanceof ToString)
            return "IF(" + ifWhere.getSource(compile) + "," + trueExpr.getSource(compile, needValue) + "," + falseExpr.getSource(compile, needValue) + ")";

        return compile.syntax.getIIF(ifWhere.getSource(compile), trueExpr.getSource(compile, needValue), falseExpr.getSource(compile, needValue));
    }

    public void fillJoinWheres(MMap<FJData, Where> joins, Where andWhere) {
        ifWhere.fillJoinWheres(joins, andWhere);
        trueExpr.fillJoinWheres(joins, andWhere.and(ifWhere));
        falseExpr.fillJoinWheres(joins, andWhere.and(ifWhere.not()));
    }

    public ImSet<OuterContext> calculateOuterDepends() {
        return SetFact.<OuterContext>toSet(ifWhere, trueExpr, falseExpr);
    }

    public ImSet<BaseExpr> getBaseExprs() {
        return trueExpr.getBaseExprs().merge(falseExpr.getBaseExprs());
    }

    protected boolean isComplex() {
        return true;
    }

    @Override
    public boolean isAlwaysPositiveOrNull() {
        return trueExpr.isAlwaysPositiveOrNull() && falseExpr.isAlwaysPositiveOrNull();
    }
}
