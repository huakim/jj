package com.ejavac;
public class Token
{
    public int line = 0;
    public int position = 0;
    public String value = "";
    public String source = "";
    public Token(String source, String value, int line, int position)
    {
        this.line = line;
        this.position = position;
        this.value = value;
        this.source = source;
    }
    
    public Token(String value)
    {
        this.value = value;
        this.source = "<untitled>";
        this.position = 0;
        this.line = 0;
    }
    
    public Token(Token token, String value)
    {
        this.value = value;
        this.source = token.source;
        this.position = token.position;
        this.line = token.line;
    }
    
    private Token()
    {
    };
    
    public int hashCode()
    {
        return this.value.hashCode();
    }
    
    public boolean equals(Object object)
    {
        if (!(object instanceof Token))
        {
            return false;
        }
        Token token = (Token) object;
        {
  //          System.out.println("" + token.value + " " + this.value + " " + this.value.equals(token.value));
            return this.value.equals(token.value);
        }
    }

    
    public String toString()
    {
        return "\"" + value + "\":(" + line + "," + position + ")";
    }
}
