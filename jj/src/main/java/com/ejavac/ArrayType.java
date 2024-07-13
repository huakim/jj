package com.ejavac;import static org.objectweb.asm.Opcodes.*;
import java.util.*;

public class ArrayType extends ClassLink implements Reference
{
    
    public ArrayType()
    {
        super();
    }
    
    public String getReference()
    {
        return this.name;
    }
    
    public static final String clone_able = "java/lang/Cloneable";
    public static final String clone_able_desc =
"Ljava/lang/Cloneable;";

    public String returnType;
    
    public String baseType;
    
    public int dimension;
    
    public static ArrayType reference(String name, int main)
    {
        ArrayType t = new ArrayType();
        // set dimension
        t.dimension = main;
        // set base type
        t.baseType = "L" + name + ";";
        // set return type
        t.returnType = name = "[".repeat(main - 1) + "L" + name + ";" ;
        // set name
        t.name = name =  "[" + name;
        // set package name
        t.packageName = "";
        // set head name
        t.headName = name;
        // set supername
        t.superName = "java/lang/Object";
        // add methodlink
        t.addMethodLink("clone", "()"+name, ACC_PUBLIC + ACC_FINAL);
        // add field
        t.addFieldLink("length", "I", ACC_PUBLIC + ACC_FINAL);
        // add interfaces 
        t.interfaces = Arrays.asList(clone_able);
        // return 
        return t;
    }
    
    public static ArrayType primitive(String name, int main)
    {
        ArrayType t = new ArrayType();
        // set dimension
        t.dimension = main;
        // set base type
        t.baseType = "L" + name + ";";
        // set return type
        t.returnType = name = "[".repeat(main - 1) +  name;
        // set name
        t.name = name =  "[" + name;
        // set package name
        t.packageName = "";
        // set head name
        t.headName = name;
        // set supername
        t.superName = "java/lang/Object";
        // add methodlink
        t.addMethodLink("clone", "()"+name, ACC_PUBLIC + ACC_FINAL);
        // add field
        t.addFieldLink("length", "I", ACC_PUBLIC + ACC_FINAL);
        // add interfaces 
        t.interfaces = Arrays.asList(clone_able);
        // return 
        return t;
    }
    
	public static void main (String[] args) {
		
	}
}

