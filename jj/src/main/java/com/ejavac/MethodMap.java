package com.ejavac;import java.util.*;
import static org.objectweb.asm.Opcodes.*;

public class MethodMap<A, B> extends AbstractMap<A, B>
{    
    public static final int obj_ref = 2147483300;
    
    public static void main(String [] args)
    {
    }
    
    public Iterator<Map.Entry<List<String>, MethodLink>> iterator(int n)
    {
        // this method will return an iterator for search function
        return new MethodMapIterator<
        Map.Entry<List<String>, MethodLink>,
        HashMap<List<String>, MethodLink>
        >(this, n);
    }
    // this is an condition interface
        public static interface Condition
        {
            boolean checkMethod(MethodLink link);
        };
    
    public static class ConditionIterator implements Iterator<MethodLink>
    {
        private Condition condition;
        private MethodLink next;
        private Iterator<MethodLink> iterator;
        public ConditionIterator(Condition condition, Iterator<MethodLink> iterator)
        {
            this.condition = condition;
            this.iterator = iterator;
        }
        
        // return next element
        public MethodLink next()
        {
            // if no next element there, then raise error 
            if (this.hasNext())
            {
                return this.next; 
            }
            else
            {
                throw new NoSuchElementException();
            }
        }
        // check for next element
        public boolean hasNext()
        {
            if (this.next == null)
            {
                // create methodlink
                MethodLink link;
                // create iterator
                Iterator<MethodLink> iter = this.iterator;
                // iterate over iterator
                while (iter.hasNext())
                {
                    //
                    link = iter.next();
                    // if got next element
                    if (condition.checkMethod(link))
                    {
                        this.next = link;
                        return true;
                    } 
                    //
                }
                // 
                this.next = null;
                return false;
            }
            else
            {
                return true;
            }
        }
    }
        // this is an implementation for superclass condition
        public static Condition privateCondition = new Condition()
        {
            public boolean checkMethod(MethodLink link)
            {
                return ((link.access & ACC_PRIVATE) == 0);
            }
        };
        public static Condition publicCondition = new Condition()
        {
            public boolean checkMethod(MethodLink link)
            {
                return ((link.access & ACC_PRIVATE) == 0) && 
                       ((link.access & (ACC_PROTECTED | ACC_PUBLIC)) > 0);
            }
        };
        public static Condition interfaceCondition = new Condition()
        {
            public boolean checkMethod(MethodLink link)
            {
                return ((link.access & ACC_STATIC) == 0) &&
                       ((link.access & ACC_PUBLIC) > 0);
            }
        };
        public static Condition createCondition(int zero, int some)
        {
            return new Condition()
            {
                public boolean checkMethod(MethodLink link)
                {
                    return ((link.access & zero) == 0) &&
                           ((link.access & some) > 0);
                }
            };
        }
    // this class for read-only methodmap with an condition
    public static class MethodMapCondition<A, B> extends MethodMap<A, B>
    {
        
        
        // this is an condition
        private Condition condition;
        // this is an methodmap
        private MethodMap map;
        // this is an constructor
        public MethodMapCondition(Condition condition, MethodMap map)
        {
            this.condition = condition;
            this.map = map;
        } 
        // this is an condition map iterator
        public Iterator<Map.Entry<List<String>, MethodLink>> iterator(int n)
        {
            return new MethodMapConditionIterator(this, n);
        }
        
        // this is an method map condition iterator
        public static class MethodMapConditionIterator implements Iterator<
             Map.Entry<List<String>, MethodLink> >
        {
            // this is an map condition
            private Condition condition;
            // this is an methodmap iterator
            private Iterator<Map.Entry<List<String>, MethodLink>> iterator;
            // this is an next element from methodmap iterator
            private Map.Entry<List<String>, MethodLink> next = null;
            public MethodMapConditionIterator(MethodMapCondition map, int size)
            {
                this.iterator = map.map.iterator(size);
                this.condition = map.condition;
            }
            // 
            public Map.Entry<List<String>, MethodLink> next()
            {
                Map.Entry<List<String>, MethodLink> n = this.next;
                if (n != null)
                {
                    this.next = null;
                    this.hasNext();
                    return n;
                }
                else
                {
                    if (hasNext())
                    {
                        return this.next;
                    }
                    else
                    {
                        throw new NoSuchElementException();
                    }
                }
            }
            // 
            public boolean hasNext()
            {
                // this method checks if there an next element that 
                // supplies conditions
                // if next element is already there, then return true;
                if (this.next!= null)
                {
                    return true;
                }
                // else, try to get next element
                else
                {
                    Map.Entry<List<String>, MethodLink> entry;
                    // if iterator hasn't next element, then return false
                    // else, iterate until entry supplies condition
                    Iterator<Map.Entry<List<String>, MethodLink>> iter = this.iterator;
                    // 
                    while (iter.hasNext())
                    {
                        // get next element
                        entry = iter.next();
                        ///
                        
                        
                //        System.out.println(""+this.iterator+" "+entry);
                        // check if entry value supplies an condition
                        if (this.condition.checkMethod(entry.getValue()))
                        {
                            this.next = entry;
                            return true;
                        }
                    }
                    return false;
                }
            }
        } 
        
