package com.ejavac;import java.nio.file.*;
import java.util.*;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.*;
public class ImportTable
{/*
    // this is an map of the primitive types
    // this method returns an class or raises error if no class there
    public ClassObject getClass(ClassObject this_class, List<Token> list)
    {
        return getClass(this_class, list, 0, list.size(), true);
    }
    
    public ClassObject getClassA(ClassObject this_class, List<Token> list)
    {
        return getClass(this_class, list, 0, list.size(), false);
    }
    // this method checks if an class is valid for future use 
    public static boolean is_valid(ClassLink link, String packageName)
    {
        return      
                    (!link.is_broken) && 
                    
                    (((link.access & ACC_PRIVATE) == 0) && 
                    (((link.access & ACC_PUBLIC) > 0) ||
                    (packageName.equals(link.packageName))));
    }
    
    public ClassObject getClass(ClassObject this_class, List<Token> list, 
      int begin, int end, boolean inherit)
    {
        return null;
    }
    // this method returns an class name
    public String className(String name)
    {
        if (this.packageName.equals(""))
        {
            return name;
        }
        else
        {
            return this.packageName + "/" + name;
        }
    }*/
    
    
    // this flag indicates if file directory is listed
    public boolean is_listed = false;
    
    // this method will parse all source files from directory
    public void parseDir()
    {
        AST ast = this.ast;
        Path directory = this.directory;
        this.is_listed = true;
        // 
        for (Path path: Compiler.listClasspath(this.packageName))
        {
            ast.checkFile(path, directory);
        }
    }
    
    // this method return an classobject from import table
    public ClassObject getClass(String name)
    {
        // check for class from sources
        ClassObject link = this.source_class.get(name);
        if (link != null) return link;
        // check for class from import list
        link = this.import_class.get(name);
        if (link != null) return link;
        // check for class from this package
        this.parseDir();
        // check classobject
        if (packageName.equals(""))
        {
            link = this.ast.getClassByName(name);
        }
        else
        {
            link = this.ast.getClassByName(this.packageName + "/" + name);
        }
        if (link != null) return link;
        // check for class from import list
        ClassObject temp = null;
        for (AST.ClassPath i: this.import_list)
        {
            // get class from classpath
            temp = i.get(name);
            // 
            
       //    
      //     
            //
            if (temp != null) 
            {
                if (link == null)
                {
                    link = temp;
                }
                else
                {
                    return ClassTable.ambiguous_object;
                }
            }
        }
        // check link
        if (link == null) return ClassTable.non_exists_object;
        // else
        else
        {
            this.import_class.put(name, link);
            link.link1();
            return link;
        }
    }
    /*
    // this method return an method with descriptor from import table
    public MethodLink getMethod(String name, List<String> desc)
    {
        // get method 
        Set<FieldName> set = this.static_map.get(name);
        //
        MethodLink link = null;
        MethodLink temp = null;
        //
        if (set != null) 
        {
            for (FieldName i: set)
    {
            // check field from classobject
            link = i.object.getTable().getMethodA(i.name.value, desc);
            // check temp field 
            // if field not exists, then continue
            if (link == ClassTable.non_exists_method) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous
            if (link == ClassTable.ambiguous_method) return link;
            // if field is already got, then return ambiguous
            if (temp != null) return ClassTable.ambiguous_method;
            // set temporary field
            temp = link;
    }
        }
        temp = null;
        // iterate over static list
        for (ClassObject i: this.static_list)
        {
            
            // check field from classobjetc
            link = i.getTable().getMethodA(name, desc);
            // check temp field 
            // if field not exists, then continue
            if (link == ClassTable.non_exists_method) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous_method
            if (link == ClassTable.ambiguous_method) return link;
            // if field is already got, then return ambiguous
            if (temp != null) return ClassTable.ambiguous_method;
            // set temporary field
            temp = link;
        }
        //
        return (temp == null) ? ClassTable.non_exists_method : temp;
    }*/
    // this method return an field from import table
    public FieldLink getField(String name)
    {
        // iterate over static map
        Set<FieldName> set = this.static_map.get(name);
        //
        FieldLink link = null;
        FieldLink temp = null;
        //
        ClassObject object;
        if (set != null) 
        {
            for (FieldName i: set)
    {
            object = i.object;
            // check field from classobject
            link = object.getTable().getFieldA(i.name.value);
            // check temp field 
            // if field is private or not public
            if ((link.access & ACC_PRIVATE) != 0) continue;
            // if object package is not the same, then field must be public
            if ((!object.packageName.equals(this.packageName)) 
            && ((link.access & ACC_PUBLIC) == 0)) continue;
            // if field not exists, then continue
            if (link == ClassTable.non_exists_field) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous_field
            if (link == ClassTable.ambiguous_field) return link;
            // if field is already got, then return ambiguous
            if (temp != null) return ClassTable.ambiguous_field;
            // set temporary field
            temp = link;
    }
        }
        temp = null;
        // iterate over static list
        for (ClassObject i: this.static_list)
        {
            
            // check field from classobject
            link = i.getTable().getFieldA(name);
            // check temp field 
            // if field is private or not public
            if ((link.access & ACC_PRIVATE) != 0) continue;
            // if object package is not the same, then field must be public
            if ((!i.packageName.equals(this.packageName)) 
            && ((link.access & ACC_PUBLIC) == 0)) continue;
            // if field not exists, then continue
            if (link == ClassTable.non_exists_field) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous_field_field
            if (link == ClassTable.ambiguous_field) return link;
            // if field is already got, then return ambiguous
            if (temp != null) return ClassTable.ambiguous_field;
            // set temporary field
            temp = link;
        }
                
        return (temp == null) ? ClassTable.non_exists_field : temp;
    }
    
