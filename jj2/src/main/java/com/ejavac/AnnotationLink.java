package com.ejavac;

import org.objectweb.asm.AnnotationVisitor;

import java.util.*;

import static org.objectweb.asm.Opcodes.ASM9;



public class AnnotationLink extends AnnotationVisitor
{

    public AnnotationLink() {
        super(ASM9);
     //   this.annotations = new ArrayList<>();
        this.values = new TreeMap<>();
    }

    public AnnotationLink(String descriptor)
    {
        this();
        this.descriptor = descriptor;
    }
    
    public AnnotationLink(String descriptor, boolean visible)
    {
        this();
        this.descriptor = descriptor;
        this.visible = visible;
    }
    
    public Object getValue(String name)
    {
        return values.get(name);
    }
    
    public boolean visible = false;
    public String descriptor;
    public TreeMap<String, Object> values;
    
    public String toString()
    {
        return "{descriptor: " + this.descriptor + 
               "; visible: " + this.visible + 
               "; values: " + this.values + " }";
    }

    @Override
    public void visit(String name, Object value) {
        
        if (value instanceof org.objectweb.asm.Type)
        {
            if (((org.objectweb.asm.Type)value).getSort() == org.objectweb.asm.Type.OBJECT)
            {
                this.values.put(name, new ASTExpr.ClassConstName(value.toString()));
            }
        }
    //    System.out.println("value: " + value.getClass().toString());
        this.values.put(name, value);
//        System.out.println(name + " " +  value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value)
    {
  //      System.out.println("enum");
        this.values.put(name, new ASTExpr.Enum(value, descriptor));
    //    System.out.println(name + " " +  value + " " + descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor)
    {
        
      //  System.out.println("note");
        AnnotationLink note = new AnnotationLink(descriptor);
        this.values.put(name, note);
        
        //        System.out.println(name + " " + descriptor);
        
        return note;
    }
    
    public AnnotationVisitor visitArray(String name)
    {
   //     System.out.println("array");
        List list = new ArrayList<>();
        this.values.put(name, list);
  //      System.out.println(name);
        return new ArrayLink(list);
    }
}
