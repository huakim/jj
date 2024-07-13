package com.ejavac;import java.util.*;

public class ReversedView<E> extends AbstractList<E>{

    public static <E> List<E> of(List<E> list) {
        return new ReversedView<>(list);
    }

    private final List<E> backingList;

    private ReversedView(List<E> backingList){
        this.backingList = backingList;
    }

    @Override
    public E get(int i) {
        return backingList.get(backingList.size()-i-1);
    }

    @Override
    public int size() {
        return backingList.size();
    }

}
