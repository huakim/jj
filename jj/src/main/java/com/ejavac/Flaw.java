package com.ejavac;import java.util.*;

public class Flaw
{
    static public void error(String name, Token token, AST ast)
    {
        int p = token.position;
        ast.error_pool.add(new Flaw(Flaw.type.custom_flaw, 
            token.source, token.line, p, 
            name));
    }
    static public enum type
    {
       // package annotations should be in file package-info.java
       pkg_info,
       pkg_exists,
       cls_duplicate,
       ref_ambiguous,
       pkg_n_exists,
       n_public_class,
       private_class,
       cls_broken,
       symbol_not_found,
       is_already_def,
       is_already_def_import,
       pkg_same_name,
       interface_n_exp,
       interface_exp,
       final_n_exp,
       enum_n_exp,
       already_def,
       repeated_interface,
       primitive_deref,
       identifier_exp,
       void_not_exp,
       void_array_not_exp,
       type_cyclic,
       invalid_note_type,
       non_static,
       too_many_dim,
       const_expr,
       not_allowable_type,
       duplicate_element,
       missing_value,
       cannot_override,
       cannot_override_ret,
       need_override,
       unrelated_defaults,
       unrelated_method_types,
       unrelated_abstract_and_default,
       cyclic_inheritance_involving,
       incompatible_types,
       method_not_found,
       no_suitable_operator,
       enclosing_instance_required,
       array_required,
       variable_expected,
       class_or_array_expected,
       is_not_statement,
       custom_flaw,
       break_outside,
       continue_outside,
       undefined_label,
       cannot_infer,
       parent_const_first,
       this_const_first,
       parent_const_denied,
       recursive_constructor,
       call_enum_constructor
    }
    
    public static interface flaw
    {
        String get(String ... names);
    }
    
    public static List error_name;
    
    static
    {
            error_name = new ArrayList(2);
            error_name.add("error");
            error_name.add("warning");
    };
    
    public static Map<type, flaw> flaws = new HashMap<>();
    
