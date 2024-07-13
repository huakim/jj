package com.ejavac;import java.util.*;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.*;
import java.nio.file.*;
import java.nio.*;
import java.io.File;
import java.io.*;

public class AST
{  
    // this is an name of the object class
//    public static String object_name = "java/lang/Object";

//    public AnnotationVisitor note_visit = new AnnotationVisitor(this){};

    public static void main(String ... args) throws Throwable
    {
        AST ast = new AST();
        for (String i: args)
        {
            ast.compileSource(i);
        }
    }
      
    public void compileSource(String source)
    {
        
        File source_file = new File(source);
        source_file = new File(source_file.getAbsolutePath());
        
        if (!source_file.exists())
        {
            System.out.print("File is not exists: ");
            System.out.println(source_file);
            return;
        }
        
        else if (source_file.isDirectory())
        {
            System.out.print("Is directory:");
            System.out.println(source_file);
            return;
        }
        
        List list = Compiler.fileToAST(
                 source_file.getAbsolutePath());
        
     //   System.out.println(list);
        
        ImportTable table = this.checkAST(list, 
            source_file.getParentFile().toPath());
        
        Iterator<ClassObject> iter = table.source_class.values().iterator();
        
        ArrayList<ClassObject> obj = new ArrayList<ClassObject>();
        
        ClassObject next;
        
        while (iter.hasNext())
        {
            next = iter.next();
            if (next.link_stage == 0)
            {
                obj.add(next);
            }
        }
        
        list = this.error_pool;
        
        if (list.size() > 0)
        {
            for(Object ii: list)
            {
                System.out.println(ii);
            }
            return ;
        }

        // iterate over list
        for (ClassObject i : obj)
        {
            i.link2();
        }
        //
        if (list.size() > 0)
        {
            for(Object ii: list)
            {
                System.out.println(ii);
            }
            return;
        }
        // iterate for this classmap
        iter = this.classes.new ClassMapIterator();
        
        while (iter.hasNext())
        {
            next = iter.next();
            if (next.link_stage > 0)
            {
                next.link2();
                 if (list.size() > 0)
        {
            for(Object ii: list)
            {
                System.out.println(ii);
            }
            return;
        }
                if (next.link_stage == 2)
                {
                    if (!this.compileObject(next))
                    {
                        for(Object ii: list)
            {
                System.out.println(ii);
            }
                    }
                }
            }
        }
        
       
    }
      
    public boolean compileObject(ClassObject object)
    {
        
        ClassWriter cw = Compiler.compileClassObject(object);
        Path path = object.table.directory.resolve(object.headName+
    ".class");
    try
    {
   FileOutputStream out = new FileOutputStream(path.toFile());
   byte [] d = cw.toByteArray();
    out.write(d, 0, d.length);
}
    catch (Exception e)
    {
        this.error_pool.add("error while writing "+object.name+": "+path);
        return false;
    }
    return true;
    }
      
    public static class ClassMap
    {
        public static ClassMap null_map = new ClassMap();
        public HashMap<String, ClassMap> map = new HashMap<>();
        public ClassObject value;
        public ClassMap parent;
        
        public class ClassMapIterator implements Iterator<ClassObject>
        {
            public Iterator<ClassObject> iter = null;
            public Iterator<ClassMap> map_iter;
            public ClassObject next;
            public ClassMapIterator()
            {
                this.map_iter = map.values().iterator();
                this.iter = new HashMap<String, ClassObject>().values().iterator();
                this.next = value;
            }
            public ClassObject next()
            {
                if (hasNext())
                {
                    ClassObject n = this.next;
                    this.next = null;
                    return n;
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }
            public boolean hasNext()
            {
                if (this.next == null)
                {
                    Iterator<ClassObject> iter = this.iter;
                    Iterator<ClassMap> map_iter = this.map_iter;
                    while (!iter.hasNext())
                    {
                        if (map_iter.hasNext())
                        {
                            iter = map_iter.next().
                                    new ClassMapIterator();
                        }
                        else
                        {
                            return false;
                        }
                    }
                    this.iter = iter;
                    this.next = iter.next();
                    return true;
                }
                else
                {
                    return true;
                }
            }
        }
        
        public ClassMap get(Token[] value)
        {
            ClassMap l = this;
            ClassMap ret;
            // iterate for all hashmap
            for (Token ii : value)
            {
                String i = ii.value; 
                ret = l.map.get(i);
                // if got null, then return null_map
                if (l == null)
                {
                    ret = new ClassMap(l);
                    l.map.put(i, ret);
                }
                l = ret;
            }
            return l;
        } 
        
        public ClassMap()
        {
            this.parent = null_map;
        }
        
        public ClassMap(ClassMap parent)
        {
            this.parent = parent;
        }
        
        public ClassMap put(Token [] name, ClassObject value)
        {
            ClassMap l = this;
            ClassMap ret;
            for (Token ii : name)
            {
                String i = ii.value;
                ret = l.map.get(i);
                // if got null, then put one new classmap
                if (ret == null)
                {
                    ret = new ClassMap(l);
                    l.map.put(i, ret);
                }
                l = ret;
            }
            l.value = value;
            return l;
        }
        public ClassMap get(Token ii)
        {
            String i = ii.value;
            ClassMap l = this;
            ClassMap ret;
            // iterate for all hashmap
        //    for (String i : value)
        //    {
                ret = l.map.get(i);
                // if got null, then return null_map
                if (l == null)
                {
                    ret = new ClassMap(l);
                    l.map.put(i, ret);
                }
                l = ret;
          //  }
            return l;
        } 
        public ClassMap get(String[] value)
        {
            ClassMap l = this;
            ClassMap ret;
            // iterate for all hashmap
            for (String i : value)
            {
                ret = l.map.get(i);
                // if got null, then return null_map
                if (ret == null)
                {
                    ret = new ClassMap(l);
                    l.map.put(i, ret);
                }
                l = ret;
            }
            return l;
        } 
        
        public ClassMap get(String[] value, int begin, int end)
        {
            ClassMap l = this;
            ClassMap ret;
            // iterate for all hashmap
            for (; begin < end; begin ++)
            {
                String i = value[begin];
                ret = l.map.get(i);
                // if got null, then return null_map
                if (ret == null)
                {
                    ret = new ClassMap(l);
                    l.map.put(i, ret);
                }
                l = ret;
            }
            return l;
        } 
        
        public ClassMap put(String [] name, ClassObject value)
        {
            ClassMap l = this;
            ClassMap ret;
            for (String i : name)
            {
                ret = l.map.get(i);
                // if got null, then put one new classmap
                if (ret == null)
                {
                    ret = new ClassMap(l);
                    l.map.put(i, ret);
                }
                l = ret;
            }
            l.value = value;
            return l;
        }
        public ClassMap get(String i)
        {
            ClassMap l = this;
            ClassMap ret;
            // iterate for all hashmap
        //    for (String i : value)
        //    {
                ret = l.map.get(i);
                // if got null, then return null_map
                if (ret == null)
                {
                    ret = new ClassMap(l);
                    l.map.put(i, ret);
                }
                l = ret;
          //  }
            return l;
        } 
        public String toString()
        {
            return "{list: " + map + "; value: " + ((value == null) ? "!null" : value.name) + " }";
        }
    }
    
    // this is an anonumous count map
    public Map<String, Integer> count_map = new HashMap();
    
    // this method will return an count
    public int getCount(String str)
    {
        int i = (int) count_map.getOrDefault(str, 0);
        i++;
        count_map.put(str, i);
        return i;
    }
    
    // this is an class name map
    public ClassMap classes = new ClassMap();
    
    // this is an class Object pool
    public List<ClassObject> object_pool = new ArrayList<>();
    
    // this is an remark pool
    public List error_pool = new LinkedList<>();
        
    // this method creates an package-info file
    public void createSource(List annotations)
    {
        
    }
    
