package com.ejavac;import java.util.*;
public class InnerClassLink
{
    public int access;
    public String name;
    public String packageName = "";
    
    public InnerClassLink(){};

    public InnerClassLink(String name, int access)
    {
        this.name = name;
        this.access = access;
        
        String[] list = name.split("/");
        int length = list.length - 1;
        if (length > 0)
        {
            int i = 0;
            StringJoiner joiner = new StringJoiner("/");
            while (i < length)
            {
                joiner.add(list[i]);
                i ++;
            }
            packageName = joiner.toString();
        }
    }

    public String toString()
    {
        return "{name: " + this.name + "; access: "+this.access+" }";
    }
}
