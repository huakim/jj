package com.ejavac;import java.util.*;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.*;



public class ClassTable
{
    // this is an ClassObject
    public ClassObject object;
    // this is an classobect import table
    public ImportTable table;
    // this is an ast cache
    public AST ast;
    // this is an superclass and interfaces 
    public List<ClassTable> parents;// = new ArrayList<>();
    // this is an error pool 
    public List<Flaw> error_pool;
    // this is an generic list
//public Map<String, ClassObject> generic;
    // this is an outer class
    public ClassObject outer = null;
    // this is an constructor
    public ClassTable(ClassObject object)
    {        
        List parents;
        AST ast = object.ast;
        
        object.link1();
        // set outer class
        this.outer = object.outer;
        // this is an count of object interfaces
        int int_count = object.interfaces.size();
        // create an new list
        this.parents = parents = new ArrayList<>(1 + int_count);
        // set classobject
        this.object = object;
        // set classobject generics
  //      this.generic = object.generic;
        // set import table
        this.table = object.table;
        // set ast cache 
        this.ast = ast;
        // add superclass and interfaces to the parent classes list
        if (object.parents != null)
        {
            this.parents = object.parents;
        }
        // if object name is not equals to the "java/lang/Object"
        else if (!object.name.equals(ClassObject.obj_name))
{
//    System.out.println(AST.object_name);
//        System.out.println(object);
//        System.out.println(object.init);
        
        ClassTable p = (ast.getClassByName(object.superName).getTable());
        if (p != null) parents.add(p);
        else parents.add(ast.getClassByName(ClassObject.obj_name).getTable());
        for (String i : object.interfaces)
        {
            p = (ast.getClassByName(i).getTable());
            if (p != null) parents.add(p);
        }
        object.parents = parents;
}
    }
    
    // this is an constant enum values
    public static final FieldLink non_exists_field = new FieldLink();
    public static final MethodLink non_exists_method = new MethodLink();
    public static final FieldLink ambiguous_field = new FieldLink();
    public static final MethodLink ambiguous_method = new MethodLink();
    public static final ClassObject non_exists_object = new ClassObject();
    public static final ClassObject ambiguous_object = new ClassObject();
    public static final InnerClassLink non_exists_inner = new InnerClassLink();
    public static final InnerClassLink ambiguous_inner = new InnerClassLink();
    
    
    public String getName()
    {
        return this.object.name;
    }
    // 
    public boolean isSameHost(ClassObject object)
    {
        return this.ast.isSameHost(this.object, object);
    }
    
    public boolean isSamePackage(ClassObject object)
    {
        return this.object.packageName.equals(object.packageName);
    }
    
    // this method return an innerclasslink from classobject and
    // parents objects
    public InnerClassLink getInnerClassA(String name)
    {
        // get inner link
        InnerClassLink inner = this.object.getInnerClass(name);
        // if inner link is not there, then iterate with inner classes
        if (inner == null)
        {
            // this is an temporary inner class
            InnerClassLink temp = null;
            // iterate for parents
            for (ClassTable i: this.parents)
            {
                // try to get inner class in classobjetc
                temp = i.getInnerClassA(name);
                // if got non_exists object, then continue
                if (temp == ClassTable.non_exists_inner)
                {
                    continue;
                }
                // if got ambiguous object, then return
                if (temp == ClassTable.ambiguous_inner)
                {
                    return temp;
                }
                // check field access 
        if (!i.isSameHost(this.object))
        {
            if ((inner.access & ACC_PRIVATE) > 0)
            {
                    continue;
            }
            else if (!i.isSamePackage(this.object))
            {
                if ((inner.access & (ACC_PUBLIC | ACC_PROTECTED)) == 0)
                {
                    continue;
                }
            }
        }
                // else, check if inner is null
                // if yes, then continue
                // else, return ambiguous object
                if (inner == null)
                {
                    inner = temp;
                }
                else
                {
                    return ClassTable.ambiguous_inner;
                }
            }
        }
        // 
        // if inner link is null, then return 
        if (inner == null) return ClassTable.non_exists_inner;
        else return inner;
    }
    
