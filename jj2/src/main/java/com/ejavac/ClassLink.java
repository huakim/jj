package com.ejavac;//package com.ejavac;

import java.nio.file.*;
import java.util.*;
import java.io.*;

import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.TypeReference.*;

import org.objectweb.asm.AnnotationVisitor;

public class ClassLink extends ClassVisitor implements Reference
{
    //*
    //public static void main(String [] args) throws Exception
    //{
    //    ClassReader cr = new ClassReader(Files.readAllBytes
    //    (new File("./task.class").toPath()));
        
        
    //    ClassLink cv = new ClassLink();
    //    cr.accept(cv, 0);
        
        //      S ystem.out.println(cv);
        
    //}
    //*/
    public String getReference()
    {
        return "L"+name+";";
    }
    
    // name of the class
    public String name;
    // name of the package
    public String packageName = "";
    // name of the superclass
    public String superName;
    // head name of the class 
    public String headName;
    // name of the outer class
    public String outerName;
    // signature
    public String signature;
    // access flag
    public int access;
    // path of the class
    public Path path;
    // set of an interfaces
    public List<String> interfaces;// = new ArrayList<>();
    // map of the fields
    public TreeMap<String, FieldLink> fields;// = new TreeMap<>();
    // list of the methods
    public List<MethodLink> method_list = new ArrayList<>();
    // map of the methods
    // 1 - method name
    // 2 - method descriptor
    // 3 - method link
    public TreeMap<String, MethodMap<List<String>, MethodLink>> methods;// = new TreeMap<>();
    // map of the annotations
    public TreeMap<String, AnnotationLink> annotations;// = new ArrayList<>();
    // map of the type annotation
    public List<TypeAnnotationLink> typeannotations;// = new ArrayList<>();
    // inner classes
    // 1 string - outer full name
    // 2 string - inner name
    // 3 - inner class link
    public TreeMap<String, TreeMap<String, InnerClassLink>> innerClasses;
    // if classlink is broken
    public boolean is_broken = false;
    // host name
    public String hostName;
    // member names 
    public Set<String> members;
    // this method adds an methodlink to the methods
    public MethodLink addMethodLink(String name, String desc, int access)
    {
        // create nameandtype
      //  NameAndType nat = new NameAndType(name, desc);
        // create new methodlink
        MethodLink link = new MethodLink();
        // set name 
        link.name = name;
        // set descriptor
        link.descriptor = desc;
        // set link access
        link.access = access; 
        // get method map 
        return this.addMethodLink(link);
    }
    
    // this method adds an fieldlink to the field
    public FieldLink addFieldLink(String name, String desc, int access)
    {
        // create fieldlink 
        FieldLink link = new FieldLink();
        // set name
        link.name = name;
        // set desc
        link.descriptor = desc;
        // set acc
        link.access = access;
        // add field
        this.fields.put(name, link);
        // return fieldlink
        return link;
    }
    
    // this method adds an methodlink to the methods
    public MethodLink addMethodLink(MethodLink link)
    {
        // get name
        String name = link.name;
        // get descriptor
      //  String desc = link.descriptor;
        // get method map 
        MethodMap<List<String>, MethodLink> map = this.methods.get(name);
        // if got null, then add new map 
        if (map == null)
        {
            map = new MethodMap<>();
            // put new map 
            this.methods.put(name, map);
        }
        // put method to the method map
        this.method_list.add(link);
        map.put(link.getList(), link);
        // return
        return link;
    }
    
    // this function will return if classlink contains an method with same name and arguments list
    public boolean containsMethod(String name, List<String> list)
    {
        // get method map
        Map<List<String>, MethodLink> map = this.methods.get(name);
        // if got null, then return false
        
        
   //     System.out.println(link);
    //    System.out.println(list);
        
        if (map == null) return false;
        // return if next element is null too
        MethodLink link = map.get(list);
        
        
        if (link == null) return false;
        // return
//        System.out.println("" + name + " " + descriptor + " " + link);
        //
        return true;
    }
    
    // this function will return if classlink contains an method with same name and arguments list
    public boolean containsMethod(MethodLink link)
    {
        return containsMethod(link.name, link.getList());
    }
    
    public String toString()
    {
        return 
        "{name: " + this.name + 
        "; superName: " + this.superName +
        "; packageName: " + this.packageName +
        "; headName: " + this.headName +
        "; access: " + this.access +
        "; signature: " + this.signature +
        "; interfaces: " + this.interfaces +
        "; fields: " + this.fields +
        "; methods: " + this.methods +
        "; annotations: " + this.annotations +
        "; typeannotations: " + this.typeannotations +
        "; innerClasses: " + this.innerClasses +
     //   "; outerName: " + this.outerName +
        "; hostName: " + this.hostName + 
        "; members: " + this.members + 
        " }";
    }
    
