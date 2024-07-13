package com.ejavac;

import java.util.*;


public class InheritanceMap<B, A>
{
    public class Node
    {
        public A value;
        public Node parent;
        public boolean inheritedBy(A def)
        {
            Node n = this;
            while (n != null)
            {
                if (n.value.equals(def)) return true;
                n = n.parent;
            }
            return false;
        }
        
        public Node inherit(B key, A def)
        {
            this.parent = get(key, def);
            return this;
        }
        
        public Node inherit(Node node)
        {
            this.parent = node;
            return this;
        }
    }
    
    public Map<B, Node> map = new HashMap<>();
    
    public Node put(B key, Node n)
    {
        map.put(key, n);
        return n;
    }
    
    public Node get(B key, A def)
    {
        Node n = map.get(key);
        if (n == null)
        {
            n = new Node();
            n.value = def;
        }
        
   //     n.value = def;
        
        map.put(key, n);
        return n;
    }

}