    static
    {
        flaws.put(type.call_enum_constructor, new flaw()
        {
            public String get(String ... names)
            {
                return "call to super not allowed in enum constructor";
            }
        });
        flaws.put(type.recursive_constructor, new flaw()
        {
            public String get(String ... names)
            {
                return "recursive constructor invocation";
            }
        });
        flaws.put(type.parent_const_denied, new flaw()
        {
            public String get(String ... names)
            {
                return "call to super not allowed in enum constructor";
            }
        });
        flaws.put(type.parent_const_first, new flaw()
        {
            public String get(String ... names)
            {
                return "call to super must be first statement in constructor";
            }
        });
        flaws.put(type.this_const_first, new flaw()
        {
            public String get(String ... names)
            {
                return "call to this must be first statement in constructor";
            }
        });
        flaws.put(type.cannot_infer, new flaw()
        {
            public String get(String ... names)
            {
                return "cannot infer type for local variable";
            }
        });
        flaws.put(type.undefined_label, new flaw()
        {
            public String get(String ... names)
            {
                return "undefined label: "+names[0];
            }
        });
        flaws.put(type.break_outside, new flaw()
        {
            public String get(String ... names)
            {
                return "break outside switch or loop";
            }
        });
        flaws.put(type.continue_outside, new flaw()
        {
            public String get(String ... names)
            {
                return "continue outside loop";
            }
        });
        flaws.put(type.custom_flaw, new flaw()
        {
            public String get(String ... names)
            {
                return names[0];
            }
        });
        flaws.put(type.is_not_statement, new flaw()
        {
            public String get(String ... names)
            {
                return "is not statement";
            }
        });
        flaws.put(type.variable_expected, new flaw()
        {
            public String get(String ... names)
            {
                return "variable type is expected";
            }
        });
        flaws.put(type.class_or_array_expected, new flaw()
        {
            public String get(String ... names)
            {
                return "class or array type is expected";
            }
        });
        flaws.put(type.array_required, new flaw()
        {
            public String get(String ... names)
            {
                return "array object is required";
            } 
        });
        flaws.put(type.enclosing_instance_required, new flaw()
        {
            public String get(String ... names)
            {
                return "an enclosing instance that contains "
                             +names[0]+" is required";
            }
        });
        flaws.put(type.no_suitable_operator, new flaw()
        {
            public String get(String ... names)
            {
                return "no suitable operator for " + names[0]+" found";
            }
        });
        flaws.put(type.method_not_found, new flaw()
        {
            public String get(String ... names)
            {
                return "no suitable method for " + names[0]+" found";
            }
        });
        flaws.put(type.incompatible_types, new flaw()
        {
            public String get(String ... names)
            {
                return "incompatible types: "+names[0]+
                     " cannot be converted to "+names[1];
            } 
        });
        flaws.put(type.cyclic_inheritance_involving, new flaw()
        {
            public String get(String ... names)
            {
                return "cyclic inheritance involving "+names[0];
            }
        }
        );
        flaws.put(type.unrelated_defaults, new flaw()
        {
            public String get(String ... names)
            {
                return "types "+names[0]+" and "+names[1] + 
                " have unrelated defaults for method "+ names[2]; 
            }
        }
        );
        flaws.put(type.unrelated_abstract_and_default, new flaw()
        {
            public String get(String ... names)
            {
                return "types "+names[0]+" and "+names[1] + 
                " are abstract and default for method "+ names[2]; 
            }
        }
        );
        flaws.put(type.unrelated_method_types, new flaw()
        {
            public String get(String ... names)
            {
                return "types "+names[0]+" and "+names[1] + 
                " have unrelated types for method "+ names[2]; 
            }
        }
        );
        flaws.put(type.need_override, new flaw()
        {
            public String get(String ... names)
            {
                return names[0]+
                " is not abstract and does not override abstract method "
                + names[1] + " in " + names[2];
            }
        }
        );
        flaws.put(type.cannot_override_ret, new flaw()
        {
            public String get(String ... names)
            {
                // this method will raise return type not compatible error
                return names[0]+" in "+names[1]+" cannot override "+names[2]+
                " in "+names[3]+": return type "+names[4]+
                " is not compatible with "+names[5];
            }
        }
        );
        flaws.put(type.cannot_override, new flaw()
        {
            public String get(String ... names)
            {
                //String out = names[0]+" in "+names[1]+" cannot override "+names[2]+" in "+names[3];
                String out = names[4];
                //
                if (out.equals("static"))
                {
                    return names[0]+" in "+names[1]+" cannot override "+names[2]+
                     " in "+names[3] + ": overriden method is static";
                }
                else if (out.equals("final"))
                {
                    return names[0]+" in "+names[1]+" cannot override "+names[2]+
                     " in "+names[3] + ": overriding method is final";
                }
                else if (out.equals("dynamic"))
                {
                    return names[0]+" in "+names[1]+" cannot override "+names[2]+
                     " in "+names[3] + ": overriding method is static";
                }
                else if (out.equals("public"))
                {
                    return names[0]+" in "+names[1]+" cannot override "+names[2]+
                     " in "+names[3] + ": attempting to assign weaker access "+
                     "privileges; was public";
                }
                else if (out.equals("protected"))
                {
                    return names[0]+" in "+names[1]+" cannot override "+names[2]+
                     " in "+names[3] + ": attempting to assign weaker access "+
                     "privileges; was protected";
                }
                else if (out.equals("package"))
                {
                    return names[0]+" in "+names[1]+" cannot override "+names[2]+
                     " in "+names[3] + ": attempting to assign weaker access "+
                     "privileges; was package";
                }
                else
                {
                    return "tuna iti kachan, vivche";
                }
            }
        }
        );
        flaws.put(type.missing_value, new flaw()
        {
            public String get(String ... names)
            {
                return "annotation "+names[0]+" is missing a default value for the element '"+names[1]+"'";
            }
        }
        );
        flaws.put(type.duplicate_element, new flaw()
        {
            public String get(String ... names)
            {
                return "duplicate element '"+names[0]+"'";
            }
        }
        );
        flaws.put(type.not_allowable_type, new flaw()
        {
            public String get(String ... names)
            {
                return "annotation value '"+names[0]+"' not of an allowable type";
            }
        }
        );
        flaws.put(type.const_expr, new flaw()
        {
            public String get(String ... names)
            {
                return "element value must be a constant expression";
            }
        }
        );
        flaws.put(type.too_many_dim, new flaw()
        {
            public String get(String ... names)
            {
                return "array type has too many dimensions";
            }
        }
        );
        flaws.put(type.invalid_note_type, new flaw()
        {
            public String get(String ... names)
            {
                return "invalid type for annotation type element";
            }
        }
        );
        flaws.put(type.non_static, new flaw()
        {
            public String get(String ... names)
            {
                return "non-static variable "+names[0]+
                " cannot be referenced from a static context";
            }
        }
        );
        flaws.put(type.void_array_not_exp, new flaw()
        {
            public String get(String ... names)
            {
                return "array of type 'void' is not expected here";
            }
        }
        );
        flaws.put(type.void_not_exp, new flaw()
        {
            public String get(String ... names)
            {
                return "'void' is not expected here";
            }
        }
        );
        flaws.put(type.type_cyclic, new flaw()
        {
            public String get(String ... names)
            {
                return "type is cyclic";
            }
        }
        );
        flaws.put(type.identifier_exp, new flaw()
        {
            public String get(String ... names)
            {
                return "<identifier> expected";
            }
        }
        );
        flaws.put(type.primitive_deref, new flaw()
        {
            public String get(String ... names)
            {
                return names[0] + " cannot be dereferenced";
            }
        }
        );
        flaws.put(type.repeated_interface, new flaw()
        {
            public String get(String ... names)
            {
                return "repeated interfaces";
            }
        }
        );
        flaws.put(type.already_def, new flaw()
        {
            public String get(String ... names)
            {
                return names[0] + " " + names[1] + " is already defined";
            }
        }
        );
        flaws.put(type.pkg_same_name, new flaw()
        {
            public String get(String ... names)
            {
                return "class " + names[0]  +" clashes with package of the same name: " + names[1];
            }
        }
        );
        flaws.put(type.interface_n_exp, new flaw()
        {
            public String get(String ... names)
            {
                return "no interface expected here";
            }
        }
        );
        flaws.put(type.interface_exp, new flaw()
        {
            public String get(String ... names)
            {
                return "interface expected here";
            }
        }
        );
        flaws.put(type.enum_n_exp, new flaw()
        {
            public String get(String ... names)
            {
                return "enum types are not extensible";
            }
        }
        );
        flaws.put(type.final_n_exp, new flaw()
        {
            public String get(String ... names)
            {
                return "final classes are not extensible";
            }
        }
        );
        flaws.put(type.ref_ambiguous, new flaw()
        {
            public String get(String ... names)
            {
                return "reference to " + names[0]  +" is ambiguous";
            }
        }
        );
        flaws.put(type.pkg_info, new flaw()
        {
            public String get(String ... names)
            {
                return "package annotations should be in file package-info.java";
            }
        }
        );
        flaws.put(type.pkg_exists, new flaw()
        {
            public String get(String ... names)
            {
                return "package exists in another module: " + names[0];
            }
        }
        );
        flaws.put(type.pkg_n_exists, new flaw()
        {
            public String get(String ... names)
            {
                return "package " + names[0] + " does not exists";
            }
        }
        );
        flaws.put(type.is_already_def_import, new flaw()
        {
            public String get(String ... names)
            {
                return "a type with the same simple name is already defined by the single-type-import of "+names[0];
            }
        }
        );
        flaws.put(type.cls_duplicate, new flaw()
        {
            public String get(String ... names)
            {
                return "duplicate class: " + names[0];
            }
        }
        );
        flaws.put(type.is_already_def, new flaw()
        {
            public String get(String ... names)
            {
                return names[0] + " is already defined in this compilation unit";
            }
        }
        );
        flaws.put(type.n_public_class, new flaw()
        {
            public String get(String ... names)
            {
                return names[0]+" is not public in "+names[1]+"; cannot be accessed from outside package";
            }
        }
        );
        flaws.put(type.private_class, new flaw()
        {
            public String get(String ... names)
            {
                return names[0]+" is private in "+names[1];
            }
        }
        );
        flaws.put(type.cls_broken, new flaw()
        {
            public String get(String ... names)
            {
                return "cannot access "+names[0]+ ": classfile for "+names[1]+" is broken";
            }
        }
        );
        
        flaws.put(type.symbol_not_found, new flaw()
        {
            public String get(String ... names)
            {
                return "cannot find symbol: "+ names[0];
            }
        }
        );
    }
    
    public flaw flaw_get;
    public String [] names;
    public String source = "";
    public int line = 0;
    public int position = 0;
    public int error_type = 0;
    
    
    public Flaw(type t, String source, int line, int position, String ... names)
    {
        this.names = names;
        this.line = line;
        this.position = position;
        this.source = source;
        this.flaw_get = flaws.get(t);
        /*
        try
        {
            throw new Exception(this.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }*/
    }
    
    public static Flaw error(AST ast, type t, Token token, String ... names)
    {
        Flaw fl;
        ast.error_pool.add(
        fl = new Flaw(t, token.source, token.line, 
        token.position, names));
        return fl;
    }
    
    public String toString()
    {
        String ret = source + ":" + line + ":" + position + ": " + error_name.get(error_type) + ": " + flaw_get.get(names);
        return ret;
    }
}