    // this method return an classobject from classobject and
    // parents objects
    public ClassObject getClassA(String name)
    {
        ClassObject link;
        // get inner class link from this object
        InnerClassLink inner = this.object.getInnerClass(name);
        // if got inner class, then return
        if (inner != null)
        { 
            // i got inner 
     //       System.out.println("inner::" + inner.name);
            link = this.ast.getClassByName(inner.name);
            link.access |= inner.access & ACC_STATIC;
            // if link is null, then return 
            if (link != null) 
            {
                return link;
            }
        }
        // else, check from parent objects
        ClassObject temp = null;
    
        // if got null, then check from parent objects
        for (ClassTable i : this.parents)
        {
   //         System.out.println(i);
   //         System.out.println(i.getTable());
            
            // check field from classobject
            link = i.getClassA(name);
            // check temp field 
            // if field not exists, then continue
            if (link == non_exists_object) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous
            if (link == ambiguous_object) return link;
            // check field access
        if (!i.isSameHost(this.object))
        {
            if ((link.access & ACC_PRIVATE) > 0)
            {
                    continue;
            }
            else if (!i.isSamePackage(this.object))
            {
                if ((link.access & (ACC_PUBLIC | ACC_PROTECTED)) == 0)
                {
                    continue;
                }
            }
        }
            // if field is already got, then return ambiguous
            if (temp != null) return ambiguous_object;
            // set temporary field
            temp = link;
        }
        // 
        return (temp == null) ? non_exists_object : temp ;
    };
    
    
    
    // this method return an classobject from classtable
    public ClassObject getClassB(String name)
    {
        // get object
        ClassObject link = this.getClassA(name);
        // if got nonexitst object, then try to get object from parent
        // if link is not null, then return 
        if (link == non_exists_object)
        {
            if (this.outer != null)
            {
                link = this.outer.getTable().getClassB(name);
            }
       //     else if (this.table != null)
       //     {
       //         link = this.table.getClass(name);
       //     }
        }
        //
        return link;
    };
    
    public ClassObject getClass(String name)
    {
        // get object
        ClassObject link = this.getClassB(name);
        // if got, then return
        if (link == non_exists_object)
        {
            if (this.table != null)
            {
                link = this.table.getClass(name);
            }
        }
        return link;
    }
    
    // this function will return an inherit level
    public int extendLevel(String name, int level)
    {
        
     // System.out.println("Base: " + object.name + " SS: " + name);
        // check if object name and name are equals, if yes, then return level + 1
        if (object.name.equals(name))
        {
            return level;
        }
        // check for equality for other objects
        else
        {
            //
            int min = -1;
            // 
            int l;
            for(ClassTable i: parents)
            {
                // check level
                l = i.extendLevel(name, level + 1);
                // if level equals to -1, then continue
                // else, set min 
                if (l > -1)
                {
                    if (min == -1)
                    {
                        min = l;
                    }
                    else if (l < min)
                    {
                        min = l;
                    }
                }
            }

            return min;
        }
    }
    
    public int extendLevel(String name)
    {
        return extendLevel(name, 0);
    }
    
    // this is an map of primitive types
    public static final Map<String, String> primitives;
    static {
        HashMap map = new HashMap<>();
        map.put("I", "Ljava/lang/Integer;");
        map.put("F", "Ljava/lang/Float;");
        map.put("J", "Ljava/lang/Long;");
        map.put("D", "Ljava/lang/Double;");
        map.put("B", "Ljava/lang/Byte;");
        map.put("C", "Ljava/lang/Character;");
        map.put("Z", "Ljava/lang/Boolean;");
        map.put("S", "Ljava/lang/Short;");
        map.put("Ljava/lang/Integer;", "I");
        map.put("Ljava/lang/Float;", "F");
        map.put("Ljava/lang/Long;", "J");
        map.put("Ljava/lang/Double;", "D");
        map.put("Ljava/lang/Byte;", "B");
        map.put("Ljava/lang/Character;", "C");
        map.put("Ljava/lang/Boolean;", "Z");
        map.put("Ljava/lang/Short;", "S");
        primitives = Collections.unmodifiableMap(map);
    } 
    
