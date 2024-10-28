package lsfusion.server.base.version.impl;

import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImList;
import lsfusion.base.col.interfaces.immutable.ImOrderSet;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.server.base.version.Version;
import lsfusion.server.base.version.interfaces.NFOrderSet;

import java.util.*;

public class NFSimpleOrderSetImpl<T> implements NFOrderSet<T> {

    private final List<T> list = Collections.synchronizedList(new ArrayList<>());

    public NFSimpleOrderSetImpl(ImList<T> startList) {
        list.addAll(startList.toJavaList());
    }

    public Iterable<T> getNFIt(Version version) {
        return getIt();
    }

    boolean iterated;
    private final Iterable<T> it = () -> new Iterator<T>() {
        private int i;

        public boolean hasNext() {
            return i<list.size();
        }

        public T next() {
            return list.get(i++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }; 
    public Iterable<T> getIt() {
        iterated = true;
        return it;
    }

    public void add(T element, Version version) {
        list.add(element);
    }

    public void finalizeCol() {
        throw new UnsupportedOperationException();
    }

    public ImSet<T> getSet() {
        return getOrderSet().getSet();
    }

    public boolean containsNF(T element, Version version) {
        assert false;
        return list.contains(element);
    }

    public ImSet<T> getNFSet(Version version) {
        assert false;
        return getSet();
    }

    public void remove(T element, Version version) {
        assert !iterated;
        list.remove(element);
    }

    public void removeAll(Version version) {
        assert false;
        list.clear();
    }

    public ImList<T> getList() {
        return getOrderSet();
    }

    public Iterable<T> getListIt() {
        return getIt();
    }

    public Iterable<T> getNFListIt(Version version) {
        return getIt();
    }

    public ImList<T> getNFList(Version version) {
        return getList();
    }

    public ImOrderSet<T> getOrderSet() {
        return SetFact.fromJavaOrderSet(list);
    }

    @Override
    public int size(Version version) {
        return list.size();
    }

    public ImOrderSet<T> getNFOrderSet(Version version) {
        throw new UnsupportedOperationException();
    }

    public void finalizeChanges() {
    }
}
