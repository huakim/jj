package com.ejavac;
public class ParameterAnnotationLink extends AnnotationLink
{
    public int parameter;
    
    public ParameterAnnotationLink()
    {
        super();
    }
    
    public String toString()
    {
        return "{descriptor: " + this.descriptor + 
               "; visible: " + this.visible + 
               "; parameter: " + this.parameter + 
               "; values: " + this.values + " }";
    }
    
    public ParameterAnnotationLink(int parameter, 
                              String descriptor, boolean visible)
    {
        super(descriptor, visible);
        this.parameter = parameter;
    }
    
    
    
}
