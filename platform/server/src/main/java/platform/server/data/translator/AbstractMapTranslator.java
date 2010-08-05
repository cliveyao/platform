package platform.server.data.translator;

import platform.server.data.expr.*;
import platform.base.ImmutableObject;

import java.util.*;

public abstract class AbstractMapTranslator extends ImmutableObject implements MapTranslate  {

    public <K> Map<K, BaseExpr> translateDirect(Map<K, ? extends BaseExpr> map) {
        Map<K, BaseExpr> transMap = new HashMap<K, BaseExpr>();
        for(Map.Entry<K,? extends BaseExpr> entry : map.entrySet())
            transMap.put(entry.getKey(),entry.getValue().translateOuter(this));
        return transMap;
    }

    public <K> Map<BaseExpr,K> translateKeys(Map<? extends BaseExpr, K> map) {
        Map<BaseExpr, K> transMap = new HashMap<BaseExpr, K>();
        for(Map.Entry<? extends BaseExpr,K> entry : map.entrySet())
            transMap.put(entry.getKey().translateOuter(this),entry.getValue());
        return transMap;
    }

    // для кэша classWhere на самом деле надо
    public <K> Map<K, VariableClassExpr> translateVariable(Map<K, ? extends VariableClassExpr> map) {
        Map<K,VariableClassExpr> transMap = new HashMap<K, VariableClassExpr>();
        for(Map.Entry<K,? extends VariableClassExpr> entry : map.entrySet())
            transMap.put(entry.getKey(),entry.getValue().translateOuter(this));
        return transMap;
    }

    public <K> Map<K, Expr> translate(Map<K, ? extends Expr> map) {
        Map<K, Expr> transMap = new HashMap<K, Expr>();
        for(Map.Entry<K,? extends Expr> entry : map.entrySet())
            transMap.put(entry.getKey(),entry.getValue().translateOuter(this));
        return transMap;
    }

    public List<BaseExpr> translateDirect(List<BaseExpr> list) {
        List<BaseExpr> result = new ArrayList<BaseExpr>();
        for(BaseExpr expr : list)
            result.add(expr.translateOuter(this));
        return result;
    }

    public Set<BaseExpr> translateDirect(Set<BaseExpr> set) {
        Set<BaseExpr> result = new HashSet<BaseExpr>();
        for(BaseExpr expr : set)
            result.add(expr.translateOuter(this));
        return result;
    }

    public Set<KeyExpr> translateKeys(Set<KeyExpr> set) {
        Set<KeyExpr> result = new HashSet<KeyExpr>();
        for(KeyExpr expr : set)
            result.add(expr.translateOuter(this));
        return result;
    }

    public Set<ValueExpr> translateValues(Set<ValueExpr> set) {
        Set<ValueExpr> result = new HashSet<ValueExpr>();
        for(ValueExpr expr : set)
            result.add(expr.translateOuter(this));
        return result;
    }

    public List<Expr> translate(List<Expr> list) {
        List<Expr> result = new ArrayList<Expr>();
        for(Expr expr : list)
            result.add(expr.translateOuter(this));
        return result;
    }

    public Set<Expr> translate(Set<Expr> set) {
        Set<Expr> result = new HashSet<Expr>();
        for(Expr expr : set)
            result.add(expr.translateOuter(this));
        return result;
    }
}