        public void clear()
        {
            return;
        }
        public Object clone()
        {
            return this;
        }
        public boolean containsKey(Object key)
        {
            B b = this.get(key);
            // return if object equals to null
            return b != null;
        }
        
        public Set<Map.Entry<A,B>> entrySet()
        {
            // create new set and return
            Set set = new HashSet<>();
            //
            for (Object entry: this.map.entrySet())
            {
                // check method value
                B value = ((Map.Entry<A, B>)entry).getValue();
                // check condition
                if (value instanceof MethodLink)
                {
                    // check method link condition
                    if (!this.condition.checkMethod((MethodLink)value))
                    {
                        continue;
                    }
                }
                // add entry
                set.add(entry);
            }
            return set;
        }
        public B get(Object key)
        {
            // get object from map
            
     //       System.out.println(key);
       //s     System.out.println(map);
            Object link = (Object) map.get(key);
            //
   //         System.out.println(link);
            
            // if got methodlink, then check condition
            if (!(link instanceof MethodLink))
            {
                return null;
            }
            // check object link condition
            else if (this.condition.checkMethod((MethodLink)link))
            {
                return (B)link;
            }
            else
            {
                return null;
            }
        }
        public B put(A key, B value)
        {
            return null;
        }
        public B remove(Object key)
        {
            return null;
        }
    }
    
    // this class is for read-only merged methodmap bucket
    public static class MethodMapMerged<A, B> extends MethodMap<A, B>
    {
        // this is an list of maps
        private List<MethodMap<A, B>> map_list ;
        // this is an constructor
        public MethodMapMerged(MethodMap<A, B> ... map_list)
        {
            this.map_list = Arrays.asList(map_list);
        } 
        public MethodMapMerged(List<MethodMap<A, B>> map_list)
        {
            this.map_list = map_list;
        }
    
        // this is an merged map iterator
        public Iterator<Map.Entry<List<String>, MethodLink>> iterator(int n)
        {
            return new MethodMapMergedIterator(this, n);
        }
        // this is an method map merged iterator
        public static class MethodMapMergedIterator implements Iterator<
             Map.Entry<List<String>, MethodLink> >
        {
            private Iterator<MethodMap<List<String>, MethodLink>> map_iter;
            private int n;
            private Set<List<String>> set;
            private Iterator<Map.Entry<List<String>, MethodLink>> cur_iter;
            private Map.Entry<List<String>, MethodLink> next = null;
            
            public MethodMapMergedIterator(MethodMapMerged map, int n)
            {
                this.n = n;
                // set map iterator
                this.map_iter = map.map_list.iterator();
                 // set new hashset
                this.set = new HashSet<>();
                // set current iterator
                this.cur_iter = new ArrayList<Map.Entry<List<String>, MethodLink>>().iterator();
            }
            
            public Map.Entry<List<String>, MethodLink> next() throws NoSuchElementException
            {
                Map.Entry<List<String>, MethodLink> n = this.next;
                if (n != null)
                {
                    this.next = null;
                    this.hasNext();
                    return n;
                }
                else
                {
                    if (hasNext())
                    {
                        return this.next;
                    }
                    else
                    {
                        throw new NoSuchElementException();
                    }
                }
            }
            
            public boolean hasNext() 
            {
                // this method checks if there an next element
                if (this.next == null)
                {
                    Iterator<Map.Entry<List<String>, MethodLink>> c = cur_iter;
                    Map.Entry<List<String>, MethodLink> entry;
                    
                    if (c.hasNext())
                        {
                            
                            while (c.hasNext())
                            {
                                
   //                     System.out.println(c);
     //                   System.out.println(c.hasNext());
       //                 System.out.println(c.hasNext());
                                entry = c.next();
                                // 
                                List<String> key = entry.getKey();
                                // if entry key is not in set
                                if (!this.set.contains(key))
                                {
  //                                  System.out.println(key);
                                    this.set.add(key);
                                    this.next = entry;
                                    this.cur_iter = c;
    //                                System.out.println("ret");
                                    return true;
                                }
                            };
                        }
                    
                    while (!c.hasNext())
                    {
                        if (map_iter.hasNext())
                        {
                            c = map_iter.next().iterator(this.n);
                        }
                        else
                        {
                            return false;
                        }
                        
   //                     System.out.println(c);
     //                   System.out.println(c.hasNext());
       //                 System.out.println(c.hasNext());
                        if (c.hasNext())
                        {
                            
                            while (c.hasNext())
                            {
                                
  //                     System.out.println(c);
    //                    System.out.println(c.hasNext());
      //                  System.out.println(c.hasNext());
                                entry = c.next();
                                // 
                                List<String> key = entry.getKey();
                                // if entry key is not in set
                                if (!this.set.contains(key))
                                {
//                                    System.out.println(key);
                                    this.set.add(key);
                                    this.next = entry;
                                    this.cur_iter = c;
  //                                  System.out.println("ret");
                                    return true;
                                }
                            };
                        }
                    }
                }
                else
                {
                    return true;
                }
   //             System.out.println("WHAT??");
                return false;
            }
        } 
        
