package com.ejavac;import java.util.*;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.*;
import java.nio.file.*;
import java.nio.*;

class ASTExpr
{
    public AST ast;
    
    public Getter cls_getter;
    
    
    public interface Getter
    {
        // this method will return an new getter
    //    abstract public Getter (String name);
        // this method will return an new class
        default Pair<Getter, ClassObject> getClassField(List ast, Token token)
        {
            return null;
        };
        //
        default Pair<Getter, ClassObject> getClassObject(List ast, Token token)
        {
            return null;
        };
        // this is an anonumous class creator
        default Pair<Getter, ClassObject> getAnonumous(List ast, Token token, List ast1)
        {
            return null;
        }
        //
        default ClassObject getAnonumous(ClassObject object, List ast1, Token toekn)
        {
            return null;
        }
        //
        default ClassObject getOwner()
        {
            return null;
        };
        // this method will return an new getter
        default Pair getGetter(List ast)
        {
            return null;
        };
        // 
        default Pair searchMethodB(Token name, List list)
        {
            return null;
        };
        //
        default Getter createGetter(ClassObject object)
        {
            return null;
        };
        //
        default Getter createGetter(String object)
        {
            return null;
        };
        // this is an field getter
        default Pair<Getter, FieldLink> getField(Token token)
        {
            return null;
        };
        // this is an method searcher
        default Pair<Getter, MethodLink> searchMethod(Token name, List ast)
        {
            return null;
        };
        // this is an this getter
        default Pair<Getter, ClassObject> getThis(Token token)
        {
            return null;
        }
        // this is an super getter
        default Pair<Getter, ClassObject> getSuper(Token token)
        {
            return null;
        }
    //    public String packageName;
    }
    
    // this getter will raise an error if do try to get object from this getter
    public static class PrimitiveGetter implements Getter
    {
        // this ast 
        private AST ast;
        // 
        private ClassObject owner;
        // name
        private String name;
        //
        public ClassObject getOwner()
        {
            return this.owner;
        }
        // 
        public Pair getField(Token token)
        {
            // add error 
            this.ast.error_pool.add(new Flaw(Flaw.type.primitive_deref, 
                               token.source, token.line, token.position, this.name));
            return new Pair(null, null);
        }
        
        public Pair getMethod(Token token, List<String> values)
        {
            // add error 
            this.ast.error_pool.add(new Flaw(Flaw.type.primitive_deref, 
                               token.source, token.line, token.position, this.name));
            return new Pair(null, null);
        }
        public PrimitiveGetter(String name, AST ast, ClassObject owner)
        {
            this.name = name;
            this.ast = ast;
            this.owner = owner;
        }
    }
    
    
    public static class ArrayGetter extends ClassGetter
    {
        ArrayType arraytype;
        PrimitiveGetter getter;
        public ArrayGetter(ArrayType type, ClassObject recipient, AST ast)
        {
            super(ast.object_class, recipient, ast);
            this.arraytype = type;
            this.getter = new PrimitiveGetter("int", ast, ast.object_class);
        }
        
        public MethodLink searchMethodA(Token token, List list)
        {
            if (token.value.equals("clone"))
            {
                if (list.size() == 0)
                {
                    return this.arraytype.getMethodLink("clone");
                }
            }
            return super.searchMethodA(token, list);
        }
        
        public Pair<Getter, FieldLink> getField(Token token)
        {
            if (token.value.equals("length"))
            {
return new Pair<>(this.getter, this.arraytype.fields.get("length"));
            }
            return super.getField(token);
        }
    }
    // this is an getter for dynamic and static fields and methods in object
    public static class ClassGetter implements Getter
    {
        private AST ast;
        // this is an classobject of the owner
        private ClassObject object;
        // this is an recipient
        private ClassObject recipient;
        // this is an flag that indicates if static inner
        private boolean is_static_inner = false;
        // this flag indicates if class must ignore non_static fields
        private boolean ignore_non_static = false;
        // this is an access level flag
        private int access_level = 0;
        // 
        
        // this method will return an owner object
        // this is an constructor
        public ClassGetter(ClassObject donor, ClassObject recipient, AST ast)
        {
            this.ast = ast;
            this.object = donor;
            this.recipient = recipient;
            this.is_static_inner = (donor.access & ACC_STATIC) > 0;
            
            
            //
            if (AST.isSameHost(donor, recipient))
            {
                access_level = 2;
            }
            else 
            {
                if (Pair.compare(recipient.packageName, donor.packageName))
                {
                    access_level = 1;
                }
            }
        }
        
        public ClassObject getOwner()
        {
            return this.object;
        }
        
        public ClassGetter staticGetter()
        {
            this.ignore_non_static = true;
            // return getter
            return this;
        }
        
        public ClassGetter dynamicGetter()
        {
            this.ignore_non_static = false;
            return this;
        }
        
        public ClassObject getAnonumous(ClassObject object, List ast1, Token token)
        {
            //
            
            object.link1();
            
            int anonumous_count = this.ast.getCount(this.object.name);
            //
            token = new Token(token.source, this.object.headName+"$"+anonumous_count,
                              token.line,   token.position);
            //
            ClassObject object2;
            object2 = this.ast.expr.generateAnonumous(ast1, token.value, this.object.table);
            object2.superName = object.superName;
            
            String hostName = object.hostName;
            if (hostName == null)
            {
                hostName = object.headName;
            }
            
            object2.link1();
            
            object2.hostName = hostName;
            
            return object2;
        }
        
        private ClassGetter(){};
        
