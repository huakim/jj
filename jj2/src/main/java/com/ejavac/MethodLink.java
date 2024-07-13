package com.ejavac;

import java.util.*;


import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.TypeReference.*;
import org.objectweb.asm.AnnotationVisitor;

public class MethodLink extends MethodVisitor
{
    // name of the field
    public String name;
    // access flag
    public int access;
    // type descriptor
    public String descriptor;
    // signature
    public String signature;
    // constant value
    public List<String> exceptions = new ArrayList<>();;
    // list of the annotations
    public TreeMap<String, AnnotationLink> annotations;// = new ArrayList<>();
    // list of the type annotation
    public List<TypeAnnotationLink> typeannotations;// = new ArrayList<>()
    // list of the parameter annotation
    public TreeMap<Integer, TreeMap<String, ParameterAnnotationLink>> parameterannotations;// = new ArrayList<>()
    // this is an default annotation parameter
    public Object annotationDefault = null;
    // this is an method link init
    public MethodInit init = null;
    // this is an list of the uninitialized 
    public List cinit;
    // this is an signature info class 

    // this is an method argument classname list
    public List<String> arguments;
    // this is an method return type 
    public String returnType = null;
    // this is an generic types
  //  public Map<String, ClassObject> generic;
    // this is an generic typeorder
  //  public Map<String, ClassObject> genericOrder;
    // this is an signature method init class
    public static class SignatureMethodInit
    {
        // this is an generic information
        public HashMap<String, ClassObject> generic;
        // this is an generic order information
        public List<String> genericOrder;
        // this is an unused elements 
        public Set<String> unused;
        //
        public SignatureMethodInit()
        {
            this.generic = new HashMap<>();
            this.genericOrder = new ArrayList<>();
        }
    }
    // this method will generate an SignatureMethodInit
 //   public SignatureMethodInit signatureInfo()
 //   {
 //       // check for signature
 //       if (this.
 //   }
    // this is an method init class
    public static class MethodInit
    {
        // this is an flag that indicates usage of this init
        public int usage;
        // this flags indicates an type of this init
        public boolean is_method;// = true;
        // this is an init object
        public List init;
        // this is an parent class
        public ClassObject parent;
        // this is an variable map
        public Map<Integer, Variable> vars;
        // this is an constructor
        public MethodInit(List init, ClassObject parent, boolean is_method)
        {
            // if this object is method
            // then is_method flag is true
            // else, if this object is annotation argument
            // then is_method flag is false
            this.init = init;
            this.parent = parent;
            this.is_method = is_method;//(parent.access & ACC_ANNOTATION) == 0;
        }
        // this is an constructor
   //     public MethodInit(List init, ClassObject parent)
    //    {
     //       this.init = init;
      //      this.parent = parent;
       // }
    }
    // this method will check if methodlink are equals
/*    public boolean (Object object)
    {
        if (object instanceof MethodLink)
        {
            MethodLink link = (MethodLink) object;
            // 
            return link.name.equals(this.name) && 
                   link.descriptor.equals(this.descriptor);
        }
        else
        {
            return false;
        }
    }
  */
    // this method will return an return type
    public String getType()
    {
        // 
        String ret = this.returnType;
        if (ret == null)
        {
            // 
            ret = this.descriptor;
            //
            this.returnType = ret = ret.substring(ret.indexOf(')')+1);
            //
        }
        return ret;
    }
    // this method will return an method argument list
    public List<String> getList()
    {
        // check if this argument list is empty
        if (this.arguments == null)
        {
            // create list
            List<String> list = new ArrayList<>();
            // set list
            this.arguments = list;
            // iterate description
            String desc = this.descriptor;
            // iterate 
            for (int i = 1, n = desc.lastIndexOf(")"); i < n; i ++)
            {
                // get char
                char t = desc.charAt(i);
                // create an StringBuilder 
                StringBuilder out = new StringBuilder(Character.toString(t));
                // repeat until t == [
                while (t == '[')
                {
                    // get new char
                    t = desc.charAt(++i);
                    // append
                    out.append(t);
                }
                // if char equals to L, then add reference
                if (t == 'L')
                {
                    // create an string builder 
                    
                    // iterate until ';' met 
                    while (t != ';')
                    {
                        i ++;
                        // get char
                        t = desc.charAt(i);
                        // add char to the out
                        out.append(t);
                    }
                    list.add(out.toString());
                }
                else
                {
                    list.add(out.toString());
                }
            }
        }
        // return
        return this.arguments;
    }
    
    public boolean equals(Object a1)
    {
        if (a1 instanceof MethodLink)
        {
            MethodLink a2 = (MethodLink) a1;
            if (this.name.equals(a2.name))
            {
                return this.descriptor.equals(a2.descriptor);
            }
        }
        return false;
    }
    
    public int hashCode()
    {
        return ((name.hashCode()/16)+(descriptor.hashCode()/8))*5;
    }
    
    public String toString()
    {
        return 
        "{name: " + this.name + 
        "; access: " + this.access +
        "; descriptor: " + this.descriptor +
        "; signature: " + this.signature +
        "; exceptions: " + this.exceptions +
        "; annotations: " + this.annotations +
        "; typeannotations: " + this.typeannotations +
        "; parameterannotations: " + this.parameterannotations +
        "; annotationdefault: " + this.annotationDefault +
        " }";
    }
    
    public MethodLink(String name, String descriptor)
    {
        this();
        this.name = name;
        this.descriptor = descriptor;
    }
    
    public void visit(String name, int acc, String desc, String sign, 
    String [] exceptions)
    {
        this.name = name;
        this.access = acc;
        this.descriptor = desc;
        this.signature = sign;
        if (exceptions != null)
        this.exceptions = new ArrayList<>(Arrays.asList(exceptions));
    }

        public MethodLink()
        {
            super(ASM9);
            this.annotations = new TreeMap<>();
            this.typeannotations = new ArrayList<>();
            this.parameterannotations = new TreeMap<>();
        }
        
    public AnnotationVisitor visitAnnotationDefault()
    {
        return new DefaultAnnotationLink(this);
    }

    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible)
    {
        // create link
        AnnotationLink l = new AnnotationLink(descriptor, visible);
        // append link 
        this.annotations.put(descriptor, l);
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
    
    public void addParameterAnnotation(ParameterAnnotationLink link)
    {
        //
        int i = link.parameter;
        String d = link.descriptor;
        // 
        TreeMap<String, ParameterAnnotationLink> map = this.parameterannotations.get(i);
        //
        if (map == null)
        {
            map = new TreeMap<>();
            this.parameterannotations.put(i, map);
        }
        map.put(d, link);
    }
    
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor,
                                                      boolean visible)
    {
        // create link
        ParameterAnnotationLink l = new ParameterAnnotationLink(parameter, descriptor, visible);
        // append link
        this.addParameterAnnotation(l);
        return l;
    }
    
    // this is an parent object
   // public ClassObject owner;
    
}