        public void clear()
        {
            return;
        }
        public Object clone()
        {
            return this;
        }
        public boolean containsKey(Object key)
        {
            for (MethodMap<A, B> i: this.map_list)
            {
                if (i.containsKey(key))
                {
                    return true;
                }
            } 
            return false;
        }
        
        public Set<Map.Entry<A,B>> entrySet()
        {
            Set set = new HashSet();
            for (MethodMap<A, B> i: this.map_list)
            {
                set.addAll(i.entrySet());
            } 
            return set;
        }
        public B get(Object key)
        {
            B o = null;
            // 
            for (MethodMap<A, B> i: this.map_list)
            {
                o = i.get(key);
                // 
                if (o != null)
                {
                    return o;
                }
            } 
            return o;
        }
        public B put(A key, B value)
        {
            return null;
        }
        public B remove(Object key)
        {
            return null;
        }
    }
    
    private static class MethodMapIterator<
    T extends Map.Entry<List<String>, MethodLink>,
    D extends HashMap<List<String>, MethodLink> > implements Iterator<T>
    {
        
        private int size;
        
        private Iterator<T> current;
        
        private Iterator<Map.Entry<Integer, D>> base;
 //       private TreeMap<>
        public MethodMapIterator(MethodMap map, int n)
        {
            size = n + 1;
            // get first map
            D map_ = (D) map.map1.get((Integer)n);
            
            if(map_ == null) map_ = (D) new HashMap<List<String>, MethodLink> ();
            // set current iterator
            this.current = (Iterator<T>) map_.entrySet().iterator();
            // set base iterattor
            this.base = (Iterator<Map.Entry<Integer, D>>) map.map2.entrySet().iterator();
        }
        public T next() throws NoSuchElementException 
        {
            if (this.hasNext())
            {
                return this.current.next();
            }
            else
            {
                throw new NoSuchElementException();
            }
        }
        public boolean hasNext()
        {
            int i;
            // until current element has next element, iterate
            Iterator <T> c = this.current;
            Iterator <Map.Entry<Integer, D>> b = this.base;
            Map.Entry<Integer, D> entry;
            
       //     System.out.println(c.hasNext());
       //     System.out.println(c.hasNext());
            
            
            while (!c.hasNext())
            {
                if (!b.hasNext())
                {
      //              System.out.println("218");
                    return false;
                }
                else
                {
                    // get entry
                    entry = b.next();
                    // get integer value
                    i = (int)entry.getKey();
                    // if i bigger that this size, 
                    // then set empty iterator
                    // and return false
                    if ((i) > this.size)
                    {
                        this.base =
                           new HashMap<Integer, D>().entrySet().iterator();
      //                     System.out.println("234");
                        return false;
                    }
                    else
                    {
                        c = (Iterator<T>)entry.getValue().entrySet().iterator();
        //                System.out.println(c.hasNext());
          //              System.out.println(entry.getValue());
            //            System.out.println(c.hasNext());
                        this.current = c;
                    }
                } 
            }
            return true;
        }
    }
    
    private TreeMap<Integer, HashMap<List<String>, MethodLink> > map1 = new TreeMap<>();
    private TreeMap<Integer, HashMap<List<String>, MethodLink> > map2 = new TreeMap<>();
    
  //  private HashMap map3 = new HashMap<>();
    
    public void clear()
    {
        map1.clear();
        map2.clear();
    }
    
