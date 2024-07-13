package com.ejavac;public class FieldName
{
        public ClassObject object;
        public Token name;
        public String toString()
        {
            return "" + object.name + " " + name;
        }
        public FieldName(ClassObject object, Token name)
        {
            this.object = object;
            this.name = name;
        }
}
