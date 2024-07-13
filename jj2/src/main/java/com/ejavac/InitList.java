package com.ejavac;
import java.util.AbstractList;

public class InitList extends AbstractList{
   
   private Object bytebuffer;
   private int size;
   private String source;
   
   public InitList(int i, Object buf, String source){
       this.size = i;
       this.bytebuffer = buf;
       this.source = source;
   }
   
   public native Object get(int i);

   public int size(){
       return this.size;
   };
}