    public Object clone()
    {
        MethodMap map = new MethodMap<>();
        
        TreeMap tree = map.map1;
        
        for (Map.Entry i: this.map1.entrySet())
        {
            tree.put(i.getKey(), ((HashMap)i.getValue()).clone());
        }
        
        tree = map.map2;
        
        for (Map.Entry i: this.map2.entrySet())
        {
            tree.put(i.getKey(), ((HashMap)i.getValue()).clone());
        }
        
    //    map.map3 = (HashMap)(this.map3.clone());
        return map;
    }
    
    public Set<Map.Entry<A,B>> entrySet()
    {
        // get all sets from map1 and map2
        Set set = new HashSet<>();
        //
        for (Map.Entry<Integer, HashMap<List<String>, MethodLink> > i : map1.entrySet())
        {
            set.addAll(i.getValue().entrySet());
        }
        //
        for (Map.Entry<Integer, HashMap<List<String>, MethodLink> > i : map2.entrySet())
        {
            set.addAll(i.getValue().entrySet());
        }
        
        return set;
    }
    
    public B put(A key, B value)
    {
        // 
        if (key instanceof List)
        {
            if (value instanceof MethodLink)
            {
                if (!this.containsKey(key))
                {
                    // this is an key
                    List list = (List) key;
                    // this is an size of key
                    Integer size = list.size();
                    // this is an value 
                    MethodLink link = (MethodLink) value;
                    // list and link.list must be same
                    if (!link.getList().equals(list))
                    {
                        return (B) value;
                    }
                    // this is an map by default
                    HashMap <List<String>, MethodLink> map;// = new HashMap<>();
                    // if method have variable arguments, then 
                    if ((link.access & ACC_VARARGS) > 0)
                    {
                        map = map2.get(size);
                        // if got null ,then put new map
                        if (map == null) map2.put(size, map = new HashMap<>());
                    }
                    // else
                    else
                    {
                        map = map1.get(size);
                        // if got null, then put new map
                        if (map == null) map1.put(size, map = new HashMap<>());
                    }
                    // add key and value to the map
                    map.put(list, link);
                }
            }
        }
        
        
        return (B)value;
    }
    
    public B remove(Object key) 
    {
        B object = null;
        B object2 = null;
        // if key is list, then remove this key from maps
        if (key instanceof List)
        {
            // 
            List list = (List) key;
            // 
            int size = list.size();
            // 
            HashMap map = this.map1.get(key);
            // if map is not null, then remove from map
            if (map != null) object = (B) map.remove(key);
            // 
            map = this.map2.get(key);
            // if map is not null, then remove from map
            if (map != null) object2 = (B) map.remove(key);
        }
        //
        if (object == null) return object2;
        else return object;
    }
    
    public B get(Object key)
    {
        // this is an object for return
        B ret = null;
        // 
        if (key instanceof List)
        {
            List list = (List) key;
            // get size
            int size = list.size();
            // check if contains key
            HashMap map = this.map1.get(size);
            // if got map
            if (map != null)
            {
                // check if map contains key
                if ((ret = (B)map.get(key)) != null)
                {
                    return ret;
                }
            }
            {
                map = this.map2.get(size);
                // if map is null, then return false
                if (map == null) return null;
                // else, check 
                else return (B)map.get(key);
            }
        }
        {
            return null;
//            return (B)this.map3.get(key);
        }
    }
    
    public boolean containsValue(Object value)
        {
            // check if value is methodlink
            if (value instanceof MethodLink)
            {
                // then just get if contains key
                return this.containsKey(((MethodLink)value).getList());
            }
            return false;
        }
    
    public boolean containsKey(Object key)
    {
        if (key instanceof List)
        {
            List list = (List) key;
            // get size
            int size = list.size();
            // check if contains key
            HashMap map = this.map1.get(size);
            // if got map
            if (map != null)
            {
                // check if map contains key
                if (map.containsKey(key))
                {
                    return true;
                }
            }
            
            {
                map = this.map2.get(size);
                // if map is null, then return false
                if (map == null) return false;
                // else, check 
                else return map.containsKey(key);
            }
        }
        {
            return false;
            //return this.map3.containsKey(key);
        }
    }

public static class IntIterator
{
    public int value;
    public IntIterator next = null;
    public void remove()
    {
        IntIterator iter = next;
        if (next == null) return;
        this.value = iter.value;
        this.next = iter.next;
    }
}
public static class MethodHandler
{
    
    public MethodHandler(AST ast)
    {
        this.ast = ast;
    };
    
