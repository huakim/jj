package com.ejavac;
public class Variable implements Reference
{
    public String descriptor;
    public String name;
    public Variable(String descriptor, String name)
    {
        this.descriptor = descriptor;
        this.name = name;
    }
    public String getReference()
    {
        return descriptor;
    }
    public String toString()
    {
        return "Variable: {name: "+name+"; descriptor: "+descriptor+" }";
    }
}