    // this method gets an class by it's package name
    public ClassObject getClassByName(String classname)
    {
        // split classname to the string array
        String [] name = classname.split("/");
        // try to get classobject from classmap
        ClassMap map = classes.get(name);
        ClassObject object = map.value;
        
    //    System.out.println(classname);
    //    System.out.println(map.value);
        // if classmap value is null, then try to get class from system
        if (object == null)
        {
            ClassLink link = Compiler.getClass(name);
            // check if classlink is broken, if yes, then return null
            if (link.is_broken)
            {
                return null;
            }
            // else, create object and return
            object = new ClassObject(link, this);
            map.value = object;
        }
         
         object.link1();
         
        return object;
    }
    // this is an set of checked source files
    public Set<String> checked = new HashSet<>();
    // this is an method for check source file
    public void check_source_file(Path name)
    {
        // get path name
        String path_name = name.toString();
        
   //     //System.out.println(path_name);
        // get path_name length
        int length = path_name.length();
        // if path name contains .java extension, then process
        if (length > 5)
        {
            // get index
            int index = path_name.lastIndexOf(".");
            // check if length - index is 5
            if ((length - index) == 5)
            { 
                if (path_name.substring(index).equals(".java"))
                {
                    this.checkAST(Compiler.fileToAST(path_name), name.getParent());
                };
            }
        } 
    }
    
    // this is an method for check package 
    public void check_package(String packageName)
        {
        //    if (!checked.contains(packageName))
            {
      //          checked.add(packageName);
                for (Path i : Compiler.listClasspath(packageName))
                {
           //         //System.out.println(i);
                    //
                    this.check_source_file(i);
                }
            }
        }
    