    private MethodHandler()
    {
        
    };
    // this method will return an method with object names from classobject and parents objects
    // 
    public MethodLink searchMethod(MethodMap map, String ... values)
    {
        
        return searchMethod(map, Arrays.asList(values));
    }
    public MethodLink searchMethod(MethodMap map, List<String> values)
    {
        
//       System.out.println(values);
        
   //     System.out.println(map);
   //     System.out.println(map.get(values));
        
        int values_size = values.size();
        // get map of methodlink with descriptions
      //  MethodMap<List<String>, MethodLink> map = this.object.methods.get(name);
        // if got null, then return null
        if (map == null)
        {
//           System.out.println("map is null");
            return ClassTable.non_exists_method;
        }
        // if values list is zero, then return first elements from methodmap
     //   if (values_size == 0)
     //   {
        //    System.out.println("no values");
     //       return (MethodLink)map.getOrDefault(values, ClassTable.non_exists_method);
            
     //   }
        // get iterator
        Iterator<Map.Entry<List<String>, MethodLink>> iter = map.iterator(values.size());
        // this is an entry map
        Map.Entry<List<String>, MethodLink> entry;
        List<Integer> temp;
        List<Integer> temp2;
        
        List<String> temp3;
        MethodLink temp4;
        // this is an suitable argument list
        List<List<Integer>> indexes = new ArrayList<>();
        List<List<Integer>> indexes2 = new ArrayList<>();
   //     List<List<String>> arglist = new ArrayList<>();
        List<MethodLink> methods = new ArrayList<>();
        List<MethodLink> methods2 = new ArrayList<>();
        // iterate over entry set
  //      System.out.println("ITER:" + iter.hasNext());
    //    System.out.println("ITER:" + iter.hasNext());
        
        while (iter.hasNext())
        {
      //      System.out.println(iter.hasNext());
            // get next entry
            entry = iter.next();
            
        //    System.out.println(entry);
            
            temp3 = entry.getKey();
            temp4 = entry.getValue();
            
     //       System.out.println(temp4);
            
      //      System.out.println("ENTRY::"+entry);
            // get args list
            temp = (checkArgs(temp3, values));
            // if got, then add
            if (temp != null)
            {
                // add args list
                indexes.add(temp);
                // add method to the list
                methods.add(temp4);
            }
            
          if ((temp4.access & ACC_VARARGS) > 0)
          {
                // get args list
            temp = (checkVarArgs(temp3, values));
            // if got, then add
            if (temp != null)
            {
                // add args list
                indexes2.add(temp);
                // add method to the list
                methods2.add(temp4);
            }
          }
        }
    //    System.out.println(methods);
     //   System.out.println(methods2);
        
        MethodLink link = searchMethod(indexes, methods, values_size);
        // if got not found methodlink
        if (link == ClassTable.non_exists_method)
        {
            link = searchMethod(indexes2, methods2, values_size);
        }
        return link;
    }
    public MethodLink searchMethod(IntIterator list, List<MethodLink> methods)
    {
        if (list.next.next == null)
        {
            return methods.get(list.value);
        }
        else
        {
            return ClassTable.ambiguous_method;
        }
    }
    // this method will return an suitable methodlink
    public MethodLink searchMethod(List<List<Integer>> indexes, List<MethodLink> methods, int values_size)
    {
        List<Integer> temp, temp2;
        // if suitable argumen list is zero, then return first method
        if (indexes.size() == 1)
        {
            return methods.get(0);
        }
        // 
        else if (indexes.size() == 0)
        {
            //
        //    System.out.println("no suitable method"); 
            //
            return ClassTable.non_exists_method;
        }
        // 
        else
        {
            // this is an list of leader indexes
            IntIterator list = new IntIterator();
            IntIterator node = list;
            // this is an list size 
            int len2 = 0;
            //
            int min = indexes.get(0).get(0);
            // check for all indexes
            int len = indexes.size();
            
            for (int i = len - 1, n = 0; i > 0; i --)
            {
                // get index
                n = indexes.get(i).get(0);
                // if got minimal, then reset
                if (n < min) min = n;
            }
            // check all indexes with same minimal value
            for (int i = 0; i < len; i ++)
            {
                if (indexes.get(i).get(0) == min)
                {
                    node.value = i;
                    node = node.next = new IntIterator();
                    len2 ++;
                }
            }
            // if got list of size of 1, then return first element
            // 
            if (values_size <= 1)
            {
                return searchMethod(list, methods);
            }
            else
            {
                if (len2 > 1)
                {
                    // iterate over list
                    for (int i = 1; i < values_size; i++)
                    {
                        // iterate over iterator
                        node = list.next;
                        // set min element
                        min = indexes.get(list.value).get(i);
                        // if node next element is null, then break;
                        if (node.next == null) 
                        {
                            break;
                        }
                        // while node has next element
                        do
                        {
                            // 
                            int g = indexes.get(node.value).get(i);
                            // if g is lesser that min
                            if (g < min)
                            {
                                // then set list iterator equals to node
                                list = node;
                                // set min element
                                min = g;
                            }
                            // if g is greater, then remove that element from list
                            else if (g > min)
                            {
                                node.remove();
                                continue;
                            }
                            node = node.next;
                        }
                        while (node.next != null);
                    }
                }
                // check if len is 1
                
                    int u = list.value;
                    // if yes, then check if method is not ambiguous
                    temp = indexes.get(u);
                    // 
                    for (int i = 0; i < len; i++)
                    {
                        // if got same object, then continue
                        if (u == i)
                        {
                            continue;
                        }
                        else
                        {
                            // check that all indexes is lesser 
                            temp2 = indexes.get(i);
                            for (int cz = 1; cz < values_size; cz ++)
                            {
                                if (temp.get(cz) > temp2.get(cz))
                                {
                                    
                                    //
        //                            System.out.println("got index level");
                   //                 System.out.println(indexes);
                                    return ClassTable.ambiguous_method;
                                }
                            } 
                        }
                    }
                return searchMethod(list, methods);
            }
        }
    }
    
    
    // this method will check two descriptions for equality 
    // 0 - better equality
    public int checkDescA(String a1, String a2)
    {
     //   System.out.println(a1 + " " + a2);
        int i1 = 0;
        int i2 = 0;
     //   System.out.println(a1 + " " +  a2);
        // a1 - function base argument type
        // a2 - argument type
        if (a2.equals("-"))
        {
            if (a1.length() > 1)
            {
                char i = a1.charAt(0);
                if (i == 'L')
                {
            //        System.out.println(0);
                    return 0;
                }
                else if (i == '[')
                {
          //          System.out.println(0);
                    return 0;
                }
            }
        //    System.out.println(-1);
            return -1;
        }
        else if (a2.equals("V"))
        {
      //      System.out.println(-1);
            return -1;
        }
        else if (a1.equals("-"))
        {
    //        System.out.println(-1);
            return -1;
        }
        else if (a1.equals("V"))
        {
  //          System.out.println(-1);
            return -1;
        }
        // 
        if (
             (a1.charAt(0) == '[') || (a2.charAt(0) == '[')
           )
        {
            i1 = a1.lastIndexOf('[') + 1;
            i2 = a2.lastIndexOf('[') + 1;
            //
            
            a1 = a1.substring(i1);
            a2 = a2.substring(i2);
                
            if (i1 < i2)
            {
                if (a1.equals(ClassObject.obj_name_desc))
                {
          //          System.out.println(MethodMap.obj_ref + i2 - i1);
                    return MethodMap.obj_ref + i2 - i1;
                }
                else if (a1.equals(ArrayType.clone_able_desc))
                {
        //            System.out.println(1);
                    return 1;
                }
            }
            
            if (i1 != i2)
            {
       //         System.out.println(-1);
                return -1;
            }
        }
        
   //     System.out.println(a1);
   //     System.out.println(a2);
        // if these name are equal, then return best equality
        if (a1.equals(a2))
        {
      //      System.out.println("BEST");
            return 0;
        }
        // if base name is object, then return object equality
        if (a1.equals(ClassObject.obj_name_desc))
        {
       //     System.out.println(MethodMap.obj_ref);
            return MethodMap.obj_ref;
        }
        // else, check if a2 is an reference type
        boolean ref_a1 = a1.charAt(0) == 'L';
        boolean ref_a2 = a2.charAt(0) == 'L';
        
        if (ref_a1 && ref_a2)
        {
             return refExtendLevel(a2, a1);
        }
        else
        {
            return -1;
        }
    }
    