    public static final Map<String, String> valueof;
     static {
        HashMap map = new HashMap<>();
        
        map.put("Ljava/lang/Integer;", "(I)Ljava/lang/Integer;");
        map.put("Ljava/lang/Float;", "(F)Ljava/lang/Float;");
        map.put("Ljava/lang/Long;", "(J)Ljava/lang/Long;");
        map.put("Ljava/lang/Double;", "(D)Ljava/lang/Double;");
        map.put("Ljava/lang/Byte;", "(B)Ljava/lang/Byte;");
        map.put("Ljava/lang/Character;", "(C)Ljava/lang/Character;");
        map.put("Ljava/lang/Boolean;", "(Z)Ljava/lang/Boolean;");
        map.put("Ljava/lang/Short;", "(S)Ljava/lang/Short;");
        
        valueof = Collections.unmodifiableMap(map);
    }
    
    public static final Map<String, MethodLink> getvalue;
    static {
        HashMap map = new HashMap<>();
        
        map.put("I", new MethodLink("intValue", "()I"));
        map.put("F", new MethodLink("floatValue", "()I"));
        map.put("J", new MethodLink("longValue", "()I"));
        map.put("D", new MethodLink("doubleValue", "()I"));
        map.put("B", new MethodLink("byteValue", "()I"));
        map.put("C", new MethodLink("charValue", "()I"));
        map.put("Z", new MethodLink("booleanValue", "()I"));
        map.put("S", new MethodLink("shortValue", "()I"));
        
        getvalue = Collections.unmodifiableMap(map);
    }
    
    public static final List<String> numbers = Collections.unmodifiableList(
    Arrays.asList("B", "S", "I", "J", "F", "D"));
    
    // this method return an method with descriptor from classobject and 
    // parents objects
 /*   public MethodLink getMethodA(String name, List<String> desc)
    {
        // if method in the class object, then return
        MethodLink link = this.object.getMethodLink(name, desc);
        // if link is not null, then return
        if (link != null) return link;
        // 
        MethodLink temp = null;
        // if got null, then check from parent objects
        for (ClassObject i : this.parents)
        {
            // check field from classobject
            link = i.getTable().getMethodA(name, desc);
            // check temp field 
            // if field not exists, then continue
            if (link == non_exists_method) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous
            if (link == ambiguous_method) return link;
            // check field access 
        if (!AST.isSameHost(i, this.object))
        {
            if ((link.access & ACC_PRIVATE) > 0)
            {
                    continue;
            }
            else if (!i.packageName.equals(this.object.packageName))
            {
                if ((link.access & (ACC_PUBLIC | ACC_PROTECTED)) == 0)
                {
                    continue;
                }
            }
        }
            // if field is already got, then return ambiguous
            if (temp != null) return ambiguous_method;
            // set temporary field
            temp = link;
        }
        // 
        return (temp == null) ? non_exists_method : temp;
    }
    
    // this method return an method with descriptor from classtable
    public MethodLink getMethod(String name, List<String> desc)
    {
        // get method 
        MethodLink link = this.getMethodA(name, desc);
        
        // 
        if (link == non_exists_method)
        {
            if (this.outer != null)
            {
                link = this.outer.getTable().getMethodA(name, desc);
            }
        }
    
        if (link != non_exists_method) return link;
        // iterate over static map
        else if (this.table != null)
        {
            return this.table.getMethod(name, desc);
        }
        else
        {
            return link;
        }
    }
    */
    
    // this method will check if object contains this method
    