    // this method return an field and owner from import table
    public Pair<ClassObject,FieldLink> getFieldPair(String name)
    {
        // iterate over static map
        Set<FieldName> set = this.static_map.get(name);
        //
        
        FieldLink link = null;
        FieldLink temp = null;
        // 
        ClassObject object = this.ast.object_class;
        ClassObject temp_obj = null;
        
  //      System.out.println(set);
        //
        if (set != null) 
        {
            for (FieldName i: set)
    {
            object = i.object;
            // check field from classobject
            link = object.getTable().getFieldA(i.name.value);
            // check temp field 
            // if field is private or not public
            if ((link.access & ACC_PRIVATE) != 0) continue;
            // if object package is not the same, then field must be public
            if ((!object.packageName.equals(this.packageName)) 
            && ((link.access & ACC_PUBLIC) == 0)) continue;
            // if field not exists, then continue
            if (link == ClassTable.non_exists_field) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous_field
            if (link == ClassTable.ambiguous_field) return new Pair<>(object, link);
            // if field is already got, then return ambiguous
            if (temp != null) return new Pair<>(object, ClassTable.ambiguous_field);
            // set temporary field
            temp = link;
            temp_obj = object;
    }
        }
        
        // if got link, then return link
        if (temp != null)
        {
            return new Pair<>(temp_obj, temp);
        }
        
        temp = null;
        // iterate over static list
        for (ClassObject i: this.static_list)
        {
            // check field from classobject
            link = i.getTable().getFieldA(name);
            // check temp field 
            // if field is private or not public
            if ((link.access & ACC_PRIVATE) != 0) continue;
            // if object package is not the same, then field must be public
            if ((!i.packageName.equals(this.packageName)) 
            && ((link.access & ACC_PUBLIC) == 0)) continue;
            // if field not exists, then continue
            if (link == ClassTable.non_exists_field) continue;
            // if got field, that meet in the parent object many 
            // times, then return ambiguous_field_field
            if (link == ClassTable.ambiguous_field) return new Pair<>(i, link);
            // if field is already got, then return ambiguous
            if (temp != null) return new Pair<>(i, ClassTable.ambiguous_field);
            // set temporary field
            temp = link;
            object = i;
        }
                
        temp = (temp == null) ? ClassTable.non_exists_field : temp;
        return new Pair<>(object, temp); 
    }
    // this is an source name
    public String source;
    // this is an package name
    public String packageName = "";
    // this is an package cache
    public Compiler.Packages temp_pkg;
    // this is an ClassMap 
    public AST.ClassMap map;
    // this is an directory where class will be created
    public Path directory;
    // this is an list of class map
    //public List<List<Token>> cls_map;
    // this is an priority class map
    public AST.ClassMap cls_map_pkg;
    // this is an AST iterator
    public AST ast;
    // this is an classes from this source file
    public Map<String, ClassObject> source_class;
    // this is an imported classes 
    public Map<String, ClassObject> import_class;
    // this is an classes for static object
    public Set<ClassObject> static_list;
    // this is class map for static object
    public Map<String, Set<FieldName>> static_map;
    // this is an import path
    public Set<AST.ClassPath> import_list;
    // this is an cache class map
    // public AST.ClassMap cache;
    // this is an constructor
    private ImportTable(){};
    public ImportTable(AST ast)
    {
       //     cls_map = new ArrayList<>();
       //     cache = new AST.ClassMap();
        source_class = new HashMap<>();
        import_class = new HashMap<>();
        static_list = new HashSet<>();
        import_list = new HashSet<>();
        static_map = new HashMap<>();
        this.ast = ast;
    }
    // this method adds an field import to the static map
    public void addFieldName(String nick, Token token, ClassObject object)
    {
        // check if an list on static map there
        Set list = (Set) static_map.get(nick);
        // if got null, then create new list
        if (list == null)
        {
            list = new HashSet<>();
            static_map.put(nick, list);
        }
        // add classobject to the list
        list.add(new FieldName(object, token));
    }
}
    
    