    // this method will check two descriptions for equality 
    // 0 - better equality
    public int checkDesc(String a1, String a2)
    {
      //  System.out.println(a1 + " " + a2);
        int i1 = 0;
        int i2 = 0;
     //   System.out.println(a1 + " " +  a2);
        // a1 - function base argument type
        // a2 - argument type
        if (a2.equals("-"))
        {
            if (a1.length() > 1)
            {
                char i = a1.charAt(0);
                if (i == 'L')
                {
          //          System.out.println(0);
                    return 0;
                }
                else if (i == '[')
                {
         //           System.out.println(0);
                    return 0;
                }
            }
    //        System.out.println(-1);
            return -1;
        }
        else if (a2.equals("V"))
        {
     //       System.out.println(-1);
            return -1;
        }
        else if (a1.equals("-"))
        {
     //       System.out.println(-1);
            return -1;
        }
        else if (a1.equals("V"))
        {
     //       System.out.println(-1);
            return -1;
        }
        // 
        if (
             (a1.charAt(0) == '[') || (a2.charAt(0) == '[')
           )
        {
            i1 = a1.lastIndexOf('[') + 1;
            i2 = a2.lastIndexOf('[') + 1;
            //
            
            a1 = a1.substring(i1);
            a2 = a2.substring(i2);
                
            if (i1 < i2)
            {
                if (a1.equals(ClassObject.obj_name_desc))
                {
           //         System.out.println(MethodMap.obj_ref + i2 - i1);
                    return MethodMap.obj_ref + i2 - i1;
                }
                else if (a1.equals(ArrayType.clone_able_desc))
                {
         //           System.out.println(1);
                    return 1;
                }
            }
            
            if (i1 != i2)
            {
       //         System.out.println(-1);
                return -1;
            }
        }
   //     System.out.println(a1);
   //     System.out.println(a2);
        // if these name are equal, then return best equality
        if (a1.equals(a2))
        {
         //   System.out.println("BEST");
            return 0;
        }
        // if base name is object, then return object equality
        if (a1.equals(ClassObject.obj_name_desc))
        {
     //       System.out.println(MethodMap.obj_ref);
            return MethodMap.obj_ref;
        }
        
        // else, check if a2 is an reference type
        boolean prim_a1 = a1.charAt(0) != 'L';
        boolean prim_a2 = a2.charAt(0) != 'L';
        
  //      if ()
  //      {
            
  //      }
        
        if (i1 > 0)
        {
            if (prim_a1 | prim_a2)
            {
    //            System.out.println(-1);
                return -1;
            }
        }
        
        if (!prim_a2)
        {
            // check if base type is reference type, else, get get an primitive wrapper
            if (prim_a1)
            {
                // get wrapper object
                a2 = ClassTable.primitives.get(a2);
                // if got null, then return -1
                if (a2 == null)
                {
       //             System.out.println(-1);
                    return -1;
                }
                if (a1.equals(a2)) return 1;
                // if a1 or a2 is an boolean object, then return -1
                if (a1.equals("Z") || a2.equals("Z"))
                {
         //           System.out.println(-1);
                    return -1;
                }
                // get indexes
                i1 = ClassTable.numbers.indexOf(a1);
                i2 = ClassTable.numbers.indexOf(a2);
                // if one of them is char and one of then is byte or short, then return -1
                if ((i1 < 2) & (i2 < 2) & ((i1 == -1) || (i2 == -1)))
                {
       //             System.out.println(-1);
                    return -1;
                }
                //
              //  if (i2 == -1) i2 == 2;
                //
                i1 = i1 - i2;
                // if i1 < -1, then return -1
       //         System.out.println((i1 < -1) ? -1 : 1 + i1);
                return (i1 < -1) ? -1 : 1 + i1;
            }
            else
            {
                // 
         //      System.out.println("cool");
     //    System.out.println("" + a2 + " " +  a1);
                // if there both are not primitive types, then just return their accesslevel
                
     //           System.out.println(refExtendLevel(a2, a1));
                return refExtendLevel(a2, a1);
            }
        }
        else
        {
            // check if second type is primitive type, else, return 
            if (prim_a1)
            {
        //        System.out.println(a1 + " " + a2);
                // if a1 or a2 is an boolean object, then return -1
                if (a1.equals("Z") || a2.equals("Z"))
                {
             //       System.out.println(-1);
                    return -1;
                }
                // get indexes
                i1 = ClassTable.numbers.indexOf(a1);
                i2 = ClassTable.numbers.indexOf(a2);
                
        //        System.out.println("" + i1 + " " + i2);
                // if one of them is char and one of then is byte or short, then return -1
                if ((i1 < 2) & (i2 < 2) & ((i1 == -1) || (i2 == -1)))
                {
           //         System.out.println(-1);
                    return -1;
                }
                
                //
                if (i2 == -1) i2 = 2;
                //
                i1 = i1 - i2;
                // if i1 < -1, then return -1
          //      System.out.println((i1 < -1) ? -1 : i1);
                return (i1 < -1) ? -1 : i1;
            }
            else
            {
             //   System.out.println("a1'"+a1);
             //   System.out.println("a2'"+a2);
                // get wrapper object
                a1 = ClassTable.primitives.get(a1);
                
                // if got null, then return null
                if (a1 == null)
                {
                    return -1;
                }
                
                if (a1.equals(a2))
                {
                    if (a1.equals("Z"))
                    {
                        return 1;
                    }
                    else if (a1.equals("C"))
                    {
               //         System.out.println(4);
                        return 6 - 2;
                    }
                    else
                    {
              //          System.out.println(
                //        6 - ClassTable.numbers.indexOf(a1));
                        return 6 - ClassTable.numbers.indexOf(a1);
                    }
                }
                // if got null, then return -1
                if (a2 == null)
                {
       //             System.out.println(-1);
                    return -1;
                }
                // if a1 or a2 is an boolean object, then return -1
                if (a1.equals("Z") || a2.equals("Z"))
                {
         //           System.out.println(-1);
                    return -1;
                }
                // get indexes
                i1 = ClassTable.numbers.indexOf(a1);
                i2 = ClassTable.numbers.indexOf(a2);
                // if one of them is char and one of then is byte or short, then return -1
                if ((i1 < 2) & (i2 < 2) & ((i1 == -1) || (i2 == -1)))
                {
        //            System.out.println(-1);
                    return -1;
                }
                //
              //  if (i2 == -1) i2 == 2;
                //
                i1 = i1 - i2;
                // if i1 < -1, then return -1
   //             System.out.println((i1 < -1) ? -1 : (6 - i2) + i1);
                return (i1 < -1) ? -1 : (6 - i2) + i1;
            }
        }
    }
    