    // this method will return an method map from this classobject
    // and parents objects
    public MethodMap getMethodMapA(String name)
    {
        List<MethodMap> list = new ArrayList<>();
        // get class method map
//        MethodMap map =
        MethodMap map = this.object.methods.get(name);
        
        // if methodmap is not empty, then add this map to the list
        if (map != null) list.add(map);
        // add all methods from superclass
        if (this.parents.size() > 0)
        {
            
            List<ClassTable> parents = this.parents;
            // get object
            ClassTable object = parents.get(0);
            // get map
            map = object.getMethodMapA(name);
            // if got null, then continue
            // check object access
            if (!object.isSameHost(this.object))
            {
                if (object.isSamePackage(this.object))
                {
                    // add condition map to the list
                    list.add(new MethodMap.MethodMapCondition(
                    MethodMap.MethodMapCondition.privateCondition, map));
                }
                else
                {
                    list.add(new MethodMap.MethodMapCondition(
                    MethodMap.MethodMapCondition.publicCondition, map));
                }
            }
            // iterate for interfaces
            for (int i = 1, n = parents.size(); i < n; i ++)
            {
            // get object
                object = parents.get(i);
            // get object table 
                list.add(object.getMethodMapInterface(name));
            } 
        }
        
     //   System.out.println("classname: "+this.object.name);
     //   System.out.println("list: " + list);
        // return merged map
        return new MethodMap.MethodMapMerged(list);
    }
    // this method will return an interface method map
    public MethodMap getMethodMapInterface(String name)
    {
        // create an list
        List<MethodMap> list = new ArrayList<>();
        // get new method
        MethodMap map = this.object.methods.get(name);
        // get all interface public abstract and default methods
        if (map != null) list.add(new MethodMap.MethodMapCondition(
        MethodMap.MethodMapCondition.interfaceCondition, map));
        // add other interface maps
        ClassObject object;
        List<ClassTable> parents = this.parents;
        for (int i = 1, n = parents.size(); i < n; i ++)
        {
            // get object table 
            list.add(parents.get(i).getMethodMapInterface(name));
        }
        // return merged map
        return new MethodMap.MethodMapMerged(list);
    }
    
    // this method will search for method from methodmap
    public MethodLink searchMethodA(String name, List<String> values)
    {
        // get method map
        MethodMap map = this.getMethodMapA(name);
        // search for method from methodmap
        MethodLink link = this.ast.mhandler.searchMethod(map, values);
        // return link
        return link;
    }
    public MethodLink searchMethodA(String name, String ... values)
    {
        // get method map
        MethodMap map = this.getMethodMapA(name);
        
   //     System.out.println(map);
        // search for method from methodmap
        MethodLink link = this.ast.mhandler.searchMethod(map, values);
        // return link
        return link;
    }
    
    // this method return an method from from classobject and 
    // parents objects
    public MethodLink getMethodA(String name, List<String> list)
    {
        // if field in the class object, then return
        MethodLink link = this.object.getMethodLink(name, list);
        // if link is not null, then return
        if (link != null) return link;
        // 
        MethodLink temp = null;
        // if got null, then check from parent objects
        for (ClassTable i : this.parents)
        {
            // check field from classobjetc
            link = i.getMethodA(name, list);
            // check temp field 
            // if field not exists, then continue
            if (link == non_exists_method) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous
            if (link == ambiguous_method) return link;
            // check field access 
        if (!(i.isSameHost(this.object)))
        {
            if ((link.access & ACC_PRIVATE) > 0)
            {
                    continue;
            }
            else if (!i.isSamePackage(this.object))
            {
                if ((link.access & (ACC_PUBLIC | ACC_PROTECTED)) == 0)
                {
                    continue;
                }
            }
        }
            // if field is already got, then return ambiguous
            if (temp != null) return ambiguous_method;
            // set temporary field
            temp = link;
        }
        return (temp == null) ? non_exists_method : temp;
    }
    
    public Iterator<MethodLink> getMethodInterfaceIteratorA()
    {
        return new MethodIterator(this, 1);
    }
    // this method will return methodIterator
    public Iterator<MethodLink> getMethodIteratorA()
    {
        return new MethodIterator(this);
    }
    
