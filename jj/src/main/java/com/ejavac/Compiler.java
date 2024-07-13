package com.ejavac;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.*;
import java.lang.annotation.*;
import java.nio.*;
import java.nio.file.*;
import java.lang.ref.*;
import java.net.*;
import java.lang.*;
import java.io.*;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.*;



public class Compiler
{
    
    // this method list directory
    public static List<Path> listDirectory(Path path)
    {
        try
        {
            return Files.list(path).collect(Collectors.toList());
        }
        catch(IOException ignore)
        {
            return Compiler.Cache.empty_list;
        }
    }
    // this method returns an name from path
    public static String getName(Path path)
    {
        String ret = path.toString();
        ret.substring(ret.lastIndexOf("/")+1);
        return ret;
    }
    
public static class Packages{
    // this is an map of the associated names
    public Map<String, Packages> map = new HashMap();
    // if type is an package
    public boolean is_package = false;
    public int level = 0;
    public String name = "";
    public String module = "";
    public String name_dots = "";
    // this is an list of files from package
    private List<Path> list_cache = null;
    // this method returns an package list or empty list if is not package
    public List<Path> getList()
    {
        if (this.list_cache != null) return this.list_cache;
        
        // check if an package here
        // if not, then return empty list
        if (is_package)
        {
            // create path from jrt file system
            Path path = Compiler.jrt_path.resolve(name_dots);
            // check if path is an directory
            if (Files.isDirectory(path))
            {
                try
                {
                // if yes, then return file list
                    this.list_cache = (Files.list(path).collect(Collectors.toList()));
                    return this.list_cache;
                }
                catch(Exception e)
                {
                    
                }
            }
        }
        this.list_cache = Cache.empty_list;
        return Cache.empty_list;
    }
    
    // this method will return an class from package 
    public ClassLink getClass(String name)
    {
        return Compiler.getClassFromList(this.name + "/" + name, this.getList());
    }
    
    // this method returns an package or null if is not package
    public Packages getPackage(String [] name, int begin, int end)
    {
        // create base pointer
        Packages base = this;
        // iterate until begin < end
        while (begin < end)
        {
            // increment begin
            // get next package
            base = base.map.get(name[begin++]);
            // check if base is null
            // if yes, then return null
            if (base == null) return Cache.empty_pkg;
        }
        // check if got element is an package 
        return base;
    }
    
    public Packages getPackage(String name)
    {
    //    //System.out.println(name);
        // create base pointer
        Packages base = this;
        // iterate until begin < end
//        while (begin < end)
//        {
            // increment begin
            // get next package
            base = base.map.get(name);//[begin++]);
            // check if base is null
            // if yes, then return null
            if (base == null) return Cache.empty_pkg;
//        }

    //    //System.out.println(base.name);
        // check if got element is an package 
        return base;
    }
    // check if class is an package 
    // public boolean is_package = false;
    // add an package and return package link
    public Packages addPackage(String basename, String module)
    {
        String [] name = basename.split("\\.");
        // if name length is null, then just return this 
        if (name.length == 0) return this;
        // parent package 
        Packages p = this;
        // new package
        Packages l;
        // iterate for name
        for (String i : name)
        {
            // get package list, if got null, then put new
            l = p.map.get(i);
            int lvl = this.level;
            if (l == null)
            {
                l = new Packages();
                l.level = ++lvl;
                p.map.put(i, l);
            }
            // set parent as new package
            p = l;
            
        }
        // set "is_package" as true
        p.is_package = true;
        p.name = String.join("/", (name));
        p.name_dots = basename;
        p.module = module;
        // return new package 
        return p;
    }
    
