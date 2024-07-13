package com.ejavac;
import java.util.*;

public class BoundedView<E> extends AbstractList<E>{

    public static <E> List<E> of(List<E> list, int size) {
        return new BoundedView<>(list, size);
    }
    
    private final int size;
    private final List<E> backingList;

    private BoundedView(List<E> backingList, int size){
        this.backingList = backingList;
        this.size = size;
    }
    

    @Override
    public E get(int i) {
        return backingList.get( i % size );
    }

    @Override
    public int size() {
        return size;
    }

}