    // this is an ast object
    public AST ast;

    // ref1 - base type
    // ref2 - parent type    
    // this method will return an references access level
    public int refExtendLevel(String ref1, String ref2)
    {
        //
        ref1 = ref1.substring(1, ref1.length() - 1);
        ref2 = ref2.substring(1, ref2.length() - 1);
        // get class by name
        ClassObject object = this.ast.getClassByName(ref1);
        // return extend level of got class
        return object.getTable().extendLevel(ref2);
    }
    
    // this method will check method arguments for compatibility
    public List<Integer> checkArgs(List<String> base, List<String> recipient)
    {
        // get base length
        int base_len = base.size();
        // get recipient length
        int rec_len = recipient.size();
        List ret;
        // if base > recipient, then return
        if (base_len != rec_len)
        {
            return null;
        }
        else
        {
            ret = new ArrayList<>(rec_len);
        }
        
        String object_name = "";
        String rec_name = "";
  //      object = this.ast.getClassByName(base.get(0));
            
        for (int i = 0; i < base_len; i ++)
        {
            // check access level
            object_name = base.get(i);
            // 
            rec_name  = recipient.get(i);

    //      System.out.println("base recipient::"+object_name+ " " +rec_name);
            //
      //     System.out.println(object);
            
            int z = this.checkDesc(object_name, rec_name);
            
      //      System.out.println("Z" + z);
            
            // if got -1, then return emp
            if ( z == -1)
            {
                // 
      //          System.out.println("fuck");
                //
                return null;
            }
            else
            {
                ret.add(z);
            }
        }
        return ret;
    }
    