        public Pair<Getter, ClassObject> getThis(Token token)
        {
            // if set ignore flag, then add error and return 
            if (this.ignore_non_static)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.non_static, 
                  token.source, token.line, token.position, "this"));
                 // return new pair
                return new Pair<>(null, null);
            }
            // return donor object and dynamic class getter
            return new Pair<>(this, this.object);
        }
        
        public Pair<Getter, ClassObject> getSuper(Token token)
        {
            // if set ignore flag, then add error and return 
            if (this.ignore_non_static)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.non_static, 
                  token.source, token.line, token.position, "super"));
                 // return new pair
                return new Pair<>(null, null);
            }
            ClassObject object = this.ast.getClassByName(this.object.superName);
            
            object.link1();
            // return donor object and dynamic class getter
            Getter getter = new ClassGetter(object, this.recipient, ast);
            //
            return new Pair<>(getter, object);
        }
        
        public Pair<Object, MethodLink> searchMethodB(Token token, List list)
        {
            // 
            this.object.link1();
            //
            ClassGetter getter = this;
            
            ClassObject outer= null;
            ClassObject object = null;
            //
            int level = -1;
            boolean is_static = this.is_static_inner;
            
            MethodLink link;
            
            Object OuterField = new ThisConst(this.object); 
            //
            while (true)
            {
                link = getter.searchMethodA(token, list);
                // 
                if (link == ClassTable.non_exists_method)
                {
                    if (outer != null) 
                    {
                        if ((outer.access & ACC_STATIC) > 0)
                        {
                            is_static = true;
                        }
                    }
                    object = getter.getOwner();
                    outer = object.outer;
                    if (outer != null)
                    {
                        outer.link1();
                        
                        //
                        OuterField = new Field
                        ( object.fields.get(object.outerField),
                          OuterField
                        );
                        //
                        getter = new ClassGetter(outer, 
                          this.recipient, this.ast);
                        //
                        getter.is_static_inner = is_static;
                        //
                        //
                        level += 1;
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }
            // if  got ambiguous method
            if (link == ClassTable.non_exists_method)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.method_not_found,
                token.source, token.line, token.position,
                token.value + list));
            }
            else if (link == ClassTable.ambiguous_method)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.ref_ambiguous,
                token.source, token.line, token.position,
                token.value));
            }
            else if (is_static)
            {
                return new Pair<>(object, link);
            }
            else
            {
                return new Pair<>(OuterField, link);
            }
            return new Pair<>(null, null);
        }
        
        public MethodLink searchMethodA(Token token, List list)
        {
            
            //
            this.object.link1();
            //
            MethodMap map;
            //
            String str = token.value;
            // get method map
            if (token.value.equals("<init>")) 
            {
                map = this.object.methods.get("<init>");
            }
            else
            {
                map = this.object.getTable().getMethodMapA(str);
            }
            //
            if (map == null)
            {
                    return ClassTable.non_exists_method;
            }
      //      System.out.println(map);
            //
            MethodMap.Condition condition;
            int must_be_zero = 0;
            int must_be = -1; 
            // check access level
            if (access_level < 2)
            {
                must_be_zero = ACC_PRIVATE;
                if (access_level < 1)
                {
                    must_be = ACC_PUBLIC;
                }
            }
            if (this.ignore_non_static)
            {
                if (must_be == -1) must_be = 0;
                must_be |= ACC_STATIC;
            }
            
            condition = MethodMap.createCondition(must_be_zero, must_be);            
        //    System.out.println(condition.checkMethod((MethodLink)map.get(new ArrayList<>())));
        //    System.out.println(list);
            //
            
      //      System.out.println(must_be);
      //      System.out.println(must_be_zero);
            
            map = (MethodMap)(new MethodMap.MethodMapCondition(condition, map));
            
     //       System.out.println(map);
            // search for method and return
            MethodLink link = this.ast.mhandler.searchMethod(map, list);
        //    System.out.println(link == ClassTable.non_exists_method);
        //    System.out.println(link == ClassTable.ambiguous_method);
        //    System.out.println(link);
            
            // 
            
            return link;
        }
        
        public Pair<Getter, MethodLink> searchMethod(Token token, List list)
        {
            String str = token.value;
            MethodLink link = searchMethodA(token, list);
            //
            if (link == null)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.symbol_not_found,
                     token.source, token.line, token.position,
                     str));
                return new Pair<>(null, null);
            }
            else if (link == ClassTable.non_exists_method)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.method_not_found, 
                      token.source, token.line, token.position, str + list));
                return new Pair<>(null, null);
            }
            else if (link == ClassTable.ambiguous_method)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, 
                        token.source, token.line, token.position, str));
                return new Pair<>(null, null);
            }
            // 
            Getter gt = getFieldGetter(link.getType(), this.object);
            return new Pair<>(gt, link);
        }
        
        public Pair<Getter, ClassObject> getAnonumous(List list, Token token, List ast1)
        {
            //
            Pair<Getter, ClassObject> pair = this.getClassObject(list, token);
            //
            int anonumous_count = this.ast.getCount(this.object.name);
            //
            token = new Token(token.source, this.object.headName+"$"+anonumous_count,
                              token.line,   token.position);
            //
            ClassObject object;
            object = this.ast.expr.generateAnonumous(ast1, token.value, this.object.table);
            object.superName = pair.b.superName;
            
            ClassObject b = pair.b;
            
            String hostName = b.hostName;
            if (hostName == null)
            {
                hostName = b.headName;
            }
            object.link1();
            object.hostName = hostName;
            pair.b = object;
            
            return pair;
        }
        
        public Pair<Getter, ClassObject> getClassObject(List list, Token token)
        {
            // get class
            ClassObject object = this.ast.getClass(this.object, this.object.table, list);
            //
            if (object == null)
            {
                return new Pair<>(null, null);
            }
            else
            {
          //      System.out.println(object.name);
            }
            //
            object.link1();
            // get getter
            Getter gt = new ClassGetter(object, this.recipient, this.ast);
            // return pair
            return new Pair<>(gt, object);
        }
        
        public Getter createGetter(ClassObject object)
        {
            return new ClassGetter(object, recipient, this.ast);
        }
        
        public Pair<Getter, ClassObject> getClassField(List list, Token token)
        {
            // get class
            ClassObject object = this.ast.getClass(this.object, this.object.table, list);
            object.link1();
            // get getter
            Getter gt = new ClassGetter(this.ast.class_class, this.recipient, this.ast);
            // return pair
            return new Pair<>(gt, object);
        }
        
        //
        private boolean checkAccess(int access)
        {
            // check access level
            if (access_level < 2)
            {
                if ((access & ACC_PRIVATE) > 0)
                {
             //       this.ast.error_pool.add(new Flaw(Flaw.type.private_class, token.source, token.line, token.position, str, object.headName));
                    return false;
                }
                else if (access_level < 1)
                {
                    if ( (access & (ACC_PUBLIC)) == 0 )
                    {
                        return false;
                      //  this.ast.error_pool.add(new Flaw(Flaw.type.private_class, token.source, token.line, token.position, str, object.headName));
                    }
                }
            }
            return true;
        }
        
        public HashMap<String, FieldLink> aliases = new HashMap<>();
        
        public void addAlias(String n, FieldLink out)
        {
            this.aliases.put(n, out);
        }
        // this method will return an first available getter
        public Pair<Pair<Getter, Object>, Integer> getGetter(List list)
        {
            int level = 0;
        //    String temp;
            ClassObject object;
            // 
            boolean is_owner = true;
            int begin = 0;
            // get first token
            Token token = (Token) list.get(0);
            String value = token.value;
            // try to find field
            FieldLink link = this.object.getTable().getFieldA(value);
            // if got field, then return 
            if (link == ClassTable.ambiguous_field)
            {
         //       this.ast.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, token.source, token.line, token.position, token.value));
                return new Pair<>(new Pair<>(null, null), null);
            }
            if ((link == ClassTable.non_exists_field) || !checkAccess(link.access))
            {
                object = this.object.outer;
                while (object != null)
                {
                    //#################
            link = object.getTable().getFieldA(value);
            if (link == ClassTable.ambiguous_field)
            {
           //     this.ast.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, token.source, token.line, token.position, token.value));
                return new Pair<>(new Pair<>(null, null), null);
            }
            else if ((link != ClassTable.non_exists_field) & checkAccess(link.access))
            {
                break;
            }
            else
            {
                if((object.access & ACC_STATIC) > 0)
                {
                    is_owner = false;
                }
                object = object.outer;
                level += 1;
            }
                    //#################
                }
            }
            else
            {
                
                this.object.link1();
                // if object is not static, then return this const
                if (((link.access&ACC_STATIC)==0)&&(!this.ignore_non_static))
                {
         //           System.out.println(""+is_static_inner + " "+ this.object.name);
                    return new Pair<>(new Pair<>(this, new ThisConst(this.object)), 0);
                }
                else
                {
                    return new Pair<>(new Pair<>(this, this.object), 0);
                }
            }
            //
            if ((link == ClassTable.non_exists_field) || !checkAccess(link.access))
            {
                Pair<ClassObject, FieldLink> pair = this.object.table.getFieldPair(value);
                // this is an fieldlink;
                link = pair.b;
                // this is an owner classlink
                object = pair.a;
                // if got link, then continue
                if (link == ClassTable.ambiguous_field)
        {
                this.ast.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, token.source, token.line, token.position, token.value));
                return new Pair<>(new Pair<>(null, null), null);
        }
                else if ((link != null) && ((link != ClassTable.non_exists_field) & checkAccess(link.access)))
                {
                    ClassGetter cgt = new 
                    ClassGetter(object, this.recipient, this.ast).
                    staticGetter();
                    cgt.addAlias(value, link);
                    return new Pair<>(new Pair<>(cgt, object), 0);
                }
                //
            }
            else
            {
    
    //            System.out.println("OWNER");
                object.link1();
                this.recipient.link1();
                
                if (this.ignore_non_static | this.is_static_inner | (!is_owner))
                {
                    return new Pair<>(new Pair<>(new ClassGetter(object,
                     this.recipient, this.ast).staticGetter(), object), 0);
                }
                else
                {
                    return new Pair<>(new Pair<>(new ClassGetter(object, 
                            this.recipient, this.ast),
    
    
                    this.ast.expr.outerField(this.object, level) ), 0);
                }
            }
            
            
            
            this.object.link1();
            // try to get inner class
            object = this.object;
            ClassObject object2;
            // try to get class
            Pair<ClassObject, Integer> pair = this.ast.getFirstClass(this.object, this.object.table, list);
            // if pair element is null, then return null
            if (pair.a == null) 
            {
                return new Pair<>(new Pair<>(null, null), null);
            }
            // set object
            object = pair.a;
            //
            int i = pair.b, n = list.size();
            //
            for (; i < n; i++)
            {
                Token tt = (Token)list.get(i);
                
                link = object.getTable().getFieldA(tt.value);
                
                if (link != ClassTable.non_exists_field)
                {
                    if (checkAccess(link.access))
                    {
                        break;
                    }
                }
                
                object2 = object.getTable().getClassA(tt.value);
                
                if (object2 == ClassTable.non_exists_object)
                {
                    break;
                }
                else if (object2 == ClassTable.ambiguous_object)
                {
             //       this.ast.error_pool.add(new Flaw(Flaw.type.
         //           ref_ambiguous, tt.source, tt.line, 
      //              tt.position, tt.value));
                    return new Pair<>(new Pair<>(null, null), null);
                }
                else if (!checkAccess(object2.access))
                {
                    break;
                }
                object = object2;
            }
            
            // else return object
            begin = i;
            // 
            
        //    System.out.println(object.name);
      //      System.out.println("system getter");
            return new Pair<>(new Pair(new ClassGetter(object, this.recipient, this.ast).staticGetter(), object), begin);
        }
        
        // this method will return an method from classobject
      //  public Pair getMethod(Token token, List<String> values)
      //  {
      //  }
        // this method will return an field from classobject
        public Pair<Getter, FieldLink> getField(Token token)
        {
            // get string
            String str = token.value;
            //
            
            // get fieldlink
            FieldLink link = this.aliases.get(str);
            
            if (link == null)
            {
                link = object.getTable().getFieldA(str);
            }
        //    System.out.println("OBJECT: " + object);
        //    System.out.println("LINK: " + link);
            // check field access level
            if (link == ClassTable.non_exists_field)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, str));
                return new Pair<>(null, null);
            }
            else if (link == ClassTable.ambiguous_field)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, token.source, token.line, token.position, str));
                return new Pair<>(null, null);
            }
            // check access level
            if (access_level < 2)
            {
                if ((link.access & ACC_PRIVATE) > 0)
                {
                    this.ast.error_pool.add(new Flaw(Flaw.type.private_class, token.source, token.line, token.position, str, object.headName));
                }
                else if (access_level < 1)
                {
                    if ( (link.access & (ACC_PUBLIC)) == 0 )
                    {
                        this.ast.error_pool.add(new Flaw(Flaw.type.private_class, token.source, token.line, token.position, str, object.headName));
                    }
                }
            }
            
            // if ignore non_static flag is set, then return null
                if (this.ignore_non_static)
                {
                    if ((link.access & ACC_STATIC) == 0)
                    {
                        this.ast.error_pool.add(new Flaw
                        (
                        Flaw.type.non_static, token.source, token.line, 
                        token.position, token.value)
                        );
                    }
                }
            // get object 
       //     ClassObject object = this.ast.getClassByName(link.descriptor);
            // get getter
            Getter gt = getFieldGetter(link.descriptor, this.object);//new ClassGetter(object, this.object, this.ast);
            // return pair
            return new Pair<>(gt, link);
        }
        //
        public Getter createGetter(String desc)
        {
            return getFieldGetter(desc, this.object);
        }
        
        private Getter getFieldGetter(String name, ClassObject object)
        {
            return this.ast.expr.getFieldGetter(name, object, this.recipient);
        }
        
        /*
        // get method
        public Pair<Getter, MethodLink> getMethod(Token token, String [] values)
        {
            // get string
            String str = token.value;
            // get method from classtable 
            MethodLink link = this.object.getTable().getMethodA(str, values);
            // check field access level
            if (link == ClassTable.non_exists_method)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, str));
                return new Pair(null, null);
            }
            else if (link == ClassTable.ambiguous_field)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, token.source, token.line, token.position, str));
                return new Pair(null, null);
            }
            // check access level
            if (access_level < 2)
            {
                if ((link.access & ACC_PRIVATE) > 0)
                {
                    this.ast.error_pool.add(new Flaw(Flaw.type.private_class, token.source, token.line, token.position, str, object.headName));
                }
                else if (access_level < 1)
                {
                    if ( (link.access & (ACC_PUBLIC | ACC_PROTECTED)) == 0 )
                    {
                        this.ast.error_pool.add(new Flaw(Flaw.type.private_class, token.source, token.line, token.position, str, object.headName));
                    }
                }
            }
            // get object
     //       ClassObject object = this.ast.getClassByName(;
            return null;
        }
        */
    }
    
    //
        public Object outerField(ClassObject object, int level)
        {
            Object OuterField = new ThisConst(object);
            while (level >= 0)
            {
                level --;
                OuterField = new Field(object.fields.get(object.outerField), 
                                          OuterField);
                object = object.outer;
            }
            return OuterField;
        }
    //
    public ASTExpr(AST ast)
    {
//        System.out.println(ast);
        
        this.ast = ast;
        
  //      System.out.println(this.ast);
    };
    
    // this method will return an field getter
        public Getter getFieldGetter(String desk, ClassObject owner, ClassObject recipient)
        {
            //
            
  //          System.out.println(desk);
       //     System.out.println(this.ast);
            // this is an field descriptor
         //   String desk = link.descriptor;
            
         //   System.out.println(desk);
            // this is length of type descriptor
            int len = desk.length();
            // check if description is not an primitive type
            if (len == 1)
            {
                switch (desk)
                {
                    case "I":
                    {
           //             System.out.println('g');
             //           System.out.println(this.ast);
                        return new PrimitiveGetter("int", this.ast, owner);
                    }
                    case "J": return new PrimitiveGetter("long", this.ast, owner);
                    case "D": return new PrimitiveGetter("double", this.ast, owner);
                    case "F": return new PrimitiveGetter("float", this.ast, owner);
                    case "B": return new PrimitiveGetter("byte", this.ast, owner);
                    case "C": return new PrimitiveGetter("char", this.ast, owner);
                    case "S": return new PrimitiveGetter("short", this.ast, owner);
                    case "Z": return new PrimitiveGetter("boolean", this.ast, owner);
                    case "V": return new PrimitiveGetter("void", this.ast, owner);
                }
                return null;
            }
            else 
            {
                AST ast = this.ast;
                
       //         System.out.println(desk);
                // this is an index of last [ element
                int array = desk.lastIndexOf("[");
                // this flag indicates if an object is primitive 
                boolean primitive = (array == (len - 2));
                
                
       //         System.out.println(primitive);
       //         System.out.println(array);
                // get desc object 
                if (primitive) 
                {
                    desk = desk.substring(len - 1, len);
                }
                else 
                {
                    desk = desk.substring(array + 2, len - 1);
                }
                
                array ++;
                // if array index is not -1, then get array object
                if (array == 0)
                {
                    return new ClassGetter(
                    ast.getClassByName(desk), recipient, ast);
                }
                // else, get an array object
                else
                {
                    // get an primitive array type
                    if (primitive)
                    {
                        return new ArrayGetter(
            ArrayType.primitive(desk, array), recipient, ast);
                    }
                    // get an object array type
                    else
                    {
                        return new ArrayGetter(
            ArrayType.reference(desk, array), recipient, ast);
                    }
                }
            }
          //  AST ast = this.ast;
            // get object with name 'desc'
          //  return new ClassGetter(this.object, ast.getClassByName(desk), ast);
        }
    
    private ASTExpr()
    {
        //
        this.ast = null;
        //
    };
        
    // this function returns an number object
    public Number getNumber(double big, int base)
    {
        if (base < 2)
        {
            if ((big - Math.ceil(big)) > 0)
            {
                base = 3;
            }
        }
        switch(base)
        {
            case 3:
            return (Double) big;
            case 2:
            if (big > Float.MAX_VALUE)
            {
                return (Double) big;
            }
            // if number is null,
            else if (big == 0)
            {
                return (Float) 0f;
            }
            else if ((big < 1) && (big < Float.MIN_VALUE))
            {
                return (Double) big;
            }
            else if ((big < 2) && (big < Float.MIN_NORMAL))
            {
                return (Double) big;
            }
            else
            {
                return (Float)(float) big;
            }
            case 1:
            
            {
                return (Long)(long) big;
            }
            default:
            {
                if ((big < Integer.MAX_VALUE)&&(big > Integer.MIN_VALUE))
                {
                    return (Integer)(int) big;
                }
                else
                {
                    return (Long)(long) big;
                }
            }
        }
    }
    //
    public Number getNumber(String name)
    {
        // check length of number name
        // if length is 1, then return 
        if (name.length() < 2)
        {
            return Integer.valueOf(name);
        }
        // 
        double big = 0;
        double low = 0;
        double div = 1;
        int base = 10;
        int i = 0;
        int len = name.length();
        // get first char from name
        char c = name.charAt(0);
        // if got 0, then check next char
        if (c == '0')
        {
            c = name.charAt(1);
            // if got b, then set base equals to 2
            if (c == 'b') base = 2;
            else if (c == 'o') base = 8;
            else if (c == 'x') base = 16; 
            i = 1;
        }
        // iterate for all symbols
        while (i < len)
        {
            c = name.charAt(i);
            if ((c >= '0') && (c <= '9'))
            {
             //   System.out.println(c);
                big = big * base + c - '0';
            }
            else if ((c >= 'a') && (c <= 'f'))
            {
                big = big * base + c - 'a' + 10;
            }
            else if (c == '.')
            {
                i ++;
                break;
            }
            i++;
        }
        while (i < len)
        {
            c = name.charAt(i);
            if ((c >= '0') && (c <= '9'))
            {
                low = low * base + c - '0';
                div = div * base;
            }
            else if ((c >= 'a') && (c <= 'f'))
            {
                low = low * base + c - 'a' + 10;
                div = div * base;
            }
            i ++;
        }
        big = big + (low / div);
        // if base is 10
        if (base == 10)
        {
            // get char element
            c = name.charAt(len - 1);
            // check if last symbol is special symbol
            switch(c)
            {
                case 'l':
                base = 1;
                break;
                case 'd':
                base = 3;
                break;
                case 'f':
                base = 2;
                break;
                default:
                base = 0;
                break;
            }
        }
        else
        {
            base = 0;
        }
        
        return getNumber(big, base);
    }
    
    // this is an method for expression class cast
    public static String classToString(Object object)
    {
        // 
        if (object instanceof ClassLink)
        {
            return "ClassObject: " + ((ClassLink)object).name;
        }
        else if (object instanceof MethodLink)
        {
            return "MethodLink: " + ((MethodLink)object).name+((MethodLink)object).descriptor;
        }
        else if (object == null)
        {
            return "_null_";
        }
        {
            return object.toString();
        }
    }
    
    // this method will return an expression description or null 
    // if object is not object reference
    public static String getReference(Object object)
    {
        if (object instanceof Reference)
        {
            // create reference
            Reference ref = (Reference)object;
            // get reference type
            return ref.getReference();
        }
        if (object == null) return null;
        return (String)primitive_reference.get(object.getClass());
    }
    
    // 
    public static Map primitive_reference = new HashMap<>();
    static
    {
        primitive_reference.put(Double.class, "D");
        primitive_reference.put(Float.class, "F");
        primitive_reference.put(Long.class, "J");
        primitive_reference.put(Integer.class, "I");
        primitive_reference.put(Short.class, "S");
        primitive_reference.put(Byte.class, "B");
        primitive_reference.put(Boolean.class, "Z");
        primitive_reference.put(InstanceOf.class, "Z");
        primitive_reference.put(Character.class, "C");
        primitive_reference.put(String.class, ClassObject.str_name_desc);
    }
    
    // this is an method expression class
    public static class Method implements Reference
    {
        // this is an owner object of this method
        public Object owner;
        // this is an methodlink of this object
        public MethodLink link;
        // this is an arguments of this method expression
        public List<Object> args;
        // 
        
        public String getReference()
        {
            return link.getType();
        }
        
        public Method(MethodLink link, Object owner, List args)
        {
            this.owner = owner;
            this.link = link;
            this.args = args;
        }
        public String toString()
        {
            return "Method: {owner: "+classToString(owner)+"; link: "+classToString(link)+"; args: "+args+" }";  
        }
    }
    
    public static class Item implements Reference
    {
        public String descriptor;
        public Object value;
        public Object owner;
        
        public Item(String desc, Object value, Object value2)
        {
            this.descriptor = desc;
            this.value = value;
            this.owner = value2;
        }
        
        public String getReference()
        {
            return descriptor;
        }
        
        public String toString()
        {
            return "Item: {value: "+classToString(value)+
            "; descriptor: "+ descriptor+"; owner: "+owner+" }";
        }
    }
    
    public static class Cast implements Reference
    {
        // this is an argument object
        public Object value;
        // this is an check object
        public String reference;
        //
        
        public String getReference()
        {
            return reference;
        }
        
        public String getString()
        {
            if (reference.charAt(0) == 'L')
            {
                return reference.substring(1, reference.length()-1);
            }
            else 
            {
                return reference;
            }
        }
        
        public Cast(String ref, Object a1)
        {
            this.reference = ref;
            this.value = a1;
        }
        //
        public String toString()
        {
            return "Cast: {value: "+classToString(value)+
            "; instance: "+getReference()+"}";
        }
    }
    
    public static class CallOperator implements Reference
    {
        public MethodLink link;
        public List<Object>args;
        
        public String getReference()
        {
            return link.getType();
        }
        public CallOperator(MethodLink link, Object ... args)
        {
            this.args = Arrays.asList(args);
            this.link = link;
        }
        public CallOperator(MethodLink link, List<Object> args)
        {
            this.args = args;
            this.link = link;
        }
        
        public String toString()
        {
            return "CallOperator: {operator"+link.name+link.descriptor+"; args: "+args+"}";
        }
    }
    
    public static class New implements Reference
    {
        public ClassObject object;
        public Method init;
        public New(ClassObject object, Method m)
        {
            this.object = object;
            this.init = m;
        }
        public String getReference()
        {
            return object.getReference();
        }
        public String toString()
        {
            return "New: {object: "+object.name+"; init: "+init+"}";
        }
    }
    
    public static class AllocArray implements Reference
    {
        public List args;
        public ArrayType object;
        public int type;
       
        public AllocArray reference(ClassObject a1, List args, int dim)
        {
            this.args = args;
            this.object = ArrayType.reference(a1.name, dim);
            this.type = 0;
            return this;
        }
        
        public AllocArray primitive(String name, List args, int dim)
        {
            this.args = args;
            this.object = ArrayType.primitive(name, dim);
            this.type = 1;
            return this;
        }
       
        public String getReference()
        {
            return this.object.getReference();
        }
        
        public AllocArray()
        {
        }
                
        public String toString()
        {
            return "AllocArray: {reference: "+getReference()
                                         +"; args: "+args+"}";
        }
    }
    
    public static class NewArray implements Reference
    {
        public List args;
        public ArrayType object;
        public int type;
       
        public NewArray reference(ClassObject a1, List args, int dim)
        {
            this.args = args;
            this.object = ArrayType.reference(a1.name, dim);
            this.type = 0;
            return this;
        }
        
        public NewArray reference2(String a1, List args, int dim)
        {
            this.args = args;
            
            char a2 = a1.charAt(0);
            if (a2 == 'L')
            {
                this.object = ArrayType.reference(
                               a1.substring(1, a1.length()-1), dim);
            }
            else
            {
                this.object = ArrayType.primitive(a1, dim);
            }
            this.type = 0;
            return this;
        }
        
        public NewArray primitive(String name, List args, int dim)
        {
            this.args = args;
            this.object = ArrayType.primitive(name, dim);
            this.type = 1;
            return this;
        }
       
        public String getReference()
        {
            return this.object.getReference();
        }
        
        public NewArray()
        {
        }
                
        public String toString()
        {
            return "NewArray: {reference: "+getReference()
                                         +"; args: "+args+"}";
        }
    }
    /*
    public static class NewArrayPrimitive implements Reference
    {
        public String name;
        public List args;
        public int dimension;
        public String getReference()
        {
            return "[".repeat(dimension)+ name;
        }
        public NewArrayPrimitive(String name, List args, int len)
        {
            this.name = name;
            this.args = args;
            this.dimension = len;
        }
        public String toString()
        {
            return "NewArrayPrimitive: {name: "+getReference()
                        +"; args: "+args+"}";
        }
    }*/
    
    public static class InstanceOf implements Reference
    {
        // this is an argument object
        public Object value;
        // this is an check object
        public ClassObject object;
        public String primitive;
        public int dimension;
        //
        public String getReference()
        {
            String str = "[".repeat(this.dimension);
            if (primitive.equals("L"))
            {
                return str + "L" + this.object.name + ";";
            }
            else
            {
                return str + this.primitive;
            }
        }
        public String getString()
        {
          if (dimension > 0)
          {
            String str = "[".repeat(this.dimension);
            if (primitive.equals("L"))
            {
                return str + "L" + this.object.name + ";";
            }
            else
            {
                return str + this.primitive;
            }
          }
          else
          {
              return object.name;
          }
        }
        
        public InstanceOf(Object value, ClassObject object, String primitive, int dimension)
        {
            this.value = value;
            this.object = object;
            this.primitive = primitive;
            this.dimension = dimension;
        }
        //
        public String toString()
        {
            return "InstanceOf: {value: "+classToString(value)+
            "; instance: "+getString()+"}";
        }
    }
    
    public static class ClassConstName 
    {
        public String name;
        public ClassConstName(String name)
        {
            this.name = name;
        }
        
        public String toString()
        {
            return "ClassConst: {object: "+this.name+" }";  
        }
    }

    // this is an class assign
    static class Assign implements Reference
    {
        public Object variable;
        public Object value;
   //     public MethodLink link;
    //    public Cast castlink;
        public Assign(Object variable, Object value)
        {
            this.variable = variable;
            this.value = value;
     //       this.link = link;
        }
        public String getReference()
        {
            return ((Reference)variable).getReference();
        }
        public String toString()
        {
            return "Assign: {variable: "+ 
              classToString(variable) + "; value: "+
              classToString(value)+"}";
        }
    }
    
    // 
    static class BinAssign implements Reference
    {
        public static TreeMap<String, MethodMap<List<String>, MethodLink>> ops;
        static
        {
            ClassLink link = new ClassLink();
            link.addMethodLink("++", "(D)D", 0);
            link.addMethodLink("++", "(F)F", 0);
            link.addMethodLink("++", "(I)I", 0);
            link.addMethodLink("++", "(J)J", 0);
            link.addMethodLink("++", "(S)S", 0);
            link.addMethodLink("++", "(B)B", 0);
            link.addMethodLink("++", "(C)C", 0);
            link.addMethodLink("--", "(D)D", 0);
            link.addMethodLink("--", "(F)F", 0);
            link.addMethodLink("--", "(I)I", 0);
            link.addMethodLink("--", "(J)J", 0);
            link.addMethodLink("--", "(S)S", 0);
            link.addMethodLink("--", "(B)B", 0);
            link.addMethodLink("--", "(C)C", 0);
            ops = link.methods;
        }
        public boolean postfix;
        public Object variable;
        public MethodLink link;
        
        public BinAssign(Object variable, boolean postfix, MethodLink link)
        {
            this.variable = variable;
            this.postfix = postfix;
            this.link = link;
        }
        public String getReference()
        {
            return link.getType();
        }
        public String toString()
        {
            return "BinAssign: {variable: "+variable+"; link: "+link.name+
                               link.descriptor+"; postfix: "+postfix+"}";
        }
    }
    
    // this is an class expression
    static class ClassConst implements Reference
    {
        // this is an classname of this class
        public ClassObject object;
        public ClassConst(ClassObject name)
        {
            this.object = name;
        }
        public String getReference()
        {
            return ClassObject.cls_name_desc;
        }
        public String toString()
        {
            return "ClassConst: {object: "+classToString(object)+" }";  
        }
        private ClassConst(){};
    }
    
    // this is an this expression
    public static class ThisConst implements Reference
    {
        // this is an classname of this class
        public ClassObject object;
        public ThisConst(ClassObject object)
        {
            this.object = object;
        }
        public String getReference()
        {
            return object.getReference();
        }
        public String toString()
        {
            return "ThisConst: {object: "+classToString(object)+" }";  
        }
        private ThisConst(){};
    } 
    
    // this is an super expression
    public static class SuperConst implements Reference
    {
        // this is an classname of this class
        public ClassObject object;
        public SuperConst(ClassObject object)
        {
            this.object = object;
        }
        public String getReference()
        {
            return object.getReference();
        }
        public String toString()
        {
            return "SuperConst: {object: "+classToString(object)+" }";  
        }
        private SuperConst(){};
    } 
    /*
    // this is an owner expression
    public static class OwnerConst implements Reference
    {
        // this is an classname of this class
        public ClassObject object;
        public int level = 0;
        public String getReference()
        {
            return object.getReference();
        }
        public OwnerConst(ClassObject object, int level)
        {
            this.object = object;
            this.level = level;
        }
        public OwnerConst(ClassObject object)
        {
            this.object = object;
        }
        public String toString()
        {
            return "OwnerConst: {object: "+classToString(object)+"; level: "+level+" }";  
        }
        private OwnerConst(){};
    } 
    */
    // this is an field expression class
    public static class Field implements Reference
    {
        // this is an fieldlink of this field
        public FieldLink link;
        // this is an owner object of this method
        public Object owner;
        // this is an constructor for field
        public String getReference()
        {
            return link.descriptor;
        }
        public Field(FieldLink link, Object owner)
        {
            this.link = link;
            this.owner = owner;
        }
        // 
        public String toString()
        {
            return "Field: {owner: "+classToString(owner)+"; link: "+link.name+"["+link.descriptor+"]"+" }";  
        }
    }
    
    // this is an interface for expressions
    public static interface Expression
    {
        public Object run(List ast);
    }
    
    public static class Enum
    {
        public String name; 
        public String descriptor;
        public String reference()
        {
            return descriptor;
        }
        public Enum(String name, String desc)
        {
            this.name = name;
            this.descriptor = desc;
        }
        public String toString()
        {
            return "Enum " + this.name + " " + this.descriptor; 
        }
        // 
    }
    // this is an letter map
    public Map<Character, Character> literals;

    {
        literals = Map.of('t', '\t',
                            'b', '\b',
                            'n', '\n',
                            'r', '\r',
                            'f', '\f');
    }
    
    // this is an interface for math operations
    static interface MathConstDouble// extends MathConst
    {
        public double run(double a1, double a2);
    }
    
    // this is an interface for bitwise math operations
    static interface MathConstBitwise// extends MathConst
    {
        public long run(long a1, long a2);
    }
    
    // this is an interface for bitwise math operations
    static interface MathConstCompare// extends MathConst
    {
        public boolean run(double a1, double a2);
    }
    
    // this is an interface for boolean operations
    static interface BoolConst
    {
        public boolean run(boolean a1, boolean a2);
    }
    
    // this is an boolean map
    public Map<String, BoolConst> bool_const_map;
    {
        bool_const_map = new HashMap<>();
        //
        BoolConst temp = new BoolConst()
        {
            public boolean run(boolean a1, boolean a2)
            {
                // 
                return a1 && a2;
            }
        };
        bool_const_map.put("&", temp);
        bool_const_map.put("&&", temp);
        //
        BoolConst temp_ = new BoolConst()
        {
            public boolean run(boolean a1, boolean a2)
            {
                // 
                return a1 || a2;
            }
        };
        bool_const_map.put("|", temp);
        bool_const_map.put("||", temp);
        //
        temp = new BoolConst()
        {
            public boolean run(boolean a1, boolean a2)
            {
                // 
                return a1 ^ a2;
            }
        };
        bool_const_map.put("^", temp);
        bool_const_map.put("^^", temp);
        //
        bool_const_map.put("==", new BoolConst()
        {
            public boolean run(boolean a1, boolean a2)
            {
                // 
                return a1 == a2;
            }
        });
        //
        bool_const_map.put("!=", new BoolConst()
        {
            public boolean run(boolean a1, boolean a2)
            {
                // 
                return a1 != a2;
            }
        });
    }
    
    // this is an math const map
    public Map<String, Object> math_const_map;
    {
        math_const_map = new HashMap<>();
        math_const_map.put(
        "+", new MathConstDouble()
        {
            public double run(double a1, double a2)
            {
                // 
                return a1 + a2;
            }
        });
        math_const_map.put(
        "-", new MathConstDouble()
        {
            public double run(double a1, double a2)
            {
                // 
                return a1 - a2;
            }
        });
        math_const_map.put(
        "/", new MathConstDouble()
        {
            public double run(double a1, double a2)
            {
                // 
                return a1 / a2;
            }
        });
        math_const_map.put(
        "*", new MathConstDouble()
        {
            public double run(double a1, double a2)
            {
                // 
                return a1 * a2;
            }
        });
        math_const_map.put(
        "%", new MathConstDouble()
        {
            public double run(double a1, double a2)
            {
                // 
                return a1 % a2;
            }
        });
        math_const_map.put(
        ">>", new MathConstBitwise()
        {
            public long run(long a1, long a2)
            {
                // 
                return a1 >> a2;
            }
        });
        math_const_map.put(
        "<<", new MathConstBitwise()
        {
            public long run(long a1, long a2)
            {
                // 
                return a1 << a2;
            }
        });
        math_const_map.put(
        ">>>", new MathConstBitwise()
        {
            public long run(long a1, long a2)
            {
                // 
                return a1 >>> a2;
            }
        });
        math_const_map.put(
        "&", new MathConstBitwise()
        {
            public long run(long a1, long a2)
            {
                // 
                return a1 & a2;
            }
        });
        math_const_map.put(
        "|", new MathConstBitwise()
        {
            public long run(long a1, long a2)
            {
                // 
                return a1 | a2;
            }
        });
        math_const_map.put(
        "^", new MathConstBitwise()
        {
            public long run(long a1, long a2)
            {
                // 
                return a1 ^ a2;
            }
        });
        math_const_map.put(
        "==", new MathConstCompare()
        {
            public boolean run(double a1, double a2)
            {
                // 
                return a1 == a2;
            }
        });
        math_const_map.put(
        "!=", new MathConstCompare()
        {
            public boolean run(double a1, double a2)
            {
                // 
                return a1 != a2;
            }
        });
        math_const_map.put(
        ">", new MathConstCompare()
        {
            public boolean run(double a1, double a2)
            {
                // 
                return a1 > a2;
            }
        });
        math_const_map.put(
        "<", new MathConstCompare()
        {
            public boolean run(double a1, double a2)
            {
                // 
                return a1 < a2;
            }
        });
        math_const_map.put(
        ">=", new MathConstCompare()
        {
            public boolean run(double a1, double a2)
            {
                // 
                return a1 >= a2;
            }
        });
        math_const_map.put(
        "<=", new MathConstCompare()
        {
            public boolean run(double a1, double a2)
            {
                // 
                return a1 <= a2;
            }
        }
        );
    }
    
    // this function returns an instanceof level of number
    public Map<Class, Integer> num_level = new HashMap<>();
    {
        num_level.put(Long.class, 1);
        num_level.put(Float.class, 2);
        num_level.put(Double.class, 3);
    }
    
    // this function returns an string object
    public String getString(String name)
    {
        // 
     //   System.out.println(name);
        
        StringBuilder out = new StringBuilder();
        // iterate for name chars
        char c;
        //
        int i = 1;
        int n = name.length() - 1;
        // iterate until i < n
        while (i < n)
        {
            // get char
            c = name.charAt(i);
            // check if c is /
            if (c == '\\')
            {
                i++;
                c = name.charAt(i);
                out.append(literals.getOrDefault(c, c));
            }
            else
            {
                out.append(c);
            }
            i++;
        }
        // return string
        return out.toString();
    }
     
    // this method returns an character implementation
    public Character getCharacter(String c)
    {
        Character ret = c.charAt(1);
        // check if / element is first
        if (ret == '\\')
        {
            ret = c.charAt(1);
            // 
            return this.literals.getOrDefault(ret, ret);
        }
        else
        {
            return ret;
        }
        //
    }
    
    //
    public ClassObject generateAnonumous(List init,
                String name, ImportTable table)
    {
        // create list
        List list = new ArrayList();
        // add list elements 
        list.add(1D);
        list.add(new Token("", name, 0, 0));
        list.add(new ArrayList());
        list.add(new ArrayList());
        list.add(new ArrayList());
        list.add(init);
        list.add(new ArrayList());
        // add some elements to the list
        return this.ast.parseInit(list, table);
    } 
    
    // this is an map of the const expression
    public Map<String, Expression> constexpr_map;
        
    // this is an map of the expressions
    public Map<String, Expression> expr_map;
    // add static interfaces
    {
        // create expression field
        Expression expr;
        // create map
        constexpr_map = new HashMap<>();
        expr_map = new HashMap<>();
        // 
        // number object
        expr = new Expression()
        {
            public Object run(List ast)
            {
                return getNumber(((Token)ast.get(1)).value);
            }
        };
        //
        expr_map.put("%d", expr);
        constexpr_map.put("%d", expr);
        
        // string object
        expr = new Expression()
        {
            public Object run(List ast)
            {
                return getString(((Token)ast.get(1)).value);
            } 
        };
        
        expr_map.put(
        "$assign", new Expression()
        {
            public Object run(List ast)
            {
                
                return checkAssign((Token)ast.get(1), 
                        getExpression((List)ast.get(2)), 
                        getExpression((List)ast.get(3))  );
            }
        });
        //
        expr_map.put("%s" , expr);
        constexpr_map.put("%s" , expr);
        
        // char object
        expr = new Expression()
        {
            public Object run(List ast)
            {
                return getCharacter(((Token)ast.get(1)).value);
            }
        };
        //
        expr_map.put("%c", expr);
        constexpr_map.put("%c", expr);
        
        expr_map.put(
        ".", new Expression()
        {
            public Object run(List ast)
            {
                return getChainValue(ast);
            } 
        });
        constexpr_map.put(
        ".", new Expression()
        {
            public Object run(List ast)
            {
                // get chain value
                return getConstChainValue(ast);
            } 
        });
        expr_map.put(
        "$cast", new Expression()
        {
            public Object run(List ast)
            {
              //  return null;
                return checkCast((List)ast.get(1), (List)ast.get(2), 
                                                  (Token)ast.get(0));
            }
        });
        expr_map.put(
        "$f_ops", new Expression()
        {
            public Object run(List ast)
            {
                // 
                Object a1 = getExpression((List)ast.get(2));
                //
                List list = (List)ast.get(1);
                //
                for(Token token: (List<Token>)list)
                {
                    if (a1 == null) return null;
                    a1 = getForward(token, a1, true);
                } 
                return a1;
            }
        });
        expr_map.put(
        "$new_arr", new Expression()
        {
            public Object run(List ast)
            {
                // get path
                List a1 = (List)((List)ast.get(1)).get(1);
                // get dimension
                int a2 = (int)(double)(Double)(ast.get(2));
                // get variable list
                List a3 = (List)ast.get(3);
                // iterate over variable list
                Object b1;
                //
                List list = new ArrayList<>();
                for (int i = a3.size() - 1; i >=0; i --)
                {
                    // get expression
                    b1 = getExpression((List)a3.get(i));
                    // if got null, then return null
                    if (b1 == null) return null;
                    // add b1
                    list.add( b1);
                }
                // create array 
                return checkNewArray(a1, a2, list, (Token)ast.get(0));
            }
        });
        expr_map.put(
        "$b_ops", new Expression()
        {
            public Object run(List ast)
            {
                // 
                Object a1 = getExpression((List)ast.get(2));
                //
                List list = (List)ast.get(1);
                //
                for(int i = list.size()-1; i >= 0; i --)
                {
                    if (a1 == null) return null;
                    a1 = getBack((Token)list.get(i), a1, false);
                } 
                return a1;
            }
        });
        expr_map.put(
        "[", new Expression()
        {
            public Object run(List ast)
            {
                return checkItem((List)ast.get(1), 
                       (List)ast.get(2), (Token)ast.get(0));
            }
        });
        expr_map.put(
        "%v", new Expression()
        {
            public Object run(List ast)
            {
                // get token
                return getChainFieldA((Token)ast.get(1));
            }
        });
        constexpr_map.put(
        "%v", new Expression()
        {
            public Object run(List ast)
            {
                // get token
                return getConstChainField((Token)ast.get(1));
            }
        });
        expr_map.put(
        "%k", new Expression()
        {
            public Object run(List ast)
            {
                // get token
                return getKeyValue((Token)ast.get(1));
            }
        });
        expr_map.put(
        "$instanceof", new Expression()
        {
            public Object run(List ast)
            {
                Token token = (Token)ast.get(0);
                
         //       System.out.println(ast);
                // get 1 object
                Object a1 = getExpression((List)ast.get(1));
                // get class
                
                List a2 = (((List)ast.get(2))); 
                //
                //
                return checkInstanceOf(a1, (List)a2.get(1), token, 
                (int)(double)(Double)(a2.get(4)));
            } 
        });
        
        
        expr_map.put(
        "$call", new Expression()
        {
            public Object run(List ast)
            {
         //       List list = new ArrayList<>();
           //     for (List I: (List<List>)ast.get(2))
             //   {
               //     list.add(getExpression(I));
                //}
                
                return checkMethod((Token)ast.get(1), (List)ast.get(2));
            }
        });
        
        expr_map.put(
        "$new", new Expression()
        {
            public Object run(List ast)
            {
                Token n = (Token)ast.get(0);
                List path = (List)((List)ast.get(1)).get(1);
                List expr = (List)ast.get(2);
                return checkNew(path, expr, n);
            }
        });
        
        expr_map.put(
        "$malloc", new Expression()
        {
            public Object run(List ast)
            {
                List path = (List)((List)ast.get(1)).get(1);
                List expr = (List)ast.get(2);
                return checkMalloc(path, expr, (Token)ast.get(0));
            }
        });
        
        //
        expr_map.put(
        "$anonumous", new Expression()
        {
            public Object run(List ast)
            {
                Token n = (Token)ast.get(0);
                List path = (List)((List)ast.get(1)).get(1);
                List expr = (List)ast.get(2);
                List body = (List)ast.get(3);
                return checkAnonumous(path, expr, n, body);
            }
        });
        
        expr_map.put(
        "$math", new Expression()
        {
            public Object run(List ast)
            {
                //
     //           System.out.println("HERE?");
                // get key
                Token key = ((Token)ast.get(1));
       //         System.out.println(key);
                // get 1 object
         //       System.out.println((List)ast.get(2));
           //     System.out.println((List)ast.get(3));
                Object a1 = getExpression((List)ast.get(2));
  //              System.out.println(a1);
    //            System.out.println("A!");
                // get 2 object
                Object a2 = getExpression((List)ast.get(3));
      //          System.out.println(a2);
                // get math key
                return getMath(key, a1, a2, true);
            }  
        });
        constexpr_map.put(
        "$math", new Expression()
        {
            public Object run(List ast)
            {
                // get key
                Token key = ((Token)ast.get(1));
                // get 1 object
                Object a1 = getExpression((List)ast.get(2));
                // get 2 object
                Object a2 = getExpression((List)ast.get(3));
                // get math key
                return getMath(key, a1, a2, false);
            }  
        });
      //  expr_map.put(
      //  "$instanceof", new Expression()
      //  {
      //      public Object run(List ast)
      //      {
                // get expression
      //          Object a1 = getExpression((List)ast.get(1));
                // get type
      //      }
      //  });
    }
    
    public static class Null implements Reference
{
    // Static variable reference of single_instance
    // of type Singleton
    public static final Null instance;
 
