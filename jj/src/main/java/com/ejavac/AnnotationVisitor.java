package com.ejavac;// this is an class for visit an annotation
abstract public class AnnotationVisitor
{    
    public AST ast;
    public AnnotationVisitor(AST ast)
    {
        this.ast = ast;
    }
    
    public Object visitClassAnnotation(AnnotationLink link, ClassObject this1)
    {
        return null;
    }
    /*
    public ClassObject getClassObject(Object value)
    {
        if (value instanceof ASTExpr.ClassConstName)
        {
            String name = ((ASTExpr.ClassConstName)value).name;
            name = name.substring(1, name.length() - 2);
            return ast.getClassByName(name);
        }
        else if (value instanceof ASTExpr.ClassConst)
        {
            return ((ASTExpr.ClassConst)value).object;
        }
        else
        {
            return null;
        }
    }
    
    public Object visitClassAnnotation(AnnotationLink link, ClassObject this1)
    {
        
        System.out.println(link);
        
        String desc = link.descriptor;
        if (desc.length() < 3)
        {
            return null;
        }
        else
        {
            desc = desc.substring(1, desc.length() -1);
            System.out.println(desc);
            ClassObject object = this.ast.getClassByName(desc);
            object.link2();
            // check if object is repeatable
            AnnotationLink link1 = object.getAnnotation(ClassObject.note_rep_desc);
            if (link1 != null)
            {
                // get class 
                ClassObject object1 = getClassObject(link1.getValue("value"));
                // get if same object there
                link1 = this1.getAnnotation(objec1.descriptor);
                // if got object, then add an annotation to values
                if (link1 != null)
                {
                    List list = link1.getValue("value");
                    list.add(link);
                }
                // 
            }
            else
            {
                
            }
            //
        }
        return null;
    }*/
}