    // this method will check method arguments for compatibility
    public List<Integer> checkVarArgs(List<String> base, List<String> recipient)
    {
        // get base length
        int base_len = base.size();
        // get recipient length
        int rec_len = recipient.size();
        List ret;
        // if base > recipient, then return
        if (base_len > (rec_len + 1))
        {
            return null;
        }
        else if (base_len == 0)
        {
            return null;
        }
        else
        {
            ret = new ArrayList<>(rec_len);
        }
        
        base_len --;
        
        String object_name = "";
        String rec_name = "";
  //      object = this.ast.getClassByName(base.get(0));
            
        for (int i = 0; i < base_len; i ++)
        {
            // check access level
            object_name = base.get(i);
            // 
            rec_name  = recipient.get(i);

    //      System.out.println("base recipient::"+object_name+ " " +rec_name);
            //
      //     System.out.println(object);
            
            int z = this.checkDesc(object_name, rec_name);
            
      //      System.out.println("Z" + z);
            
            // if got -1, then return emp
            if ( z == -1)
            {
                // 
      //          System.out.println("fuck");
                //
                return null;
            }
            else
            {
                ret.add(z);
            }
        }
        
        object_name = base.get(base_len).substring(1);
        // because last object name started with [, get substring of object name
        
        while (base_len < rec_len)
        {
            int z = this.checkDesc(object_name, recipient.get(base_len++));
            // if got -1, then return null
            if (z == -1)
            {
                return null;
            }
            else
            {
                ret.add(z);
            }
        }
        
 //       System.out.println("fun");
        
        return ret;
    }
}    
}