    // this method returns an inner class from classlink
  /*  public InnerClassLink getInnerClass(ClassLink link, List<Token> name, int begin, int end)
    {
        return getInnerClass(  link,  name,  begin,  end,  true);
    }
    */
    
    
    // continue here
    // this function must be remarked
    public InnerClassLink getInnerClass(ClassObject link, List<Token> name, int begin, int end, ImportTable temp_table)//, boolean is_public)
    {
        String packageName = temp_table.packageName;
        Token token = name.get(begin++);
     //   String packageName = link.packageName;
        String str = link.name;
        // this is an map for innerclasslink
        Map map = (Map)link.innerClasses.get(str);
        // if map is null, then return null
        if (map == null)
        
            {
                error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, token.value));
                return null;
            }
        // this is an innerclasslink
        InnerClassLink ret;
     //   InnerClassLink past = null;
        // get first innerclasslink
        ret = (InnerClassLink) map.get(token.value);
        // if ret is null, then return null
        if (ret == null)
            {
                error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, token.value));
                return null;
            }
        // iterate until begin lesses that end
        while (begin < end)
        {
           //  check if public is true
            if (ret.packageName.equals(packageName))
            {
                if ((ret.access & ACC_PUBLIC) == 0)
                {
                    error_pool.add(new Flaw(Flaw.type.n_public_class, token.source, token.line, token.position, token.value, str));
                    return null;
                }
            }
            else
            {
                if ((ret.access & ACC_PRIVATE) > 0)
                {
                    error_pool.add(new Flaw(Flaw.type.private_class, token.source, token.line, token.position, token.value, str));
                    return null;
                }
            } 
      //      past = ret;
            str = ret.name;
            // get map object 
            map = (Map) link.innerClasses.get(str);
            // if map is null, then return null
            if (map == null) 
            {
                error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, token.value));
                return null;
            }
            // get next innerclasslink
            token = name.get(begin++);
            ret = (InnerClassLink) map.get(token.value);
            // if ret is null, then return null
            if (ret == null) 
            {
                error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, token.value));
                return null;
            }
        }
        if (!ret.packageName.equals(packageName))
        {
                if ((ret.access & ACC_PUBLIC) == 0)
                {
                    error_pool.add(new Flaw(Flaw.type.n_public_class, token.source, token.line, token.position, token.value, str));
                    return null;
                }
        }
        else
        {
                if ((ret.access & ACC_PRIVATE) > 0)
                {
                    error_pool.add(new Flaw(Flaw.type.private_class, token.source, token.line, token.position, token.value, str));
                    return null;
                }
        }
    /*    if ((ret.access & ACC_STATIC) == 0)
        {
            if (past != null)
            {
                if (!past.packageName.equals(packageName))
                {
                    if ((ret.access & ACC_PUBLIC) == 0)
                    {
                        error_pool.add(new Flaw(Flaw.type.n_public_class, temp_table.source, token.line, token.position, str, past.name));
                        return null;
                    }
                }
                else
                {
                    if ((ret.access & ACC_PRIVATE) > 0)
                    {
                        error_pool.add(new Flaw(Flaw.type.private_class, temp_table.source, token.line, token.position, str, past.name));
                        return null;
                    }*//*
                }
            }
        } */
        if (ret == null) 
            {
                error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, token.value));
            }
        return ret;
    }
    
        
    public static interface ClassPath
    {
        ClassObject get(String name);
        public static ClassPath newClassPath(String directory, AST.ClassMap map1, ImportTable table, List<Path> ... paths)
        {
            // get package name 
            String packageName = table.packageName;
            // get ast 
            AST ast = table.ast; 
            //
            return new ClassPath()
            {
                
                public ClassObject get(String name)
                {
                    
                //   System.out.println(name);
                    // try with classmap
                    AST.ClassMap map = map1.get(name);
                    // 
                    String classname = directory + "/" + name;
                    //
                    Path path = Compiler.searchForFileFromClasspath(classname + ".java");
                    // if path here
                    if (path != null) 
                    {
                        ast.checkAST(Compiler.fileToAST(path.toString()), path.getParent());
                    }
                    // if map.value is not null
                    if (map.value != null) return map.value;
                    else
                    {
                        // create classname
                      //  String classname = directory + "/" + name;
                      ClassLink link = Compiler.Cache.broken_link;
                        // try to find class 
                        for (List<Path> list: paths)
                        {
                   //         System.out.println(classname);
                   //         System.out.println(list);
                            link = Compiler.getClassFromList(classname, list);
                            // 
                            if (link !=null)
                            {
                  //              System.out.println(link.name);
                                break;
                            }
                        }
                        //
                  //      System.out.println(directory);
                        
                   //     for (Object i: paths)
                   //     {
                   //         System.out.println(i);
                   //     }
                        // if got broken link, then return null
                        if (link.is_broken) return null;
                        // else, check link access
                        if (
                    ((link.access & ACC_PRIVATE) == 0) &&
                    (((link.access & ACC_PUBLIC) > 0) ||
                    (packageName.equals(link.packageName)))
                        )
                        {
                            map.value = new ClassObject(link, ast);
                            return map.value;
                        }
                        else
                        {
                            return null;
                        }
                    }
                };
            };
        }
        public static ClassPath newClassPath(ClassObject object, AST.ClassMap map1, ImportTable table, List<Path> ... paths)
        {
            // try to get class map
   //         Map classes1 = object.innerClasses.get(object.name);
            // if got null, then create new array
    //        if (classes1 == null) classes1 = Compiler.Cache.empty_map;
            // get package name
            String packageName = table.packageName;
            AST ast = table.ast;
            // return object
            return new ClassPath()
            {
                // this is an cache
                public Map classes = object.innerClasses.get(object.name);
                {
                    if (classes == null)
                    {
                        classes = Compiler.Cache.empty_map;
                    }
                }
                public AST.ClassMap map = map1;
                // 
                public ClassObject get(String name)
                {
                    InnerClassLink innerlink = (InnerClassLink)classes.get(name);
                    // chech for access, if is private or null, then return null
                    if (
                    ((innerlink.access & ACC_PRIVATE) == 0) &&
                    (((innerlink.access & ACC_PUBLIC) > 0) ||
                    (packageName.equals(innerlink.packageName)))
                    )
                    {
                        // try to get class from map with same name
                        map = map.get(name);
                        // check value, if null, then try to get class from classpath
                        if (map.value == null)
                        {
                            // get class link
                            ClassLink link = Compiler.getClassFromList(innerlink.name, paths);
                            // check link access
                            if (link.is_broken) return null;
                    else if (
                    ((link.access & ACC_PRIVATE) == 0) && 
                    (((link.access & ACC_PUBLIC) > 0) ||
                    (packageName.equals(link.packageName)))
                    )
                            {
                                // then return classobject
                                map.value = new ClassObject(link, ast);
                                return map.value;
                            }
                            else
                            {
                                // 
                                return null;
                            }
                        }
                        else
                        {
                            return map.value;
                        }
                    }
                    else
                    {
                        return null;
                    }
                };
            };
        }
    }
    
        
    
    // this is an java lang standart class path
    public ClassPath java_lang;
    {
        AST this_ = this;
        java_lang = new ClassPath()
    {
        
        public ClassMap map1 = classes.get("java").get("lang");
        public List list = Compiler.packages.getPackage("java").getPackage("lang").getList();
        public AST ast = this_;
        
            public ClassObject get(String name)
            {
                
         //       System.out.println(name);
                
                    // try with classmap
                    AST.ClassMap map = map1.get(name);
                    // 
                    String classname =  "java/lang/" + name;
                    //
               //     Path path = Compiler.searchForFileFromClasspath(classname + ".java");
                    // if path here
             //       if (path != null) 
             //       {
             //           ast.parseAST(Compiler.fileToAST(path.toString()), path.getParent());
             //       }
                    // if map.value is not null
                    if (map.value != null) 
                    {
                        ClassObject link = map.value;
                        if ((link.access & ACC_PUBLIC) > 0)
                        {
                            return map.value;
                        }
                        else
                        {
                            return null;
                        }
                    }
                    else
                    {
                        // create classname
                      //  String classname = directory + "/" + name;
                        // try to find class 
                        ClassLink link = Compiler.getClassFromList(classname, list);
                        // check link
                   //     System.out.println(link);
                        // if got broken link, then return null
                        if (link.is_broken) return null;
                        // 
                        map.value = new ClassObject(link, ast);
                        // else, check link access
                        if ((link.access & ACC_PUBLIC) > 0)
                        {
                 //           System.out.println("// are you okay?");
                            return map.value;
                        }
                        else
                        {
                            return null;
                        }
                    }
            };
    };
    }
    
    // this method creates an new classpath
    public ClassPath importClassPath(List <Token> name, ImportTable table)
    {
        return importClassPath(name, table, 0, name.size());
    }
    
    public ClassPath importClassPath(List <Token> name, ImportTable temp_table, int start_index, int end)
    {
                //
        String stop_package = null;
        int stop_begin = start_index;
        String packageName = temp_table.packageName;
        ClassMap clsmap = this.classes;
        ClassObject object;
        InnerClassLink innerlink;
        //
        Token token = name.get(start_index);
        String str = token.value;
        Path path;
     //   ////System.out.println("TOKEN::" +  token);
        // try import from system
        List<Path> list = Compiler.Cache.empty_list;
        // set length
//        int end = name.size();
        // get package
        Compiler.Packages pkg = Compiler.packages;
        
     //   ////System.out.println("PKG::"+pkg);
        // 
        int begin = start_index + 1;
        // 
        ClassLink link;
        while (begin < end)
        {
            pkg = pkg.getPackage(str);
            
            token = name.get(begin);
            str = token.value;
            // if pkg is package, then process with it
            if (pkg.is_package)
            {
                stop_package = pkg.name;
                stop_begin = begin;
                // get classlink
                link = Compiler.getClassFromList(pkg.name + "/" + str, pkg.getList());
                // if link broken, then continue
                if (link.is_broken)
                {
                    begin ++;
                    continue;
                }
                else if ((link.access & ACC_PUBLIC) > 0)
                {
                //    ////System.out.println("help me");
                //    ////System.out.println("i'm on the way:: " + (begin + 1) + " " +  end);
                    // if public, then process with this class
                    begin += 1;
                    ClassMap m = this.classes;
                    for (int i = 0; i < begin; i ++)
                    {
                        m = m.get(name.get(i).value);
                    }
                    
   //                 ////System.out.println("i did it");
     //               //System.out.println(m);
                    // set class to the map
                    m.value = new ClassObject(link, this);
                    //
                    if ((begin) == end) return ClassPath.newClassPath(m.value, m, temp_table, list, Compiler.classpath);
                    
                    begin += 1;
                    
                    // get inner class
                    innerlink = this.getInnerClass(new ClassObject(link, 
                         this), name, begin, end, temp_table);
                    if (innerlink == null) return null;
                    // get classlink
                   // for (int i = begin; i < end; i ++)
                   // {
                        m = m.parent.get(innerlink.name);
                   // }
                    // get inner class link
                    link = Compiler.getClassFromList(innerlink.name, pkg.getList());
                    // if link is broken, then return none
                    if (link.is_broken) return null;
                    // else, check link access
                    if ((link.access & ACC_PUBLIC) > 0)
                    {
                        // if is public, then return link
                        m.value = new ClassObject(link, this);
                        return ClassPath.newClassPath(m.value, m, temp_table, list, Compiler.classpath);
                    }
                    return null;
                }
            }
            
            begin ++;
        }
        // get package
        pkg = pkg.getPackage(str);
        // if got package, then return directory ClassPath
        if (pkg.is_package)
        {
            for (Token i: name)
            {
                // set class map
                clsmap = clsmap.get(i.value);
            }
            // return classpath
            return ClassPath.newClassPath(pkg.name, clsmap, temp_table, pkg.getList(), Compiler.classpath);
        }
        
        // try with classpath
        StringJoiner joiner = new StringJoiner("/");
        String classname = null;
        if (stop_package == null)
        {
            token = name.get(start_index);
            str = token.value;
            joiner.add(str);
            classname = str;
            clsmap = this.classes.get(str);
            begin = start_index + 1;
        }
        else
        {
            begin = stop_begin;
            token = name.get(begin);
            str = token.value;
            joiner.add(stop_package);
            joiner.add(str);
            classname = joiner.toString();
     //       //System.out.println(classname);
            clsmap = this.classes.get(classname.split("/"));
            begin ++;
        }
        
    //    //System.out.println(classname);
        // 
        // temporary class map
        ClassMap tempmap;
        
        // if these is not directory str
        
        while (begin < end)
        {
            if (Compiler.searchForDirectoryFromClasspath(classname) == null)
        {
         //   error_pool.add(new Flaw(Flaw.type.pkg_n_exists, temp_table.source, token.line, token.position, token.value));
     //       //System.out.println("error here");
           // return null;
           break;
        }
            // get token
            token = name.get(begin++);
            // get token name
            str = token.value;
            // get directory path
            
            // add str to joiner
            joiner.add(str);
            // get classname 
            classname = joiner.toString();
            // check if here an source file 
            path = Compiler.searchForFileFromClasspath(classname + ".java");
            
   //         //System.out.println("got path::" + path.toString());
            // get source path
            if (path != null) 
          //  {
                 this.checkAST(Compiler.fileToAST(path.toString()), path.getParent());
         //   }
            // check if there an classobject
            clsmap = clsmap.get(str);
            // if got classmap, then continue
            if (clsmap.value != null)
            {
                // if begin == end, then just return classobject
                if (begin == end)
                {
                    return ClassPath.newClassPath(clsmap.value, clsmap, temp_table, Compiler.classpath);
                };
                // get inner classlink
                innerlink = this.getInnerClass(clsmap.value, name, begin, end, temp_table);
                // if innerlink is null, then return null
                if (innerlink == null) return null;
                // else, get classmap
                tempmap = clsmap.parent.get(innerlink.name);
    //            for (int i = begin; i < end; i ++)
     //           {
         //           tempmap = tempmap.get(name.get(i).value);
       //         }
                // if tempmap is an class, then return
                if (tempmap.value != null)
                {
                    object = tempmap.value;
                    // check if package names are equal
                    if (object.packageName.equals(packageName))
                    {
                        // if yes, then return object
                        return ClassPath.newClassPath( object, tempmap, temp_table, Compiler.classpath);
                    }
                    // chekc if access is public, if yes, then return
                    if ((object.access & ACC_PUBLIC) > 0)
                    {
                        return ClassPath.newClassPath( object, tempmap, temp_table, Compiler.classpath);
                    }
                    Token t = name.get(end-1);
                    error_pool.add(new Flaw(Flaw.type.n_public_class, t.source, t.line, t.position, t.value));
                        
                    return null;
                }
                else
                {
                    // if tempmap is not an class, then get class
                    link = Compiler.getClassFromList(innerlink.name, Compiler.classpath);
                    // if link is broken, then return null
                    if (link.is_broken)
                    {
                        Token t = name.get(end-1);
                        error_pool.add(new Flaw(Flaw.type.cls_broken, t.source, t.line, t.position, t.value, innerlink.name));
                        return null;
                    }
                    else 
                        {
                        tempmap.value = object = new ClassObject(link, this);
                        if (((link.access & ACC_PUBLIC) == 0) && (!link.packageName.equals(packageName)))
                    {
                        Token t = name.get(end-1);
                        error_pool.add(new Flaw(Flaw.type.cls_broken, t.source, t.line, t.position, t.value, innerlink.name));
                        return null;
                    }
                    else
                    {
                   //     tempmap.value = object = new ClassObject(link);
                        return ClassPath.newClassPath( object, tempmap, temp_table, Compiler.classpath);
                    }
                        }
                }
            }
            // if not class here
            else
            {
                // get class link
                link = Compiler.getClassFromList(classname, Compiler.classpath);
                // if link is broken, then continue
                if (link.is_broken)
                {
                    continue;
                }
                else if (((link.access & ACC_PUBLIC) == 0) && (!link.packageName.equals(packageName)))
                {
                    clsmap.value = new ClassObject(link, this);
                    continue;
                }
                else
                {
                    object = new ClassObject(link, this);
                    clsmap.value = object;
                    // if begin == end, then return class link
                    if (begin == end)
                    {
                        return ClassPath.newClassPath( object, clsmap, temp_table, Compiler.classpath);
                    }
                    // else, get inner class and classlink
                    else
                    {
                        // get inner class link
                        innerlink = this.getInnerClass(new ClassObject(link, 
                        this), name, begin, end, temp_table);
                        // if innerlink is null, then return null
                        if (innerlink == null) return null;
                        // get inner class name
                        link = Compiler.getClassFromList(innerlink.name, Compiler.classpath);
                        // get classmap
                        tempmap = clsmap.parent.get(innerlink.name);
                  //      for (int i = begin; i < end; i ++)
                  //      {
                  //          tempmap = tempmap.get(name.get(i).value);
                  //      }
                        // 
                        if (link.is_broken)
                        {
                        
                            Token t = name.get(end-1);
                            error_pool.add(new Flaw(Flaw.type.cls_broken, t.source, t.line, t.position, t.value, innerlink.name));
                            return null;
                        }
                        else if (((link.access & ACC_PUBLIC) == 0) && (!link.packageName.equals(packageName)))
                        {
                            tempmap.value = new ClassObject(link, this);
                            Token t = name.get(end-1);
                            error_pool.add(new Flaw(Flaw.type.cls_broken, t.source, t.line, t.position, t.value, innerlink.name));
                            return null;
                        }
                        else
                        {
                            tempmap.value = object = new ClassObject(link, this);
                            return ClassPath.newClassPath( object, tempmap, temp_table, Compiler.classpath);
                        }
                    }
                }
            }
        }
        // check for directory
        path = Compiler.searchForDirectoryFromClasspath(classname);
        // if directory is not exists, then return null
        if (path == null)
        {
            // get token
            token = (Token)name.get(0);
            // get package name with dots
            joiner = new StringJoiner(".");
            // iterate with name
            for (Object t :name)
            {
                joiner.add(((Token) t).value);
            }
            // add error to the error pool
            error_pool.add(new Flaw(Flaw.type.pkg_n_exists, token.source, token.line, token.position, joiner.toString()));
            return null;
        }
        // else, return
        else
        {
            return ClassPath.newClassPath( classname, clsmap, temp_table, Compiler.classpath);
        }
    }
    
    // this function imports an class from system
    public ClassObject importClass(List<Token> name, ImportTable temp_table)
    {
        return importClass(name, temp_table, 0, name.size());
    }
    public ClassObject importClass(List<Token> name, ImportTable temp_table, int start_index, int end)
    {
        //
        String stop_package = null;
        int stop_begin = start_index;
        // get package name 
        String packageName = temp_table.packageName;
        // get classmap
        ClassMap clsmap = this.classes;
        ClassObject object;
        InnerClassLink innerlink;
        List<Path> list;
        //
        Token token = name.get(start_index);
        String str = token.value;
        Path path;
     //   //System.out.println("TOKEN::" +  token);
        // try import from system
        // set length
//        int end = name.size();
        // get package
        Compiler.Packages pkg = Compiler.packages;
        
     //   //System.out.println("PKG::"+pkg);
        // 
        int begin = start_index + 1;
        // 
        ClassLink link;
        while (begin < end)
        {
            pkg = pkg.getPackage(str);
            
            token = name.get(begin);
            str = token.value;
            // if pkg is package, then process with it
            if (pkg.is_package)
            {
                stop_package = pkg.name;
                stop_begin = begin;
                // get list
                list = pkg.getList();
                // get classlink
                link = Compiler.getClassFromList(pkg.name + "/" + str, list);
                // if link broken, then continue
                if (link.is_broken)
                {
                    begin ++;
                    continue;
                }
                else if ((link.access & ACC_PUBLIC) > 0)
                {
                //    //System.out.println("help me");
                //    //System.out.println("i'm on the way:: " + (begin + 1) + " " +  end);
                    // if public, then process with this class
                    begin ++;
                    ClassMap m = this.classes;
                    for (int i = 0; i < begin; i ++)
                    {
                        m = m.get(name.get(i).value);
                    }
                    
   //                 //System.out.println("i did it");
     //               //System.out.println(m);
                    // set class to the map
                    m.value = new ClassObject(link, this);
                    //
                    
                    if ((begin) == end)
                    {
                         return m.value;
                    }
                    // get inner class
                    innerlink = this.getInnerClass(new ClassObject(link
                    , this), name, begin, end, temp_table);
                    if (innerlink == null) return null;
                    // get classlink
                   // for (int i = begin; i < end; i ++)
                   // {
                        m = m.parent.get(innerlink.name);//name.get(i).value);
                   // }
                    // get inner class link
                    link = Compiler.getClassFromList(innerlink.name, list, Compiler.classpath);
                    // if link is broken, then return none
                    if (link.is_broken) return null;
                    // else, check link access
                    if ((link.access & ACC_PUBLIC) > 0)
                    {
                        // if is public, then return link
                        m.value = new ClassObject(link, this);
                        return m.value;
                    }
                    return null;
                }
            }
            
            begin ++;
        }
        
        // try with classpath
        StringJoiner joiner = new StringJoiner("/");
        String classname = null;
        if (stop_package == null)
        {
            token = name.get(start_index);
            str = token.value;
            joiner.add(str);
            classname = str;
            clsmap = this.classes.get(str);
            begin = start_index + 1;
        }
        else
        {
            begin = stop_begin;
            token = name.get(begin);
            str = token.value;
            joiner.add(stop_package);
            joiner.add(str);
            classname = joiner.toString();
     //       //System.out.println(classname);
            clsmap = this.classes.get(classname.split("/"));
            begin ++;
        }
        
    //    //System.out.println(classname);
        // 
        // temporary class map
        ClassMap tempmap;
        
        // if these is not directory str
        
        while (begin < end)
        {
            if (Compiler.searchForDirectoryFromClasspath(classname) == null)
        {
            error_pool.add(new Flaw(Flaw.type.pkg_n_exists, token.source, token.line, token.position, token.value));
     //       //System.out.println("error here");
            return null;
        }
            // get token
            token = name.get(begin++);
            // get token name
            str = token.value;
            // add str to joiner
            joiner.add(str);
            // get classname 
            classname = joiner.toString();
            // check if here an source file 
            path = Compiler.searchForFileFromClasspath(classname + ".java");
            
   //         //System.out.println("got path::" + path.toString());
            // get source path
            if (path != null) this.checkAST(Compiler.fileToAST(path.toString()), path.getParent());
            // check if there an classobject
            clsmap = clsmap.get(str);            // if got classmap, then continue
            if (clsmap.value != null)
            {
                // if begin == end, then just return classobject
                if (begin == end) return clsmap.value;
                // get inner classlink
                innerlink = this.getInnerClass(clsmap.value, name, begin, end, temp_table);
                // if innerlink is null, then return null
                if (innerlink == null) 
                {
        //      /*      System.out.println("errorr hereeeeee");
        //            System.out.println(clsmap.value.name);
        //            System.out.println(clsmap.value.getInnerClass(((Token)name.get(begin)).value));
        //            System.out.println(((Token)name.get(begin)).value);
        //            * */
                    return null;
                }
                // else, get classmap
                tempmap = clsmap.parent.get(innerlink.name);
          //      for (int i = begin; i < end; i ++)
          //      {
          //          tempmap = tempmap.get(name.get(i).value);
          //      }
                // if tempmap is an class, then return
                if (tempmap.value != null)
                {
                    object = tempmap.value;
                    // check if package names are equal
                    if (object.packageName.equals(packageName))
                    {
                        // if yes, then return object
                        return object;
                    }
                    // chekc if access is public, if yes, then return
                    if ((object.access & ACC_PUBLIC) > 0)
                    {
                        return object;
                    }
                    Token t = name.get(end-1);
                    error_pool.add(new Flaw(Flaw.type.n_public_class, t.source, t.line, t.position, t.value));
                        
                    return null;
                }
                else
                {
                    // if tempmap is not an class, then get class
                    link = Compiler.getClassFromList(innerlink.name, Compiler.classpath);
                    // if link is broken, then return null
                    if (link.is_broken)
                    {
                        Token t = name.get(end-1);
                        error_pool.add(new Flaw(Flaw.type.cls_broken, t.source, t.line, t.position, t.value, innerlink.name));
                        return null;
                    }
                    else if (((link.access & ACC_PUBLIC) == 0) && (!link.packageName.equals(packageName)))
                    {
                        Token t = name.get(end-1);
                        error_pool.add(new Flaw(Flaw.type.cls_broken, t.source, t.line, t.position, t.value, innerlink.name));
                        return null;
                    }
                    else
                    {
                        tempmap.value = new ClassObject(link, this);
                        return tempmap.value;
                    }
                }
            }
            // if not class here
            else
            {
                // get class link
                link = Compiler.getClassFromList(classname, Compiler.classpath);
                // if link is broken, then continue
                if (link.is_broken)
                {
                    continue;
                }
                else if (((link.access & ACC_PUBLIC) == 0) && (!link.packageName.equals(packageName)))
                {
                    continue;
                }
                else
                {
                    object = new ClassObject(link, this);
                    clsmap.value = object;
                    // if begin == end, then return class link
                    if (begin == end)
                    {
                        return object;
                    }
                    // else, get inner class and classlink
                    else
                    {
                        // get inner class link
                        innerlink = this.getInnerClass(new ClassObject(link
                          , this), name, begin, end, temp_table);
                        // if innerlink is null, then return null
                        if (innerlink == null) return null;
                        // get inner class name
                        link = Compiler.getClassFromList(innerlink.name, Compiler.classpath);
                        // get classmap
                        tempmap = clsmap.parent.get(innerlink.name);
             //           for (int i = begin; i < end; i ++)
             //           {
             //               tempmap = tempmap.get(name.get(i).value);
             //           }
                        // 
                        if (link.is_broken)
                        {
                        
                            Token t = name.get(end-1);
                            error_pool.add(new Flaw(Flaw.type.cls_broken, t.source, t.line, t.position, t.value, innerlink.name));
                            return null;
                        }
                        else if (((link.access & ACC_PUBLIC) == 0) && (!link.packageName.equals(packageName)))
                        {
                            
                            Token t = name.get(end-1);
                            error_pool.add(new Flaw(Flaw.type.cls_broken, t.source, t.line, t.position, t.value, innerlink.name));
                            return null;
                        }
                        else
                        {
                            tempmap.value = new ClassObject(link, this);
                            return tempmap.value;
                        }
                    }
                }
            }
        }
        error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, token.value));
        return null;
    }
    
 //   public ImportTable temp_table; 

    // this method 
    
    // this method parses an import construction
    public void imp_(List imp, ImportTable table)
    {
    //    temp_table = table;
        // check if import is static
        boolean is_static = ((int)(double)(imp.get(2))) == 1;
        // check for import name
        String name = ((Token)imp.get(1)).value;
        // check for classpath
        List list = (List) imp.get(0);
        // check for is list token
        boolean is_list = name.equals("*");
        // 
        //
        if (is_static)
        {
            int temp_int = list.size();
            Token value;
                // get object
                ClassObject object;
                // check if any class with same name in the import table
                
            if (is_list)
            {
                // add object to the static import list
                table.static_list.add(importClass(list, table, 0, temp_int));
            }
            else
            {
                temp_int --;
                // set value
                value = (Token) list.get(temp_int);
                // get string
                String temp = value.value;
                // import object
                object = importClass(list, table, 0, temp_int);
                // check if object is null, if yes, then return
                if (object == null) return;
                // check if any static field, class, method in this class
                boolean f = true;
                // if inner class there
                if (object.getInnerClass(temp)!= null) f = false;
                // if an method there
                if (object.methods.get(temp) != null) f = false;
                // if an field there 
                if (object.fields.get(temp) != null) f = false;
                // if not all them there, then raise error
                if (f)
                {
                    error_pool.add(new Flaw(Flaw.type.symbol_not_found, value.source, value.line, value.position, temp));
                }
                else
                {
                    table.addFieldName(name, value, object);
                }
            }
        }
        else
        {
            if (is_list)
            {
                // if is list, then add classpath to the list 
                table.import_list.add(importClassPath(list, table));
            }
            else
            {
                // get object
                ClassObject object = this.importClass(list, table);
                // if got null, then return
                if (object == null) return;
                // check if any class with same name in the import table
                if (table.source_class.get(name) != null)
                {
                    Token value = (Token)list.get(1);
                    error_pool.add(new Flaw(Flaw.type.is_already_def, value.source, value.line, value.position, name));
                }
                else 
        {
            ClassObject temp_table = table.import_class.get(name);
                if (temp_table != null)
                {
                    if (!temp_table.equals(object))
        {
                    Token value = (Token)list.get(1);
                    error_pool.add(new Flaw(Flaw.type.is_already_def_import, value.source, value.line, value.position, name));
        }
                }
                else
                {
                    table.import_class.put(name, object);
                }
        }
            }
        }
    }
    
    public void addInnerClass(List ast, ImportTable table, ClassObject outer, ClassMap map, Set<String> members, String host)
    {
        this.addInnerClass(ast, table, outer, map, members, host, 0);
    }
    
    public void addInnerClass(List ast, ImportTable table, ClassObject outer, ClassMap map, Set<String> members, String host, int level)
    {
        String outerName = outer.name;
     //   if (table.ast == null) System.out.println("ERROR HERE");
        // 
        Token token = (Token) ast.get(1);
        
        String name = token.value;
        // if name in members 
        if (members.contains(name))
        {
            this.error_pool.add(new Flaw(Flaw.type.already_def, 
                  token.source, token.line, token.position, 
                  "class", name, host));
        }
        else if (name.equals(host))
        {
            this.error_pool.add(new Flaw(Flaw.type.already_def, 
                  token.source, token.line, token.position, 
                  "class", name, host));
        }
        //
        int access = (int)(double)(ast.get(0));
        // create object
        ClassObject object = new ClassObject();
        // 
        // set name
        object.name = outerName + "$" + name;
        // set head name                                                        
        object.headName = outer.headName + "$" + name;
        // add head name to the head set
        members.add(name);
        // set host name
        object.hostName = host;
        // set table
        object.table = table;
        // set package name
        object.packageName = table.packageName;
        // set access
        object.access = access;
        // set object init
        object.init = ast;
        // set outer object
        object.outer = outer;
        // set ast 
        object.ast = table.ast;  
        // set map value
        map.get(object.headName).value = (object);
        // add object
        outer.visitInnerClass(object.name, outerName, name, object.access);
        // if access is not static, then add this object
        if ((access & ACC_STATIC) == 0)
        {
            String outerField = "this$"+level;
            object.addFieldLink("this$"+level, "L"+object.name+";", ACC_FINAL + ACC_SYNTHETIC);
            object.outerField = outerField;
            level ++;
        }
        // set object inner class map
        object.innerClasses = outer.innerClasses;
        // add object to the object pool
        this.object_pool.add(object);
        // iterate for object inner classes
        for (Object i : (List)((List)(ast.get(5))).get(0))
        {
            // get list of inner classes
            List ii = (List) i;
            // get name
     //       String str = ( (Token) ii.get(1) ).value;
            // set inner class
            this.addInnerClass(ii, table, object, map, members, host, level);
        }
    }
    
    // this method converts to the class access object
    public static int classAccess(int access)
    {
        return
        (access & ACC_PUBLIC) + 
        (access & ACC_PRIVATE) + 
        (access & ACC_PROTECTED) + 
        (access & ACC_STATIC) + 
        (access & ACC_FINAL) + 
        (access & ACC_INTERFACE) + 
        (access & ACC_ABSTRACT) + 
        (access & ACC_ANNOTATION) + 
        (access & ACC_ENUM); 
    }
    
    ClassObject object_class;
    
    {
        object_class = getClassByName(ClassObject.obj_name);
    }
    
    ClassObject class_class;
    {
        class_class = getClassByName(ClassObject.cls_name);
    }
    
    ClassObject annotation_target_class;
    ClassObject annotation_retention_class;
    {
        annotation_target_class = getClassByName(ClassObject.target_name);
        annotation_retention_class = getClassByName(ClassObject.retent_name);
    }
    
    public ASTExpr expr = new ASTExpr(this);
    public MethodMap.MethodHandler mhandler = new MethodMap.MethodHandler(this);
    //// this method will return an array object of primitive type
    //public ClassObject getArrayTypeClassByName(String name, int array)
    //{
        //// 
        //return ArrayObject.primitive(name, array);
    //}
    
    //// this method will return an array object of object 
    //public ClassObject getArrayObjectClassByName(String name, int array)
    //{
        ////
        //return ArrayObject.reference(name, array);
    //}
    
    
    
    // this method will return an first available class object
    public Pair<ClassObject, Integer> getFirstClass(ClassObject tt, ImportTable table, List ast)
    {
           //     boolean not_inner = true;
        // 
        ClassObject t = ClassTable.non_exists_object;
        // this flag indicates if class is innerclass
        {
            // get first token
            String str = ((Token) ast.get(0)).value;
            
            // get class table
            if (tt != null)
        {
            // check if inner class in classtable
            // if first object in the generics, then add class to the generic
            if (tt.generic != null)
            {
                t = tt.generic.getOrDefault(str, ClassTable.non_exists_object);
            }
            
        //    ClassTable l = tt.getTable();

            if (t == ClassTable.non_exists_object)
    {
        //    System.out.println(str);
                t = tt.getTable().getClass(str);
    }
        }
            
            if (t == ClassTable.non_exists_object)
            {
                if (table != null)
                {
                    t = table.getClass(str);
                    // 
                }
            }
            
            if (t == null)
            {
                t = ClassTable.non_exists_object;
            }
            
        }
        
        int begin_index = 0;
        int end = ast.size();
        //   System.out.println("getClass " + ast.toString());
        
        int begin = begin_index + 1;
        
    
        // get first token
        Token token = (Token) ast.get(begin_index);
        // get name 
        String str = token.value;
        // 
   //     ClassObject object = ClassTable.non_exists_object;
        // get object
   //     object = t.getTable().getClass(str);
        
   //     System.out.println(object);
        
    //    System.out.println();
    //    System.out.println("___________________");
        
        ClassObject object = t;
        
        // if got non_exists_object, then try to get class from system
        if (object == ClassTable.non_exists_object)
        {
            //
            object = null;
            int i = begin;
            int n = end;
            // this is an stop point
       //     int stop_point = 0;
            ClassLink link;
            // 
            ClassMap map = this.classes;
            // 
            Compiler.Packages pkg = Compiler.packages;
       //     Compiler.Packages temp = null;
            // this is an path from classpath
            StringJoiner list = new StringJoiner("/");
            list.add(str);
            // iterate for system packages
            while (i < n)
            {
                // get system package 
                pkg = pkg.getPackage(str);
                // get map
                map = map.get(str);
                // if got value, then break
                object = map.value;
                if (object != null)
                break;
                // get next token
                token = (Token)ast.get(i);
                // get string
                str = token.value;
                // if got package, then check if an class there
                if (pkg.is_package)
                {
                    // get link
                    link = pkg.getClass(str);
                    // if link is not null, then break
                    if (!link.is_broken)
                    {
                        // get object
                        object = new ClassObject(link, this);
                        // set object to the map
                        map.value = object;
                        // 
                        break;
                    }
                }
                // increment
                i++;
            }
            // if got object, then continue 
            if (object != null)
            {
                begin = i + 1;
            }
            // else, continue
            else
            {
                // reset map
                map = this.classes;
                // reset begin
                Path path = null;
                // 
                i = begin_index;
                // 
                String classname;
                // create string joiner
                StringJoiner joiner = new StringJoiner("/");
                // iterate until i < n
                while (i < n)
                {
                    // get token
                    token = (Token) ast.get(i);
                    // get name
                    str = token.value;
                    // add str to joiner
                    joiner.add(str);
            // get classname
            classname = joiner.toString();
            // check if here an source file 
            path = Compiler.searchForFileFromClasspath(classname + ".java");
            
     //       System.out.println(classname + ".java");
   //         //System.out.println("got path::" + path.toString());
            // get source path
            if (path != null)
            {
                List list1 = (Compiler.fileToAST(path.toString()));//, path.getParent());
                
       //         System.out.println(list1);
            
                this.checkAST(list1, path.getParent());
                
         //       System.out.println(classes.get("net"));
            }
                    
                    
                    // get map
                    map = map.get(str);
                    // get object
                    object = map.value;
                    // if object got, then break
                    if (object != null)
                    {
                        break;
                    }
                    // if classlink is there
                    link = Compiler.getClassFromList(classname, Compiler.classpath);
                    // 
                    if (!link.is_broken)
                    {
                        object = new ClassObject(link, this);
                        map.value = object;
                        break;
                    }
                    // try to get directory, if not got, then raise error and retunr
                    if (Compiler.searchForDirectoryFromClasspath(classname) == null)
                    {
                        error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, str));
                        return new Pair<>(null, null);
                    }
                    i ++;
                }
                
                begin = i + 1;
                // if object is null, then raise symbol not found error
                if (object == null) 
                {
                    error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, str));
                    return new Pair<>(null, null);
                }
            }
            
        }
        // if got ambiguous object, then raise error and return
        else if (object == ClassTable.ambiguous_object)
        {
            this.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, 
                 token.source, token.line, token.position, str));
            return new Pair<>(null, null);
        }
        else 
        {
            
        }
        
        object.link1();
        
        return new Pair<ClassObject, Integer>(object, begin);
        
    }
    
    // this method will return an class for classobject
    public ClassObject getClass(ClassObject tt, ImportTable table, List ast)
    {
   //     boolean not_inner = true;
        // 
        ClassObject t = ClassTable.non_exists_object;
        // this flag indicates if class is innerclass
        {
            // get first token
            String str = ((Token) ast.get(0)).value;
            
            // get class table
            if (tt != null)
        {
            // check if inner class in classtable
            // if first object in the generics, then add class to the generic
            if (tt.generic != null)
            {
                t = tt.generic.getOrDefault(str, ClassTable.non_exists_object);
            }
            
        //    ClassTable l = tt.getTable();

            if (t == ClassTable.non_exists_object)
    {
        //    System.out.println(str);
                t = tt.getTable().getClass(str);
                
            // if got, then set inner as true
    //        if (t == ClassTable.non_exists_object)
    //        {
    //            if (tt.outer != null) 
    //            {
    //                t = tt.outer.getTable().getClassA(str);
           //         not_inner = false;
   //             }
            //    if (t != ClassTable.non_exists_object)
        //        {
              //      not_inner = false;
          //      }
   //         }
   ////         else 
   //         {
          //      not_inner = false;
   //         }
            
    }
        }
            
            else
            {
                if (table != null)
                {
                    t = table.getClass(str);
                    // 
                    
                }
            }
            
            if (t == null)
            {
                t = ClassTable.non_exists_object;
            }
            /*
            else if (t != ClassTable.non_exists_object)
            {
                if (t != ClassTable.ambiguous_object)
                {
                    String name;
                    if (t.packageName == null)
                    {
                        
                        
                    }
                //    if (tt.getTable().extendLevel() > -1)
                //    {
               //     }
                }
            }
            */
        }
    //    // if object is not non_exists, then set is_inner to true
    //    if (t != ClassTable.non_exists_object)
    //    {
    //        is_inner = true;
    //    }
    //    return class object
        return getClassA(t, ast, 0, ast.size(), table, tt);
    }
    public ClassObject getClassA(ClassObject object, List ast, int begin_index, int end, ImportTable table, ClassObject parent)
    {
        //
        
        
     //   System.out.println("getClass " + ast.toString());
        
        int begin = begin_index + 1;
        // get first token
        Token token = (Token) ast.get(begin_index);
        // get name 
        String str = token.value;
        // 
   //     ClassObject object = ClassTable.non_exists_object;
        // get object
   //     object = t.getTable().getClass(str);
        
   //     System.out.println(object);
        
    //    System.out.println();
    //    System.out.println("___________________");
        
        
        // if got non_exists_object, then try to get class from system
        if (object == ClassTable.non_exists_object)
        {
            //
            object = null;
            int i = begin;
            int n = end;
            // this is an stop point
       //     int stop_point = 0;
            ClassLink link;
            // 
            ClassMap map = this.classes;
            // 
            Compiler.Packages pkg = Compiler.packages;
       //     Compiler.Packages temp = null;
            // this is an path from classpath
            StringJoiner list = new StringJoiner("/");
            list.add(str);
            // iterate for system packages
            while (i < n)
            {
                // get system package 
                pkg = pkg.getPackage(str);
                // get map
                map = map.get(str);
                // if got value, then break
                object = map.value;
                if (object != null)
                break;
                // get next token
                token = (Token)ast.get(i);
                // get string
                str = token.value;
                // if got package, then check if an class there
                if (pkg.is_package)
                {
                    // get link
                    link = pkg.getClass(str);
                    // if link is not null, then break
                    if (!link.is_broken)
                    {
                        // get object
                        object = new ClassObject(link, this);
                        // set object to the map
                        map.value = object;
                        // 
                        break;
                    }
                }
                // increment
                i++;
            }
            // if got object, then continue 
            if (object != null)
            {
                begin = i + 1;
            }
            // else, continue
            else
            {
                // reset map
                map = this.classes;
                // reset begin
                Path path = null;
                // 
                i = begin_index;
                // 
                String classname;
                // create string joiner
                StringJoiner joiner = new StringJoiner("/");
                // iterate until i < n
                while (i < n)
                {
                    // get token
                    token = (Token) ast.get(i);
                    // get name
                    str = token.value;
                    // add str to joiner
                    joiner.add(str);
            // get classname
            classname = joiner.toString();
            // check if here an source file 
            path = Compiler.searchForFileFromClasspath(classname + ".java");
            
     //       System.out.println(classname + ".java");
   //         //System.out.println("got path::" + path.toString());
            // get source path
            if (path != null)
            {
                List list1 = (Compiler.fileToAST(path.toString()));//, path.getParent());
                
       //         System.out.println(list1);
            
                this.checkAST(list1, path.getParent());
                
         //       System.out.println(classes.get("net"));
            }
                    
                    
                    // get map
                    map = map.get(str);
                    // get object
                    object = map.value;
                    // if object got, then break
                    if (object != null)
                    {
                        break;
                    }
                    // if classlink is there
                    link = Compiler.getClassFromList(classname, Compiler.classpath);
                    // 
                    if (!link.is_broken)
                    {
                        object = new ClassObject(link, this);
                        map.value = object;
                        break;
                    }
                    // try to get directory, if not got, then raise error and retunr
                    if (Compiler.searchForDirectoryFromClasspath(classname) == null)
                    {
                        error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, str));
                        return null;
                    }
                    i ++;
                }
                
                begin = i + 1;
                // if object is null, then raise symbol not found error
                if (object == null) 
                {
                    error_pool.add(new Flaw(Flaw.type.symbol_not_found, token.source, token.line, token.position, str));
                    return null;
                }
            }
            
        }
        // if got ambiguous object, then raise error and return
        else if (object == ClassTable.ambiguous_object)
        {
            this.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, 
                 token.source, token.line, token.position, str));
            return null;
        }
        else 
        {
            
        }
        // if begin == end
    //    if (begin == end)
    //    {
            // if object is not inner, then check if class is public or 
            // class package is the same
    //        return object;
    //    }
   //    System.out.println(object.name);
 //       System.out.println("Name+ "+ object.name);
        
  //      System.out.println(begin);
  //      System.out.println(end);
        
        // else, iterate until begin < end
        while (begin < end)
        {
            // get token
            token = (Token) ast.get(begin);
            str = token.value;
            
   //         System.out.println("STR");
   //         System.out.println(str);
      //      System.out.println(str);
            //
            String temp = object.name;
       //     System.out.println(object.getInnerClass(str));
            
            object = object.getTable().getClassA(str);
            
    //        System.out.println(object.name + " " + (object.access & ACC_PROTECTED) );
   //         System.out.println("GOT " + object);
            // 
            if (object == ClassTable.non_exists_object)
            {
                this.error_pool.add(new Flaw(Flaw.type.symbol_not_found, 
                     token.source, token.line, token.position, str));
                     return null;
            }
            else if (object == ClassTable.ambiguous_object)
            {
                this.error_pool.add(new Flaw(Flaw.type.ref_ambiguous, 
                     token.source, token.line, token.position, str));
                     return null;
            }
    // continue here        
    if (!AST.isSameHost(object, parent)){
        
    //    System.out.println("same name");
            // check if object is public or package name is the same 
            if (object.packageName.equals(table.packageName))
            {
                if ((object.access & ACC_PRIVATE) > 0)
                {
                    token = (Token) ast.get(begin );
                    
                    this.error_pool.add(new Flaw(Flaw.type.private_class, 
                        token.source, token.line, token.position, token.value, 
                         temp));
                    return null;
                }
            }
            else
            {
                if ((object.access & ACC_PUBLIC) == 0)
                {
                    token = (Token) ast.get(begin  );
                    
                    this.error_pool.add(new Flaw(Flaw.type.n_public_class, 
                        token.source, token.line, token.position, token.value, 
                         temp));
                    return null;
                }
            }
    }
            
            begin ++;
        }
        // if object is not inner, then check 
        // continue here
   //     if ()
             object.link1();
         
            return object;
    }    
    
    // this method checks if objects have same host 
    public static boolean isSameHost(ClassObject donor, ClassObject recipient)
    {
            if (donor.name.equals(recipient.name))
            {
                return true;
            }
            if (!donor.packageName.equals(recipient.packageName))
            {
                return false;
            }
            //
            String rhost = recipient.hostName;
            //
            String dhost = donor.hostName;
            //
            String rhead = recipient.headName;
            //
            String dhead = donor.headName;
            
            if (Pair.compare(dhost, rhead))
            {
                if (recipient.members.contains(dhead)) return true;
            }
            else if (Pair.compare(rhost, dhead))
            {
                if (donor.members.contains(rhead)) return true;
            }
            
            return Pair.compare(dhost, rhost);
    }
    
    // this method will check file
    public void checkFile(Path path, Path directory)
    {
        String str = path.toString();
        
   //     System.out.println(str);
        // check appendix
        int i = str.lastIndexOf(".");
        // if got -1, then return
        if (i == -1) return;
        // if got > 1, then check with length
        if ((str.length() - i) != 5)
        {
            return;
        }
        if (!str.substring(i).equals(".java")) return;
        try
        {
            this.parseAST(Compiler.fileToAST(path.toString()), directory);
        }
        catch (Exception e)
        {
            
        }
    }
    
    public ImportTable checkAST(List ast, Path directory)
    {
        try
        {
            return this.parseAST(ast, directory);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    // this method converts an init to class object
    public ClassObject parseInit(List temp1, ImportTable table)
    {
        // is package is not null
        boolean is_pkg = false;
        // get package name
        String packageName = table.packageName;
        // check
        if (packageName!= null)
        {
            is_pkg = !packageName.equals("");
        }
        // run
        return parseInit(temp1, table.temp_pkg, table.directory, 
                 is_pkg, packageName, table, table.map);
    }
    public ClassObject parseInit(List temp1, Compiler.Packages temp_pkg, 
            Path directory, boolean is_pkg, String packageName, ImportTable table,
            ClassMap map)
    {        
        // get name and access from the ast
            int access = (int )(double )(temp1.get(0));
            Token name = ((Token)temp1.get(1));
            String name_value = name.value;
            //
            // if the name is clashes with package of same name
            Compiler.Packages temp_pkg2 = temp_pkg.getPackage(name_value);
            if (temp_pkg2.is_package)
            {
      //          //System.out.println(temp_pkg2.name);
                this.error_pool.add(new Flaw(Flaw.type.pkg_same_name, 
                       name.source, name.line, name.position, name_value, 
                       temp_pkg2.name_dots));
                return null;
            }
            // then add error to the error pool
            String object_name;
            // create new ObjectClass
            ClassObject object = new ClassObject();
            // this is an member set of the classobject
            Set<String> members ;
            object.members = members = new HashSet<>();
            // set name and access to the classobject
            object.name = object_name = (is_pkg)? (packageName + "/" + name_value) : name_value;
            object.access = AST.classAccess(access);
            // set package name and head name
            object.packageName = packageName;
            object.headName = name_value;
            // set init
            object.init = temp1;
            // set table
            object.table = table;
            // set ast
            object.ast = this;
            // set classobject to the map
            ClassMap m = map.get(name_value);
            // check if classobject is already defined
            if (m.value == null)
            {
                // set value
                m.value = (object);
          //      System.out.println("m.value " + m);
          //      System.out.println("classes " + this.classes);
                table.source_class.put(name_value, object);
                // add object to the class pool
                object_pool.add(object);
            }
            else
            {
                // if defined, then add error to the error_pool
                this.error_pool.add(new Flaw(Flaw.type.cls_duplicate, 
                  name.source, name.line, name.position, name_value));
            }
            
            for (Object i : (List)((List)(temp1.get(5))).get(0))
            {
                List ii = (List) i;
                // add inner class 
                this.addInnerClass(ii, table, object, m.parent, 
                                    members, name_value);
            }
            return object;
    } 
    
    // this method converts an AST to class object
    public ImportTable parseAST(List ast, Path directory) throws Exception
    {
        List temp1 = (List) ast.get(0);
        // set source
        String source = ((Token) temp1.get(2)).value;
        
        // if source already in the checked set
        if (checked.contains(source))
        {
            return null;
        }
        else
        {
 //           System.out.println(source);
            checked.add(source);
        }
        // create import table
        ImportTable table = new ImportTable(this);
        table.directory = directory;
        // 
        // get source 
        table.source = source;
        
  //      System.out.println(source);
        
  //      System.out.println(source);
        
        // get annotatinos 
        List temp2 = (List) temp1.get(1);
        // if source is package-info, then create synthetic class package-info
        if (source.equals("package-info.java")) createSource(temp2);
        // else, check if temp2 is empty, if not, then append an error
        else
        {
            if (temp2.size() > 0)
            {
           //     System.out.println(temp2);
                
                Token token = (Token)((List)temp2.get(0));
                error_pool.add(new Flaw(Flaw.type.pkg_info, source, token.line, token.position));
            }
        }
        // get package 
        temp2 = (List) temp1.get(0);
        // get string implementation
        int l = temp2.size();
        // 
        String [] pkg = new String [l];
        StringJoiner joiner = new StringJoiner("/");
        // iterate
        for (int i = 0; i < l; i ++)
        {
            String str = ((Token)temp2.get(i)).value;
            pkg[i] = str;
            joiner.add(str);
        }
        // check if package name is got
        boolean is_pkg = l > 0;
        // get system package, if exists, then raise error
        Compiler.Packages temp_pkg = Compiler.packages.getPackage(pkg, 0, pkg.length);
        Compiler.Packages temp_pkg2 = Compiler.packages.getPackage(pkg, 0, pkg.length);
        // if is package, then add error to the error pool and return
        if (is_pkg) if (temp_pkg.is_package)
        {
            Token t = ((Token)temp2.get(0));
            error_pool.add(new Flaw(Flaw.type.pkg_exists, source, t.line, t.position, temp_pkg.module, temp_pkg.module));
            return null;
        }
        // create package name string
        String packageName = table.packageName = joiner.toString();
        // get classmap
        ClassMap map = this.classes.get(pkg);
        // add all classes from ast to newly created classmap
        l = ast.size();
        if (l == 2) return null;
        //
        table.temp_pkg = temp_pkg;
        table.map = map;
        // iterate until l greater that 2
        while (l > 2)
        {
            l --;
            // get class ast init
            temp1 = (List)ast.get(l);
            parseInit(temp1, temp_pkg, directory, is_pkg, packageName, 
                            table, map);
        }
        // iterate for import table
        // set package map
        table.cls_map_pkg = map;
        // set package ast parser
        table.ast = this;
        // get import list
        temp1 = (List<List>) ast.get(1);
        
        // add java lang classpath
        table.import_list.add(this.java_lang);
        // iterate with list 
        for (Object i : temp1)
        {
            // import 
            imp_((List)i, table);
        }
        /*
        // 
        boolean ispkg;
        String[] joiner;
        // create import table
        ImportTable table  = new ImportTable();
        // convert object to list
        List ast = (List) ast__;
        // this is an temporary list
        List temp = (List) ast.get(0);
        List temp2;
        // get source field
        String source = ((Token) temp.get(2)).value;
        table.source = source;
        String packageName;
 */       // check if source file is 'package-info', if not, then annotation field must be empty
   /*     if (source.equals("package-info.java"))
        {
            createSource((List)temp.get(1));
        }
        else
        {
            
            temp2 = (List)temp.get(1);
            // check if annotation field is empty, if not, then add remark to the remark pool
            if (temp2.size() > 0)
            {
                Token y = (Token)((List)temp2.get(0)).get(0);
                error_pool.add(new Flaw(Flaw.type.pkg_info, source, y.line, y.position, ""));
            }
        }*//*
        // create package name
        {
            temp2 = (List)temp.get(0);
            ispkg = temp2.size() > 0;
            joiner = new String[(temp2.size())];
            int tt = 0;
            // iterate for all elements on list
            for (Object i : temp2)
            {
             // create token object
                Token t = (Token) i;
                // add to joiner
                joiner[tt] = t.value;
                tt ++;
            }
            Compiler.Packages pkg = Compiler.packages.getPackage(joiner, 0, joiner.length);
            // if package name in the Compiler.packages
            if (pkg.is_package)
            {
                Token y = (Token)(temp2.get(0));
                error_pool.add(new Flaw(Flaw.type.pkg_exists, source, y.line, y.position, pkg.module)); 
            }
            // 
            table.packageName = packageName = String.join("/", joiner);
        }*//*
        // iterate object name and access list
        int i = ast.size();
        while (i > 2)
        {
            i --;
            // get class ast
            temp = (List)ast.get(i);
            // get class name 
            String name = ((Token)(temp.get(1))).value;
            // get class access
            int access = (int)(double)(temp.get(0));
            // create class object
            ClassObject object = new ClassObject();
            object.access = classAccess(access);
            if (ispkg) object.name = String.join("/", packageName, name);
            else object.name = name;
            object.table = table;
            object.packageName = packageName;
            object.init = temp;
            // add classobject to the classes
            classes.put(joiner, object);
            
        }*/
     /*
        // this is an import list
        temp = (List) ast.get(1);
        // iterate import list
        for (Object ti : temp)
        {
            imp_( (List)ti, table);
        }
        */
  //      //System.out.println(error_pool);
        return table;
    }
    
   
    
   
}