    // this method will check an object for ambiguous or non_exists
    public static boolean checkObject(AST ast, ClassObject object, Token token)
    {
        if (object == ClassTable.non_exists_object)
        {
            ast.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, 
                   token.source, token.line, token.position, object.name));
            return false;
        }
        else if (object == ClassTable.ambiguous_object)
        {
            ast.error_pool.add(new Flaw(Flaw.type.symbol_not_found, 
                   token.source, token.line, token.position, object.name));
            return false;
        }
        else
        {
            return true;
        }
    }
    
    // this is an method iterator for this object
    public static class MethodIterator implements Iterator<MethodLink>
    {
        private Iterator<MethodLink> current;
        private Iterator<ClassTable> base;
        private int left = 0;
        private ClassObject object;
        private MethodLink next = null;
        private BaseCondition condition;
    //    private Iterator<MethodLink> parent;
        private MethodIterator()
        {
        }
        
        public MethodIterator(ClassObject object)
        {
            this.object = object;
            // if table have parents
            // set current iterator
            this.current = object.method_list.iterator();
            // set parent iterator
            Iterator<ClassTable> base = this.base = object.getTable().parents.iterator();
            // 
            // set condition
            this.condition = new BaseCondition();
        }
        public MethodIterator(ClassTable table)
        {
            // set current iterator
    //        System.out.println("ITERATOR::" + table.object.method_list);
    //        System.out.println("ITERATOR::" + table.object.methods);
            this(table.object);
        }
        public MethodIterator(ClassObject object, int left)
        {
            this(object);
            this.left = left;
            
            for (int i = 0; i < this.left; i ++)
            {
                if (base.hasNext())
                {
                    base.next();
                }
                else
                {
                    break;
                }
            }
        }
        
        public MethodIterator(ClassTable table, int left)
        {
            this(table.object, left);
        }
        
        public static class BaseCondition implements MethodMap.Condition
        {
            // this is an cache for base condition
            private Map<String, Set<String>> map = new HashMap<>();
            // 
            public BaseCondition()
            {
            }
            public boolean checkMethod(MethodLink link)
            {
                Set set = this.map.get(link.name);
                // if got null, then add new set and return true
                if (set == null) 
                {
                    // create set
                    set = new HashSet<>();
                    // add descriptor
                    set.add(link.descriptor);
                    // return true
                    return true;
                }
                // else, then add descriptor to the set
                else
                {
                    if (set.contains(link.descriptor))
                    {
                        return false;
                    }
                    else
                    {
                        set.add(link.descriptor);
                        return true;
                    }
                }
            };
        }
        
        public MethodLink next()
        {
            if (this.hasNext())
            {
                count --;
                MethodLink link = this.next;
                this.next = null;
                return link;
            }
            else
            {
                throw new NoSuchElementException();
            }
        }
        
        public static int count = 0;
        
        public boolean hasNext()
        {
            //
            count ++;
            if (count == 100000) System.exit(0);
            // get current iterator
            Iterator<MethodLink> iter = this.current;
            Iterator<ClassTable> iter2 = this.base;
           //
            MethodLink link;
            // 
            if (this.next != null) return true;
            // 
            do
            {
                // that correspond condition
                while (iter.hasNext())
                    {
                        link = iter.next();
                        if (condition.checkMethod(link))
                        {
                            this.next = link;
                            this.current = iter;
                            return true;
                        }
                    }
                // check if base have next element
                if (iter2.hasNext())
                {
        //            System.out.println("had next");
                    ClassTable object = iter2.next();
        //            System.out.println(object.name);
        iter = new MethodIterator(object, this.left);
         //           System.out.println(iter.hasNext());
                }
                else
                {
                    return false;
                }
                //
            } while (!iter.hasNext());
            // 
            this.current = iter;
            return false;
        }
    }

    // this method return an field from from classobject and 
    // parents objects
    public FieldLink getFieldA(String name)
    {
        // if field in the class object, then return
        FieldLink link = this.object.fields.get(name);
        // if link is not null, then return
        if (link != null) return link;
        // 
        FieldLink temp = null;
        // if got null, then check from parent objects
        for (ClassTable i : this.parents)
        {
            // check field from classobjetc
            link = i.getFieldA(name);
            // check temp field 
            // if field not exists, then continue
            if (link == non_exists_field) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous
            if (link == ambiguous_field) return link;
            // check field access 
        if (!i.isSameHost(this.object))
        {
            if ((link.access & ACC_PRIVATE) > 0)
            {
                    continue;
            }
            else if (!i.isSamePackage(this.object))
            {
                if ((link.access & (ACC_PUBLIC | ACC_PROTECTED)) == 0)
                {
                    continue;
                }
            }
        }
            // if field is already got, then return ambiguous
            if (temp != null) return ambiguous_field;
            // set temporary field
            temp = link;
        }
        return (temp == null) ? non_exists_field : temp;
    }
    
    // this method return an field from classtable
    public FieldLink getField(String name)
    {
        FieldLink link = this.getFieldA(name);
        // 
        if (link == non_exists_field)
        {
            if (this.outer != null)
            {
                link = this.outer.getTable().getField(name);
            }
            else if (this.table != null)
            {
                return this.table.getField(name);
            }
        }
        // 
        {
            return link;
        }
    }
}