    public String toString()
    {
        return map.toString();
    }
}

public static class Cache
{
    // 
    public static final Map empty_map;
    public static final Packages empty_pkg;
    public static final List empty_list;
    // this is an broken classlink
    public static final ClassLink broken_link;
    static
    {
        empty_pkg = new Packages();
        empty_pkg.is_package = false;
        empty_list = Collections.unmodifiableList(new LinkedList());
        empty_map = Collections.unmodifiableMap(new HashMap());
        broken_link = new ClassLink();
        broken_link.is_broken = true;
    }
    // this is an cache for classes
    public static final Map<String, ClassLink> classes = new HashMap<>();
}
    
    
    static
    {
        System.load(new java.io.File("libCompiler.so").getAbsolutePath());
    }
    // this method generates an ast
    public static native List fileToAST(String name);
    // this is an system classloader, that will be used later;
    public static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    
    public static final FileSystem jrt = FileSystems.getFileSystem(URI.create("jrt:/"));  
    // public static final Path modules = jrt.getPath("modules");
    public static final Path jrt_path = jrt.getPath("packages");
    // 
    //
    public static final List<Path> classpath = new ArrayList<Path>();
    static
    {
        List<Path> ls = Compiler.classpath;
        List paths;
        try
        {
            paths = Collections.list(Compiler.classLoader.getResources(""));
            
        for (var i : paths)
        {
            // add path
            try
            {
                ls.add(Paths.get(((URL)i).toURI()));
            }
            catch (URISyntaxException er)
            {
                er.printStackTrace();
            }
        }
            
        }
        catch (IOException er)
        {
            er.printStackTrace();
        }
    }
    // 
    public static final Packages packages = new Packages();
    //
    static
    {
        for (Object i : ModuleLayer.boot().modules().stream().toArray())
        {
            Module k = (Module) i;
            // add packages in module
            for (String ii: k.getPackages())
            {
                if (k.isExported(ii))
                {
           //         //System.out.println(ii);
                    //  add package  
                    packages.addPackage(ii, k.getName());
                }
            }
        }
    }
    
    // this method returns an inner class from classlink
    public static InnerClassLink getInnerClass(ClassLink link, String [] name, int begin, int end)
    {
        return getInnerClass(  link,  name,  begin,  end,  true);
    }
    
    public static InnerClassLink getInnerClass(ClassLink link, String [] name, int begin, int end, boolean is_public)
    {
        // this is an map for innerclasslink
        Map map = (Map)link.innerClasses.get(link.name);
        // if map is null, then return null
        if (map == null) return null;
        // this is an innerclasslink
        InnerClassLink ret;
        // get first innerclasslink
        ret = (InnerClassLink) map.get(name[begin++]);
        // if ret is null, then return null
        if (ret == null) return null;
        // iterate until begin lesses that end
        while (begin < end)
        {
           //  check if public is true
            if (is_public)
            {
                if ((ret.access & ACC_PUBLIC) == 0)
                {
                    return null;
                }
            }
            else
            {
                if ((ret.access & ACC_PRIVATE) > 0)
                {
                    return null;
                }
            }
            // get map object 
            map = (Map) link.innerClasses.get(ret.name);
            // if map is null, then return null
            if (map == null) return null;
            // get next innerclasslink
            ret = (InnerClassLink) map.get(name[begin++]);
            // if ret is null, then return null
            if (ret == null) return null;
        }
        if (is_public)
        {
                if ((ret.access & ACC_PUBLIC) == 0)
                {
                    return null;
                }
        }
        else
        {
                if ((ret.access & ACC_PRIVATE) > 0)
                {
                    return null;
                }
        }
        return ret;
    }
    
    
    // this method search for file path in the classpath
    public static Path searchForFileFromClasspath(String file)
    {
        for (Path i: Compiler.classpath)
        {
            // get file
            i = i.resolve(file);
            // check if path is file
            if (Files.isRegularFile(i))
            {
                // if yes, then return 
                return i;
            }
        }
        // else, return null
        return null;
    }
    
    
    // this method returns list of files from classpath
    public static List<Path> listClasspath(String directory)
    {
        List<Path> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (Path ii: Compiler.classpath)
        {
            // get file
            for (Path i: Compiler.listDirectory(ii.resolve(directory)))
    {
            // check if path is file
            if (Files.isRegularFile(i))
            {
                // if set is not contains same name string, then add path to the list
                if (!set.contains(getName(i)))
                {
                    list.add(i);
                }
            }
    }
        }
        // else, return null
        return list;
    }
    
