package com.ejavac;

import org.objectweb.asm.AnnotationVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ASM9;



public class DefaultAnnotationLink extends AnnotationVisitor
{
    
    public MethodLink link;

    public DefaultAnnotationLink(MethodLink link) 
    {
        super(ASM9);
        this.link = link;
    }

    @Override
    public void visit(String name, Object value) {
        
    //    System.out.println("value: " + value.getClass().toString());
    //    this.values.put(name, value);
        this.link.annotationDefault = (value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value)
    {
  //      System.out.println("enum");
        this.link.annotationDefault = (new ASTExpr.Enum(value, descriptor));
       // System.out.println(name + " " +  value + " " + descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor)
    {
        
      //  System.out.println("note");
        AnnotationLink note = new AnnotationLink(descriptor);
   //     this.values.put(name, note);
        this.link.annotationDefault = note;
   //     System.out.println(name + " " + descriptor);
        
        return note;
    }
    
    public AnnotationVisitor visitArray(String name)
    {
   //     System.out.println("array");
        List list = new ArrayList<>();
        this.link.annotationDefault = list;
   //     this.values.put(name, list);
  //      System.out.println(name);
        return new ArrayLink(list);
   //     return null;
    }
}
