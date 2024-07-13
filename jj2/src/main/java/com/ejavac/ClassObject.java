package com.ejavac;import java.util.*;
import java.nio.*;
import java.nio.file.*;
import java.io.*;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.*;

public class ClassObject extends ClassLink implements Cloneable
{
    // 
    public static final String obj_name = "java/lang/Object",
                               str_name = "java/lang/String",
                               obj_name_desc = "Ljava/lang/Object;",
                               str_name_desc = "Ljava/lang/String;",
                               enum_name = "java/lang/Enum",
                               cls_name = "java/lang/Class",
                               cls_name_desc = "Ljava/lang/Class;",
                               note_name = "java/lang/annotation/Annotation",
                               note_rep_name = "java/lang/annotation/Repeatable",
                               note_rep_desc = "Ljava/lang/annotation/Repeatable;",
                               target_name = "java/lang/annotation/Target",
                               retent_name = "java/lang/annotation/Retention",
                               enum_values = "$VALUES",
                               thw_name_desc = "Ljava/lang/Throwable;",
                               clinit = "<clinit>",
                               constr = "<init>",
                               ddinit = "this$init";
    /*
    public static interface Getter
    {
        public Object get(Object ast, int flags);
    }*/
    
    // this method will return an annotation type parameters list
    
    public static class NoteAndSignatureInit
    {
        public List notes;
        public List signatures;
        
        public NoteAndSignatureInit(List a1, List a2)
        {
            notes = a1;
            signatures = a2;
        }
    }
    
    // this flag indicate if classobject is compiled to the classlink
    //public boolean is_linked = false;
    // this is an generic names
    public Map<String, ClassObject> generic;
    // this is an generic order
    public List<String> genericOrder = new ArrayList<>();
    // this is an stage of classobject linkind
    public int link_stage = 0;
    // this is an stage of classobject that current processing
    public int cur_link_stage = 0;
    // this flag indicate if classobject is compiled to the class
   // public boolean is_compiled = false;
    // this is an set of static method names
 //   public Set<String> static_methods;
    // this is an import table
    public ImportTable table = null;
    // this is an AST object
    public AST ast;
    // this is an field initialize code
 //   public Map<String, Object> field_init = new TreeMap<>();
 //   public Map<String, Object> field_type_init = new TreeMap<>();
 //   public Map<String, Object> field_note_init = new TreeMap<>();
    // this is an method initialize code
  //  public Map<NameAndType, Object> method_init;

    // this is an static constructor init list
    public List static_const = new ArrayList<>();
    // this is an dynamic constructor init list
    public List dynamic_const = new ArrayList<>();
    // this is an list of parent objects
    public List<ClassTable> parents = null;
    // this is an superclass signature and annotations
    public NoteAndSignatureInit superClass_init = null;
    // this is an interface signature and annotations list
    public List<NoteAndSignatureInit> interfaces_init = new ArrayList<>();
    // this is an list of 
    // this is an classtable for getter
    public ClassTable getter;
    // public ClassObject outer
    public ClassObject outer;
    
    public Object init;
    
    public String outerField;
    
    public List<Token> enum_cache = new ArrayList<>();
    
    public boolean equals(Object a)
    {
        if (a instanceof ClassObject)
        {
            if (this.name != null)
            {
                return this.name.equals(((ClassObject) a).name);
            }
        }
        return false;
    }
    
    
    
    // this method will convert an integer list to the typepath
    public static TypePath createTypePath(int ... args)
    {
        // create an stringjoiner
        StringJoiner joiner = new StringJoiner(";", "", ";");
        // add all numbers to the joiner 
        for (int i: args)
        {
            joiner.add(Integer.toString(i));
        }
        // return typepath
        return TypePath.fromString(joiner.toString());
    }
    