    public void visit(String name, String superName, String signature,
                     int access, String[] interfaces)
    {
        // set name
        this.name = name;
        // set supername
        this.superName = superName;
        // set signature
        this.signature = signature;
        // set access flag
        this.access = access;
        // set interface names list
        this.interfaces = new ArrayList<>(Arrays.asList(interfaces));
        // set field link map
        this.fields = new TreeMap<>();
        // set method link map
        this.methods = new TreeMap<>();
        // set inner classes map
        this.innerClasses = new TreeMap<>();
        // set annotation list
        this.annotations = new TreeMap<>();
        // set typeannotation list
        this.typeannotations = new ArrayList<>();
        // set member list
        this.members = new HashSet<>();
        // split name 
        String[] list = name.split("/");
        // get array size
        int size = list.length;
        // set head name
        this.headName = list[size - 1];
        // set package name
        if (size > 1)
        {
            // create stringbuilder
            StringBuilder out = new StringBuilder(list[0]);
            // iterate for names in list
            for (int i = 1; i < size; i ++)
            {
                // add slash
                out.append("/");
                // add name to out
                out.append(list[i]);
            }
            this.packageName = out.toString();
        }
    }
    
    // this function will return an innerclasslink 
    public InnerClassLink getInnerClass(String ... names)
    {
        InnerClassLink inner = null;
        // 
        Map map;
        // get string
        String str = this.name;
        // iterate for names
        for (String i : names)
        {
            map = (Map)(innerClasses.get(str));
            // if map is null, then return null
            if (map == null) return null;
            // get inner class link
            inner = (InnerClassLink)map.get(i);
            // if inner is null, then return null
            if (inner == null) return null;
            // get inner class name
            str = inner.name;
        }
        // return inner name
        return inner;
    }
    
    // 
    public MethodLink getMethodLink(String name, String ... list)
    {
        return this.getMethodLink(name, Arrays.asList(list));
    }
    
    // this method will return an methodlink
    public MethodLink getMethodLink(String name, List<String> list)
    {
        // get map object from methods
        Map<List<String>, MethodLink> temp = this.methods.get(name);
        // if got null, then return null
        if (temp == null) return null;
        // else, get an method link
        else
        {
            return temp.get(list);
        }
    }
    
    // this function will return an innerclasslink from other inner class 
    public InnerClassLink getInnerClassA(String str, String ... names)
    {
        InnerClassLink inner = null;
        // 
        Map map;
        // iterate for names
        for (String i : names)
        {
            map = (Map)(innerClasses.get(str));
            // if map is null, then return null
            if (map == null) return null;
            // get inner class link
            inner = (InnerClassLink)map.get(i);
            // if inner is null, then return null
            if (inner == null) return null;
            // get inner class name
            str = inner.name;
        }
        // return inner name
        return inner;
    }
    
    public ClassLink()
    {
        super(ASM9);
        this.fields = new TreeMap<>();
        this.methods = new TreeMap<>();
        this.innerClasses = new TreeMap<>();
        this.annotations = new TreeMap<>();
        this.typeannotations = new ArrayList<>();
    }
        // visit for name access version and etc
    public void visit(int version, int access, 
        java.lang.String name, java.lang.String signature, 
        java.lang.String superName, java.lang.String[] interfaces)
    {
        this.visit(name, superName, signature, access, interfaces);
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
    
    public AnnotationLink getAnnotation(String name)
    {
        return this.annotations.get(name);
    }
    
    
    public void visitInnerClass(String innerName, String outerName, String name, int access){
        // if not outer name and name here
        if (name == null) return;
        if (outerName == null) return;
        // create inner class
        InnerClassLink inner = new InnerClassLink(innerName, access);
        // add inner class
        TreeMap<String, InnerClassLink> map = innerClasses.get(outerName);
        // if got null, then create map and add to the map
        if (map == null)
        {
            map = new TreeMap<String, InnerClassLink>();
            innerClasses.put(outerName, map);
        }
        // add inner class link
        map.put(name, inner);
    }
        
        // visit for field
    public FieldVisitor visitField(int access, String name, String descriptor,
                                       String signature, Object value)
    {
            // create field
        FieldLink field = new FieldLink();
            // fill field with values
        field.visit(name, access, descriptor, signature, value);
            // put field into field map
        this.fields.put(name, field);
            // create new FieldVisitor and return
        return field;
    }
    // visit for method
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions)
    {
        // create method link
        MethodLink method = new MethodLink();
        // fill method with values
        method.visit(name, access, descriptor, signature, exceptions);
        // put method into method map
        this.addMethodLink(method);
       // this.methods.put(new NameAndType(name, descriptor), method);
        // create new MethodVisitor and return
        return method;
    }
    public void visitNestMember(String member)
    {
        this.members.add(member);
    }
    
    public void visitNestHost(String host)
    {
        this.hostName = host;
    }
    
//    public void visitOuterClass(String owner, String name, String desc)
//    {
//       System.out.println(owner + " " + name + " " + desc);
//    }
    
//    public void visitPermittedSubclass(String permitted)
//    {
//        System.out.println(permitted);
//    }
    
    public void visitEnd()
    {
        if (this.interfaces == null)
        {
            this.interfaces = new ArrayList<>();
        }
        else if (this.superName == null)
        {
            this.superName = "java/lang/Object";
            this.packageName = "java/lang";
        }
    }
}