    // this method search for file path in the classpath
    public static Path searchForDirectoryFromClasspath(String file)
    {
        for (Path i: Compiler.classpath)
        {
            // get file
            i = i.resolve(file);
            // check if path is file
            if (Files.isDirectory(i))
            {
                // if yes, then return 
                return i;
            }
        }
        // else, return null
        return null;
    }
    
    // this method returns an classlink from list of patches
    public static ClassLink getClassFromList(String name, List<Path> ... list)
    {
        // check class with name in the cache
        ClassLink ret;// = Cache.classpath_map.get(name);
        // 
    //    if (ret != null) return ret;
        // get path 
        String path = name + ".class";
        // iterate until regular file meet
    for (List<Path> l: list)
    {
        for (Path i : l)
        {
            // get path
            // get classlink
            ret = readClassFromPath(i.resolve(path), name);
            // check if got an broken link
            if (ret.is_broken)
            {
                continue;
            }
            // if not, then return that link
            else
            {
          //      Cache.classpath_map.put(name, ret);
                return ret;
            }
        }
    }
        return Cache.broken_link;
  //      Cache.classpath_map.put(name, ret);
    }
    // this method returns an classlink from path
// and checks 
    public static ClassLink readClassFromPath(Path path, String name)
    {
        try
        {
            ClassReader cr = new ClassReader(Files.readAllBytes(path));
            ClassLink cl = new ClassLink();
            cr.accept(cl, 0);
            cl.is_broken = !cl.name.equals(name);
            return cl;
        }
        catch (Exception ignore)
        {
     //       ignore.printStackTrace();
            return Cache.broken_link;
        }
    }
    // this function returns an classlink
    public static ClassLink getClass(String [] name)
    {
        return getClass(name, 0, name.length);
    }
    public static ClassLink getClass(String [] name, int begin, int end)
    {
        // create packages 
        List list = Compiler.packages
        .getPackage(name, begin, end-1).getList();
      //  //System.out.println(list);
        // create an stringbuilder
        StringBuilder classname = new StringBuilder(name[begin++]);
        // repeat until begin < end
        while (begin < end)
        {
            classname.append("/");
            // add name to the classname
            classname.append(name[begin++]);
        }
   //     //System.out.println(classname.toString());
        // return classlink
        return getClassFromList(classname.toString(), list, Compiler.classpath);
    }
    
    public static ClassWriter compileClassObject(ClassObject object)
    {
        // 
        ClassWriter cw = new ClassWriter(
                ClassWriter.COMPUTE_FRAMES |
                ClassWriter.COMPUTE_MAXS);
        //
        cw.visit(
           55, object.access|ACC_SUPER, object.name, null, 
           object.superName, object.interfaces.toArray(new String[0]));
        //
        cw.visitSource(object.table.source, null);
        //
        for (String i: object.members)
        {
            cw.visitNestMember(i);
        }
        //
        if (object.hostName!= null)
        {
            cw.visitNestHost(object.hostName);
        }

        //
        for (FieldLink i: object.fields.values())
        {
            cw.visitField(
                   i.access,
                   i.name,
                   i.descriptor,
                   null,
                   i.value);
        }
        //
        for (MethodLink i: object.method_list)
        {
            visitMethodCode(object, i, cw.visitMethod(
                    i.access,
                    i.name, 
                    i.descriptor,
                    null,
                    i.exceptions.toArray(new String[0])));
        }
        
        cw.visitEnd();
        
        return cw;
        
    }
    
    public static void visitMethodCode(ClassObject object, MethodLink link, MethodVisitor mv)
    {
        ASTExpr.CodeVisitor cv = object.ast.expr.new CodeVisitor(mv);
        mv.visitCode();
        for (Object a1: link.cinit)
        {
            cv.visitAll(a1);
        }
        int i = link.getList().size();
        i += 1 - ((link.access & ACC_STATIC) / ACC_STATIC);
        
//        System.out.println(":"+link.name);
        
        mv.visitMaxs(i, cv.max + i);
        mv.visitEnd();
    }
    
    
    
    //
//    public static void main(String [] args)
//    {//System.out.println(Compiler.getClass(new String[]{"kllop"}));    }
//
}
