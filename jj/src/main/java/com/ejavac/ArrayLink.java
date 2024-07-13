package com.ejavac;

import org.objectweb.asm.AnnotationVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ASM9;

class ArrayLink extends AnnotationVisitor
{
    public List list;
    public ArrayLink(List list)
    {
        super(ASM9);
        this.list = list;
    }
    
    @Override
    public void visitEnum(String name, String descriptor, String value)
    {
 //       System.out.println("enum");
        this.list.add(new ASTExpr.Enum(value, descriptor));
 //       System.out.println( " " +  value + " " + descriptor);
    }
    
    @Override
    public void visit(String name, Object value) {
        
 //       System.out.println("value: " + value.getClass().toString());
        this.list.add( value);
//        System.out.println(name + " " +  value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor)
    {
        
 //       System.out.println("note");
        AnnotationLink note = new AnnotationLink(descriptor);
        this.list.add(note);
 //       System.out.println( " " + descriptor);
        
        return note;
    }
    
    public AnnotationVisitor visitArray(String name)
    {
//        System.out.println("array");
          
        List list = new ArrayList<>();
        this.list.add(list);
  //      System.out.println(name);
        return new ArrayLink(list);
    }
}
