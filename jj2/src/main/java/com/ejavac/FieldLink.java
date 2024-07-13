package com.ejavac;

import java.util.*;


import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.TypeReference.*;
import org.objectweb.asm.AnnotationVisitor;

public class FieldLink extends FieldVisitor
{
    public FieldLink(String name, String desc, int i)
    {
        this();
        this.access = i;
        this.name = name;
        this.descriptor = desc;
    }
    // this is an class for initialization
    public static class FieldInit
    {
        // this is an flag that indicates usage of this init
        public int usage;
        // this is an note init 
        public List note_init;
        // this is an type init object
        public List type_init;
        // this is an init object
        public List init;
        // this is an parent classobject
        public ClassObject parent;
        // this is an constructor
        public FieldInit(List a1, List a2, List a3, ClassObject object)
        {
            this.init = a1;
            this.type_init = a2;
            this.note_init = a3;
            this.parent = object;
        }
    }
    // name of the field
    public String name;
    // access flag
    public int access;
    // type descriptor
    public String descriptor;
    // signature
    public String signature;
    // constant value
    public Object value;
    // field init
    public FieldInit init = null;
    // list of the annotations
    public List<AnnotationLink> annotations;// = new ArrayList<>();
    // list of the type annotation
    public List<TypeAnnotationLink> typeannotations;// = new ArrayList<>()
    // this is an init object
 //   public List init = null;
    // this is an note init object
 //   public List note_init = null;
    // this is an type init object
 //   public List type_init = null;
    // this is an parent object
  //  public ClassObject owner;
    // this method will check this field constructor
    // and return an object value if got one
    
    public void visit(String name, int acc, String desc, String sign, Object val)
    {
        this.name = name;
        this.access = acc;
        this.descriptor = desc;
        this.signature = sign;
        this.value = val;
    }
    
    public String toString()
    {
        return 
        "{name: " + this.name + 
        "; access: " + this.access +
        "; descriptor: " + this.descriptor +
        "; signature: " + this.signature +
        "; value: " + this.value + 
        "; annotations: " + this.annotations +
        "; typeannotations: " + this.typeannotations +
        " }";
    }
    
        public FieldLink()
        {
            super(ASM9);
            this.annotations = new ArrayList();
            this.typeannotations = new ArrayList();
        }

    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible)
    {
        // create link
        AnnotationLink l = new AnnotationLink(descriptor, visible);
        // append link 
        this.annotations.add(l);
        return l;
    }
    
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath,
    String descriptor, boolean visible)
    {
        // create link
        TypeAnnotationLink l = new TypeAnnotationLink(
                typeRef, typePath, descriptor, visible);
        // append link 
        this.typeannotations.add(l);
        return l;
    }
}