    // this method will add an enum link from init
    public FieldLink addEnumFieldLink(List temp2)
    {
        
  //      System.out.println("----------------");
  //      System.out.println(temp2);
//            temp2 = (List) i;
            Token token = ((Token)temp2.get(1));
            String name = token.value;
            // create new FiledLink
            FieldLink fieldlink = new FieldLink();
            // add this object
      //      fieldlink.owner = this;
            // add name
            fieldlink.name = name;
            // add enum cache
            this.enum_cache.add(token);
            // add access
            fieldlink.access = ACC_PUBLIC + ACC_STATIC + ACC_ENUM + ACC_FINAL;
            // add descriptor
            fieldlink.descriptor = getDescriptor(this.name);
            // check if field with this name is exists, 
            // if yes, then raise error, else
            // add field to the fieldmap
            if (this.fields.containsKey(name))
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.already_def, token.source, token.line, 
                token.position, "field", token.value, this.name));
            }
            else
            {
            this.fields.put(name, fieldlink);
            // 
            fieldlink.init = new FieldLink.FieldInit(temp2, null, null, this);
            }
            
     ///       System.out.println(temp2);
            
            this.static_const.add(temp2);
            
            return fieldlink;
    }
    
    // this method will process with annotations 
    public AnnotationLink getAnnotationLink(List temp2)
    {
        AnnotationLink link; 
        
   //     System.out.println(temp2);
        // add annotation and finish
        link = this.ast.expr.getAnnotation(new ASTExpr.ClassGetter(this, this, this.ast), temp2); 
        //
        return link;
    };
   /* 
    // this method will process with type annotation
    public TypeAnnotationLink addTypeAnnotationLink(List temp2, int typeref, TypePath typePath)
    {
        TypeAnnotationLink link;
        // add annotation and finish
        this.typeannotations.add(link = new TypeAnnotationLink(
        typeref, typePath, this.ast.expr.getAnnotation(new ASTExpr.ClassGetter(this, this, this.ast), temp2))); 
        //
        return link;
    }
    */
    
    
    // (List)((List)object.init).get(6)
    // this method will add an fieldlink from init
    public FieldLink addFieldLink(List temp2)
    {
           //  System.out.println(i);
            // list
            String name;
            Token token ;
            // create new fieldlink
            FieldLink fieldlink = new FieldLink();
            List temp6;
            ClassObject object;
          //  temp2 = (List) i;
            // access 
           int access = (int)(double)(temp2.get(0));
            // get type list
           List temp3 = (List) temp2.get(1);
            // get array level 
           int  level = (int)(double)temp3.get(4);
           
            // get annotation list
           List temp4 = (List) temp2.get(3);
            // get class list
           List temp5 = (List) temp3.get(1);
           // if level > 255, then raise error
           if (level > 255)
           {
               token = (Token)temp5.get(0);
               this.ast.error_pool.add(new Flaw(Flaw.type.too_many_dim, 
                  token.source, token.line, token.position));
                level = 255;
           }
            // check if list element is an primitive type descriptor
            // if list element is an primitive type descriptor, 
            // then class list must be 1 size
           String object_name = null; 
            if (temp5.size() == 1)
            {
                // get token
                token = (Token)temp5.get(0);
                // 
                object_name = token.value;
                // if object name in the primitive list, then leave object name
                object_name = ClassObject.primitive.get(object_name);
                // if got null, then check if object_name contains from generic
                if (object_name == null)
                {
                    object = this.generic.get(token.value);
                    // if got object, then check for object name
                    if (object != null)
                    {
                        // set signature
                        fieldlink.signature = "T" + token.value + ";";
                    }
                }
            }
            
            if (object_name == null){
                object = this.ast.getClass(this, this.table, temp5);
                // if got null object, then continue
                if (object == null) return null;
                // else, get object name
                object_name = "L"+object.name+";";
            }
            // else, get object name
            {
                // iterate for names
                for (Object ii: (List) temp2.get(2))
                {
                    temp5 = (List) ii;
                    // get name value from list
                    token = ((Token)temp5.get(0));
                    name = token.value;
                    // add this object
              //      fieldlink.owner = this;
                    fieldlink.access = access;
                    fieldlink.name = name;
                    
                    int lvl = level + (int)(double)(temp5.get(2));
            if (lvl > 255)
            {
                lvl = 255;
                    this.ast.error_pool.add(new Flaw(Flaw.type.too_many_dim, 
                  token.source, token.line, token.position));
            }
            
                    fieldlink.descriptor = "[".repeat(lvl) + object_name;
                    
            if (this.fields.containsKey(name))
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.already_def, token.source, 
                token.line, token.position, "field", token.value, this.name));
            }
            else
            {
                    // put field 
                    this.fields.put(name, fieldlink);
                    // put field init
                    temp6 = (List) temp5.get(1);
                    
                    FieldLink.FieldInit init = new FieldLink.FieldInit(null, null, null, this);
                    
                    if (temp3 != null) init.type_init = (temp3);
                    if (temp4 != null) init.note_init = (temp4);
                    // check if field is static
            
            if (temp6 != null) 
            {
                   init.init = (temp6);
                    if ((fieldlink.access & ACC_STATIC) == 0)
                    {
                        this.dynamic_const.add(Arrays.asList
                        (new Token(token,"$assign"), new Token(token,"="), 
                        Arrays.asList(new Token(token,"%v"), token), temp6));
                    }
                    else
                    {
                        this.static_const.add(Arrays.asList
                        (new Token(token, "$assign"), new Token(token, "="), 
                        Arrays.asList(new Token(token, "%v"), token), temp6));
                    }
            }
                    fieldlink.init = init;
                        // temp3 - type init
                        // temp4 - annotations
                        // temp5[1] - field constructor
              }
            }
        }
        return fieldlink;
    }
    
    
    // this method will add an method from init
    public MethodLink addMethodLink(List temp2, int IS_ENUM)
    {
        boolean is_enum = (IS_ENUM & ACC_ENUM) > 0;
        boolean is_static = (IS_ENUM & ACC_STATIC) == 0;
        // get method init
            List temp3;
            List temp4;
            
            Map <Integer, Variable> mapvar = new HashMap<>();
            
            Set<String> set_ = new HashSet<>();
            
            List tempii = (List)temp2.get(5);
            // description
            String desc;
            String desc2;
            // methodlink
            MethodLink methodlink;
            // access
            int access;
            // create Method Link
            methodlink = new MethodLink();
            //
            List<String> exceptions = new ArrayList<>(tempii.size());
            //
            methodlink.exceptions = exceptions;
            
  //          System.out.println(tempii);
            //
            for (List i: (List<List>)tempii)
            {
                // get exception descriptor
                desc = getObjectName(i, true);
                // add exception
                exceptions.add(desc);
            }
            
            // local var count
            int local_count = 0;
            // get methodlink name
            Token token = ((Token) temp2.get(3));
            String name = token.value;
            // if name is an $initializer, then continue
            // create new StringBuilder
            StringBuilder out = new StringBuilder("(");
            
            // set methodlink access
            access = (int)(double)temp2.get(0);
            
            // if name is $identifier, then set name <init>
            if (name.equals("$initializer"))
            {
                // construct initializer here
                name = "<init>";
                // if class is enum, then add some symbols to the signature
                if (is_enum)
                {
                    access |= ACC_PRIVATE;
                    access &= -1 - ACC_PUBLIC;
                    access &= -1 - ACC_PROTECTED;
                    out.append("Ljava/lang/String;I");
                    mapvar.put(0, new Variable(
                           "Ljava/lang/String;", "##-0"));
                    mapvar.put(1, new Variable("I", "##-1"));
                    local_count = 2;
                }
                else if (is_static)
                {
                    desc = "L"+this.outer.name+";";
                    out.append(desc);
                    mapvar.put(0, new Variable(desc, "##-1"));
                    local_count = 1;
                }
                // 
                desc = "V";
            }
            else
            {
                
                // get methodlink return descriptor list
                temp3 = (List) temp2.get(2);
                // get methodlink description
                
           //     System.out.println("--method");
                desc = getObjectName(temp3, true);
           //     System.out.println("__end_method");
                // if desc is null, then return null
                if (desc == null) return null;
            }
            // add this object
       //     methodlink.owner = this;
            methodlink.name = name;
    //        System.out.println(temp3);
            // get methodlink return descriptor
      //      System.out.println(temp2);
            // get methodlink attributes list
            temp3 = (List) temp2.get(4);
            
    //        System.out.println(temp3);
    //        System.out.println(temp3.get(0));
            // iterate for methodlink attributes
            for (Object ii : temp3)
            {
                tempii = (List) ii;
                temp4 = (List) tempii.get(0);
                //
                // get attribute descriptor 
                desc2 = (getObjectName(temp4, false));
                // if desk2 is null, then return null
                if (desc2 == null) return null;
                // if first token is varargs, then add level
             //   System.out.println(((Token)temp4.get(0)).value);
                if (((Token)temp4.get(0)).value.equals("$vararg"))
                {
                    desc2 = "["+desc2;
                    access = access | ACC_VARARGS;
                //    System.out.println(methodlink.access);
                }
                out.append(desc2);
                // create new variable
                // get token
                token = (Token) tempii.get(1);
                
                name = token.value;
                //
                if (set_.contains(name))
                {
                    this.ast.error_pool.add(new Flaw(
                              Flaw.type.duplicate_element, 
                              token.source, token.line, token.position,
                              name));
                }
                //
                mapvar.put(local_count, new Variable(desc2,name));
                set_.add(name);
                local_count ++;
            }
            out.append(")");
            out.append(desc);
            // check method link descriptor
            
            // add methodlink to the map
            methodlink.access = access;
            methodlink.descriptor = desc = out.toString();
            // add methodlink and method init
            if (this.containsMethod(methodlink))
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.already_def, token.source, 
                token.line, token.position, "method", name + desc, this.name));
            }
            else
            {
                // add method link init
                methodlink.init = new MethodLink.MethodInit(temp2, this, true);
                methodlink.init.vars = mapvar;
                this.addMethodLink(methodlink);
            }
            return methodlink;
    }
    
    // this method will add an annotation parameter
    public MethodLink addParameter(List temp2)
    {
        
    //    System.out.println("PARAMETER IS: TEMP2::" + temp2);
            // get parameter return descriptor list
            List temp1 = (List) temp2.get(1);
            String object_name;
            String desc = null; 
        
        // get descriptor
        List temp3 = (List) temp1.get(1);
        int level = (int)(double)temp1.get(4);
        
        // this is an name of this methodlink
        Token name_token = (Token) temp2.get(2);
        Token token;
        // if level > 1, then add error
        if (level > 1)
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.invalid_note_type,
            name_token.source, name_token.line, name_token.position));
            return null;
        }
        
        // 
    //    System.out.println("!!LEVELUP::1");
        //
        if (temp3.size() == 1)
        {
            token = (Token)temp3.get(0);
                object_name = token.value;
                // check if object name is void
                if (object_name.equals("void"))
                {
                    this.ast.error_pool.add(new Flaw(Flaw.type.void_not_exp,
                        token.source, token.line, token.position));
                    return null;
                }
                // check if object name is primitive type
                object_name = ClassObject.primitive.get(object_name);
                // if desc is null, then get class name
                if (object_name != null)
                {
                    desc = ((level == 0) ? "" : "[") +  object_name;
                }
        }
            
     //   System.out.println("!!LEVELUP::2");
        
    if (desc == null)
    {
        // get object 
        ClassObject object = this.ast.getClass(this, this.table, temp3);
        object_name = object.name;
        // if got same object, then add cyclic error
        if (object.name.equals(this.name))
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.type_cyclic, 
                  name_token.source, name_token.line, name_token.position));
            return null;
        }
        // if got enum type, annotation type or class type, then continue
        else if 
           (  ((object.access & ACC_ENUM) > 0)||
              ((object.access & ACC_ANNOTATION) > 0)||
              (object.name.equals(ClassObject.str_name))||
              (object.name.equals(ClassObject.cls_name)) )
        {
            // if got cucle element, then raise error
            // get description
            desc = ClassObject.getDescriptor(object_name, level);
        }
        // raise error
        else
        {
            
            this.ast.error_pool.add(new Flaw(Flaw.type.invalid_note_type,
            name_token.source, name_token.line, name_token.position));
   //     System.out.println("WHAT?::"+ object.name);
   //     System.out.println("WHAT?::"+ str_name);
  //      System.out.println("WHAT?::"+ str_name.equals(object.name));
            return null;
        }
        
    }
    
    
 //       System.out.println("!!LEVELUP::3");
        // 
        MethodLink link = new MethodLink();
        //
        link.access = ACC_PUBLIC | ACC_ABSTRACT;
        link.descriptor = "()" + desc;
        link.name = name_token.value;
        
    
        
  //      System.out.println("GOT LINK: " + link);
        
        if (this.containsMethod(link))
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.already_def, name_token.source, 
                name_token.line, name_token.position, "method", name + desc, this.name));
            }
            
            else
        {
            this.addMethodLink(link);
        }
        
            link.init = new MethodLink.MethodInit(temp2, this, false);
        return link;
    }
    
     // this method adds an methodlink to the methods and method init
    public MethodLink addMethodLink(MethodLink link, List init)
    {
        // call super method
        super.addMethodLink(link);
        // add method link init
        link.init = new MethodLink.MethodInit(init, this, true);
        // return methodlink
        return link;
    }
    
    // this method adds an methodlink to the methods
    public MethodLink addMethodLink(String name, String desc, int access, List init)
    {
        // call super method
        MethodLink link = super.addMethodLink(name, desc, access);        
        // 
        link.init = new MethodLink.MethodInit(init, this, true);
        // return methodlink
        return link;
    }
    
    public int hashCode()
    {
        if (this.name == null) return 0;
        return this.name.hashCode();
    }
    
    // link classobject
    public boolean link1()
    {
        // link stage must be 0
        // else, then return false
        if (this.link_stage > 0)
        {
            return false;
        }
        else
        {
            this.link_stage = 1;
            this.cur_link_stage = 1;
        }
        
        String source = this.table.source;
        String name;
        int access, level;
        String object_name;
        String desc;
        StringBuilder out;
        List error_pool = this.table.ast.error_pool;
        FieldLink fieldlink = null;
        MethodLink methodlink = null;
        ClassObject object;
//        NameAndType nameandtype;
        Token token;
        // create ast
        List ast = (List) this.init;
        // reserve temp list
        List temp = (List) ast.get(3);
        List temp1 = null;
        List temp2 = null;
        List temp3 = null;
        List temp4 = null;
        List temp5 = null;
        List temp6 = null;
        
    //## ADD GENERIC TYPES
     // 
    Map generic = new TreeMap<>();
    // add fields to the generic
    // get generic type list
    temp1 = (List) ast.get(2);
    
    
    int nlen = temp1.size();
    
    boolean any = nlen > 0;
    // iterate over generic type list
    if (any) generateGeneric(temp1, nlen);
    else this.generic = new HashMap<>();
      
    temp = (List)ast.get(3);
        
     //   System.out.println("NAME: "+this.name+" " + temp); 
    //## ADD SUPERCLASS NAME
    // if temp size is null or temp is null, then continue
        
        // if class access is enum, then add enum classname
        if ((this.access & ACC_ENUM) > 0)
        {
            // 
            this.superName = enum_name;
        }
        // if class access is annotation, then add annotation classname
        else if ((this.access & ACC_ANNOTATION) > 0)
        {
            this.superName = obj_name;
        }
        else if ((this.access & ACC_INTERFACE) > 0)
        {
            this.superName = obj_name;
        }
        else if (temp == null)
        {
            this.superName = obj_name;
        }
        else if (temp.size() == 0)
        {
            this.superName = obj_name;
        }
        else
    {
        // check superclass of this class
        temp1 = (List) temp.get(1);
        // get type from superclass
        object = this.ast.getClass(this.outer, this.table, temp1);
        
        // if got null object, then add error to the ast
        if (object == null)
        {
            this.superName = obj_name;
        }
        else
        {
            token = (Token)temp1.get(0);
            // check if object is in linking
            object.link1();
            //
            
            if (object.cur_link_stage == 1)
            {
                token = (Token)((List)object.init).get(1);
                error_pool.add(new Flaw(Flaw.type.cyclic_inheritance_involving,
                token.source, token.line, token.position, token.value));
                this.superName = obj_name;
            }
            // check if object is final or enum or interface
            else if ((object.access & ACC_INTERFACE) > 0)
            {
                error_pool.add(new Flaw(Flaw.type.interface_n_exp, 
                source, token.line, token.position));
                this.superName = obj_name;
            }
            else if ((object.access & ACC_ENUM) > 0)
            {
                error_pool.add(new Flaw(Flaw.type.enum_n_exp, 
                source, token.line, token.position));
                this.superName = obj_name;
            }
            else if ((object.access & ACC_FINAL) > 0)
            {
                error_pool.add(new Flaw(Flaw.type.final_n_exp, 
                source, token.line, token.position));
                this.superName = obj_name;
            }
            else
            {
                this.superName = object.name;
                //
                temp6 = (List) temp.get(5);
                if (!any) any = temp6.size() > 0;
                //
                this.superClass_init = new NoteAndSignatureInit
                ( (List) temp.get(3), temp6 );
            }
        }
    }
    //## ADD INTERFACE NAMES
    if ((this.access & ACC_ANNOTATION) > 0) 
    {
        // 
        this.interfaces_init = new ArrayList<>(1);
        this.interfaces_init.add(null);
        //
        this.interfaces = new ArrayList<>(1);
        this.interfaces.add(note_name);
    }
    else
    {   
        {
            // get interface list
            temp2 = (List)ast.get(4);
            // get size of interface list
            int size = temp2.size();
            //
            // set new arraylist
            this.interfaces_init = new ArrayList<>(size);
            this.interfaces = new ArrayList<>(size);
            Set interface_set = new HashSet(size);
            // add interfaces to the array list
            int i = 0;
            while (i < size)
            {
                // check superclass of this class
        temp = (List)temp2.get(i);
        
        i ++;
        
        temp1 = (List) temp.get(1);
        // get type from superclass
        object = this.ast.getClass(this.outer, this.table, temp1);
        // if got null object, then add error to the ast
        
        if (object != null)
        {
         //   
            token = (Token)temp1.get(0);
            
            // check if object is final or enum or interface
            if ((object.access & ACC_INTERFACE) == 0)
            {
                error_pool.add(new Flaw(Flaw.type.interface_exp, 
                source, token.line, token.position));
            }/*
            else if ((object.access & ACC_FINAL) > 0)
            {
                error_pool.add(new Flaw(Flaw.type.final_n_exp, 
                source, token.line, token.position));
            }*/
            else
            {
            
            // check if object is in linking
            object.link1();
            //
            
            if (object.cur_link_stage == 1)
            {
                token = (Token)((List)object.init).get(1);
                error_pool.add(new Flaw(Flaw.type.cyclic_inheritance_involving,
                token.source, token.line, token.position, token.value));
                continue;
            }
            
                name = object.name;
                // 
            // check if interfaces set already contains an interface
            if (interface_set.contains(name))
            {
                error_pool.add(new Flaw(Flaw.type.repeated_interface, token.source,
                                 token.line, token.position));
            }
            else
            {
            // if not contains, then add new interface
                this.interfaces.add(name);
                interface_set.add(name);
                
                //
                temp6 = (List) temp.get(5);
                if (!any) any = temp6.size() > 0;
                //
                this.interfaces_init.add(new NoteAndSignatureInit
                ( (List) temp.get(3), temp6 ));
            }
            }
        }
            }
        }
    }
    
    // this function will create an signature for this object
    // and generate an parent objects
    if (any) this.createSignature();
      
    //## ADD FIELD NAMES        
    
        temp = (List) ast.get(5);
    //    System.out.println(temp);
        // link fields for class 
        // if enum class there, then iterate for enum variables
        if ((this.access & ACC_ENUM) > 0)
        {
            
          fieldlink = new FieldLink();
          fieldlink.access = ACC_PRIVATE + ACC_STATIC + ACC_FINAL + ACC_SYNTHETIC;
          fieldlink.name = enum_values;
          // add this object
      //    fieldlink.owner = this;
          
          fieldlink.descriptor = getDescriptor(this.name, 1);
          // add field to the fieldmap
          this.fields.put(enum_values, fieldlink);
            
      //      System.out.println(temp1);
            
        
        }
        
    // ## ADD METHOD NAMES
        // this flag indicate if object is an enum
        int is_enum = (this.access & ACC_ENUM);
        // if enum class there, then iterate for enum 
        if ((is_enum & ACC_ENUM) > 0)
        {   
            this.addMethodLink("values", "()" + getDescriptor(this.name, 1), ACC_PUBLIC + ACC_STATIC);
    //        MethodLink link1 = this.addMethodLink("values", "()" + getDescriptor(this.name, 1), ACC_PUBLIC + ACC_STATIC, null);
            this.addMethodLink("valueOf", "(Ljava/lang/String;)" + getDescriptor(this.name, 0), ACC_PUBLIC + ACC_STATIC);
        } 
        
    int t1 = 0;
    int t2 = 0;
    int t3 = 0;
    int t4 = 0;
    int t5 = 0;
    int t6 = 0;
    
    temp1 = (List) temp.get(1);
    temp2 = (List) temp.get(2);
    temp3 = (List) temp.get(3);
    temp4 = (List) temp.get(4);
    temp5 = (List) temp.get(5);
    
    if (this.outer != null)
    {
        is_enum |= (this.access & ACC_STATIC);
    }
    else
    {
        is_enum |= ACC_STATIC;
    }
    
    for (Object i: ((List) temp.get(6)))
    {
        t6 = (int) (double) (Double) i;
        // switch t6
        switch(t6)
        {
            case 1:
            temp6 = (List)temp1.get(t1); 
            
            this.addMethodLink(temp6, is_enum); 
            
            t1 ++;
            break;
            case 2:
            temp6 = (List)temp2.get(t2); 
            
            this.addFieldLink(temp6);
            
            t2 ++;
            break;
            case 3:
            temp6 = (List)temp3.get(t3); 
            
            this.addConstructor(temp6);
            
            t3 ++;
            break;
            case 4:
            temp6 = (List)temp4.get(t4); 
            
            this.addEnumFieldLink(temp6);
            
            t4 ++;
            break;
            case 5:
            temp6 = (List)temp5.get(t5); 
            
            this.addParameter(temp6);
            
            t5 ++;
            break;
            default:
            
        }
        t6 ++;
    }
    // check for constructor
    if (!this.methods.containsKey("<init>"))
    {
        if ((is_enum & ACC_ENUM) > 0)
        {
            this.addMethodLink("<init>", "(ILjava/lang/String;)V",
                               ACC_PRIVATE, null);
        }
        else if ((is_enum & ACC_STATIC) == 0)
        {
            this.addMethodLink("<init>", "(L"+this.outer.name+";)V", ACC_PUBLIC, null);
        }
        else //if (this.outer == null)
        {
            this.addMethodLink("<init>", "()V", ACC_PUBLIC, null);
        }
    }
    /*
        // iterate enum
        temp1 = (List) temp.get(4);
        for (Object i : temp1)
        {
            this.addEnumFieldLink((List) i);
        }
        // iterate annotation parameters
        temp1 = (List) temp.get(5);
        for (Object i : temp1)
        {
            this.addParameter((List) i);
        }
        // 
        temp1 = (List) temp.get(2);
        // iterate for fields
        for (Object i: temp1)
        {
            this.addFieldLink((List)i);
        }
        // iterate for methods
        temp1 = (List) temp.get(1);
        //
        for (Object i : temp1)
        {
            this.addMethodLink((List)i, is_enum);
        }
      */  
        if (!this.name.equals(ClassObject.obj_name))
    { 
        // this is an error map for abstract methods
        Map<Pair<String, List<String>>, List<Flaw>> map_method_abstract = new HashMap<>();
        //
            Map<Pair<String, List<String>>, ClassTable> map_object = new HashMap<>();
            Map<Pair<String, List<String>>, ClassTable> map_object_abstract = new HashMap<>();
        //
            Map<Pair<String, List<String>>, Pair<MethodLink, ClassTable>> map_types = new HashMap<>();
        // 
        boolean na = ((this.access & ACC_ABSTRACT) == 0);
        // methodlink
        MethodLink link, link2;
        // get iterator
        Iterator<ClassTable> parents = this.getTable().parents.iterator();
        // get first classobject
        ClassTable object1 = parents.next();
        // get first superclass iterator
        Iterator<MethodLink> iterator = object1.getMethodIteratorA();
        // check inheritance level and get condition object
        MethodMap.Condition condition = (object1.isSamePackage(this))
              ? MethodMap.privateCondition
              : MethodMap.publicCondition;
        // iterate over iterator
        while (iterator.hasNext())
        {
            // get next link
            link = iterator.next();
            // if link name is init or clinit
            if (link.name.charAt(0) == '<')
            {
                continue;
            }
            // get base link
            link2 = this.getMethodLink(link.name, link.getList());
            // check link
            if (condition.checkMethod(link) && (link2 != null))
            {
                if ((link.access & ACC_FINAL) > 0)
                {
                    token = (Token)link2.init.init.get(3);
                    this.ast.error_pool.add(new Flaw(Flaw.type.cannot_override, 
                        token.source, token.line, token.position,  
                        link2.name+link2.getList(), this.name, link.name + link.getList(), object1.getName(),
                        "final" ));
                }
                // then check access level, and static level
                else if (((link.access | link2.access)&ACC_STATIC) == 0)
                {
                    
                    if ((link.access & ACC_PROTECTED) > 0)
                    {
                        if ((link2.access & (ACC_PUBLIC|ACC_PROTECTED)) == 0)
                        {
                            // raise cannot override error
                    token = (Token)link2.init.init.get(3);
                    this.ast.error_pool.add(new Flaw(Flaw.type.cannot_override, 
                        token.source, token.line, token.position,  
                        link2.name+link2.getList(), this.name, link.name + link.getList(), object1.getName(),
                        "protected" ));
                        }
                    }
                    else if ((link.access & ACC_PUBLIC) > 0)
                    {
                        if ((link2.access & ACC_PUBLIC) == 0)
                        {
                            // raise cannot override error
                    token = (Token)link2.init.init.get(3);
                    this.ast.error_pool.add(new Flaw(Flaw.type.cannot_override, 
                        token.source, token.line, token.position,  
                        link2.name+link2.getList(), this.name, link.name + link.getList(), object1.getName(),
                        "public" ));
                        }
                    }
                    else
                    {
                        if ((link2.access & ACC_PRIVATE) > 0)
                        {
                            // raise cannot override error
                    token = (Token)link2.init.init.get(3);
                    this.ast.error_pool.add(new Flaw(Flaw.type.cannot_override, 
                        token.source, token.line, token.position,  
                        link2.name+link2.getList(), this.name, link.name + link.getList(), object1.getName(),
                        "package" ));
                        }
                    }
                }
                else
                {
                    // raise cannot override error
                    token = (Token)link2.init.init.get(3);
                    this.ast.error_pool.add(new Flaw(Flaw.type.cannot_override, 
                        token.source, token.line, token.position,  
                        link.name+link.getList(), this.name, link2.name + link2.getList(), object1.getName(),
                        ((link.access&ACC_STATIC)>0) ? "dynamic" : "static" ));
                }
                {
                    //
                    String ret1 = link.getType();
                    String ret2 = link2.getType();
                    // check if > -1
                    if (this.ast.mhandler.checkDescA(ret1, ret2) == -1)
                    {
                        // raise cannot override error
                    token = (Token)link2.init.init.get(3);
                    Flaw f = (new Flaw(Flaw.type.cannot_override_ret, 
                        token.source, token.line, token.position,  
                         link.name+link.getList(), this.name, link2.name + link2.getList(), object1.getName(),
                        ret1, ret2));
                        
                    this.ast.error_pool.add(f);
                    
                    };
                }
            }
            else if ((link.access & ACC_ABSTRACT) > 0) 
            {
                Pair p = new Pair<>(link.name, link.getList());
                // raise if super method is abstract
                if (na)
                {
                    token = (Token)((List)this.init).get(1);
                    
                    
                    List lst = map_method_abstract.getOrDefault(p, new ArrayList<Flaw>());
                    
                    lst.add
                    (new Flaw(Flaw.type.need_override, 
                        token.source, token.line, token.position, 
                        this.name, link.name + link.getList(), object1.getName()));
                        
                    map_method_abstract.put(p, lst);
                }
                else
                {
                    map_object_abstract.put(p, object1);

                    map_types.put(p, new Pair<>(link, object1));
                }
            }
        }
        // iterate over interfaces
        {
            ClassTable object2;
            // create map 
        //    Map<Pair<String, List<String>>, List<>> map_method_default = new HashMap<>();
            // set condition
            MethodMap.Condition condition2 = MethodMap.interfaceCondition;
            // iterate until parents have next object
            while (parents.hasNext())
            {
                
                // get next object
                object2 = parents.next();
                
             //   System.out.println(object2.name);
                // iterate for all methods from object
                iterator = object2.getMethodInterfaceIteratorA();
                // 
                while (iterator.hasNext())
                {
                // get link 
                    link = iterator.next();
                    
            // if link name is init or clinit
            if (link.name.charAt(0) == '<')
            {
                continue;
            }
                // check link
                    if (!condition2.checkMethod(link))
                    {
                        continue;
                    }
                // get same link
                    link2 = this.getMethodLink(link.name, link.getList());
                // if link is not null
                    if (link2 != null)
                    {
                        // 
                        
                            token = (Token)link2.init.init.get(3);
                        // check link2 for compatibility
                            if ((link2.access & ACC_STATIC) > 0)
                            {
                            // add error
                        this.ast.error_pool.add(
                        new Flaw(Flaw.type.cannot_override, 
                        token.source, token.line, token.position,  
                        link.descriptor, this.name, link2.name + link2.getList(), 
                        object2.getName(), "dynamic"));
                            //
                            }
                            else if ((link2.access & ACC_PUBLIC) == 0)
                            {
                            //
                        this.ast.error_pool.add(
                        new Flaw(Flaw.type.cannot_override, 
                        token.source, token.line, token.position,  
                        link.descriptor, this.name, link2.name + link2.getList(), 
                        object2.getName(), "public"));
                            //
                            }
                            // 
                            //
                    String ret1 = link.getType();
                    String ret2 = link2.getType();
                    // check if > -1
                    if (this.ast.mhandler.checkDescA(ret1, ret2) == -1)
                        {
                        // raise cannot override error
                    token = (Token)link2.init.init.get(3);
                    //
                    Flaw f = (new Flaw(Flaw.type.cannot_override_ret, 
                        token.source, token.line, token.position,  
                        link.descriptor, this.name, link2.name + link2.getList(), object1.getName(),
                        ret1, ret2));
                        // 
                    this.ast.error_pool.add(f);
                    
                        };
                    }
                    else
                    {
                        link2 = object1.getMethodA(link.name, link.getList());
                        //
                        if ((link2 == ClassTable.ambiguous_method) ||
                            (link2 == ClassTable.non_exists_method))
                        {
                            link2 = null;
                        }
                        else if (!condition.checkMethod(link2))
                        {
                            link2 = null;
                        }
                        // if object is not abstract
                            Pair<String, List<String>> p = new Pair<>(link.name, link.getList());
                        if (na)
                        {
                            
                        if (link2 != null)
                        if ((link2.access & ACC_ABSTRACT) > 0)
                        {
                            link2 = null;
                        }
                            // 
                            if ((link.access & ACC_ABSTRACT) > 0)
                            {
                                if (link2 == null)
                                {
            //###############
                    token = (Token)((List)this.init).get(1);

                    List lst = map_method_abstract.getOrDefault(p, new ArrayList<Flaw>());
                    
                    lst.add (new Flaw(Flaw.type.need_override, 
                        token.source, token.line, token.position, 
                        this.name, link.name + link.getList(), object1.getName()));
                        
                    map_method_abstract.put(p, lst);
            //###############
                                    
                                }
                            }
                            else
                            {
            //###############
                    map_method_abstract.remove(p);
                    
                    ClassTable obj = map_object.get(p);
                    
                    if (obj == null)
                    {
                        map_object.put(p, object2);
                    }
                    else
                    {
                        token = (Token)((List)this.init).get(1);
                        this.ast.error_pool.add(new Flaw(Flaw.type.unrelated_defaults,
                        token.source, token.line, token.position, 
                              obj.getName(), object2.getName(), link.name + link.getList()));
                    }
                    
                    map_object.put(p, obj);
                    
                    
            //###############
                            }
                            
                        }
                        // else
                        else
                        {
                            if ((link.access & ACC_ABSTRACT) > 0)
                            {
            //################
                    map_object_abstract.put(p, object2);
                    
          //          System.out.println(p);
                    
                    ClassTable obj = map_object.get(p);
                    
            //        System.out.println(map_object);
                    
                    if (obj != null)
                    {
                        
                        token = (Token)((List)this.init).get(1);
                        this.ast.error_pool.add(new Flaw(Flaw.type.unrelated_abstract_and_default,
                        token.source, token.line, token.position, 
                              object2.getName(), obj.getName(), link.name + link.getList()));
                    }
            //################
                            }
                            else
                            {
            //################
                    ClassTable obj = map_object.get(p);
                    
                    
                    
                    if (obj == null)
                    {
                        map_object.put(p, object2);
                    }
                    else
                    {
                        token = (Token)((List)this.init).get(1);
                        this.ast.error_pool.add(new Flaw(Flaw.type.unrelated_defaults,
                        token.source, token.line, token.position, 
                              obj.getName(), object2.getName(), link.name + link.getList()));
                        map_object.put(p, obj);
                    }
                    
                    
                    obj = map_object_abstract.get(p);
                    
                    if (obj != null)
                    {
                        token = (Token)((List)this.init).get(1);
                        this.ast.error_pool.add(new Flaw(Flaw.type.unrelated_abstract_and_default,
                        token.source, token.line, token.position, 
                              obj.getName(), object2.getName(), link.name + link.getList()));
                    }
                    
            //################
                            }
                        }
                      //(((((((((((((((  
                      if (!na)
                      {
                          Pair<MethodLink, ClassTable> pair;
                        if (link2 == null)
                        {
                            //
                            pair = map_types.get(p);
                            //
                        }
                        else
                        {
                            map_types.put(p, (pair = new Pair<>(link2, object1)));
                        }
                        
                        if (pair == null)
                        {
                            map_types.put(p, new Pair<>(link, object2));
                        }
                        else
                        {
                            link2 = pair.a;
                    
                    String ret1 = link.getType();
                    String ret2 = link2.getType();
                    // check if > -1
                    int i = (this.ast.mhandler.checkDescA(ret1, ret2));
                    // if 
                    if (i == -1)
                    {
                        map_types.put(p, new Pair<>(link2, pair.b));
                        i = (this.ast.mhandler.checkDescA(ret2, ret1));
                    }
                    // 
                    if (i == -1)
                    {
                        // raise cannot override error
                    token = (Token)((List)this.init).get(1);
                    //
                    Flaw f = (new Flaw(Flaw.type.unrelated_method_types, 
                        token.source, token.line, token.position,  
                        
                        object2.getName(), pair.b.getName(), link2.name + link2.getList(), 
                        
                        ret1, ret2)
                        );
                        // 
                    this.ast.error_pool.add(f);
                    
                    }
                        }
                      } 
                    //(((((((((((((((((
                    }
                }
            }
        }
        for (List<Flaw> ji: map_method_abstract.values())
        {
            for (Flaw jii: ji)
            {
                this.ast.error_pool.add(jii);
            }
        }
    }
        //
         
        this.cur_link_stage = 0;
        return true;
    };
    
    
   
    
    public boolean addConstructor(List list)
    {
        // check constructor access 
        boolean is_static = (((double)(Double)list.get(0)) > 0);
        // get constructor
        List c = (List)list.get(1);
        // 
        if (is_static)
        {
            this.static_const.add(Arrays.asList(new Token("$init"),c));
        }
        else
        {
            this.dynamic_const.add(Arrays.asList(new Token("$init"), c));
        }
        
        return true;
    }
    
    public boolean link2()
    {
        
        // link1 object
        // link stage must be 1
        // if no, then return false
        if (this.link_stage > 1)
        {
            return false;
        }
        else
        {
            this.link1();
            this.cur_link_stage = 2;
        }
        /*
        // add annotations
        List<List> list = (List)((List)this.init).get(6);
        // iterate over annotations 
        for (List i: list)
        {
            this.addAnnotationLink(this.getAnnotationLink(i));
        }
        // set link stage to 2 
        // and return
        */
//        System.out.println(this.static_const);
  //      System.out.println(this.dynamic_const);
        // iterate over static const
        
        
        
        Map<String, Object> static_field_list = new HashMap<>();
        Map<String, Object> dynamic_field_list = new HashMap<>();
        //
        for (FieldLink link1: this.fields.values())
        {
          if ((link1.access & ACC_FINAL) > 0)
          {
            if ((link1.access & ACC_STATIC) > 0)
            {
                static_field_list.put(link1.name, 
   new ASTExpr.Field(
    new FieldLink(link1.name, link1.descriptor, ACC_STATIC),
    link1.init.parent)
            );
    
            }
            else
            {
                dynamic_field_list.put(link1.name, 
   new ASTExpr.Field(
    new FieldLink(link1.name, link1.descriptor, 0),
    new ASTExpr.ThisConst(link1.init.parent))
            );
            }
          } 
        }
        
        ASTExpr.Local local1 = new ASTExpr.Local();
        ASTExpr.Local local2 = new ASTExpr.Local();
        
        this.ast.expr.local_map.add(local1);
        this.ast.expr.local_map.add(local2);
        
        
        local1.variables = static_field_list;
        
        
//        System.out.println(local1.variables);
        
  //      System.out.println(local2.variables);
        //
        MethodLink method = new MethodLink();
        method.access = ACC_STATIC;
        method.name = ClassObject.clinit;
        method.descriptor = "()V";
        //
        List list;
        method.cinit = list = new ArrayList<>();
        //
        ASTExpr.Getter gt = this.ast.expr.getter;
        //
        this.ast.expr.setClass(this, true);
        //
        // if class is an enum, then iterate for enum
        //
        for (List ls: (List<List>)this.static_const)
        {
            // System.out.println(ls);
            this.ast.expr.localexpr(list, ls);
        };
        //
        boolean is_enum = ((this.access & ACC_ENUM) > 0);
        //
        if (is_enum)
        {
            
list.add(new ASTExpr.Assign(new ASTExpr.Field(
/// add field
          this.addFieldLink("$VALUES", 
    "["+this.name+";", ACC_PRIVATE+ACC_STATIC+
                       ACC_SYNTHETIC+ACC_FINAL
                            ),
        this
                    ),
//////////////////////////////////////////////////
            this.ast.expr.checkNewArrayOfEnum(
               this, 1, this.enum_cache, new Token("")
               )
               )
               );
        }
        //
        list.add(new ASTExpr.Return(null));
        
        this.addMethodLink(method, list);
        
        local2.variables = new HashMap<>();
        local1.variables = dynamic_field_list;
        
        method = new MethodLink();
        method.access = ACC_PRIVATE;
        method.name = ClassObject.ddinit;
        method.descriptor = "()V";
        
        method.cinit = list = new ArrayList<>();
        //
        this.ast.expr.setClass(this, false);
        //
        for (List ls: (List<List>)this.dynamic_const)
        {
            //
            this.ast.expr.localexpr(list, ls);
        }
        //
        list.add(new ASTExpr.Return(null));
        
        this.addMethodLink(method, list);
  //      System.out.println(list);
   //      System.out.println(list);
        InheritanceMap<String, Token> map = new InheritanceMap<>();
        //
        for (MethodLink link: this.method_list)
        {
            // add method to the inheritance map
            if (link.name.equals(ClassObject.ddinit))
            {
                
            }
            else if (link.name.equals(ClassObject.clinit))
            {
                
            }
            else if ((link.access & (ACC_NATIVE|ACC_ABSTRACT)) > 0)
            {
                
            }
            else if (link.name.equals(ClassObject.constr))
            {
                
                if (link.init == null)
                {
                }
                else
                {
                    if (link.init.init == null)
                    {
                        link.init = null;
                    }
                }
                {
                    
                    local2.variables = new HashMap<>();
                    local1.variables = dynamic_field_list;
               //     System.out.println(link.init.init);
                    //
                    list = new ArrayList<>();
                    //
                    this.ast.expr.getConstructor(
            list, link, this, map);
            
                    list.add(new ASTExpr.Return(null));
                    //
                    link.cinit = list;
                }
            }
            else
            {
                if (link.init == null)
                {
                    
                }
                else
                {
       //        

local2.variables = new HashMap<>();
        local1.variables = new HashMap<>();       
       
        //      System.out.println(link.name + link.descriptor);
                    list = new ArrayList<>();
//                    System.out.println("'LINK NAME"+link.name);
                    this.ast.expr.getFunction(list, link, this);
                    switch (link.getType())
                    {
                        case "V":
                        list.add(new ASTExpr.Return(null));
                        break;
                        case "J":
                        list.add(new ASTExpr.Return((Long)0l));
                        break;
                        case "D":
                        list.add(new ASTExpr.Return((Double)0d));
                        break;
                        case "F":
                        list.add(new ASTExpr.Return((Float)0f));
                        break;
                        case "I":
                        list.add(new ASTExpr.Return((Integer)0));
                        break;
                        case "S":
                        list.add(new ASTExpr.Return((Short)(short)0));
                        break;
                        case "B":
                        list.add(new ASTExpr.Return((Byte)(byte)0));
                        break;
                        case "C":
                        list.add(new ASTExpr.Return((Character)(char)0));
                        break;
                        case "Z":
                        list.add(new ASTExpr.Return((Boolean)false));
                        break;
                        default:
                        list.add(new ASTExpr.Return(ASTExpr.Null.instance));
                    }
                    link.cinit = list;
                    
                }
            }
        }
        this.ast.expr.getter = gt;
        //
        
//this.addMethodLink("values", "()" + getDescriptor(
//this.name, 1), ACC_PUBLIC + ACC_STATIC, null);
if (is_enum)
{
//this.addMethodLink("valueOf", "(Ljava/lang/String;)"+getDescriptor(
//this.name, 0), ACC_PUBLIC + ACC_STATIC, null);
MethodLink nod = this.getMethodLink("values");

  nod.cinit = Arrays.asList(
    new  ASTExpr.Return
      (   
        new ASTExpr.Method(
            ArrayType.reference(this.name, 1).getMethodLink("clone"),
            new ASTExpr.Field(
                this.fields.get("$VALUES"),
                this
            ),
            new ArrayList<>()     
        )
      )
    );
  nod = this.getMethodLink("valueOf", ClassObject.str_name_desc);
 
 ClassObject obj1 =this.ast.getClassByName("java/lang/Enum");
 
  nod.cinit = Arrays.asList(
    
    new ASTExpr.Return
    (
       new ASTExpr.Method
       (
          obj1.getMethodLink("valueOf", 
             ClassObject.cls_name_desc,
             ClassObject.str_name_desc),
          obj1,
          Arrays.asList(
          new ASTExpr.ClassConst(this),
          new Variable("0", ClassObject.str_name_desc)
          )
       )
    )
  );
}       
        //
        this.link_stage = 2;
        this.cur_link_stage = 0;
        return true;
    }
    
    //
    public void addAnnotationLink(AnnotationLink link)
    {
   //     this.ast.note_visit.visitClassAnnotation(link, this);
    }
    
    //
    
    // this method will generate an generic objects
    public void generateGeneric(List temp1, int nlen)
    {/*
        String name, superName;
        List<String> order = this.genericOrder;
        HashMap<String, ClassObject> generic = new HashMap<>(nlen);
        List temp2, temp3;
        for (int i = 0; i < nlen; i ++)
    {
        // get class object description
        temp2 = (List) temp1.get(i);
        
        // get name
        name = ((Token) temp2.get(0)).value;
        // get list
        temp3 = (List) temp2.get(1);
        
        // if got null, then set object type
        if (temp3 != null)
        {
        //    
            temp3 = (List)temp3.get(1);
        //   
        }
        else
        {
      //    
            order.add(name);
            generic.put(name, this.table.ast.object_class);
            continue;
        }
        // get object
        ClassObject object = this.ast.getClass(this.outer, this.table, temp3);
        // if got null object, then continue
        if (object == null) continue;
        // if object is not null, then check object
        else
        {
            Token token = (Token)temp3.get(0);
            // check if object is final or enum or interface
            /*
            if ((object.access & ACC_INTERFACE) > 0)
            {
                error_pool.add(new Flaw(Flaw.type.interface_n_exp, 
                source, token.line, token.position));
            }
            *//*
            if ((object.access & ACC_ENUM) > 0)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.enum_n_exp, 
                token.source, token.line, token.position));
            }
            else if ((object.access & ACC_FINAL) > 0)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.final_n_exp, 
                token.source, token.line, token.position));
            }
            else
            {
                // put generic object
                order.add(name);
                generic.put(name, object);
            }
        }
    }*/
        this.generic = new HashMap<>();
    }
    
    //
    public void link3()
    {
        
    }
    
    // this method will create an signature
    public void createSignature()
    {
        
        // get this signature names
        // List list = this.ast.get(2);
        // iterate over got list
        
    }
    
    // this is an set of visited fields
    public Map<String, Object> fields_constructor = new TreeMap<>();
    
    // this method returns object name from list
    public String getObjectName(List ast, boolean is_void)
    {
        //            System.out.println("#object" + ast);
        String object_name;
        List temp3 = (List) ast.get(1);
        int level = (int)(double)ast.get(4);
        //
        if (temp3.size() == 1)
            {
                Token token = (Token) temp3.get(0);
                object_name = token.value;
                // check if object name is void
                
                if (object_name.equals("void"))
            {
                    
                if (is_void)    
                {   
                  if (level > 0)
                  {
                    this.ast.error_pool.add(new Flaw(Flaw.type.void_array_not_exp,
                    token.source, token.line, token.position));
                    return null;
                  }
                  return "V";
                }
                else
                {
    //              System.out.println("--" + is_void);
    
                  this.ast.error_pool.add(new Flaw(Flaw.type.void_not_exp,
                  token.source, token.line, token.position));
                  return null;
                }
            }
                // check if object name is primitive type
                object_name = ClassObject.primitive.get(object_name);
                // if desc is null, then get class name
                if (object_name != null)
                {
                    return "[".repeat(level) +  object_name;
                }
            }
                // get object 
                    ClassObject object = this.ast.getClass(this, this.table, temp3);
                    // get description
                    if (object == null)
                    {
                        return null;
                    }
                    object_name = object.name;
                    // get descriptor
                    return ClassObject.getDescriptor(object_name, level);
    }
    
    // this method returns an classtable
    public ClassTable getTable()
    {
        // 
        if (this.getter == null)
        {
            this.getter = new ClassTable(this);
        }
        //
        return this.getter;
    }
    
    // this method creates an descriptor
    public static String getDescriptor(String classname)
    {
        return getDescriptor(classname, 0);
    }
    
    public static String getDescriptor(String classname, int level)
    {
        // 
        StringBuilder out = new StringBuilder();
        // add [ 
        while (level > 0)
        {
            level -- ;
            out.append("[");
        }
        
        // check if classname is an primitive type
        String prim = ClassObject.primitive.get(classname);
        if (prim != null)
        {
            out.append(prim);
            return out.toString();
        }
        // add package name
        out.append("L");
        out.append(classname);
        out.append(";");
        // return descriptor
        return out.toString();
    }
    
    final public static Map<String, String> primitive;
    
    static
    {
        primitive = new TreeMap<>();
        primitive.put("int", "I");
        primitive.put("byte", "B");
        primitive.put("short", "S");
        primitive.put("long", "J");
        primitive.put("float", "F");
        primitive.put("double", "D");
        primitive.put("boolean", "Z");
        primitive.put("char", "C");
    }
    
    public Object clone()
    {
        return new ClassObject(this);
    }
    
    public ClassObject(ClassObject object)
    {
        this((ClassLink) object, object.ast);
        this.link_stage = object.link_stage;
        
    }
    
    public ClassObject()
    {
        super();
   //     static_methods = new HashSet<>();
   //     table = new ImportTable();
  //      field_init = new TreeMap<>();
//        method_init = new TreeMap<>();
        interfaces = new ArrayList<>();
    //    class_init = new ArrayList<>();
    //    note_init = new ArrayList<>();
    }
   
    public ClassObject(ClassLink link, AST ast)
    {
        this();
        this.link_stage = 40;
    // empty list of generic
        this.generic = new TreeMap<>();
    // ast parser
        this.ast = ast;
    // name of the class
        this.name = link.name;
    // name of the package
        this.packageName = link.packageName;
    // name of the superclass
        this.superName = link.superName;
    // signature
        this.signature = link.signature;
    // access flag
        this.access = link.access;
    // path of the class
        this.path = link.path;
    // list of an interfaces
        this.interfaces = link.interfaces;// = new ArrayList<>();
    // map of the fields
        this.fields = link.fields;// = new TreeMap<>();
    // map of the methods
        this.methods = link.methods;// = new TreeMap<>();
    // map of the annotations
        this.annotations = link.annotations;// = new ArrayList<>();
    // map of the type annotation
        this.typeannotations = link.typeannotations;// = new ArrayList<>();
    // inner classes
        this.innerClasses = link.innerClasses;
    // if classlink is broken
        this.is_broken = link.is_broken;
    // method list
        this.method_list = link.method_list;
    // outer name
        this.outerName = link.outerName;
        // iterate over method list
    }
}

