package com.ejavac;import java.util.*;

public class ChainView
{

    public static List<Token> of(List list2, final int size) 
    {
        // create new array list object
        List list = new ArrayList<>(size);
        // iterate until size > 0
        int i = 0;
        //
        while (i < size)
        {
            // add chain value
            list.add( (Token)     ((List)(list2.get(i))).get(1)   );
            // increment i
            i ++;
        }
        return list;
    }
}