    public String getReference()
    {
        return "-";
    }
    
    public String toString()
    {
        return "Null";
    }
 
    static 
    {
        instance = new Null();
    }
    // Constructor
    // Here we will be creating private constructor
    // restricted to this class itself
    private Null()
    {
    }
    // Static method
    // Static method to create instance of Singleton class
}
    
    // this function will return an key value
    public Object getKeyValue(Token token)
    {
        // get key
        String key = token.value;
        // get value
        switch (key)
        {
            case "this":
            {
                return new ThisConst(this.getter.getThis(token).b);
            }
            case "true":
            {
                return true;
            }
            case "false":
            {
                return false;
            }
            case "super":
            {
                return new SuperConst(this.getter.getSuper(token).b);
            }
            case "null":
            {
                return Null.instance;
            }
            default:
            {
                return null;
            }
        }
    }
    
    // this function will return an field
    public Field getChainField(Token token)
    {
        
//        System.out.println(((ClassGetter)this.getter).is_static_inner);
        // create chain list
        List chain = new ArrayList<>(1);
        chain.add(token);
        // get class and getter
        Pair<Pair<Getter, Object>, Integer> pair = this.getter.getGetter(chain);
        // check pair element
        if (pair.b != null)
        {
            if ((pair.b) == 0)
            {
                // get field
                Pair<Getter, FieldLink> p = pair.a.a.getField(token);
                
        //        System.out.println(p);
                // return object
                return new Field(p.b, pair.a.b);
            }
        }
        return null;
    }
    
    //
    public Method checkMethod(Token name, List expr)
    {
        // get expression list
        Pair<List, List<String>> pair = getExpressionList(expr);
        if (pair == null) return null;
        if (pair.b == null) return null;
        // get owner object
        Pair<Object, MethodLink> pair1 = this.getter.searchMethodB(name, pair.b);
        // 
        if (pair1 == null) return null;
        if (pair1.b == null) return null;
        return new Method(pair1.b, pair1.a, pair.a);
    }
    
    public static Method new_method(Object a1, MethodLink link, List a2)
    {
        if ((link.access & ACC_VARARGS) == 0)
        {
            return new Method(link, a1, a2);
        }
        else
        {
            List<String> ls = link.getList();
            String desc = (String) ls.get(ls.size()-1);
            // if list size is < a2 size
            int ii = ls.size();
            int iu = a2.size();
            if (iu < ii)
            {
                //
                int i = desc.lastIndexOf("[")+1;
                desc = desc.substring(i, desc.length());
                //
                a2.add(new NewArray().reference2(desc, new ArrayList<>(), i));
                return new Method(link, a1, a2);
            }
            else if (iu > ii)
            {
                List a22 = a2.subList(0, ii-1);
                List arr = a2.subList(ii-1, a2.size());
                
                int i = desc.lastIndexOf("[")+1;
                desc = desc.substring(i, desc.length());
                //
                a2.add(new NewArray().reference2(desc, arr, i));
                return new Method(link, a1, a22);
            }
            else 
            {
                int i = a2.size() -1;
                Object a22 = a2.get(i);
                if (getReference(a22).lastIndexOf("[") >
                                 desc.lastIndexOf("["))
                {
                    
          int di = desc.lastIndexOf("[");
          desc = desc.substring(di, desc.length());
          
          a22 = new NewArray().reference2(desc, 
                            Arrays.asList(a22), di);
          
          a2.add(i, a22);
          
                }
                
                return new Method(link, a1, a2);
            }
        }
    }
    
    public static Method checkMethod(Getter getter, Token name, List expr)
    {
        List<String> list = new ArrayList<>(expr.size());
        //
        for (Object i: expr)
        {
            list.add(getReference(i));
        }
        //
        
    //    System.out.println("FOOL "+getter.getOwner().name);
        // search for method
        Pair<Getter, MethodLink> pair = getter.searchMethod(name, list);
        // if got null, then return null
        if (pair.b == null) return null;
        // else
        else
        {
     //   System.out.println(pair.a);
     //   System.out.println(pair.b);
            return new Method(pair.b, pair.a.getOwner(), expr);
        }
    }
    
    // this function will return an expression list and description list
    public Pair<List, List<String>> getExpressionList(List dd)
    {
        List list = new ArrayList<>();
        List<String> desc = new ArrayList<>();
        
        Object a;
        
        Pair pair = new Pair(list, desc);
        
        for (Object j: dd)
        {
            list.add((a = getExpression((List)j)));
            if (a == null) return null;
            desc.add(getReference(a));
      //      System.out.println(getReference(a));
        }
        return pair;
    }
    
    
    
    // this function will check for new dynamic constructor
    public New checkNew(Getter gt, ClassObject object, List<List> expr, Token n, Object ... chain_node)
    {
        List args = new ArrayList<>();
        for (Object jk : chain_node)
        {
            args.add(jk);
        }
        
        gt = new ClassGetter(object, object, this.ast);
        // iterate over expr
        for (List i : expr)
        {
            args.add(getExpression(i));
        }
        
   //     System.out.println(args);
        //
        // check method
        Method m = checkMethod(gt, new Token(
                        n.source, "<init>", n.line, n.position), args);
        // if got method, then return
        if (m == null)
        {
            return null;
        }
        else
        {
            return new New(object, m);
        }
    }
    
    // this function will check for new constructor
    public Cast checkAnonumous(List<Token> path, List<List> expr, 
                                  Token n, List init)
    {
        New ret = checkNew(path, expr, n);
        ClassObject object = ret.object;
        ret.object = getter.getAnonumous(ret.object, init, n);
        return new Cast("L"+object.name+";", ret);
    }
    
   // this function will check for new constructor
    public New checkNew(List<Token> path, List<List> expr, Token n)
    {
        // get class ;
        Pair<Getter, ClassObject> pair = this.getter.getClassObject(path, n);
        // check classobject
        ClassObject object  = pair.b;
        //
        object.link1();
    //    System.out.println(object.methods);
        //
        if (object == null) return null;
        //
        List args = new ArrayList<>();
        // if this is an inner dymanic class, then add this pair owner
        if ((object.hostName!=null)&((object.access&ACC_STATIC)==0))
        {
            if (path.size() > 1)
            {
                this.ast.error_pool.add(new Flaw(
                     Flaw.type.enclosing_instance_required,
                     n.source, n.line, n.position, 
                     object.name));
                return null;
            }
            ClassObject obj2 = this.getter.getOwner();
            String name = ((Token)path.get(0)).value;
            //
            InnerClassLink link = obj2.getTable().getInnerClassA(name);
            //
            int level = -1;
            //
            while (link == ClassTable.non_exists_inner)
            {
                level += 1;
                obj2 = obj2.outer;
                link = obj2.getTable().getInnerClassA(name);
            };
            
   //         System.out.println(obj2.name);
     //       System.out.println(level);
            if (level == -1)
            {
                args.add(new ThisConst(obj2));
            }
            else
            {
                args.add(outerField(this.getter.getOwner(), level));
            }
        }
        // iterate over expr
        for (List i : expr)
        {
            args.add(getExpression(i));
        }
        // check method
        Method m = checkMethod(pair.a, new Token(
                        n.source, "<init>", n.line, n.position), args);
        // if got method, then return
        if (m == null)
        {
            return null;
        }
        else
        {
            return new New(object, m);
        }
    }
    
    // this function will check for new array
    public Object checkNewArrayOfEnum(ClassObject object, int dim, List expr, Token token)
    {
        String desc = null, sp;
        // get class object
        desc = object.getReference();
        // get descriptor
        String desc1 = "[".repeat(dim - 1) + desc;
        //
    //    System.out.println(desc1); 
        //
        boolean error = false;
        
        List args = new ArrayList<>(expr.size());
        
        for (Object a1: expr)
        {
            a1 =(getChainField((Token)a1));
            if (a1 == null) error = true;
            args.add(a1);
        }
        
        if (error)
        {
            return false;
        }
        
        // check all arguments
        for (Object a1: args)
        {
//            System.out.println(a1);
            if (this.ast.mhandler.checkDesc(desc1, sp=getReference(a1))==-1)
            {
  //              System.out.println("HERE?");
                this.ast.error_pool.add(new Flaw(
                    Flaw.type.incompatible_types,
                    token.source, token.line, token.position,
                    sp, desc1));
                error = true;
            };
        };
        
        if (error) return null;
        //
        return new NewArray().reference(object, args, dim);
    }
    
    // this function will check for new array
    public Object checkNewArray(List path, int dim, List args, Token token)
    {
        String desc = null, sp;
        ClassObject object = null;
        if (path.size() == 1)
        {
            //
            desc = ClassObject.primitive.get(((Token)path.get(0)).value);
            //
        }
        // get class object
        if (desc == null)
        {
            object = getter.getClassObject(path, token).b;
            if (object == null) return null;
            desc = object.name;
        }
        
        // get descriptor
        String desc1 = "[".repeat(dim - 1) + desc;
        //
    //    System.out.println(desc1); 
        //
        boolean error = false;
        // check all arguments
        for (Object a1: args)
        {
            if (this.ast.mhandler.checkDesc(desc1, sp=getReference(a1))==-1)
            {
                this.ast.error_pool.add(new Flaw(
                    Flaw.type.incompatible_types,
                    token.source, token.line, token.position,
                    sp, desc1));
                error = true;
            };
        };
        
        if (error) return null;
        //
        if (object == null)
        {
            return new NewArray().primitive(desc, args, dim);
        }
        else
        {
            return new NewArray().reference(object, args, dim);
        }
    }
    
    // this function will check for new array
    public Object checkMalloc(List path, List expr, Token token)
    {
        String desc = null, sp;
        ClassObject object = null;
        if (path.size() == 1)
        {
            //
            desc = ClassObject.primitive.get(((Token)path.get(0)).value);
            //
        }
        // get class object
        if (desc == null)
        {
            object = getter.getClassObject(path, token).b;
            if (object == null) return null;
        }
        // get array of expressions
        List<Object> args = new ArrayList<>();
        //
        boolean error = false;
        int n = expr.size()- 1;
        //
//        System.out.println(expr);
        
        for (int i = 0; i < n; i++)
        {
            // get expression
            Object j = (getExpression((List)expr.get(i)));
            
  //          System.out.println(j);
            //
            args.add(j);
            // check expression
            int ii = this.ast.mhandler.checkDesc("I", sp=getReference(j));
            // if got -1, then return null
            if (ii == -1)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.incompatible_types,
                    token.source, token.line, token.position, sp, "int"));
                error = true;
            }
        }
        if (error) return null;
        // get type level
        int level = (int)(double)(Double)expr.get(n);
        // if null, then return null
        // return new function
        if (object != null)
        {
            return new AllocArray().reference(object, args, level);
        }
        else
        {
            return new AllocArray().primitive(desc, args, level);
        }
    }
    
    
    // this function will check for addition information
    public CallOperator checkCallOperator(Token token, Object ... args)
    {
        String key = token.value;
        List<String> list = new ArrayList();
        for (Object i: args)
        {
            list.add(this.getReference(i));
        }
        MethodMap map = ASTExpr.operator_map.get(key);
        
        
  //      System.out.println(list);
        
        MethodLink link = this.ast.mhandler.searchMethod(map, list);
        
        if (link == ClassTable.non_exists_method)
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.no_suitable_operator, 
                token.source, token.line, token.position, 
                key + list)); 
                
            return null;
        };
        
        return new CallOperator(link, args);
    }
    
    public Object checkInstanceOf(Object a1, List a2, Token token, int dim)
    {
        String j = null;
        if (a2.size() == 1)
        {
            j = ClassObject.primitive.get(((Token)a2.get(0)).value);
        }
        if (j != null)
        {
            if (dim > 0)
            {
                return checkInstanceOf(a1, j, token, dim);
            }
            else
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.class_or_array_expected, 
                    token.source, token.line, token.position));
                return null;
            }
        }
        else
        {
            
            // 
            ClassObject object = this.getter.getClassObject(a2, token).b;
            // if got object 
            if (object == null)
            {
                return null;
            }
            else
            {
                return checkInstanceOf(a1, object, token, dim);
            }
        }
    }
    
    public Object checkItem( Object a1, Object a2, Token token)
    {
        //
         a1 = getExpression((List)a1);
         a2 = getExpression((List)a2);
         if ((a1 == null)||(a2 == null)) return null;
        //
        String desc = getReference(a2);
        // check desc access level
        int t = this.ast.mhandler.checkDesc("I", desc);
        //
        if (t == -1)
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.incompatible_types,
            token.source, token.line, token.position, 
            "int", desc));
            return null;
        }
        else
        {
            desc = getReference(a1);
            if (desc == null)
            {
                return null;
            }
          //  if (desc.length()>0)
            {
                if (desc.charAt(0) == '[')
                {
                    return new Item(desc.substring(desc.lastIndexOf("[") + 1),
                            a2, a1);
                }
                else
                {
                    this.ast.error_pool.add(new Flaw(
            Flaw.type.array_required, token.source, token.line, 
            token.position));
                    return null;
                }
            }
        }
        //
    }
    
    public Object checkCast( List a2, Object a1, Token token)
    {
        //
        
  //      System.out.println("CHECKCAST::" + a2);
        
    //    System.out.println(a1);
        a1 = getExpression((List)a1);
        
//        System.out.println(a1);
        //
        if (a1 == null) return null;
        //
        int dim = (int)(double)(Double)a2.get(4);
        List path = (List)a2.get(1);
        
        
        ClassObject object = null;
        String desc = null;
        
        if (path.size() == 1)
        {
            desc = ((Token)path.get(0)).value;
            
  //          System.out.println("DESC::: "+desc);
            
            desc = ClassObject.primitive.get(desc);
        }
        
        if (desc == null)
        {
            object = this.getter.getClassObject(path, token).b;
            
            if (object == null)
            {
                return null;
            }
            else
            {
                return new Cast("[".repeat(dim) + "L"+object.name+";", a1);
            }
        }
        else
        {
            return new Cast("[".repeat(dim) + desc, a1);
        }
    }
    // this function will check for instanceof field
    public Object checkInstanceOf(Object a1, String desc, Token token, int dim)
    {
        // check a1 description
        InstanceOf ins = new InstanceOf(a1, null, desc, dim);
        String name = ins.getString();
        desc = getReference(a1);
        // check desc access level
        int t = this.ast.mhandler.checkDescA(desc, name);
        if (t == -1) t = this.ast.mhandler.checkDescA(name, desc);
        // if t is -1
        if (t == -1)
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.incompatible_types,
            token.source, token.line, token.position, 
            desc, name));
            return null;
        }
        else
        {
            return ins;
        }
    }
    public Object checkInstanceOf(Object a1, ClassObject a2, Token token, int dim)
    {
        // check a1 description
        InstanceOf ins = new InstanceOf(a1, a2, "L", dim);
        String name = ins.getReference();
        String desc = getReference(a1);
        // check desc access level
        int t = this.ast.mhandler.checkDescA(desc, name);
        if (t == -1) t = this.ast.mhandler.checkDescA(name, desc);
        // if t is -1
        if (t == -1)
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.incompatible_types,
            token.source, token.line, token.position, 
            desc, name));
            return null;
        }
        else
        {
            return ins;
        }
    }
    
    // this function will create an class getter
    public void setClass(ClassObject object, boolean is_static)
    {
        ClassGetter obj = new ClassGetter(object, object, this.ast);
        if (is_static)
        {
            obj.staticGetter();
        }
        
        this.getter = obj;
    }
    
    // this function will return an constant field
    public Object getConstChainField(Token token)
    {
        // create chain list
        List chain = new ArrayList<>(1);
        chain.add(token);
        // get class and getter
        Pair<Pair<Getter, Object>, Integer> pair = this.getter.getGetter(chain);
        // check pair element
        if (pair.b != null)
        {
            if ((pair.b) == 0)
            {
                // set object
                Object object = pair.a.b;
                // get field
                FieldLink link = (FieldLink)pair.a.a.getField(token).b;
                // return object
                {
                    // convert to the field
              //      Field field = (Field) object;
                    // get field 
              //      FieldLink link = field.link;
                    // get field parent object
              //      object = field.owner;
                    // this is an description
                    String desc;
                    // check if object is an ClassObject instance
                    // if not, then add error to the error pool
                    if (object instanceof ClassObject)
                    {
        // backup error pool
        List list = this.ast.error_pool;
        // create new error pool
        this.ast.error_pool = new LinkedList<>();
        // get object
        object = getFieldConstValue(link);
        // reset error pool
        this.ast.error_pool = list;
                        // if object is not null
                        if (object != null)
                        {
                            return object;
                        }
                    }
                        // if object is not null, then continue
                }
            }
        }
        
        this.ast.error_pool.add(new Flaw(Flaw.type.const_expr,
        token.source, token.line, token.position));
        
        return null;
    }
    
    // this function will order an chain list
    public Object getConstChainValue(List ast)
    {
        // get chain value
        Object object = this.getChainValue(ast);
        // check if object is an field object
        if (object instanceof Field)
                {
                    // convert to the field
                    Field field = (Field) object;
                    // get field 
                    FieldLink link = field.link;
                    // get field parent object
                    object = field.owner;
                    // this is an description
                    String desc;
                    // check if object is an ClassObject instance
                    // if not, then add error to the error pool
                    if (object instanceof ClassObject)
                    {
        // backup error pool
        List list = this.ast.error_pool;
        // create new error pool
        this.ast.error_pool = new LinkedList<>();
        // get object
        object = getFieldConstValue(link);
        // reset error pool
        this.ast.error_pool = list;
                        // if object is not null
                        if (object != null)
                        {
                            return object;
                        }
                    }
                    else
                    {
                        object = null;
                    }
                        // if object is not null, then continue
                    // 
                }
        // check if object is an class object
        else if (object instanceof ClassConst)
        {
            return object;
        }
        
     //   System.out.println(object);
        
        Token token = (Token)ast.get(0);
        this.ast.error_pool.add(new Flaw(Flaw.type.const_expr,
        token.source, token.line, token.position));
         
        return null;
    }
    
    // this function will check an constant value for compatibility
    public boolean validateConstant(String desc, Object object)
    {
        
        //        System.out.println(desc);
          //      System.out.println("L"+ClassObject.str_name+";");
            //    System.out.println(object.getClass());
        if (object == null) return false;
        // check object value and field description
        if (desc.equals(ClassObject.str_name_desc))
        {
            return object instanceof String;
        } 
        
        // check object value and field description
        if (desc.equals(ClassObject.cls_name_desc))
        {
            return object instanceof ClassConst;
        } 
        
        // 
        if (object instanceof ASTExpr.Enum)
        {
            // 
            ASTExpr.Enum e = (ASTExpr.Enum) object;
            //
            if (e.descriptor.equals(desc))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        //
        if (desc.length() != 1)
        {
            return false;
        }
        else
        switch(desc)
        {
            case "Z": 
            if (!(object instanceof Boolean))
            {
                return false;
            }
            break;
            case "C":
            if (!(object instanceof Character))
            {
                return false;
            }
            break;
            case "B":
            if (object instanceof Short)
            {
                return false;
            }
            case "S":
            if (object instanceof Integer)
            {
                return false;
            }
            if (object instanceof Character)
            {
                return false;
            }
            case "I":
            if (object instanceof Long)
            {
                return false;
            }
            case "J":
            if (object instanceof Float)
            {
                return false;
            }
            case "F":
            if (object instanceof Double)
            {
                return false;
            }
            case "D":
            if (object instanceof Character)
            {
                object = (int)(char)(Character)(object);
            }
            if (!(object instanceof Number))
            {
                return false;
            }
            else
            {
                return true;
            }
            default:
            {
                return false;
            }
        }
        return false;
    }
    
    // this function will get an default value from fieldlink
    public Object getFieldConstValue(FieldLink link)
    {
        // if link is enum type, then return enum
        if ((link.access & ACC_ENUM) > 0) 
        {
            return new Enum(link.name, link.descriptor);
        }
        // get value from link
        Object object = link.value; 
        // if got not null, then return
        if (object != null) return object;
        // if fieldlink don't have an init, then continue
        if (link.init == null)
        {
            return null;
        }
        // else, check usage of this element
        else
        {
            if ((link.init.usage & 1) > 0)
            {
                // then return null
                return null;
            }
            else
            {
                link.init.usage |= 1;
            }
        };
        
        // get an expression
        object = this.getConstExpression(new ClassGetter(
          link.init.parent, link.init.parent, this.ast), link.init.init);
        // if got null or error pool size != 0
        if (object == null)
        {
            return null;
        } 
        //
        String desc = link.descriptor;
        // check for compatibility
        if (!validateConstant(desc, object))
        {
            return null;
        }
        //
        link.value = object;
        return object;
    }
    
    // this function will order an chain list
    public Object getChainValue(List ast)
    {
        
     //   System.out.println(ast);
        // this is an token object
        Token token = null; 
        // this is an reverted list
        List reversed = new ArrayList<>();
        // get parent object
        List parent = ast;
        // get child object
        List child = null;
        // this is an pointer of last special object
        int point = -1;
        // this flag indicates if class pointer is meet
        boolean clazz = false;
        // get key of parent list
        String key = null;
        
  //      System.out.println("BEGIN" + key);
    //    System.out.println("BEGIN" + ast);
        // iterate for reversed list
        do
        {
            // check parent object
            child = (List) parent.get(2);
            // add child object
            reversed.add(child);
            // get child key
            key = ((Token)child.get(0)).value;
            // if key is not equals to "%v", then set pointer
            if (!key.equals("%v"))
            {
      //          System.out.println("WHAT HAPPENED?" + key);
                point = reversed.size();
                clazz = key.equals("%k");
                if (clazz)
                {
                    token = (Token)child.get(1);
                    clazz = token.value.equals("class");
                // get token
                }
            }
            // 
            parent = (List) parent.get(1);
            key = ((Token)parent.get(0)).value;
            // get 
        } while (key.equals("."));
        
   //     System.out.println("$KEY"+key+clazz);
        // add last element to the reversed list 
        reversed.add(parent);
        // if parent key is not '%v', then set pointer
        if (!key.equals("%v"))
            {
                point = reversed.size();
                clazz = key.equals("%k");
                if (clazz)
                {
                // get token
                    token = (Token)parent.get(1);
                    clazz = token.value.equals("class");
                }
            }
    //    System.out.println(reversed);
        // get reversed chain order
        List chain = ReversedView.of(reversed);
        //
    //    System.out.println(chain);
        // get reversed pointer
        if (point > -1) point = chain.size() - point;
        else point = chain.size() - 1;
        // iterate for chain
        // this is an chain object
        Object chain_node = null;
        // this is an getter
        Getter gt = null;
        
        int end = chain.size();
       //     System.out.println(chain);
  //     System.out.println("CLAZZ"+ clazz);
        // if class flag is true, then get class expression
        if (clazz)
        {
      //      System.out.println(token);
            // get token value
            key = token.value;
            // get class getter
            
            switch(key)
            {/*
                case "super":
                   pair = this.getter.getSuper(token);
                // set getter
                    gt = pair.a;
                    // if got null, then return
                    if (gt == null) return null;
            // get chain expression
                    chain_node = new OwnerConst(pair.b);
            // increment point
                    point ++;
                
                break;
                case "this":
                    pair = this.getter.getThis(token);
                // set getter
                    gt = pair.a;
                    // if got null, then return
                    if (gt == null) return null;
            // get chain expression
                    chain_node = new ThisConst(pair.b);
            // increment point
                    point ++;
            // if const expression expected, then add error
      /*      if (is_const)
            {
                // add error pool
                this.error_pool.add(new Flaw(
                Flaw.type.const_expr, token.source, token.line, token.position));
                // 
                is_const = false;
            }
        */
                case "class":
        Pair<Getter, ClassObject> pair = this.
              getter.getClassField(ChainView.of(chain, point), token);
                // set getter
                    gt = pair.a;
                    // if got null, then return
                    if (gt == null) return null;
            // get chain expression
            
        //    System.out.println("key");
            
      //      System.out.println("CLASSCONST OF "+ pair.b);
            
                    chain_node = new ClassConst(pair.b);
                    
    //        System.out.println("CHAIN NODE OF " + chain_node);
            // increment point
                    point ++;
                    // if const expression expected, then check if next element is there
         /*           if (is_const)
                    {
                        if (end != point)
                        {
                            // add error pool
                            this.ast.error_pool.add(new Flaw(
                            Flaw.type.const_expr, token.source, token.line, 
                            token.position));
                            // 
                            is_const = false;
                        }
                    }
                    */
                    break;
            
                default:
               //     gt = null;
               //     chain_node = new Object();
                    return null;
            }
        }
        else if (point > 0)
        {
            
            chain_node = null;
            
            List list43 = (List)chain.get(0);
            
            
            if (((Token)list43.get(0)).value.equals("%v"))
            {
                token = (Token)list43.get(1);
                chain_node  = getChainFieldB(token);
                point = 1;
                if (chain_node != null)
            {
                gt = this.getter.createGetter(getReference(chain_node));
            }
            }
            
        if (chain_node == null)
        {
            // get getter
            Pair<Pair<Getter, Object>, Integer> pair = this.getter.getGetter(
                             ChainView.of(chain, point));
            // set getter
            gt = pair.a.a;
            //
            if (gt != null)
          {
              
            // set chain_node
            chain_node = pair.a.b;
            // set point 
             point = (int) pair.b;
          }
          else
          {              
              point = 0;
          }
        }
    }
     if (chain_node == null)
     {
        if (point == 0)
        {            
            
            point ++;
            chain_node = getExpression((List)chain.get(0));
            
            if (chain_node == null) return null;
            
            gt = this.getter.createGetter(getReference(chain_node));
        }
    }
//    else 
    {
 //           point ++;
  //          gt = this.getter.createGetter(getReference(chain_node));
    }
        
        // iterate until point < end
        List node;
        
        
        
 //       System.out.println(point);
   //     System.out.println(chain);
     //   System.out.println(chain.get(1));
        
        Pair pair;
        ClassObject object;
        // iterate for point
        while (point < end)
        {
            // get chain list element
            node = (List)chain.get(point);
            // increment point
            point ++;
      //      System.out.println(node);
            // get key from list
            key = ((Token)node.get(0)).value;
            // if key equals to "$new", then get getter
            if (key.equals("$new"))
            {
                // get name
                token = (Token)((List)node.get(1)).get(0);
                //
                List expr = (List)node.get(2);
                // get class
                object = gt.getOwner().getTable().getClassA(token.value);
                // if got object
                if (ClassTable.checkObject(this.ast, object, token))
                {
                    //
                    chain_node = checkNew(gt, object, expr, token, chain_node);
                }
                else
                {
                    return null;
                }
                gt = gt.createGetter(object);
            }
            // 
            if (key.equals("$anonumous"))
            {
                // get name
                token = (Token)((List)node.get(1)).get(0);
                //
                ClassObject object2 = gt.getOwner().getTable()
                                       .getClassA(token.value);
                //
                List expr = (List)node.get(2);
                // get class
                object = gt.getAnonumous(object2, (List)node.get(3), 
                         (Token)node.get(0));
                // if got object
                if (ClassTable.checkObject(this.ast, object, token))
                {
                    //
                    chain_node = new Cast(
                     "L"+object2+";",
                     checkNew(gt, object, expr, token, chain_node));
                }
                else
                {
                    return null;
                }
                gt = gt.createGetter(object2);
            }
            else if (key.equals("$call"))
            {
                // get name
                token = (Token)node.get(1);            
                // get list
                List list = (List)node.get(2)    ;
                //
     //           System.out.println(list);
                Pair<List, List<String>> pair1 = getExpressionList(list);
                //
                if (pair1 == null) return null;
                //
                Pair<Getter, MethodLink> pair2 = gt.searchMethod(token, pair1.b);
                //
                if (pair2.b == null) return null;
                gt = pair2.a;
                chain_node = new Method(pair2.b, chain_node, pair1.a);
            }
            // if key equals to "%v", then get field
            else if (key.equals("%v"))
            {
                pair = gt.getField((Token)node.get(1));
                // if got null, then return
                if (pair.a == null) return null;
                
            //    System.out.println(pair.a);
                // get getter and set node
                gt = (Getter) pair.a;
                // get fieldlink
                FieldLink fieldlink = (FieldLink) pair.b;
                // if constant expression is expected
             /*   if (is_const)
                {
                    // then add error if met an non constant element
                    if ((fieldlink.access & (ACC_STATIC | ACC_FINAL)) > 0)
                    {
                        is_const = false;
                    }
                    // check if next element is there
                    else if (point < end)
                    {
                        is_const = false;
                    }
                    else
                    {
                        // get description
                        String desc = fieldlink.descriptor;
                        // check description
                        // if description length is equals to 1, 
                        // then set constant and return 
                        if (desc.length() == 1)
                        {
                            Object value = fieldlink.value;
                            // if value is null, then add error
                            if (value == null)
                            {
                                this.ast.error_pool.add(new Flaw(
                                Flaw.type.const_expr, 
                            }
                            switch (desc)
                            {
                                
                            }
                        }
                        else if (fieldlink.access & ACC_ENUM)
                        {
                        // pass
                        };
                    }
                }
                */
                // get chain node
        //        if (is_const) {};
                chain_node = new Field((FieldLink)pair.b, chain_node);
                //
            }
            else
            {
            }
        }
        return chain_node;
    }
    
    public interface BoolConstUnary
    {
        public boolean run(boolean a1);
    }
    
    public interface MathConstUnary
    {
        public double run(double a1);
    }
    
    public interface BitwiseUnary
    {
        public long run(long a1);
    }
    
    public static Map<String, Object> math_const_unary = new HashMap<>();
    static
    {
        math_const_unary.put("-", new MathConstUnary()
        {
            public double run(double a1)
            {
                return -a1;
            }
        });
        math_const_unary.put("+", new MathConstUnary()
        {
            public double run(double a1)
            {
                return a1;
            }
        });
        math_const_unary.put("~", new BitwiseUnary()
        {
            public long run(long a1)
            {
                return ~a1;
            }
        });
    }
    
    public interface CallOperatorRun
    {
        public void run(CodeVisitor visit, Object ... args);
        public static final CallOperatorRun empty = new CallOperatorRun()
        {
            public void run(CodeVisitor visit, Object ... args)
            {
                for (Object s1: args)
                {
                    visit.getExpression(s1);
                }
            }
        };
        public static final CallOperatorRun neg = new CallOperatorRun()
        {
            public void run(CodeVisitor visit, Object ... args)
            {
                for (Object s1: args)
                {
                    visit.getExpression(s1);
                }
                org.objectweb.asm.Label lbl1 = 
                           new org.objectweb.asm.Label();
                org.objectweb.asm.Label lbl2 = 
                           new org.objectweb.asm.Label();
                visit.visitor.visitJumpInsn(IFNE, lbl1);
                visit.visitor.visitInsn(ICONST_1);
                visit.visitor.visitJumpInsn(GOTO, lbl2);
                visit.visitor.visitLabel(lbl1);
                visit.visitor.visitInsn(ICONST_0);
                visit.visitor.visitLabel(lbl2);
            }
        };
        public static final CallOperatorRun concat = new CallOperatorRun()
        {
            public void run(CodeVisitor visit, Object ... args)
            {
                Object a1 = args[0];
                Object a2 = args[1];
                visit.getExpression(a1);
                visit.getExpression(a2);
                visit.wrap(a2);
                visit.visitor.visitMethodInsn(
                       INVOKEVIRTUAL,
                       "java/lang/Object",
                       "toString",
"()Ljava/lang/String;",
                       false);
                visit.visitor.visitMethodInsn(
                       INVOKEVIRTUAL,
                       "java/lang/String",
                       "concat",
"(Ljava/lang/String;)Ljava/lang/String;",
                       false);
                          
            }
        };
        public static final CallOperatorRun repeat = new CallOperatorRun()
        {
            public void run(CodeVisitor visit, Object ... args)
            {
                
                Object a1 = args[0];
                Object a2 = args[1];
                visit.getExpression(a1);
                visit.getExpression(a2);
                visit.peel(a2);
                visit.visitor.visitMethodInsn(
                       INVOKEVIRTUAL,
                       "java/lang/String",
                       "repeat",
"(I)Ljava/lang/String;",
                       false);
                          
            }
        };
        public static final CallOperatorRun Ixor = new CallOperatorRun()
        {
            public void run(CodeVisitor visit, Object ... args)
            {
                
            String ref;
            for (Object a1: args)
            {
                visit.getExpression(a1);
                ref = visit.peel(a1);
                visit.castPrimitive(ref, "I");
            }
                visit.visitor.visitInsn(ICONST_M1);
                visit.visitor.visitInsn(IXOR);
            }
        };
        public static final CallOperatorRun Lxor = new CallOperatorRun()
        {
            public void run(CodeVisitor visit, Object ... args)
            {
                String ref;
                for (Object s1: args)
                {
                    visit.getExpression(s1);
                    ref = visit.peel(s1);
                    visit.castPrimitive(ref, "J");
                }
                long i = -1;
                visit.visitor.visitLdcInsn((Long)i);
                visit.visitor.visitInsn(LXOR);
            }
        };
    }
    
    public static class CallOperatorRun2 implements CallOperatorRun
    {
      //  public String a;
        public int op;
        public CallOperatorRun2(int opcode)
        {
     //       this.a = a;
            this.op = opcode;
        }
        public void run(CodeVisitor visit, Object ... args)
        {
            String ref;
            for (Object a1: args)
            {
                visit.getExpression(a1);
                ref = visit.peel(a1);
                visit.castPrimitive(ref, "I");
            }
            org.objectweb.asm.Label lbl1 = new org.objectweb.asm.Label();
            org.objectweb.asm.Label lbl2 = new org.objectweb.asm.Label();
            visit.visitor.visitJumpInsn(op, lbl1);
            visit.visitor.visitInsn(ICONST_0);
            visit.visitor.visitJumpInsn(GOTO, lbl2);
            visit.visitor.visitLabel(lbl1);
            visit.visitor.visitInsn(ICONST_1);
            visit.visitor.visitLabel(lbl2);
        }
        
    }
    
    public static class CallOperatorRun3 implements CallOperatorRun
    {
        public String a;
        public int op, cop;
        public CallOperatorRun3(String a, int opcode, int cop)
        {
            this.a = a;
            this.cop = cop;
            this.op = opcode;
        }
        public void run(CodeVisitor visit, Object ... args)
        {
            String ref;
            for (Object a1: args)
            {
                visit.getExpression(a1);
                ref = visit.peel(a1);
                visit.castPrimitive(ref, a);
            }
            org.objectweb.asm.Label lbl1 = new org.objectweb.asm.Label();
            org.objectweb.asm.Label lbl2 = new org.objectweb.asm.Label();
            visit.visitor.visitInsn(cop);
            visit.visitor.visitJumpInsn(op, lbl1);
            visit.visitor.visitInsn(ICONST_0);
            visit.visitor.visitJumpInsn(GOTO, lbl2);
            visit.visitor.visitLabel(lbl1);
            visit.visitor.visitInsn(ICONST_1);
            visit.visitor.visitLabel(lbl2);
        }
        
    }
    
    public static class CallOperatorRun1 implements CallOperatorRun
    {
        public String a;
        public int op;
        public CallOperatorRun1(String a, int opcode)
        {
            this.a = a;
            this.op = opcode;
        }
        public void run(CodeVisitor visit, Object ... args)
        {
            String ref;
            for (Object a1: args)
            {
                visit.getExpression(a1);
                ref = visit.peel(a1);
                visit.castPrimitive(ref, a);
            }
            visit.visitor.visitInsn(op);
        }
        
    }
    
    public static class CallOperatorTool
    {
        public ClassLink link; 
        public HashMap<MethodLink, CallOperatorRun> call_map = new HashMap<>(); 
        public CallOperatorTool(ClassLink link)
        {
            this.link = link;
        }
        public void addMethodLink(String a1, String a2, String a3,int opcode)
        {
            call_map.put(link.addMethodLink(a1, a2, 0), 
                         new CallOperatorRun1(a3, opcode));
        }
        
        public void addMethodLink(String a1, String a2)
        {
            call_map.put(link.addMethodLink(a1, a2, 0),
                        CallOperatorRun.empty);
        }
        
        public void addMethodLinkIxor(String a1, String a2)
        {
            call_map.put(link.addMethodLink(a1, a2, 0),
                        CallOperatorRun.Ixor);
        }
        
        public void addMethodLinkLxor(String a1, String a2)
        {
            call_map.put(link.addMethodLink(a1, a2, 0),
                        CallOperatorRun.Lxor);
        }
        
        public void addMethodLinkBol(String a1, String a2, String a3, int opcode)
        {
            MethodLink l = link.addMethodLink(a1, a2, 0);
            switch(a3)
            {
                case "I":
                call_map.put(l, new CallOperatorRun2(opcode));
                break;
                case "F":
                call_map.put(l, new CallOperatorRun3("F", opcode, FCMPL));
                break;
                case "D":
                call_map.put(l, new CallOperatorRun3("D", opcode, DCMPL));
                break;
                case "L":
                call_map.put(l, new CallOperatorRun3("L", opcode, LCMP));
                break;
                
            }
        }
        
        public void addMethodLinkConcat(String a1, String a2)
        {
            call_map.put(link.addMethodLink(a1, a2, 0),
                        CallOperatorRun.concat);
        }
        
        public void addMethodLinkRepeat(String a1, String a2)
        {
            call_map.put(link.addMethodLink(a1, a2, 0),
                        CallOperatorRun.repeat);
        }
        
        public void addMethodLinkNeg(String a1, String a2)
        {
            call_map.put(link.addMethodLink(a1, a2, 0), 
                        CallOperatorRun.neg);
        
        }
    }
    
    public static TreeMap<String, MethodMap<List<String>,MethodLink>> operator_map;
    public static HashMap<MethodLink, CallOperatorRun> call_map;
    static
    {
        ClassLink link2 = new ClassLink();
        CallOperatorTool link = new CallOperatorTool(link2);
        link.addMethodLink("+", "(DD)D", "D", DADD);
        link.addMethodLink("+", "(FF)F", "F", FADD);
        link.addMethodLink("+", "(JJ)J", "J", LADD);
        link.addMethodLink("+", "(II)I", "I", IADD);
        link.addMethodLink("+", "(SS)S", "I", IADD);
        link.addMethodLink("+", "(BB)B", "I", IADD);
        
        link.addMethodLinkConcat("+", "("+ClassObject.str_name_desc+
                                    ClassObject.obj_name_desc+
                                    ")"+ClassObject.str_name_desc);
        
        link.addMethodLinkRepeat("*", "("+ClassObject.str_name_desc+
                            "I)"+ClassObject.str_name_desc);
        
        
        link.addMethodLink("-", "(DD)D", "D", DSUB);
        link.addMethodLink("-", "(FF)F", "F", FSUB);
        link.addMethodLink("-", "(JJ)J", "J", LSUB);
        link.addMethodLink("-", "(II)I", "I", ISUB);
        link.addMethodLink("-", "(SS)I", "I", ISUB);
        link.addMethodLink("-", "(BB)I", "I", ISUB);
        
        link.addMethodLink("*", "(DD)D", "D", DMUL);
        link.addMethodLink("*", "(FF)F", "F", FMUL);
        link.addMethodLink("*", "(JJ)J", "J", LMUL);
        link.addMethodLink("*", "(II)I", "I", IMUL);
        link.addMethodLink("*", "(SS)I", "I", IMUL);
        link.addMethodLink("*", "(BB)I", "I", IMUL);
        
        link.addMethodLink("%", "(DD)D","D", DREM);
        link.addMethodLink("%", "(FF)F","F", FREM);
        link.addMethodLink("%", "(JJ)J","J", LREM);
        link.addMethodLink("%", "(II)I","I", IREM);
        link.addMethodLink("%", "(SS)I","I", IREM);
        link.addMethodLink("%", "(BB)I","I", IREM);
        
        link.addMethodLink("/", "(DD)D", "D", DDIV);
        link.addMethodLink("/", "(FF)F", "F", FDIV);
        link.addMethodLink("/", "(JJ)J", "J", LDIV);
        link.addMethodLink("/", "(II)I", "I", IDIV);
        link.addMethodLink("/", "(SS)I", "I", IDIV);
        link.addMethodLink("/", "(BB)I", "I", IDIV);
        
        link.addMethodLink("+", "(D)D");
        link.addMethodLink("+", "(I)I");
        link.addMethodLink("+", "(F)F");
        link.addMethodLink("+", "(J)J");
        link.addMethodLink("+", "(S)S");
        link.addMethodLink("+", "(B)B");
        
        link.addMethodLink("-", "(D)D", "D", DNEG);
        link.addMethodLink("-", "(I)I", "F", FNEG);
        link.addMethodLink("-", "(F)F", "J", LNEG);
        link.addMethodLink("-", "(J)J", "I", INEG);
        link.addMethodLink("-", "(S)S", "I", INEG);
        link.addMethodLink("-", "(B)B", "I", INEG);
        
        link.addMethodLinkNeg("!", "(Z)Z");
        
        link.addMethodLinkBol("==", "(DD)Z", "D", IFEQ);
        link.addMethodLinkBol("==", "(FF)Z", "F", IFEQ);
        link.addMethodLinkBol("==", "(JJ)Z", "J", IFEQ);
        link.addMethodLinkBol("==", "(II)Z", "I", IF_ICMPEQ);
        link.addMethodLinkBol("==", "(SS)Z", "I", IF_ICMPEQ);
        link.addMethodLinkBol("==", "(BB)Z", "I", IF_ICMPEQ);
        
        link.addMethodLinkBol("==", "(ZZ)Z", "I", IF_ICMPEQ);
        
        link.addMethodLinkBol("!=", "(DD)Z", "D", IFNE);
        link.addMethodLinkBol("!=", "(FF)Z", "F", IFNE);
        link.addMethodLinkBol("!=", "(JJ)Z", "J", IFNE);
        link.addMethodLinkBol("!=", "(II)Z", "I", IF_ICMPNE);
        link.addMethodLinkBol("!=", "(SS)Z", "I", IF_ICMPNE);
        link.addMethodLinkBol("!=", "(BB)Z", "I", IF_ICMPNE);
                                           
        link.addMethodLinkBol("!=", "(ZZ)Z", "I", IF_ICMPNE);
       
        link.addMethodLinkBol(">", "(DD)Z", "D", IFGT);
        link.addMethodLinkBol(">", "(FF)Z", "F", IFGT);
        link.addMethodLinkBol(">", "(JJ)Z", "J", IFGT);
        link.addMethodLinkBol(">", "(II)Z", "I", IF_ICMPGT);
        link.addMethodLinkBol(">", "(SS)Z", "I", IF_ICMPGT);
        link.addMethodLinkBol(">", "(BB)Z", "I", IF_ICMPGT);
                                          
        link.addMethodLinkBol(">=", "(DD)Z", "D", IFGE);
        link.addMethodLinkBol(">=", "(FF)Z", "F", IFGE);
        link.addMethodLinkBol(">=", "(JJ)Z", "J", IFGE);
        link.addMethodLinkBol(">=", "(II)Z", "I", IF_ICMPGE);
        link.addMethodLinkBol(">=", "(SS)Z", "I", IF_ICMPGE);
        link.addMethodLinkBol(">=", "(BB)Z", "I", IF_ICMPGE);
        
        
        link.addMethodLinkBol("<", "(DD)Z", "D", IFLT);
        link.addMethodLinkBol("<", "(FF)Z", "F", IFLT);
        link.addMethodLinkBol("<", "(JJ)Z", "J", IFLT);
        link.addMethodLinkBol("<", "(II)Z", "I", IF_ICMPLT);
        link.addMethodLinkBol("<", "(SS)Z", "I", IF_ICMPLT);
        link.addMethodLinkBol("<", "(BB)Z", "I", IF_ICMPLT);
        
        link.addMethodLinkBol("<=", "(DD)Z",  "D", IFLE);
        link.addMethodLinkBol("<=", "(FF)Z",  "F", IFLE);
        link.addMethodLinkBol("<=", "(JJ)Z",  "J", IFLE);
        link.addMethodLinkBol("<=", "(II)Z",  "I", IF_ICMPLE);
        link.addMethodLinkBol("<=", "(SS)Z",  "I", IF_ICMPLE);
        link.addMethodLinkBol("<=", "(BB)Z",  "I", IF_ICMPLE);
        
        link.addMethodLink("&&", "(ZZ)Z", "I", IAND);
        link.addMethodLink("||", "(ZZ)Z", "I", IOR);
        link.addMethodLink("|", "(ZZ)Z", "I", IOR);
        link.addMethodLink("&", "(ZZ)Z", "I", IAND);
        link.addMethodLink("^", "(ZZ)Z", "I", IXOR);
        link.addMethodLink("^^", "(ZZ)Z", "I", IXOR);
        
        link.addMethodLink("&", "(JJ)J", "J", LAND);
        link.addMethodLink("&", "(II)I", "I", IAND);
        link.addMethodLink("&", "(SS)I", "I", IAND);
        link.addMethodLink("&", "(BB)I", "I", IAND);
        
        link.addMethodLink("|", "(JJ)J","J", LOR);
        link.addMethodLink("|", "(II)I","I", IOR);
        link.addMethodLink("|", "(SS)I","I", IOR);
        link.addMethodLink("|", "(BB)I","I", IOR);
        
        link.addMethodLink("^", "(JJ)J","J", LXOR);
        link.addMethodLink("^", "(II)I","I", IXOR);
        link.addMethodLink("^", "(SS)I","I", IXOR);
        link.addMethodLink("^", "(BB)I","I", IXOR);
        
        link.addMethodLinkLxor("~", "(J)J");
        link.addMethodLinkIxor("~", "(I)I");
        link.addMethodLinkIxor("~", "(S)S");
        link.addMethodLinkIxor("~", "(B)B");
        
        link.addMethodLink("<<", "(JJ)J","J", LSHL);
        link.addMethodLink("<<", "(II)I","I", ISHL);
        link.addMethodLink("<<", "(SS)I","I", ISHL);
        link.addMethodLink("<<", "(BB)I","I", ISHL);
        
        link.addMethodLink(">>", "(JJ)J","J", LSHR);
        link.addMethodLink(">>", "(II)I","I", ISHR);
        link.addMethodLink(">>", "(SS)I","I", ISHR);
        link.addMethodLink(">>", "(BB)I","I", ISHR);
        
        link.addMethodLink(">>>", "(JJ)J","J", LUSHR);
        link.addMethodLink(">>>", "(II)I","I", IUSHR);
        link.addMethodLink(">>>", "(SS)I","I", IUSHR);
        link.addMethodLink(">>>", "(BB)I","I", IUSHR);
        operator_map = link2.methods;
        //
        call_map = link.call_map;
        //???????????????????????????????????????
        
    }
    
    public static Map<String, BoolConstUnary> bool_const_unary = new HashMap<>();
    static 
    {
        bool_const_unary.put("!", new BoolConstUnary()
        {
            public boolean run(boolean a1)
            {
                return !a1;
            }
        });
    }
    //
    
    // this function will iterate with forward operators
    public Object getForward(Token token, Object a1, boolean allow_dynamic)
    {
        if (allow_dynamic)
        {
            // check if key is an back operator
            MethodMap map = BinAssign.ops.get(token.value);
            // if map is not null
            if (map != null)
            {
                return checkBinAssign(token, map, a1, true);
            }
            // else, return call operator
            return checkCallOperator(token, a1);
        }
        return null;
    }
    
    // this function will iterate with back operators
    public Object getBack(Token token, Object a1, boolean allow_dynamic)
    {   
        
        String key = token.value;
        if (a1 instanceof Character)
        {
            a1 = (Integer) a1;
        }
        // check if a1 is constant
        if (a1 instanceof Boolean)
        {
            BoolConstUnary j = bool_const_unary.get(key);
            // return 
            return j.run((boolean)(Boolean)a1);
        }
        else if (a1 instanceof Number)
        {
            //
            int lvl = num_level.getOrDefault(a1.getClass(), 0);
            //
            Object j = math_const_unary.get(key);
            // if got bitwise, then check level
            if (j instanceof BitwiseUnary)
            {
                if (lvl < 2)
                {
                    long jk = ((BitwiseUnary)j).run((long)(Long)a1);
                    
                    a1 = (Long) jk;
                    if (a1 instanceof Short)
                    {
                        return (Short) a1;
                    }
                    if (a1 instanceof Byte)
                    {
                        return (Byte) a1;
                    }
                    if (a1 instanceof Integer)
                    {
                        return (Integer) a1;
                    }
                    return a1;
                }
            }
            else if (j instanceof MathConstUnary)
            {
                    double jk = ((MathConstUnary)j).run((double)(Double)a1);
                    a1 = (Double) jk;
                    if (a1 instanceof Short)
                    {
                        return (Short) a1;
                    }
                    if (a1 instanceof Byte)
                    {
                        return (Byte) a1;
                    }
                    if (a1 instanceof Integer)
                    {
                        return (Integer) a1;
                    }
                    if (a1 instanceof Long)
                    {
                        return (Long) a1;
                    }
                    if (a1 instanceof Float)
                    {
                        return (Float) a1;
                    }
                    return a1;
            }
        }
        
        if (a1 == null) return null;
        if (allow_dynamic)
        {
            // check if key is an back operator
            MethodMap map = BinAssign.ops.get(key);
            // if map is not null
            if (map != null)
            {
                return checkBinAssign(token, map, a1, false);
            }
            // else, return call operator
            return checkCallOperator(token, a1);
        }
        return null;
    }
    
    // 
    static public boolean isVar(Object a1)
    {
        if (a1 instanceof Field) 
        {
            
 //           System.out.println(a1);
            boolean a = (((Field)a1).link.access&ACC_FINAL)==0;
            
            return true;
        }
        if (a1 instanceof Item) return true;
        if (a1 instanceof Variable) return true;
        return false;
    }
    
    // this function will check for BinAssign operator
    public Object checkBinAssign(Token key, MethodMap map, Object a1, boolean c)
    {
        boolean is_var = false;
        String desc1;
        is_var = isVar(a1);
        if (is_var)
        {
            // get assign type
            desc1 = getReference(a1);
        }
        else
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.variable_expected, 
                    key.source, key.line, key.position));
            return null;
        }
        ArrayList<String> list = new ArrayList<>(1);
        list.add(desc1);
        MethodLink link = this.ast.mhandler.searchMethod(map, list);
        if (link == ClassTable.non_exists_method)
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.no_suitable_operator, 
                      key.source, key.line, key.position, key.value+list));
            
            return null;
        }
        
        return new BinAssign(a1, c, link);
    }
    
    // this function will return math operation expression
    public Object getMath(Token token, Object a1, Object a2, boolean allow_dynamic)
    {
        String key = token.value;
        // check if first object are string
        // if yes, then check key
        if (a1 instanceof String)
        {
            // check a2 object
            if (
                (a2 instanceof Character)||
                (a2 instanceof Number)||
                (a2 instanceof Boolean)||
                (a2 instanceof String)
                )
                {
                    // check if + object there
                    if (key.equals("+"))
                    {
                        return a1.toString() + a2.toString();
                    }
                }
        }
        // check if objects are numbers
        if (((a1 instanceof Character) || (a1 instanceof Number)) &&
            ((a2 instanceof Character) || (a2 instanceof Number)) )
            {
                // convert character
                if (a1 instanceof Character)
                {
                    a1 = (Integer)(int)(char)a1;
                }
                if (a2 instanceof Character)
                {
                    a2 = (Integer)(int)(char)a2;
                }
                // get math 
                Object math = math_const_map.get(key);
                // 
                if (math == null) return null;
                //
            if (math instanceof MathConstDouble)
        {
            MathConstDouble math_const = (MathConstDouble) math;
            // get max level
            int lvl = num_level.getOrDefault(a1.getClass(), 0);
            lvl = Math.max(lvl, num_level.getOrDefault(a2.getClass(), 0));
            // return math 
            return getNumber(math_const.run(((Number)a1).doubleValue(), ((Number)a2).doubleValue()), lvl);
        }
            
            if (math instanceof MathConstBitwise)
        {
            MathConstBitwise math_const = (MathConstBitwise) math;
            // get max level
            int lvl = num_level.getOrDefault(a1.getClass(), 0);
            lvl = Math.max(lvl, num_level.getOrDefault(a2.getClass(), 0));
            // return math 
            return getNumber(math_const.run(((Number)a1).longValue(), ((Number)a2).longValue()), (lvl > 0) ? 1 : 0);
        }
            
            if (math instanceof MathConstCompare)
        {
            MathConstCompare math_const = (MathConstCompare) math;
            // return math 
            return math_const.run(((Number)a1).doubleValue(), ((Number)a1).doubleValue());
        }
            
            }
            
        // check if objects are booleans
        if ((a1 instanceof Boolean) && (a2 instanceof Boolean))
        {
            // then get boolean operator
            BoolConst math = bool_const_map.get(key);
            // 
            if (math == null) return null;
            // 
            return math.run((boolean)(Boolean) a1, (boolean)(Boolean) a2);
        }
        
        if (a1 == null || a2 == null) return null;
        
//        System.out.println("MATH::");
        
        if (allow_dynamic) return checkCallOperator(token, a1, a2);
        return null;
    }
    
    
    // this is empty expression 
    public Expression empty = new Expression()
    {
        public Object run(List ast)
        {
            return null;
        }
    };
    
    // this is an temporaly table
    public Getter getter;
    // this is an flag of usage expression
    public boolean isConst = false;
    
    
    public Object checkAssign(Token key, Object b1, Object b2)
    {
        
        boolean is_var = false;
        // get name
        String name = key.value;
        // get variable
   //     Object b1 = getExpression(a1);
   //     Object b2 = getExpression(a2);
        
        String desc1 = null;
        String desc2 = null;
        List list = null;
        MethodLink link = null;
        // 
        // if not b1 is variable, then raise error 'variable is expected'
        is_var = isVar(b1);
        if (is_var)
        {
            // get assign type
            desc1 = getReference(b1);
            desc2 = getReference(b2);
            
//            System.out.println(desc1);
  //          System.out.println(desc2);
    //        System.out.println(b1);
      //      System.out.println(b2);
            
        }
        else
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.variable_expected, 
                    key.source, key.line, key.position));
            return null;
        }
        int t = name.length();
        if (t == 1)
        {
            is_var = this.ast.mhandler.checkDesc(desc1, desc2) > -1;
        }
        else
        {
            name = name.substring(0, t-1);
            // get methodlink
            MethodMap map = ASTExpr.operator_map.get(name);
            link = this.ast.mhandler.searchMethod(map, list = Arrays.asList(desc1, desc2));
            // if got non_exists link, then raise error and return
            if (link == ClassTable.non_exists_method)
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.no_suitable_operator, 
                      key.source, key.line, key.position, name+list));
                return null;
            }
            // check methodlink return type and this return type
            is_var = this.ast.mhandler.checkDesc(desc1, link.getType()) > -1;
        }
        if (is_var)
        {
            if (link != null)
            {
                b1 = new CallOperator(link, b1, b2);
            }
            
            desc2 = getReference(b1);
     //       System.out.println(desc2);
            
            if (!getReference(b2).equals(desc2))
            {
                b2 = new Cast(desc2, b2);
            }
            return new Assign(b1, b2);
        }
        else
        {
            this.ast.error_pool.add(new Flaw(Flaw.type.incompatible_types, 
                      key.source, key.line, key.position, desc2, desc1));
        }
        
        return null;
    }
    
    
    // this method will return an annotation
    public AnnotationLink getAnnotation(Getter getter, List list)
    {
    //    this.ast.error_pool.add(new Flaw(Flaw.type.already_def, "d", 2, 2, "d", "d", "234"));
        // store flag
      boolean flag = this.isConst;
        // store table
      Getter temp_table = this.getter; 
        // set temp table
      this.getter = getter;
        // set temp flag
      this.isConst = true; 
        
        Token token;
        // the getter will get an class and static fields
        // try to get class
        Token note_init = (Token)list.get(0);
        
        ClassObject object = getter.getClassField((List)list.get(1), note_init).b;
        //
        object.link1();
        // create ast hashmap
        HashMap<Token, List> map = new HashMap<>();
        // this is an name
        String name;
        // add list to the map
        
        Object list3 = (List)list.get(2);
        if (((List)list3).get(0) instanceof Token)
        {
   //         System.out.println("TOKNE");
            ArrayList list4 = new ArrayList(1);
            list4.add(list3);
            list3 = list4;
        }
        
    //    System.out.println(list3);
        
        for (List l: (List<List>)(list3))
        {
            token = (Token) l.get(0);
            // get name
            name = token.value;
            
      //      System.out.println("NAME: " + name);
            // if map contains key, then raise repeated error
            if (map.containsKey(name))
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.duplicate_element,
                token.source, token.line, token.position, name));
            }
            // else put name and list
            map.put(token, (List)l.get(1));
        }
        //
        MethodLink link;
        list = Compiler.Cache.empty_list;
        AnnotationLink notelink = new AnnotationLink();
        // this is an value map
        TreeMap<String, Object> values = notelink.values;
        // iterate for methods from hashmap
        
   //     System.out.println("OBJECT::"+object);
    //    System.out.println("METHODS::"+object.methods);
        //
        for (MethodMap i: object.methods.values())
        {
            // get methodlink from methods 
            link = (MethodLink)i.get(list);
            
       //     System.out.println("####LINK####"+ link);
            // get name
            name = link.name;
            // get object
            Object o = this.addNoteVar(link, map, note_init, object.name);
            // if got null, then add error 
            map.remove(name);
       //     System.out.println("####OBJECT@@@@"+o);
            // add note variable if not exists
            values.put(name, o);
        }
        // add note description
        notelink.descriptor = "L" + object.name + ";";
        // iterate for left values
        for (Token str: map.keySet())
        {
            if (!values.containsKey(str.value))
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.symbol_not_found, 
                str.source, str.line, str.position, str.value));
            } 
        }
        // reset temp table
      this.getter = temp_table;
        // reset temp flag
      this.isConst = flag; 
      
        return notelink;
    }
    
    // this method will add note variable to the annotation
    public Object addNoteVar(MethodLink link, Map map, Token token, String object_name)
    {
        // get link name
        String name = link.name;
        // check if variable with same name exists in map
        // else, check for default variable
        Object object = (map.get(new Token(name)));
     //   System.out.println("____"+name);
        // if object is not null
        if (object != null)
        {
            
            //
            //
            return this.checkNoteVar(link.descriptor.substring(2), (List)object, name);
            // 
        }
        // else, get default object
        else
        {
            // 
            Object o = getAnnotationDefault(link);
            // 
            if (o == null) 
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.missing_value, 
                token.source, token.line, token.position, object_name, link.name));
                return null;
            }
            else
            {
                return o;
            }
        }
    }
    
    // this method will process with annotation variable
    public Object checkNoteVar(String desc, List list, String name)
    {
        // 
        List<List> temp;
        // 
        Object object;
        // try to get const value from init
        if (desc.charAt(0) == '[')
        {
            // create new arraylist
            List array = new ArrayList<>();
            // 
            desc = desc.substring(1);
            // if first token is array
            if (((Token)list.get(0)).value.equals("$array"))
            {
                temp = (List<List>) list.get(1);
            }
            else
            {
                temp = new ArrayList<>();
                temp.add(list);
            }
            // iterate for temp
            for (List cv: temp)
            {
                // get object
                object = this.getExpression(cv);
                
                // validate object
                if (validateConstant(desc, object))
                {
                    array.add(object);
                }
                else
                {
                    Token token = (Token)cv.get(0);
                    this.ast.error_pool.add(new Flaw(Flaw.type.not_allowable_type,
                    token.source, token.line, token.position, name)); 
                }
            }
            return array;
        }
        else
        {
            // get object
            // 
 //              System.out.println("??");
            
   //             System.out.println(list);
            
                object = this.getExpression(list);
                
     //           System.out.println(object);
       //         System.out.println(object.getClass());
                
          ///      if (object != null) System.out.println(object.getClass());
                // validate object
                if (validateConstant(desc, object))
                {
                    return object;
                }
                else
                {
                    
         //           System.out.println("YOU HERE?");
                    
                    Token token = (Token)list.get(0);
                    this.ast.error_pool.add(new Flaw(Flaw.type.not_allowable_type,
                    token.source, token.line, token.position, name));
                    return null; 
                }
        }
    }
    
    // this method will get an annotation parameter default value
    public Object getAnnotationDefault(MethodLink parameter)
    {
        // get parameter default annotation
        Object object = parameter.annotationDefault;
        // if object is not null, then return 
        if (object != null) return object;
        // get parameter init
        MethodLink.MethodInit init = parameter.init;
        // if got null, then return default
        if (init == null)
        {
            return null;
        }
        else
        {
            // method init must be parameter
            if (init.is_method)
            {
      //          System.out.println("Critical compiler error");
                throw new Error();
            }
            if ((init.usage & 1) > 0)
            {
                return null;
            }
            else
            {
                // set init visited
                init.usage |= 1;
                // try to get const value from init
                List list = (List)init.init.get(3);
                
   //             System.out.println(list);
                //
                if (list != null)
                {
                // return object
                    return checkNoteVar(parameter.descriptor.substring(2), list, parameter.name);
                }
                else
                {
                    return null;
                }
            }
        }
    }
    
    public Map<String, Expression> cur_map = expr_map;
    
    // this method will return an map

    
    // this method will return an expression
    public Object getExpression(List ast)
    {
        if (ast == null ) return null;
        // check key
        String key = ((Token) ast.get(0)).value;
        // get runnable 
        return this.cur_map.getOrDefault(key, empty).run(ast);
    }
    
    //
    public Object getExpression(Getter table, List ast)
    {
        return getExpression(table, ast, false);
    }
    //
    public Object getExpression(Getter table, List ast, boolean isConst)
    {
        // store flag
        Map<String, Expression> map = this.cur_map;
        // store table
        Getter temp_table = this.getter; 
        // set temp table
        this.getter = table;
        // set temp flag
        if (isConst)
        {
            this.cur_map = constexpr_map;
        }
        else
        {
            this.cur_map = expr_map;
        } 
        // get object
        Object object = getExpression(ast);
        // reset table
        this.getter = temp_table;
        // reset flag
        this.cur_map = map;
        // return object
        return object;
    }
    
    //
 //   public Map<String, Expression> localexpr_map;
     //   localexpr_map = new HashMap<>();
     //   localexpr_map.putAll(exp
                //
            public Object getChainFieldA(Token token)
            {
                //
                String str = token.value;
                // check if str in locals 
                Object a1 = null;
                //
                Map<String, Object> map;
                //
                int len = local_map.size();
                
            //    System.out.println("II"+len);
                for (int i = len-1; i >= 0; i --)
                {
                    //
                    map = local_map.get(i).variables;
           //         System.out.println(map);
                    //
                    a1 = map.get(str);
                    // if got object, then return
                    if (a1 != null) break;
                }
                //
                if (a1 == null)
                {
                    return getChainField(token);
                    // if got null, then return null
                }
                // 
                if (len > 0)
                {
                    //
                    len --;
                    //
                    map = local_map.get(len).variables;
                    //
                    map.put(str, a1);
                }
                return a1;
            }
            
             public Object getChainFieldB(Token token)
            {
                //
                String str = token.value;
                // check if str in locals 
                Object a1 = null;
                //
                Map<String, Object> map;
                //
                int len = local_map.size();
                
                for (int i = len-1; i >= 0; i --)
                {
                    //
                    map = local_map.get(i).variables;
                    //
                    a1 = map.get(str);
                    // if got object, then return
                    if (a1 != null) break;
                }
                //
                if (a1 == null)
                {
                    return null;
                    // if got null, then return null
                }
                // 
                if (len > 0)
                {
                    //
                    len --;
                    //
                    map = local_map.get(len).variables;
                    //
                    map.put(str, a1);
                }
                return a1;
            }
    
    public static  class IfGoto
    {
        public Object value;
        public String name;
        public IfGoto(Object value, String name)
        {
            this.value = value;
            this.name = name;
        }
        public String toString()
        {
            return "IfGoto: {value: "+classToString(value)+"; name: "+name+"}";
        }
    }
    
    public Object createIfGoto(Object value, String name, Token token)
    {
        if (value == null) return null;
        String kj;
        if (this.ast.mhandler.checkDesc("Z", kj = getReference(value)) > -1)
        {
            return new IfGoto(value, name);
        }
        else
        {
            Flaw.error(this.ast, Flaw.type.incompatible_types, 
                   token, kj, "boolean");
            return null;
        }
    }
    
    public static class RemoveVars
    {
        
    }
    
    public static class This
    {
        public Method init;
        public This(Method init)
        {
            this.init = init;
        }
        public String toString()
        {
            return "tcall: "+init;
        }
    }
    
    public static class Super
    {
        public Method init;
        public Super(Method init)
        {
            this.init = init;
        }
        public String toString()
        {
            return "scall: "+init;
        }
    }
    
    public static  class Throw
    {
        public Object value;
        public Throw(Object value)
        {
            this.value = value;
        }
        public String toString()
        {
            return "Throw: "+value;
        }
    }
    
    public static class Return
    {
        public Object value;
        public Return(Object value)
        {
            this.value = value;
        }
        public String toString()
        {
            return "Return: "+value;
        }
    }
    
    public  static class Block
    {
        public List<Object> values;
        
        public Block(List<Object> value)
        {
            this.values = value;
        }
    }
    
    public static  class While
    {
        public boolean do_while = false;
        public Object value;
        public Block block;
        public While while_do(Object value, Block block)
        {
            While this1 = new While();
            this1.value = value;
            this1.block = block;
            return this1;
        }
        
        public While do_while(Object value, Block block)
        {
            While this1 = new While();
            this1.do_while = true;
            this1.value = value;
            this1.block = block;
            return this1;
        }
        
        private While()
        {
        }
    }
    
    public  static class Label
    {
        public String name;
        public Label(String name)
        {
            this.name = name;
        }
        public String toString()
        {
            return "Label: "+name;
        }
    }
    
    public  static class Switch
    {
        public Block block;
        public List<Pair<Object, String>> labels;
        public String default_label;
    }
    
    public  static class Goto
    {
        public String name;
        public Goto(String name)
        {
            this.name = name;
        }
        public String toString()
        {
            return "Goto: "+name;
        }
    }
    
    public static  class If
    {
        public Block main;
        public Block second;
        public Object value;
    }
    
    public static  class Try
    {
        public Block main;
        public Block finish; 
        public List< Pair< ClassObject, Block > > handlers;
    }
    
    public static  class Catch
    {
        public Variable var;
        public Catch(Variable a)
        {
            this.var = a;
        }
        public String toString()
        {
            return "Catch: "+var;
        }
    }
    
    // check if map is statement
    public boolean isStatement(Object a1)
    {
        if (a1 instanceof Method) return true;
        if (a1 instanceof CallOperator) return true;
        if (a1 instanceof Assign) return true;
        if (a1 instanceof BinAssign) return true;
        if (a1 instanceof New) return true;
        return false;
    }
    
    // get object name
    public String getObjectName(List list, Token token)
    {
        List a2 = (List)list.get(1);
        int dim = (int)(double)(Double)list.get(4);
        String j = null;
        if (a2.size() == 1)
        {
            j = ClassObject.primitive.get(((Token)a2.get(0)).value);
        }
        if (j != null)
        {
            return "[".repeat(dim) + j;
        }
        else
        {
            
            // 
            ClassObject object = this.getter.getClassObject(a2, token).b;
            // if got object 
            if (object == null)
            {
                return null;
            }
            else
            {
                return "[".repeat(dim) +  object.getReference();
            }
        }
    }
    
    public boolean allocateVariable(String str, Token token)
    {
        
        
     //   String str = getObjectName(desc, token);
        
        if (str == null) return false;
        
        Variable var = new Variable(str, token.value);
        
        local_map.get(
    local_map.size()-1).variables.put(token.value, var);
    
        return true;
    }
    
    public boolean getCatch(List list, List list1, Token token, int dd, String desc)
    {
        //
        String key;
        
        String dk;
        //
        Expression expr;
        // create locals
        Local local = new Local();
        // add locals
        local_map.add(local);
        
        // check catch descriptor
        if (this.ast.mhandler.checkDesc(
        dk = ClassObject.thw_name_desc,
        desc
        ) == -1)
        {
            Flaw.error(this.ast, Flaw.type.incompatible_types, 
            token, desc, dk);
            return false;
        }
        
        Variable v =  new Variable(String.valueOf(dd), desc);
        local.variables.put(token.value,v);
        
        list.add(new Catch(v));
        
        // iterate for all elements
        for (List i : (List<List>)list1)
        {
            // get expression
            boolean ir = localexpr(list, i);
            //
            if (ir == false) return false;
        }
        // remove last element
        local_map.remove(local_map.size()-1);
        return true;
    }
    
    public boolean getFinally(List list, List list1, int dd)
    {
        String desc = ClassObject.thw_name_desc;
        //
        String key;
        //
        Expression expr;
        // create locals
        Local local = new Local();
        // add locals
        local_map.add(local);
        
        Variable v =  new Variable(String.valueOf(dd), desc);
        
        list.add(new Catch(v));
        // iterate for all elements
        for (List i : (List<List>)list1)
        {
            // get expression
            boolean ir = localexpr(list, i);
            //
            if (ir == false) return false;
        }
        // remove last element
        local_map.remove(local_map.size()-1);
        
        list.add(new Throw(v));
        
        return true;
    }
    
    public boolean setVariable(String desc, List list1, List list)
    {
        
        // get expression
                    Object a1 = getExpression((List)list1.get(1));
                    // 
             String str;       
                    // get name
Token                    token = (Token)list1.get(0);

                      desc = "[".repeat((int)(double)(list1.get(2)))+desc;
                    // if variable with same name 
                    if (local_map.get(
    local_map.size()-1).variables.containsKey(
    token.value))
                    {
                        Flaw.error(this.ast, 
        Flaw.type.already_def, token, "variable", token.value);
        return false;
                    }
                    
        if (a1 != null)
        {
             str = getReference(a1);
                    //
                    if (str.equals("-"))
                    {
                        Flaw.error(this.ast, 
        Flaw.type.cannot_infer, token);
        return false;
                    }
                    else if (str.equals("V"))
                    {
                        Flaw.error(this.ast, 
        Flaw.type.cannot_infer, token);
        return false;
                    }
                    
                    // check desc
                    if (this.ast.mhandler.checkDesc(desc, str) == -1)
                    {
                        Flaw.error(this.ast, 
            Flaw.type.incompatible_types, token, str, desc);
                        return false;
                    } 
                    
            }
                    // create type
                    Variable v = new Variable(
                       desc, ""+var_count);
                    // increment count
                    var_count++;
                    // add type
                    local_map.get(
    local_map.size()-1).variables.put(token.value, v);
            if (a1 != null)
            { 
        
        String    desc2 = getReference(v);
  //      System.out.println(desc2);
            if (!getReference(a1).equals(desc2))
            {
                a1 = new Cast(desc2, a1);
            }
                    // add assignment
                    list.add(new Assign(
    v, a1));
            }
                  return true;
    
    }
    
    // get local expression
    public boolean localexpr(List list, List list1)
    {
        // get key
        Token token = (Token)list1.get(0);
        // 
        String str = token.value;
        Object a1;
        //
        Expression expr = this.cur_map.get(str);
        // if got not null, then continue 
        if (expr != null)
        {
            
      //      System.out.println(local_map.get(0).variables);
            
            a1 = expr.run(list1);
            //
            if (isStatement(a1))
            {
                list.add(a1);
                return true;
            }
            else
            {
                this.ast.error_pool.add(new Flaw(Flaw.type.is_not_statement,
                     token.source, token.line, token.position));
                return false;
            }
        }
        else
        {
            switch (str)
            {
                case "$for":
                {
                    //
                    int i = (int)(double)(Double)list1.get(1);
                    //
                    if (i == 1)
                    {
                        
                        Local local = new Local();
                        
                        local_map.add(local);
                        
                        String k, c, b;
                        boolean j = false;
                        if (last_label != null)
                        {
                            k = "#"+last_label;
                            c = "%"+last_label;
                            j = true;
                        }
                        else
                        {
                            k = ""+labels.size();
                            c = "%"+k;
                            k = "#"+k;
                        }
                        
                            ret_label.add(k);
                            con_label.add(c);
                        //
                        a1 = list1.get(2);
                        
                        if (a1 != null)
                    {
                        if (!localexpr(list, (List)a1))
                        {
                        last_label = null;
                            return false;
                        }
                    }
                        //
                        if (j)
                        {
                            ret_label.add(k);
                            con_label.add(c);
                        }
                        //
                        b = "^"+c;
                        labels.add(c);
                        list.add(new Label(c));
                        //
                        a1 = list1.get(3);
                        if (a1 != null)
                        {
                            a1 = createIfGoto(
                              getExpression((List)a1),
                              b, token);
                              if (a1 == null)
                              {
                        last_label = null;
                                  return false;
                              }
                              list.add(a1);
                              list.add(new Goto(k));
                              labels.add(b);
                              list.add(new Label(b));
                        }
                        else
                        {
                            
                        }
                        //
                        for (List ir: (List<List>)list1.get(5))
                        {
                            if (!localexpr(list, ir))
                            {
                        last_label = null;
                                return false;
                            }
                        }
                        
                        a1 = list1.get(4);
                        
                        if (a1 != null)
                    {
                        list.add(getExpression((List)a1));
                    }
                        
                        labels.add(k);
                        list.add(new Goto(c));
                        list.add(new Label(k));
                        //
                        {
                            ret_label.remove(ret_label.size()-1);
                            con_label.remove(con_label.size()-1);
                        }
                        //
                        local_map.remove(local_map.size()-1);
                    }
                        last_label = null;
                    return true;
                }
                //# - break
                //% - continue
                case "$set_enum":
                {
                    // get name 
                    token = (Token)list1.get(1);
                    // get field
                    a1 = getChainField(token);
                    //
                    
              //      System.out.println("here ");
                    // get new object
                    New n = checkNew(this.getter, this.getter.getOwner(),
                                  (List)(list1.get(2)), token, token.value, 
                                  (int)(double)(Double)list1.get(3));
            
                    // if got null, then return
                    if (a1 == null) return false;
                    if (n == null) return false;
                    //
                    list.add(new Assign(a1, n));
                    return true;
                }
                case "$scall":
                {
                    Flaw.error(this.ast, Flaw.type.parent_const_first, token);
                    return false;
                }
                case "$tcall":
                {
                     Flaw.error(this.ast, Flaw.type.this_const_first, token);
                    return false;
                }
                case "$try":
                {
                    //
                    String k, v, b, jc;
                    //
                    boolean j = false;
                    //
                    if (last_label != null)
                    {
                        //
                        k = "*"+last_label;
                        //
                        j = true;
                    }
                    else
                    {
                        //
                        k = "*"+String.valueOf(labels.size());
                    }
                    //
                    if (j)
                    {
                         v = "#"+last_label;
                         ret_label.add(v);
                    }
                    else
                    {
                        v = "^"+k;
                    }
                    //
                    labels.add(k);
                    list.add(new Label(k));
                    //
                    if (!getBlock(list, (List)((List)list1.get(1)).get(1)))
                    {
                        last_label = null;
                        return false;
                    }
                    //
                    labels.add(b = ";"+k);
                    list.add(new Label(b));
                    //
                    int ii = 0;
                    
                    int dd = var_count;
                    var_count++;
                    list.add(new Goto(v));
                    
                    // 
                    for (List i: (List<List>)list1.get(2))
                    {
                        List y = (List)i.get(0);
                       
                       token = (Token)y.get(1);
                       // 
                       ii += 1;
                       
                        str = getObjectName((List)y.get(0), token);                        
                        
                       if (str == null)
                       {
                        last_label = null;
                           return false;
                       }
                    //    labels_error.put();
                    
     list.add(new Handler(k, b, jc =  ";" +  ii+";"+ k, str));

                    
                       
                       list.add(new Label(jc));
                       labels.add(jc);
                       
        if (!getCatch(list, (List)i.get(1), token, dd, str))
                        {
                        last_label = null;
                            return false;
                        }
                        list.add(new Goto(v));
                    }
            
                
                List y = (List)list1.get(3);
                   if (y != null) {
                       // 
                       ii += 1;
                       
            
                    //    labels_error.put();
                    
    list.add(new Handler(k, b, jc =  ";" +  ii  +";"+ k, null));
                    
                     
                       list.add(new Label(jc));
                       labels.add(jc);
                       
                        if (!getFinally(list, y, dd))
                        {
                        last_label = null;
                            return false;
                        }
                        
                        list.add(new Goto(v));
                    }
                    
                    labels.add(v);
                    list.add(new Label(v));
                    if (j)
                    {
    ret_label.remove(ret_label.size() -1);
                    }
                    //
                        last_label = null;
                    return true;
                }
                case "$set":
                {
         //           System.out.println("SETTING");
                    
           //         System.out.println(list1);
                    // get type
                    str = getObjectName((List)list1.get(1), token);
                    // iterate over variables
             //       System.out.println(list1);
                    
                    for (List i: (List<List>) list1.get(2))
                    {
                        if (!setVariable(str, i, list))
                        {
                        last_label = null;
                            return false;
                        }
                    }
                        last_label = null;
                    return true;
                }
                case "$var":
                {
                    // get expression
                    a1 = getExpression((List)list1.get(2));
                    // get name
                    token = (Token)list1.get(1);
                    // if variable with same name 
                    if (local_map.get(
    local_map.size()-1).variables.containsKey(
    token.value))
                    {
                        Flaw.error(this.ast, 
        Flaw.type.already_def, token, "variable", token.value);
                        last_label = null;
        return false;
                    }
                    
                    str = getReference(a1);
                    //
                    if (str.equals("-"))
                    {
                        Flaw.error(this.ast, 
        Flaw.type.cannot_infer, token);
                        last_label = null;
        return false;
                    }
                    else if (str.equals("V"))
                    {
                        Flaw.error(this.ast, 
        Flaw.type.cannot_infer, token);
                        last_label = null;
        return false;
                    }
                    // create type
                    Variable v = new Variable(
                       str, ""+var_count);
                    // increment count
                    var_count++;
                    // add type
                    local_map.get(
    local_map.size()-1).variables.put(token.value, v);
                    // add assignment
                    
                    
                    
         String    desc2 = getReference(v);
            if (!getReference(a1).equals(desc2))
            {
               a1 = new Cast(desc2, a1);
            }
                    
                    list.add(new Assign(
    v, a1));
                        last_label = null;
                    return true;
                }
                case "$goto":
                {
                    //
                    token = (Token)list1.get(1);
                    //
                    if (labels.contains(token.value)
                    ){
                    }
                    else
                    {
                    labels_exp.put(token.value, token);
                    }
                    list.add(new Goto(token.value));
                        last_label = null;
                    return true;
                    //
                }
                case "$init":
                {
                    //
                    if (!getInit(list, (List)list1.get(1)))
                    {
                        return false;
                    };
                    return true;
                    //
                }
                case "$body":
                {
                    String ret = null;
                    //
                    if (last_label != null)
                    {
                        ret = "#" + last_label;
                        this.ret_label.add(ret);
                    }
              //      System.out.println(list1);
                    if (!getBlock(list, (List)list1.get(1)))
                    {
                        last_label = null;
                        return false;
                    }
                    if (ret != null)
                    {
                        ret_label.remove(ret_label.size() - 1);
                        list.add(new Label(ret));
                    }
                    
                               last_label = null;
                               return true;
                    //
                }
                case "$break":
                {
                    //
                    a1 = list1.get(1);
                    //
                    if (a1 == null)
                    {
                        if (ret_label.size() > 0)
                        {
                            //S
                            
                            list.add(new Goto(ret_label.get(
                               ret_label.size() - 1)));
                               last_label = null;
                               return true;
                        }
                        else
                        {
                            Flaw.error(this.ast, Flaw.type.break_outside, 
                                 token);
                               last_label = null;
                                 return false;
                        }
                    }
                    //
                    else
                    {
     //                   System.out.println("fun");
                        //
                        str = ((Token)a1).value;
                        // check for #label element
                        if (this.labels.contains(str))
                        {
                            if (this.ret_label.contains("#"+str))
                            {
                                list.add(new Goto("#"+str));
                               last_label = null;
                               return true;
                            }
                            else
                            {
                                
                            Flaw.error(this.ast, Flaw.type.break_outside, 
                                 token);
                               last_label = null;
                                 return false;
                            }
                        }
                        else
                        {
                     //       System.out.println("ADDING");
                            Flaw.error(this.ast, Flaw.type.undefined_label,
                                 token, str);
                               last_label = null;
                                 return false;
                        }
                        //
                    }
                }
                case "$label":
                {
                //    System.out.println(list1.get(1));
                    token = (Token)(list1.get(1));
                    String label = token.value;
                    if (!labels.contains(label))
                    {
                        labels_exp.remove(label);
                        //
                        list.add(new Label(label));
                        //
                        labels.add(label);
                        last_label = label;
                        return true;
                    }
                    else
                    {
                        this.ast.error_pool.add(new Flaw(
                             Flaw.type.duplicate_element,
                             token.source, token.line, token.position,
                             label));
                             
                        last_label = null;
                        return false;
                    }
                }
                case "$while":
                {
                    String k;
                    if (last_label != null)
                    {
                        k = last_label;
                    }
                    else
                    {
                        k = String.valueOf(labels.size());
                    }
                    // # - break;
                    // % - continue;
                    String ret_label1 = "#" + k;
                    String con_label1 = "%" + k;
                    ret_label.add(ret_label1);
                    con_label.add(con_label1);
                    
                    labels.add(ret_label1);
                    labels.add(con_label1);
                    labels.add("^"+con_label1);
                    
                    list.add(new Label(con_label1));
                    // get expression
                    a1 = createIfGoto(getExpression(
                            (List)list1.get(1)),"^"+con_label1, token);
                    //
                    if (a1 == null)
                    {
                        last_label = null;
                        return false;
                    }
                    //
                    //
                    list.add(a1);
                    if (!getBlock(list, (List)list1.get(3)))
                    {
                               last_label = null;
                        return false;
                    }
                    list.add(new Goto(ret_label1));
                    list.add(new Label("^"+con_label1));
                    //
                    List list2 = (List) list1.get(2);
                    //
                    last_label = null;
                    if (!getBlock(list, list2))
                    {
                        
                        last_label = null;
                        return false;
                    }
                    else
                    {
                               last_label = null;
                        list.add(new Goto(con_label1));
                        list.add(new Label(ret_label1));
                        ret_label.remove(ret_label.size() -1 );
                        con_label.remove(con_label.size() -1 );
                        return true;
                    }
                }
                case "$do":
                {
                    String k;
                    if (last_label != null)
                    {
                        k = last_label;
                    }
                    else
                    {
                        k = String.valueOf(labels.size());
                    }
                    // # - break;
                    // % - continue;
                    String ret_label1 = "#" + k;
                    String con_label1 = "%" + k;
                    ret_label.add(ret_label1);
                    con_label.add(con_label1);
               //     labels.add(ret_label);
               //     labels.add(con_label);
                    labels.add("^"+con_label);
                    
               //     list.add(new Label(con_label));
                    // get expression
                    a1 = createIfGoto(getExpression(
                            (List)list1.get(1)),"^"+con_label, token);
                    
                    list.add(new Label("^"+con_label));
                    //
                    if (a1 == null)
                    {
                               last_label = null;
                        return false;
                    }
                    //
                    //
           //         list.add(new Label("$"+con_label));
           //         list.add(a1);
                    //
                    List list2 = (List) list1.get(2);
                    //
                    last_label = null;
                    if (!getBlock(list, list2))
                    {
                               last_label = null;
                        return false;
                    }
                    else
                    {
                        list.add(con_label);
                        list.add(a1);
                        list.add(ret_label);
                               last_label = null;
                        ret_label.remove(ret_label.size()-1); 
                        con_label.remove(con_label.size()-1); 
                        return true;
                    }
                }
                case "$if":
                {
            //        System.out.println(list1);
                    String k;
                    
                    boolean bn =  (last_label != null);
                    if (bn)
                    {
                        k = last_label;
                    }
                    else
                    {
                        k = String.valueOf(labels.size());
                    }
                    // get expression
                    a1 = createIfGoto(getExpression(
                            (List)list1.get(1)),"^"+k, token);
                    //
                    if (a1 == null) {
                        
                               last_label = null;
                               return false;
                           }
                    labels.add("^"+k);
                    labels.add("*"+k);
                    // 
                    list.add(a1);
                    
                    if (bn)
                    {
                        ret_label.add(("#"+last_label));
                    }
                    if (!getBlock(list, (List)list1.get(3)))
                    {
                               last_label = null;
                        return false;
                    }
                    list.add(new Goto("*"+k));
                    list.add(new Label("^"+k));
                    //
                    if (!getBlock(list, (List)list1.get(2)))
                    {
                               last_label = null;
                        return false;
                    }
                    list.add(new Label("*"+k));
                    if (bn)
                    {
                        list.add(new Label("#"+last_label));
                        ret_label.remove(("#"+last_label));
                    }
                    
                    last_label = null;
                    //
                               last_label = null;
                    return true;
                }
                case "$throw":
                {
                    
                    last_label = null;
                    a1 = (List)list1.get(1);
                    if (a1 == null)
                    {
                               last_label = null;
                         return false;
                     }
                    a1 = getExpression((List)a1);
                    if (a1 == null) {
                               last_label = null;
                        return false;
                    }
                    else
                    {
                               last_label = null;
                        list.add(new Throw(a1));
                        return true;
                    }
                }
                case "$return":
                {
                    
                    last_label = null;
                    if (return_type == null)
                    {
                        Flaw.error("return is not expected here", token,
                               this.ast);
                        return false;
                    }
                    //
                    a1 = (list1.get(1));
                    //
                    if (a1 == null)
                    {
                        if (!return_type.equals("V"))
                        {
                            Flaw.error("return value is not expected here", 
                               token,
                               this.ast);
                            return false;
                        }
                        else
                        {
                            list.add(new Return(null));
                        }
                    }
                    else
                    {
                        //
                        a1 = getExpression((List)a1);
                        if (a1 == null) return false;
                        //
                        String d;
                        int i =this.ast.mhandler.checkDesc(
                               return_type, d = getReference(a1));
                        if (i == -1)
                        {
                             Flaw.error
                             (
                                 ast,
                                 Flaw.type.incompatible_types,
                                 token,
                                 return_type, 
                                 d
                             );
                             return false;
                            
                        }
                        else
                        {
                            list.add(new Return(new Cast(return_type, a1)));
                            return true;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean getBlock(List list, List ast, Pair<String,Token> ... pair)
    {
        //
        String key;
        //
        Expression expr;
        // create locals
        Local local = new Local();
        // add locals
        local_map.add(local);
        ///
        for (Pair <String, Token> p: pair)
        {
            allocateVariable(p.a, p.b);
        }
        // iterate for all elements
        for (List i : (List<List>)ast)
        {
            // get expression
            boolean ir = localexpr(list, i);
            //
            if (ir == false) return false;
        }
        // remove last element
        local_map.remove(local_map.size()-1);
        return true;
    }
    
    public boolean getInit(List list, List ast)
    {
        //
        String key;
        
        Set<String> set = this.labels;
        int temp1 = this.var_count;
        Map temp2= this.labels_exp;
        String ret_type = this.return_type;
        
        this.var_count = 0;
        this.labels = new HashSet<>();
        this.labels_exp = new HashMap<>();
        this.return_type = null;
        
    //    System.out.println(link);
    //    System.out.println(link.init);
        //
        Local local = new Local();
        // add locals
        local_map.add(local);
        
        boolean ir = true;
        // iterate for all elements
        for (List i : (List<List>)ast)
        {
            // get expression
            if (!localexpr(list, i)) ir = false;
        }
        
        list.add(new RemoveVars());
        
        for (Map.Entry<String, Token> en : this.labels_exp.entrySet())
        {
            ir = false;
            Flaw.error( this.ast, Flaw.type.undefined_label, en.getValue(), en.getKey());
        }
        
        //
        this.labels = set;
        this.var_count = temp1;
        this.labels_exp = temp2;
        this.return_type = ret_type;
        // remove last element
        local_map.remove(local_map.size()-1);
        return ir;
    }
    

    public boolean getFunction(List list, MethodLink link, ClassObject object)
    {
        //
        String key;
        Getter gt = this.getter;
        
        int add = 0;
        
        if ((link.access & ACC_STATIC) > 0)
    {
        add = 1;
        this.getter = new ClassGetter(object, object, this.ast).staticGetter();
    }
    else
    {
        this.getter = new ClassGetter(object, object, this.ast);
     }   
     
//     System.out.println(((ClassGetter)this.getter).is_static_inner);
     
        Variable vj;
        // create locals
        Local local = new Local();
        // get function variables 
//        System.out.println(link.name);
        
        Map map = link.init.vars;
        // 
        // 
        int max = add;
        int temp = 0;
        // iterate for map
        for (Map.Entry<Integer, Variable> entry: 
               (Set<Map.Entry<Integer, Variable>>) map.entrySet())
        {
            //
            vj = entry.getValue();
            
            ////
            temp = entry.getKey()+add;
            //
            if (temp > max) max = temp;
            //
            vj = new Variable(vj.descriptor, String.valueOf(temp));
            
            //
            local.variables.put(vj.name, vj);
     //       list.add(new Catch(vj));
        }
        //
        
        Set<String> set = this.labels;
        int temp1 = this.var_count;
        Map temp2= this.labels_exp;
        String ret_type = this.return_type;
        
        this.var_count = max;
        this.labels = new HashSet<>();
        this.labels_exp = new HashMap<>();
        this.return_type = link.getType();
        
    //    System.out.println(link);
    //    System.out.println(link.init);
        //
        List ast = (List) link.init.init.get(6);
        
        //
        Expression expr;
        // add locals
        local_map.add(local);
        
        boolean ir = true;
        // iterate for all elements
        for (List i : (List<List>)ast)
        {
            // get expression
            if (!localexpr(list, i))
            {
                ir = false;
            }
        }
        
        
        
        for (Map.Entry<String, Token> en : this.labels_exp.entrySet())
        {
            ir = false;
            Flaw.error( this.ast, Flaw.type.undefined_label, en.getValue(), en.getKey());
        }
        
        //
        this.labels = set;
        this.var_count = temp1;
        this.labels_exp = temp2;
        this.return_type = ret_type;
        this.getter = gt;
        // remove last element
        local_map.remove(local_map.size()-1);
        return ir;
    }
    
    public boolean getConstructor(List list, MethodLink link, 
    ClassObject object, InheritanceMap<String, Token> maph)
    {
        
        if (link.init == null)
        {
            Getter gta = this.getter;
                        //
                        this.getter = this.getter.getSuper(new Token("")).a;
                        
                Method m = checkMethod(new Token(
                             "<init>"), new ArrayList<>());
                
//                System.out.println(m);
                
                m.owner = new SuperConst(this.getter.getOwner());
                list.add(m);
                //
                        this.getter = gta;
                list.add(checkMethod(
                          new Token( ClassObject.ddinit), 
                                   new ArrayList<>()));
                                   //
                        
            list.add(new Return(null));
            return true;
        }
        
        Token token_name = (Token)link.init.init.get(3);
        token_name = new Token(token_name, link.descriptor);
        
 //       System.out.println(token_name);
        //
        String key;
        Getter gt = this.getter;
        
        int add = 0;
        
        if ((link.access & ACC_STATIC) > 0)
    {
        add = 1;
        this.getter = new ClassGetter(object, object, this.ast).staticGetter();
    }
    else
    {
        this.getter = new ClassGetter(object, object, this.ast);
     }   
     
//     System.out.println(((ClassGetter)this.getter).is_static_inner);
     
        Variable vj;
        // create locals
        Local local = new Local();
        // get function variables 
        Map map = link.init.vars;
        // 
        int max = add;
        int temp = 0;
        // iterate for map
        for (Map.Entry<Integer, Variable> entry: 
               (Set<Map.Entry<Integer, Variable>>) map.entrySet())
        {
            //
            vj = entry.getValue();
            
            ////
            temp = entry.getKey()+add;
            //
            if (temp > max) max = temp;
            //
            vj = new Variable(vj.descriptor, String.valueOf(temp));
            
            //
            local.variables.put(vj.name, vj);
     //       list.add(new Catch(vj));
        }
        //
        
        Set<String> set = this.labels;
        int temp1 = this.var_count;
        Map temp2= this.labels_exp;
        String ret_type = this.return_type;
        
        this.var_count = max;
        this.labels = new HashSet<>();
        this.labels_exp = new HashMap<>();
        this.return_type = link.getType();
        
    //    System.out.println(link);
    //    System.out.println(link.init);
        //
        List ast = (List) link.init.init.get(6);
        //
        Expression expr;
        // add locals
        local_map.add(local);
        //
        if (ast.size() == 0)
        {
            Getter gta = this.getter;
                        //
                        this.getter = this.getter.getSuper(new Token("")).a;
                        
                Method m = checkMethod(new Token(
                             "<init>"), new ArrayList<>());
                
//                System.out.println(m);
                
                m.owner = new SuperConst(this.getter.getOwner());
                list.add(m);
                //
                        this.getter = gta;
                list.add(checkMethod(
                          new Token( ClassObject.ddinit), 
                                   new ArrayList<>()));
                                   //
                        
            list.add(new Return(null));
            return true;
        }
        //
        boolean first = true;
        // iterate for all elements
        for (List i : (List<List>)ast)
        {
            //
            if (first)
            {
                //
                first = false;
                //
                Token key1 = (Token)i.get(0);
                // if got tcall, then check in inheritance map
                if (key1.value.equals("$tcall"))
                {
                    //
                    Method m1 = checkMethod(new Token(key1, "<init>"), (List)i.get(1));
                    if (m1 == null) return false;
                    // check if method is inherited
                    Token t1 = new Token(token_name, m1.link.descriptor);
                    
                    // 
                    InheritanceMap<String, Token>.Node b1 = maph.get(t1.value, t1);
                    //
                    if (b1.inheritedBy(token_name))
                    {
                        Flaw.error(this.ast, Flaw.type.recursive_constructor, b1.value);
                        return false;
                    }
                    
                    b1 = maph.get(token_name.value, token_name).inherit(b1);
                    
                    list.add(m1);;
                    continue;
                }
                else
                {
                    //
                    if (key1.value.equals("$scall"))
                    {
                        if ((object.access & ACC_ENUM)>0)
                        {
                            Flaw.error(this.ast, Flaw.type.call_enum_constructor, key1);
                        }
                        // 
                        Getter gta = this.getter;
                        //
                        this.getter = this.getter.getSuper(token_name).a;
                        //
                        Method m1 = checkMethod(new Token(key1, "<init>"), (List)i.get(1));
                        //
                        m1.owner = new SuperConst(this.getter.getOwner());
                         
                        this.getter = gta;
                        //
                        list.add(m1);
                        //
                        list.add(checkMethod(new Token(
                               token_name, ClassObject.ddinit), new ArrayList<>()));
                        //
                        continue;
                    }
                }
                //
                        Getter gta = this.getter;
                        //
                        this.getter = this.getter.getSuper(token_name).a;
                        
            Method m1 = checkMethod(new Token(key1, "<init>"), new ArrayList<>());
                        //
                        m1.owner = new SuperConst(this.getter.getOwner());
                         
                         list.add(m1);
                //
                        this.getter = gta;
                list.add(checkMethod(
                          new Token(token_name, ClassObject.ddinit), 
                                   new ArrayList<>()));
                                   //
                //
            }
            // get expression
            boolean ir = localexpr(list, i);
            //
            if (ir == false) return false;
        }
        
        for (Map.Entry<String, Token> en : this.labels_exp.entrySet())
        {
            Flaw.error( this.ast, Flaw.type.undefined_label, en.getValue(), en.getKey());
        }
        
        //
        this.labels = set;
        this.var_count = temp1;
        this.labels_exp = temp2;
        this.return_type = ret_type;
        this.getter = gt;
        // remove last element
        local_map.remove(local_map.size()-1);
        return true;
    }
    
    public static class Handler
    {
        public String start;
        public String stop;
        public String handler;
        public String error;
        public Handler(String a, String b, String c, String d)
        {
            start = a;
            stop  =b ;
            handler = c;
            error = d;
        }
    }
    
    
    
    public List<Local> local_map = new ArrayList<>();
    
    public Set<String> labels;
    
    public Map<String, Token> labels_exp;
   // public List<Handler> labels_error;
    
    public String last_label = null;
    
    public List<String> ret_label = new ArrayList<>();
    public List<String> con_label = new ArrayList<>();
    
    public int var_count;
    
    public String return_type;
    
    public static class Local
    {
        public Map<String, Object> variables = new HashMap<>();
    }
    // 
    public Object getConstExpression(Getter table, List ast)
    {
        return getExpression(table, ast, true);
    }
    
    public class CodeVisitor
    {
        public HashMap<String, org.objectweb.asm.Label> labels = new HashMap<>();
        public MethodVisitor visitor;
        public int max = 0;
        public void visitMax(int m)
        {
            if (m >= this.max)
            {
                this.max = m+1;
            }
        }
        public CodeVisitor(MethodVisitor vs)
        {
            this.visitor = vs;
        }
        public String getRef(Object a1)
        {
            return getReference(a1);
        }
        
        public void free(Object a1)
        {
            switch (getReference(a1))
            {
                case "-":
                case "V":
                return;
                case "J":
                case "D":
                visitor.visitInsn(POP2);
                break;
                default:
                visitor.visitInsn(POP);
                break;
            }
        }
        
        public void copy(Object a1)
        {
            switch (getReference(a1))
            {
                case "J":
                case "D":
                visitor.visitInsn(DUP2);
                break;
                default:
                visitor.visitInsn(DUP);
                break;
            }
        }
        
        public void copyA(String a1)
        {
            switch (a1)
            {
                case "J":
                case "D":
                visitor.visitInsn(DUP2);
                break;
                default:
                visitor.visitInsn(DUP);
                break;
            }
        }
        
        public void visitAll(Object obj)
        {
            if (obj instanceof Label)
            {
                visitLabel(((Label)obj).name);
            }
            else if (obj instanceof Goto)
            {
                visitGoto(((Goto)obj).name);
            }
            else if (obj instanceof RemoveVars)
            {
                resetLabels();
            }
            else if (obj instanceof Throw)
            {
                visitThrow((Throw) obj);
            }
            else if (obj instanceof IfGoto)
            {
                visitIfGoto((IfGoto)obj);
            }
            else if (obj instanceof Handler)
            {
                visitHandler((Handler) obj);
            }
            else if (obj instanceof Assign)
            {
                visitAssign((Assign) obj);
                free(obj);
            }
            else if (obj instanceof Null)
            {
                return;
            }
            else if (obj instanceof Return)
            {
                getReturn((Return) obj);
            }
            
            else if (obj instanceof Variable)
            {
                getVariable((Variable)obj);
                free(obj);
            }
            else if (obj instanceof Method)
            {
                visitMethod((Method) obj);
                free(obj);
            }
            else if (obj instanceof Cast)
            {
                cast((Cast) obj);
                free(obj);   
            }
            else if (obj instanceof Field)
            {
                getField((Field) obj);
                free(obj);
            }
            else if (obj instanceof ClassConst)
            {
                return;
           //     getClassConst((ClassConst) obj);
            }
            else if (obj instanceof CallOperator)
            {
                visitCallOperator((CallOperator)obj);
                free(obj);
            }
            else if (obj instanceof ThisConst)
            {
                return;
            }
            else if (obj instanceof Item)
            {
                getItem((Item)obj);
                free(obj);
            }
            else if (obj instanceof New)
            {
                visitNew((New)obj);
                free(obj);
            }
            else if (obj instanceof NewArray)
            {
                visitNewArray((NewArray)obj);
                free(obj);
            }
            else if (obj instanceof AllocArray)
            {
                visitAllocArray((AllocArray)obj);
                free(obj);
            }
            else if (obj instanceof Number)
            {
                return;
            }
            else if (obj instanceof String)
            {
                return;
            }
            else if (obj instanceof Boolean)
            {
                return;
            }
            else if (obj instanceof Character)
            {
                return;
            }
        }
        
        public void loadArguments(List args, List<String> list)
        {
            
            if (args.size() == 0) return;
            //
            Iterator<String> iter = list.iterator();
            //
            String desc, temp, t = iter.next();
            
            int i = 0;
            int d = t.length();
            
            for (Object a1: args)
            {
                //
                getExpression(a1);
                //
                desc = getReference(a1);
                //
                i = desc.length();
                // check descriptor
                if (i == 1)
                {
                    if (d > 1)
                    {
                   //     temp = ClassTable.primitives.get(desc);
                        visitor.visitMethodInsn( 
                                INVOKESTATIC,
                                desc.substring(1, desc.length() - 1),
                                "valueOf",
                                ClassTable.valueof.get(desc),
                                false);
                    }
                }
                else
                {
                    if (d == 1)
                    {
                        temp = ClassTable.primitives.get(t);
                        MethodLink link = ClassTable.getvalue.get(t);
                        visitor.visitMethodInsn(
                                INVOKESTATIC,
                                temp.substring(1, temp.length() - 1),
                                link.name,
                                link.descriptor,
                                false);
                    }
                }
                //
                if (iter.hasNext())
                {
                    t = iter.next();
                    d = t.length();
                }
                //
            }
        }
        
        public void visitAssign(Assign arg)
        {
            Object a1 = null;
            Object a2 = arg.variable;
      ///      if (arg.link != null)
      //      {
       //         a1 = new CallOperator(arg.link, a2, arg.value);
     //       }
     //       else
     //       {
                a1 = arg.value;
     //       }
            
            if (a2 instanceof Item)
            {
                setItem((Item)a2, a1);
            }
            else if (a2 instanceof Field)
            {
                setField((Field)a2, a1);
            }
            else if (a2 instanceof Variable)
            {
                setVariable((Variable)a2, a1);
            }
        }
        
       
        public void getExpression(Object obj)
        {
//            System.out.println(obj);
            if (obj instanceof Variable)
            {
                getVariable((Variable)obj);
            }
            else if (obj instanceof Method)
            {
                visitMethod((Method) obj);
            }
            else if (obj instanceof Cast)
            {
                cast((Cast) obj);   
            }
            else if (obj instanceof Field)
            {
                getField((Field) obj);
            }
            else if (obj instanceof ClassConst)
            {
                getClassConst((ClassConst) obj);
            }
            else if (obj instanceof ThisConst)
            {
  //              System.out.println("THIS");
                getThis();
            }
            else if (obj instanceof Item)
            {
                getItem((Item)obj);
            }
            else if (obj instanceof New)
            {
                visitNew((New)obj);
            }
            else if (obj instanceof NewArray)
            {
                visitNewArray((NewArray)obj);
            }
            else if (obj instanceof AllocArray)
            {
                visitAllocArray((AllocArray)obj);
            }
            else if (obj instanceof Null)
            {
                visitNull();
            }
            else if (obj instanceof CallOperator)
            {
       //         System.out.println("HI");
                visitCallOperator((CallOperator)obj);
            }
            else if (obj instanceof String)
            {
                visitor.visitLdcInsn(obj);
            }
            else if (obj instanceof Boolean)
            {
                boolean b = (boolean) obj;
                if (b)
                {
                    visitor.visitInsn(ICONST_1);
                }
                else
                {
                    visitor.visitInsn(ICONST_0);
                }
            }
            else if (obj instanceof Byte)
            {
                visitor.visitIntInsn(BIPUSH, (byte)obj);
            }
            else if (obj instanceof Short)
            {
                visitor.visitIntInsn(SIPUSH, (short)obj);
            }
            else if (obj instanceof Number)
            {
                visitor.visitLdcInsn(obj);
            }
            else if (obj instanceof Character)
            {
                visitor.visitLdcInsn(obj);
            }
        }
        
        public void getVariable(Variable obj)
        {
            // get int 
            int i = Integer.parseInt(obj.name);
            int opcode;
            // set int
            if (obj.descriptor.length()>1)
            {
                opcode = ALOAD;
            }
            else
            {
                switch (obj.descriptor)
                {
                    case "D":
                    opcode = DLOAD;
                    break;
                    case "F":
                    opcode = FLOAD;
                    break;
                    case "J":
                    opcode = LLOAD;
                    break;
                    default:
                    opcode = ILOAD;
                    break;
                }
            }
            visitor.visitVarInsn(opcode, i);
        }
        
        
        
        public void setVariable(Variable obj, Object a1)
        {
            // get int 
            int i = Integer.parseInt(obj.name);
            int opcode;
            // set int
            if (obj.descriptor.length()>1)
            {
                opcode = ASTORE;
            }
            else
            {
                switch (obj.descriptor)
                {
                    case "D":
                    opcode = DSTORE;
                    break;
                    case "F":
                    opcode = FSTORE;
                    break;
                    case "J":
                    opcode = LSTORE;
                    break;
                    default:
                    opcode = ISTORE;
                    break;
                }
            }
            this.getExpression(a1);
            if (opcode == ASTORE)
            {
                this.wrap(a1);
            }
            else
            {
                this.peel(a1);
            }
  //          System.out.println(getReference(a1));
            this.copy(a1);
            visitor.visitVarInsn(opcode, i);
        }
        
        
        public void getClassConst(ClassConst c)
        {
            visitor.visitLdcInsn(Type.getType(getReference(c.object)));
        }
        
        public void getConst(String c)
        {
            visitor.visitLdcInsn(c);
        }
        
        public void instanceOf(InstanceOf of)
        {
            getExpression(of.value);
            visitor.visitTypeInsn(INSTANCEOF, of.getString());
        }
        
        public void wrap(Object a1)
        {
            String str = getReference(a1);
            if (str.length() == 1)
            {
                //
                str = ClassTable.primitives.get(str);
                //
                String desc = ClassTable.valueof.get(str);
                //
                visitor.visitMethodInsn(INVOKESTATIC,
                        str.substring(1, str.length()-1),
                        "valueOf",
                        desc,
                        false);
            }
        }
        
        public void wrapA(String str)
        {
            if (str.length() == 1)
            {
                //
                str = ClassTable.primitives.get(str);
                //
                String desc = ClassTable.valueof.get(str);
                //
                visitor.visitMethodInsn(INVOKESTATIC,
                        str.substring(1, str.length()-1),
                        "valueOf",
                        desc,
                        false);
            }
        }
        
        public void visitCallOperator(CallOperator op)
        {
      //      System.out.println(op.link);
            call_map.get(op.link).run(this, op.args.toArray(new Object[0]));
        }
        
        public String peel(Object a1)
        {
            String str = getReference(a1);
            if (str.length() > 1)
            {
                //
                String desc = ClassTable.primitives.get(str);
                //
                str = str.substring(1, str.length()-1);
                //
                MethodLink link = ClassTable.getvalue.get(desc);
                //
                visitor.visitMethodInsn(INVOKEVIRTUAL, 
                        str, 
                        link.name,
                        link.descriptor,
                        false);
                //
                return desc;
            }
            return str;
        }
       
        public void castPrimitive(String i, String prim)
        {      
            // i    - from
            // prim - to
                int second = 0;
                int first = 0;
                //
                switch (i)
                {
                    case "C":
                    second = I2C;
                    i = "I";
                    break;
                    case "S":
                    second = I2S;
                    i = "I";
                    break;
                    case "B":
                    second = I2B;
                    i = "I";
                    break;
                    default:
                }
                //
            back:
                switch (prim)
                {
                    case "F":
                    switch (i) 
                    {
                        case "F":
                        break back;
                        case "D":
                        first = F2D;
                        break back;
                        case "I":
                        first = F2I;
                        break back;
                        case "J":
                        first = F2L;
                        break back;
                    }
                    case "D":
                    switch (i)
                    {
                        case "D":
                        break back;
                        case "F":
                        first = D2F;
                        break back;
                        case "I":
                        first = D2I;
                        break back;
                        case "J":
                        first = D2L;
                        break back;
                    }
                    case "J":
                    switch (i)
                    {
                        case "J":
                        break back;
                        case "F":
                        first = L2F;
                        break back;
                        case "I":
                        first = L2I;
                        break back;
                        case "D":
                        first = L2D;
                        break back;
                    }
                    case "I":
                    switch (i)
                    {
                        case "I":
                        break back;
                        case "F":
                        first = I2F;
                        break back;
                        case "D":
                        first = I2D;
                        break back;
                        case "J":
                        first = I2L;
                        break back;
                    }
                }
                if (first > 0)
                {
                    visitor.visitInsn(first);
                }
                if (second > 0)
                {
                    visitor.visitInsn(second);
                }
        } 
        
        public void cast(Cast of)
        {
            Object a1 = of.value;
            //
            getExpression(a1);
            //
            char tt = of.reference.charAt(0);
            if ((tt == 'L') || (tt == '['))
            {
            //
                wrap(a1);
                visitor.visitTypeInsn(CHECKCAST, of.getString());
            }
            else
            {
                String i = peel(a1);
                // get opcode
                castPrimitive(i, of.reference);
            }
        }
        
        public void visitThrow(Object a1)
        {
            getExpression(a1);
            visitor.visitInsn(ATHROW);
        }
        
        public void getThis()
        {
            visitor.visitVarInsn(ALOAD, 0);
        }
        
        public org.objectweb.asm.Label getLabel(String lbl)
        {
            org.objectweb.asm.Label l = this.labels.get(lbl);
            if (l == null)
            {
                l = new org.objectweb.asm.Label();
                this.labels.put(lbl, l);
            }
            return l;
        }
        
        public void visitHandler(Handler hd)
        {
            visitor.visitTryCatchBlock(
                  getLabel(hd.start),
                  getLabel(hd.stop),
                  getLabel(hd.handler),
                           hd.error);
        }
        
        public void getReturn(Return a12)
        {
            Object a1 = a12.value;
            if (a1 == null)
            {
                visitor.visitInsn(RETURN);
                return ;
            }
            String desc = getReference(a1);
            getExpression(a1);
            int opcode;
            if (desc.length() > 1)
            {
                opcode = ARETURN;
            }
            else
            {
                switch(desc)
                {
                    case "D":
                    opcode = DRETURN;
                    break;
                    case "F":
                    opcode = FRETURN;
                    break;
                    case "J":
                    opcode = LRETURN;
                    break;
                    default:
                    opcode = IRETURN;
                    break;
                }
            }
            visitor.visitInsn(opcode);
        }
        
        
        
        public void getItem(Item obj)
        {
            //
            getExpression(obj.owner);
            getExpression(obj.value);
            //
            int opcode;
            // set int
            if (obj.descriptor.length()>1)
            {
                opcode = AALOAD;
            }
            else
            {
                switch (obj.descriptor)
                {
                    case "D":
                    opcode = DALOAD;
                    break;
                    case "F":
                    opcode = FALOAD;
                    break;
                    case "J":
                    opcode = LALOAD;
                    break;
                    case "I":
                    opcode = IALOAD;
                    break;
                    case "B":
                    opcode = BALOAD;
                    break;
                    case "C":
                    opcode = BALOAD;
                    break;
                    default:
                    opcode = BALOAD;
                    break;
                }
            }
            visitor.visitInsn(opcode);
            //
        }
        
        public void setItem(Item obj, Object a1)
        {
            //
            getExpression(obj.owner);
            getExpression(obj.value);
            //
            int opcode;
            // set int
            if (obj.descriptor.length()>1)
            {
                opcode = AASTORE;
            }
            else
            {
                switch (obj.descriptor)
                {
                    case "D":
                    opcode = DASTORE;
                    break;
                    case "F":
                    opcode = FASTORE;
                    break;
                    case "J":
                    opcode = LASTORE;
                    break;
                    case "I":
                    opcode = IASTORE;
                    break;
                    case "S":
                    opcode = SASTORE;
                    break;
                    case "C":
                    opcode = CASTORE;
                    break;
                    default:
                    opcode = BASTORE;
                    break;
                }
            }
            this.getExpression(a1);
            this.copy(a1);
            visitor.visitInsn(opcode);
            //
        }
        
        
        public void getField(Field m)
        {
            FieldLink link = m.link;
            Object o = m.owner;
            //
            if (o instanceof ClassObject ||
                o instanceof SuperConst)
            {
                String name = getReference(o);
                visitor.visitFieldInsn(GETSTATIC, 
                    name.substring(1, name.length()-1),
                    link.name, 
                    link.descriptor);
            }
          else
          {
            //
            getExpression(o);
            //
            if ((link.access & ACC_STATIC) > 0)
            {
                free(o);
                String name = getReference(o);
                visitor.visitFieldInsn(GETSTATIC,
                    name.substring(1, name.length()-1),
                    link.name,
                    link.descriptor);
            }
            else
            {
                String name = getReference(o);
                visitor.visitFieldInsn(GETFIELD,
                    name.substring(1, name.length()-1),
                    link.name,
                    link.descriptor);
            }
          }
        }
        
        
        public void setField(Field m, Object a1)
        {
            FieldLink link = m.link;
            Object o = m.owner;
                String value = getReference(o);
                value = value.substring(1, value.length() - 1);
            //
            if (o instanceof ClassObject ||
                o instanceof SuperConst)
            {
                getExpression(a1);
            
            if (getReference(a1).length() == 1)
            {
                this.wrap(a1);
            }
            else
            {
                this.peel(a1);
            }
            
                switch(getReference(a1))
            {
                case "J":
                case "D":
                case "F":
                visitor.visitInsn(DUP2);
                break;
                default:
                visitor.visitInsn(DUP);
            }
                visitor.visitFieldInsn(PUTSTATIC, 
                    value,
                    link.name, 
                    link.descriptor);
            }
          else
          {
            //
            getExpression(o);
            
            if (getReference(a1).length() == 1)
            {
                this.wrap(a1);
            }
            else
            {
                this.peel(a1);
            }
            
            //
            if ((link.access & ACC_STATIC) > 0)
            {
                free(o);
                
                getExpression(a1);
                
            if (getReference(a1).length() == 1)
            {
                this.wrap(a1);
            }
            else
            {
                this.peel(a1);
            }
            
                
                switch(getReference(a1))
            {
                case "J":
                case "D":
                case "F":
                visitor.visitInsn(DUP2);
                break;
                default:
                visitor.visitInsn(DUP);
            }
                visitor.visitFieldInsn(PUTSTATIC,
                    value,
                    link.name,
                    link.descriptor);
            }
            else
            {
                getExpression(a1);
                
            if (getReference(a1).length() == 1)
            {
                this.wrap(a1);
            }
            else
            {
                this.peel(a1);
            }
            
                
                switch(getReference(a1))
            {
                case "J":
                case "D":
                case "F":
                visitor.visitInsn(DUP2_X1);
                break;
                default:
                visitor.visitInsn(DUP_X1);
            }
                visitor.visitFieldInsn(PUTFIELD,
                    value,
                    link.name,
                    link.descriptor);
            }
          }
        }
        
        public void visitMethod(Method m)
        {
            MethodLink link = m.link;
            Object o = m.owner;
            //
            if (o instanceof ClassObject)
            {
                ClassObject obj = (ClassObject) o;
                
                loadArguments(m.args, m.link.getList());
                
      //              System.out.println(o);
    //System.out.println(link.name);
                visitor.visitMethodInsn(
                INVOKESTATIC,
                    obj.name, 
                    link.name,
                    link.descriptor,
                    false);
                      
            }
            else if (o instanceof SuperConst)
            {
                getThis();
                SuperConst obj = (SuperConst) o;
                
                loadArguments(m.args, m.link.getList());
                
  //  System.out.println(obj.object.name);
                visitor.visitMethodInsn(
                     INVOKESPECIAL,
                     obj.object.name,
                     link.name,
                     link.descriptor,
                     false);
            }
            else
            {
                getExpression(o);
                // if is static
                if ((link.access & ACC_STATIC) > 0)
                {
                    free(o);
                }
                
                String obj = getReference(o);
                if (obj.charAt(0) == 'L')
                {
                    obj = obj.substring(1, obj.length()-1);
                }
                
//    System.out.println(obj);
                loadArguments(m.args, m.link.getList());
                
                // if is static
                if ((link.access & ACC_STATIC) > 0)
                {
                   visitor.visitMethodInsn(
                     INVOKESTATIC,
                     obj,
                     link.name,
                     link.descriptor,
                     false);
                }
                else if ((link.access & ACC_PRIVATE) > 0)
                {
       //             System.out.println(o);
     //       System.out.println(link.name);
                   visitor.visitMethodInsn(
                     INVOKESPECIAL,
                     obj,
                     link.name,
                     link.descriptor,
                     false);
                }
                else 
                {
                   visitor.visitMethodInsn(
                     INVOKEVIRTUAL,
                     obj,
                     link.name,
                     link.descriptor,
                     false);
                }
            }
        }
    
        public void visitNull()
        {
            visitor.visitInsn(ACONST_NULL);
        }
        
        public void visitNew(New lb)
        {
            visitor.visitTypeInsn(NEW, lb.object.name);
            visitor.visitInsn(DUP);
            
            loadArguments(lb.init.args, lb.init.link.getList());
            
            visitor.visitMethodInsn(INVOKESPECIAL, lb.object.name, 
                lb.init.link.name, lb.init.link.descriptor, false); 
        }
        
        public void visitAllocArray(AllocArray array)
        {
            //
            List args = array.args;
     //       System.out.println(args);
            //
            int dim = args.size();
            // 
            loadArguments(args, Arrays.asList("I"));
            //
            // check descriptor
            String desc = array.object.returnType;
            // 
            
    //        System.out.println(desc);
            
            if (desc.length() > 1)
            {
                visitor.visitMultiANewArrayInsn(
                                        desc, dim);
            }
            else
            {
                switch (desc)
                    {
                        case "D":
                        visitor.visitIntInsn(NEWARRAY, T_DOUBLE); 
                        break;
                        case "F":
                        visitor.visitIntInsn(NEWARRAY, T_FLOAT);  
                        break;
                        case "J":
                        visitor.visitIntInsn(NEWARRAY, T_LONG);  
                        break;
                        case "I":
                        visitor.visitIntInsn(NEWARRAY, T_INT);  
                        break;
                        case "S":
                        visitor.visitIntInsn(NEWARRAY, T_SHORT);  
                        break;
                        case "B":
                        visitor.visitIntInsn(NEWARRAY, T_BYTE); 
                        break;
                        case "C":
                        visitor.visitIntInsn(NEWARRAY, T_CHAR);  
                        break;
                        case "Z":
                        visitor.visitIntInsn(NEWARRAY, T_BOOLEAN); 
                        break;
                    }
            }
        }
        
        public void visitNewArray(NewArray array)
        {
            //
            List args = array.args;
            //
            String t = array.object.returnType;
            String desc;
            //
            boolean dm = (t.length() == 1);
            //
            MethodLink link = null;
            //
            int dim = array.object.dimension;
            //
            getExpression(array.args.size());
            //
            int opcode = AASTORE;
            //
            if (dim == 1)
            {
                // 
                if (t.length() == 1)
                {
                    //
                    switch (t)
                    {
                        case "D":
                        visitor.visitIntInsn(NEWARRAY, T_DOUBLE); 
            opcode = DASTORE;  
                        break;
                        case "F":
                        visitor.visitIntInsn(NEWARRAY, T_FLOAT);  
            opcode = FASTORE; 
                        break;
                        case "J":
                        visitor.visitIntInsn(NEWARRAY, T_LONG);  
            opcode = LASTORE; 
                        break;
                        case "I":
                        visitor.visitIntInsn(NEWARRAY, T_INT);  
            opcode = IASTORE; 
                        break;
                        case "S":
                        visitor.visitIntInsn(NEWARRAY, T_SHORT);  
            opcode = SASTORE; 
                        break;
                        case "B":
                        visitor.visitIntInsn(NEWARRAY, T_BYTE); 
            opcode = BASTORE;  
                        break;
                        case "C":
                        visitor.visitIntInsn(NEWARRAY, T_CHAR);  
            opcode = CASTORE;
                        break;
                        case "Z":
                        visitor.visitIntInsn(NEWARRAY, T_BOOLEAN); 
            opcode = BASTORE; 
                        break;
                    }
                    //
                }          
                else
                {
                    //
                    //
                    desc = ClassTable.primitives.get(t);
                    //
                    if (desc != null)
                    {
                        link = ClassTable.getvalue.get(desc);
                    }
                    
                    t = t.substring(1,t.length()-1);
                    //
                    visitor.visitTypeInsn(ANEWARRAY, t);
                }
            }
            else
            {
                visitor.visitTypeInsn(ANEWARRAY, t);
            };
            int i = 0;
            for (Object a1: args)
            {
                //
                visitor.visitInsn(DUP);
                //
                visitor.visitLdcInsn(i);
                //
                getExpression(a1);
                //
                desc = getReference(a1);
                i = desc.length();
                //
                if (i == 1)
                {
                    if (!dm)
                    {
                   //     temp = ClassTable.primitives.get(desc);
                        visitor.visitMethodInsn( 
                                INVOKESTATIC,
                                desc.substring(1, desc.length() - 1),
                                "valueOf",
                                ClassTable.valueof.get(desc),
                                false);
                    }
                }
                else
                {
                    if (dm)
                    {
                        visitor.visitMethodInsn(
                                INVOKESTATIC,
                                t,
                                link.name,
                                link.descriptor,
                                false);
                    }
                }
                //
                visitor.visitInsn(opcode);
            }
        }
        
        public void visitLabel(String name)
        {
            org.objectweb.asm.Label lb = labels.get(name);
            
            if (lb == null)
            {
                lb = new org.objectweb.asm.Label();
                labels.put(name, lb);
            }
            
            visitor.visitLabel(lb);  
        }
        public void visitGoto(String name)
        {
            org.objectweb.asm.Label lb = labels.get(name);
            
            if (lb == null)
            {
                lb = new org.objectweb.asm.Label();
                labels.put(name, lb);
            }
            
            visitor.visitJumpInsn(GOTO, lb);            
        }
        public void visitIfGoto(IfGoto g)
        {
            String name = g.name;
            getExpression(g.value);
            
            org.objectweb.asm.Label lb = labels.get(name);
            
            if (lb == null)
            {
                lb = new org.objectweb.asm.Label();
                labels.put(name, lb);
            }
            
            visitor.visitJumpInsn(IFNE, lb);            
        }
        public void resetLabels()
        {
            this.labels = new HashMap<>();
        }
    }
}
