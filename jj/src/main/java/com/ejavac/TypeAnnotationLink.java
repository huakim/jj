package com.ejavac;
import org.objectweb.asm.*;
import java.util.*;

public class TypeAnnotationLink extends AnnotationLink
{
    public TypeReference typeRef;
    public TypePath typePath;
    
    public TypeAnnotationLink()
    {
        super();
    }
    
    public String toString()
    {
        return "{descriptor: " + this.descriptor + 
               "; visible: " + this.visible + 
               "; typePath: " + this.typePath + 
               "; typeRef: " + this.typeRef.getValue() + 
               "; values: " + this.values + " }";
    }
    
    public TypeAnnotationLink(int typeRef, TypePath typePath, AnnotationLink link)
    {
        this(typeRef, typePath, link.descriptor, link.visible);;
    }
    
    public TypeAnnotationLink(int typeRef, TypePath typePath, 
                              String descriptor, boolean visible)
    {
        super(descriptor, visible);
        this.typeRef = new TypeReference(typeRef);
        this.typePath = typePath;
    }
    
    
    
}
