#include <set>
#include <map>
#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include <fstream>
#include <fstream>
#include <exception>
#include <algorithm>
#include <assert.h>
#include <locale>
#include <list>
#include <codecvt>
#define _public  0x0001
#define _private  0x0002
#define _protected  0x0004
#define _interface  0x0200
#define _abstract  0x0400
#define _enum  0x4000
#define _static  0x0008
#define _final  0x0010
#define _volatile  0x0040
#define _transient  0x0080     //# или varargs или static_phase
#define _synchronized  0x0020  //# или super или open или transitive
#define _native  0x0100
#define _strictfp  0x0800
#define _class  0x10000       //#
#define _default  0x100000     //#
#define _annotation  0x2000
#define _synthetic  0x1000
#define _mandated  0x8000
#ifndef library
#define check_buf 234
#endif

using namespace std;


static void throw_token_error(int line, int position, wstring out);

// конвертировать ASCII строку в строку юникод
static std::wstring s2ws(const std::string& str)
{
    using convert_typeX = std::codecvt_utf8<wchar_t>;
    std::wstring_convert<convert_typeX, wchar_t> converterX;

    return converterX.from_bytes(str);
}

// конвертировать строку юникод в ASCII строку
static std::string ws2s(const std::wstring& wstr)
{
    using convert_typeX = std::codecvt_utf8<wchar_t>;
    std::wstring_convert<convert_typeX, wchar_t> converterX;

    return converterX.to_bytes(wstr);
}


// класс, реализующий функционал токена
class Token: public wstring{
    public:  
    int line = 0;
    int position = 0;
    int type = 0;
    Token()
    :wstring()
{
}


Token(wstring s)
    :wstring(s)
{
}  

Token(wchar_t *j) 
    :wstring(j)
{
}

Token(wstring s, int i)
    :wstring(s)
{
    this -> type = i;
}  

Token(wstring s, int i, int line, int position)
    :wstring(s)
{
    this -> type = i;
    this -> line = line;
    this -> position = position;
}  
};

char * source_file = 0;
Token error_token;


struct invalid_token : public std::exception
{
    public :
    char * name;
    
    invalid_token()
    {
    }
    
    invalid_token(char * n){
        this->name = n;
    }
	const char * what () const throw ()
    {
    	return this->name;
    }
};

struct syntax_error : public std::exception
{
    public :
    char * name;
    syntax_error()
    {
    }
    
    syntax_error(char * n){
        this->name = n;
    }
	const char * what () const throw ()
    {
    	return this->name;
    }
};

struct invalid_type : public std::exception
{
    public :
    const char * name;
    invalid_type(const char * n){
        this->name = n;
    }
	const char * what () const throw ()
    {
    	return this->name;
    }
};


// предопределить типы токенов
#define WORD 11
#define SPACE 1
#define NUMBER 2
#define SPECIAL 4
#define MATH 5
#define COMMENT 6
#define STRING_  7
#define BR_open 8
#define BR_close 9
#define CHARACTER 10
#define NULL_ 0

// токены для открывающихся скобок
static set<wchar_t> BR_open_ar{L'[', L'(', L'{'};

// токены для закрывающихся скобок
static set<wchar_t> BR_close_ar{L']', L')', L'}'};

// карта для скобок
static map<wchar_t, wchar_t> BR_map{
    {L'[', L']'},
    {L'(', L')'},
    {L'{', L'}'}
};

// функция возвращает соотв. закрывающуюся скобку
static wchar_t br_close(wchar_t i){
    return BR_map[i];
}




// символы арифметических операции
static set<wchar_t> math_keys {L'!',L'%',L'&',L'*',
    L'+',L'-',L'/',L'<',L'=',L'>',L'^',L'|', L'~'};


template <class j, class l>
static inline bool contains(set<j> & x, l y)
{ 
  return x.find(y) != x.end();
}


template<class t, class b>
static int fibMonaccianSearch(t * arr, b x, int n)
{
    int i;
    
    if ((n % 2)==1)
    {
        i = n / 2;
        if (arr[i] == x){
            return i; 
        }
    } 
    
    else
    {
        i = n / 2;
        
        if (arr[i] == x){
            return i; 
        }
        
        i -= 1;
        
        if (arr[i] == x){
            return i; 
        }
    }
    
    /* Initialize fibonacci numbers */
    int fibMMm2 = 0; // (m-2)'th Fibonacci No.
    int fibMMm1 = 1; // (m-1)'th Fibonacci No.
    int fibM = fibMMm2 + fibMMm1; // m'th Fibonacci
 
    /* fibM is going to store the smallest Fibonacci
       Number greater than or equal to n */
    while (fibM < n) {
        fibMMm2 = fibMMm1;
        fibMMm1 = fibM;
        fibM = fibMMm2 + fibMMm1;
    }
 
    // Marks the eliminated range from front
    int offset = -1;
 
    /* while there are elements to be inspected. Note that
       we compare arr[fibMm2] with x. When fibM becomes 1,
       fibMm2 becomes 0 */
    while (fibM > 1) {
        // Check if fibMm2 is a valid location
        i = offset + fibMMm2;
        if (i>=n) i = n - 1;
 
        /* If x is greater than the value at index fibMm2,
           cut the subarray array from offset to i */
        if (arr[i] < x) {
            fibM = fibMMm1;
            fibMMm1 = fibMMm2;
            fibMMm2 = fibM - fibMMm1;
            offset = i;
        }
 
        /* If x is greater than the value at index fibMm2,
           cut the subarray after i+1  */
        else if (arr[i] > x) {
            fibM = fibMMm2;
            fibMMm1 = fibMMm1 - fibMMm2;
            fibMMm2 = fibM - fibMMm1;
        }
 
        /* element found. return index */
        else
            return i;
    }
 
    /* comparing the last element with x */
    if (fibMMm1 && arr[offset + 1] == x)
        return offset + 1;
 
    /*element not found. return -1 */
    return -1;
}

class buffer
{
    public:
    // an stream
    wistream * fin;
    // an map with line length;
    vector<int> map_line;

    // line, begin's with 1
    int line = 1;
    // position in line, begin's with 0
    int position = 0;
    
    // 
    buffer(wistream & f)
    {
        this->fin = &f;
    }
    // get symbol
    int get()
    {
        
   //     wcout<<"GETTING"<<endl;
        
        int i = fin->get();
        
   //     wcout<<(wchar_t)i<<endl;
   //     wcout<<"already got, continue;"<<endl;
        
        // if i is equals to '\n', then add line
        if (i == L'\n')
        {
            // check if size of the map is lesser that line position
       //     if (this->map_line.size() < this->line)
       //     {
            // if yes, then add line length to line position
                this->map_line.push_back(this->position + 1);
                // set position of new line equals to 0
                this->position = 0;
                this->line ++;
       //     }
        }
        // if i greater that -1, then displace position
        else if (i > -1)
        {
   //         wcout<<"here an segmentation failure"<<endl;
            // displace position
            this->position++;
   //         wcout<<"no, isn't here"<<endl;
        }
        return i;
    }
    // go back
    void back(int i)
    {
        // seek
        this->fin->seekg((long long)this->fin->tellg() - i);
        // repeat until i > position
        while (i > this->position)
        {
            // decrease i pointer
            i -= this->position;
            i --;
            // decrease line position
            this->line --;
            // if line pointer is equals to 0, then reset
            if (this->line == 0)
            {
                this->position = 0;
                this->line = 1;
            }
            // set position equals to last element in vector 
            this->position = this->map_line.back();
            this->map_line.pop_back();
        }
        
        this->position -= i;
    }
};

//# get token ( an keyword )
static Token * get_token(buffer & f){//wistream & f){
  //
    wstring t(L"");
  //  # read 1 byte from stream
    int line = f.line, 
    position = f.position;
    #ifdef check_buf
    wcout<<(line)<<L" ";
    wcout<<(position)<<endl;
    #endif
    
    
    int u = f.get();
    
  //  wcout<<(wchar_t)u<<endl;
    
  //  # if character is null
    if (u == -1) return new Token(L"", NULL_, line, position);
 //   # if is space
    
    if (iswspace(u)){
    
    
    #define isdigit iswalnum
    #define isdecimal iswalnum
    #define isletter iswalpha
    #define wc(x) (wchar_t) x
        
    //    #then return spacearray
        do{
            t += (wchar_t)u;
            u = f.get();
        } while (iswspace(u));
     //   #if u is not empty, then seek at -1 position
        if (u != -1) f.back(1);//f.seekg((long long)f.tellg()- 1);
        return new Token(t, SPACE, line, position);
    }
 //   # if is character, then build word
    if (iswalpha(u)||(u == L'_')){
    
      //  # building word
      
        do {
            t += (wchar_t)u;
            u = f.get();
        } while (iswalpha(u)||iswalnum(u)||(u=='_'));
      //  #if u is not empty, then seek at -1 position
        if (u != -1) f.back(1);// f.seekg((long long)f.tellg()-1);
        return new Token(t, WORD, line, position);
    }
  //  # if is number, then build number
  
    if (isdigit(u))
    {
   //     t += wc(u);
        // if dot is met
        bool dot = false;
        
        bool finish = false;
        // if special finish symbol met
    //bool finish = false;
      //  # the binary, hex, octal numbers begin with 0
        if (u == L'0')
        {
            t += wc(u);
            u = towlower(f.get());
         //   #if after 0 - x, then it is hex number
            if ((u == L'x')){
            //    # let hexadecimal number be an decimal number
                t = L"0x";
             //   # do while u is an hexadecimal symbol
                while (true){
                    u = f.get();
                  //  # if u is a decimal
                    if ((u >= L'0') && (u <= L'9')){
                        t += wc(u);
                        continue;
                    }
                    u = (towlower(u));
                  //  # if u is a special keychar
                    if (u >= 'a' && u <= 'f'){
                        t += wc(u);
                        continue;
                    }
                    // if dot meet
                    if (u == '.'){
                        // if dot already met
                        if (dot) 
                        throw_token_error(line, position, L"dot is already here");
                        dot = true;
                        t+=L".";
                        continue;
                    }
                    if (u == '_'){
                        continue;
                    }
                //    # if u is not a special keychar
                    if (isletter(u)||isdecimal(u)){
                        throw_token_error(line, position, L"not expected an identifier here");
                  //  # else
                    }
                    break;
                }
             //   #if u is not empty, then seek at -1 position
                if (u != -1) f.back(1);// f.seekg((long long)f.tellg()-1);
                return new Token(t, NUMBER, line, position);
            }
                
        //    #if after 0 - o, then it is octal number
            else if (u == 'o'){
            //    # let hexadecimal number be an decimal number
                t = L"0o";
            //    # do while u is an octal symbol
                while (true){
                    u = f.get();
                //    # if u is a decimal
                    if (u >= '0' and u <= '7'){
                        t += u;
                        continue;
                    }
                    // if dot meet
                    if (u == '.'){
                        // if dot already met
                        if (dot) 
                        throw_token_error(line, position, L"dot is already here");
                        dot = true;
                        t+=L".";
                        continue;
                    }
                    if (u == '_') continue;
                //    # if u is a keychar or decimal
                    if (isletter(u)||isdecimal(u)){
                        throw_token_error(line, position, L"not expected an identifier here");
                    }
                   // # else
                    break;
                }
             //   #if u is not empty, then seek at -1 position
                if (u != -1) f.back(1);//f.seekg((long long)f.tellg()-1);
                return new Token(t, NUMBER, line, position);
            }
       //     #if after 0 - o, then it is binary number
            else if (u == 'b'){
             //   # let binary number be an decimal number
                t = L"0b";
             //   # do while u is an binary symbol
                while (true){
                    u = f.get();
                   // # if u is a binary symbol
                    if (u == '0' || u == '1'){
                        t += u;
                        continue;
                    }
                    // if dot meet
                    if (u == '.'){
                        // if dot already met
                        if (dot) 
                        throw_token_error(line, position, L"dot is already here");
                        dot = true;
                        t+=L".";
                        continue;
                    }
                    if (u == '_') continue;
                 //   # if u is a keychar
                    if (isletter(u) || isdecimal(u)){
                        throw_token_error(line, position, L"not expected an identifier here");
                    }
                 //   # else
                    break;
                }
              ///  #if u is not empty, then seek at -1 position
                if (u != -1) f.back(1);//f.seekg((long long)f.tellg()-1);
                return new Token(t, NUMBER, line, position); 
            }
       /*     else{
                if (u == L'_')
                {
                    u = L'0';
                }
                if (u == L'.')
                {
                    dot = true;
                    t += L'.';
                    u = f.get();
                }
            }*/
        }
    //    # check if t is an expected identifier
   /*     if (u < '0' || u > '9'){
            
            #ifdef check_buf
            wcout<<"hello world\n";
            #endif
            
          // if is identifier here, then raise error
          if (isletter(u)) throw_token_error(line, position, L"not expected an identifier here");
          // else
          if (u != -1) f.back(1);//f.seekg(f.tellg() - 1);
          //
          return new Token(t, NUMBER, line, position);
          
     // # if ok, then continue
        }
     */   
        
        goto label1234;
          
        while (true)
        {
            
                u = towlower(f.get());
                
                label1234:
                
                if (u == '_'){
                    continue;
                }
                if (u == 'f')
                {
                    
                    #ifdef check_buf
                    wcout<<"get_token f"<<endl;
                    #endif
                    
                    u = towlower(f.get());
                    t += L"f";
                    finish = true;
                }
                else if (u == 'd')
                {
                    #ifdef check_buf
                    wcout<<"get_token d"<<endl;
                    #endif
                    
                    u = towlower(f.get());
                    t += L"d";
                    finish = true;
                }
                else if (u == 'l')
                {
                    #ifdef check_buf
                    wcout<<"get_token l"<<endl;
                    #endif
                    
                    u = towlower(f.get());
                    t += L"l";
                    finish = true;
                }
                // if dot meet
                if (u == '.'){
                        // if dot already met
                        if (dot) 
                        throw_token_error(line, position, L"dot is not expected here");
                        if (finish)
                        throw_token_error(line, position, L"not expected an identifier here");
                        dot = true;
                        t+=L".";
                        continue;
                }
                
                
                
        //    # check if u is an decimal number
                if ((u >= '0' && u <= '9') && (!finish)) 
                {
                    t += u;
                }
           // # if not, then
                else{
                    
                   
          //      # check if u is a keychar
                   if (isdecimal(u) || isletter(u))
                       throw_token_error(line, position, L"not expected an identifier here");
         //       # else return number
                   else{
                      if (u != -1)
                          f.back(1);//f.seekg((long long)f.tellg() - 1);
                      return new Token(t, NUMBER, line, position);
                   }
                }
        }
    }
    else{
      //  # check if symbol is an comment
        if (u == L'/'){
          //  # then t = ''
            t = L"";
            u = f.get();
      //      # check if comment is multiline
            if (u == '/')
            {
                u = f.get();
          //      # if comment is not multiline, then procees while \n not meet
                while (u !='\n' && u!= -1){
                    t += wc(u);
                    u = f.get();
                    
       //             wcout<<u<<endl;
                    
                }
              //  # return token
                return new Token(t, COMMENT, line, position);
            }
            else if (u!='*'){
          //      wcout<<"hi"<<endl;
                if (u != -1) f.back(1);//f.seekg((long long)f.tellg()-1);
                u = L'/';
                goto label12_;
            }
            else{
              //  # if comment is multiline, then process while */ not meet
                u = f.get();
                while (true){
                    if (u == -1){
                //        # if end of file reached, return comment
                        return new Token(t, COMMENT, line, position);
                    }
                //    # if reached ], check, if here an # symbol
                    if (u == L'*'){
                        u = f.get();
                  //      # if here an # symbol, then return token
                        if (u == L'/'){
                            return new Token(t, COMMENT, line, position);
                        }
                        else if (u == -1)
                        {
                            return new Token(t, COMMENT, line, position);
                        }
                        else{
                            t += L"*";
                        }
                    }
                    t += wc(u);
                    u = f.get();
                }
            }
        }
  //      # if meet an character beginning symbol, then process symbol
        if (u == L'\''){
            t = L"'";
            // repeat until ' meet
            int a = f.get();
            // if got empty literal, then throw_error
            if (a == L'\'')
            {
                throw_token_error(line, position, L"empty character literal");
            }
            else if (a == -1)
            {
                throw_token_error(line, position, L"illegal line end in character literal");
            }
            else
            {
                
                // get next element, if got \ symbol, then continue
           //     a = f.get();
                // 
                if (a == L'\\')
                {
                    t += wc(a);
                    a = f.get();
                }
            if (a == -1)
            {
                throw_token_error(line, position, L"illegal line end in character literal");
            }
                // if got element, then add this element
                if (a == L'\'')
                {
                    throw_token_error(line, position, L"697illegal line end in character literal");
                } 
                else
                {
                    t += wc(a);
                    // next element must be closing literal
                    if (f.get() != L'\'')
        {
                    throw_token_error(line, position, L"unclosed character literal");
        }
                }
            }
            
            t += L'\'';
            
            return new Token(t, CHARACTER, line, position);
        }
    //    # if meet an string beginnig symbol, then process string token
        if (u == L'"')
        {
          //  get next symbol
            auto a = f.get();
            
            if (a != L'"')
            {
                // repeat until '"' meet
                t = L"\"";
                
                while (a == L'\\')
                {
                    t += L'\\';
                    a = f.get();
                    t += wc(a);
                    a = f.get();
           //         wcout<<L"FUKUKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK"<<endl;
                }
                goto labelHH;
                // repeat 
                while (a != L'"')
                {
                    labelH:
                    // append
                    t += wc(a);
                    // get next
                    a = f.get();
                    labelHH:
                    // check if a is newline, if yes, then raise error
                    if (a == L'\n') throw_token_error(line, position, L"the newline is not expected here"); 
                    // if met null, then raise error
                    if (a == -1)
                    {
                        throw_token_error(line, position, L"the closing \" is expected");
                    }
                    // if met /, then pass symbol checking
                    else if (a == L'\\')
                    {
                        // pass symbol checking
                        goto labelH;
                    }
                }
                t += L'"';
                // finish
                return new Token(t, STRING_, line, position);
            }
            else
            // if equals to " , then check if there are multiline string
            {
                // check next symbol
                a = f.get();
                // if a is not '"', then return
                if (a != L'"'){
                    // seek back
                    if (a != -1) f.back(1);//f.seekg(f.tellg() - 1);
                    // return empty string
                    return new Token(L"\"\"", STRING_, line, position);
                }
                // else process multiline string
                else
                {
                    int counter = 0;
                    // 
                    t += L'"';
                    
                    label321:
                    // repeat until 3 of " meet
                    a = f.get();
                    // check if a is '\'
                    if (a == L'\\')
                    {
                        t += L'\\';
                        // get next symbol
                        a = f.get();
                        goto label322;
                    }
                    // check if a is ", if not, then reset counter
                    if (a == L'"')
                    {
                        counter ++;
                        // if counter reached 3, then return
                        if (counter == 3)
                        {
                            t += L'"'; 
                            return new Token(t, STRING_, line, position);
                        }
                        goto label321;
                    }
                    else
                    {
                        label322:
                        while (counter > 0)
                        {
                            counter --;
                            t += L'"';
                        }
                    }
            
                    // check  if a is -1, if yes, then raise error
                    if (a == -1)
                    {
                        throw_token_error(line, position, L"the closing 3 of \" is expected");
                    }
                    // check if new_line is here
                    if (a == L'\n')
                    {
                        t += L"\\n";
                    }
                    else
                    {
                        t += wc(a);
                    }
                    // go back
                    goto label321;
                }
            }            
        }
      //  # if u in opening brackets
       // for (int i = 0; i < 3; i++){
            if (contains(BR_open_ar, u)){
                Token * t = new Token(L"", BR_open, line, position);
                *t += wc(u);
                return t;
            }
            else if (contains(BR_close_ar, u)){
                Token * t = new Token(L"", BR_close, line, position);
                *t += wc(u);
                return t;                
            }
            
        label12_:
      //  # if u is an math expression
        if (contains(math_keys, u)){
    //        wcout<<"MATH\n";
        //    # check if operator is assigment or relational
            int a = f.get();
            int b;
        //    # check if there is an empty symbol
            if (a == -1){
                Token *k = new Token(L"", MATH, line, position);
                *k += wc(u);
                return k;
            }
            //check if here an - and >, if yes, 
            // then return special
            if (u == L'-'){
                if (a == L'>'){
                    return new Token(L"->", SPECIAL, line, position);
                }
                else if (a == L'-')
                {
                    return new Token(L"--", MATH, line, position);
                }
                else if (a == L'=')
                {
                    return new Token(L"-=", MATH, line, position);
                }
            }
            
            //# check that t = '='
            else if (a == L'='){
                t = wc(u);
                t += wc(a);
            //    # if that right, return splitted keys
                return new Token(t, MATH, line, position);
            }
 //           # check if we meet an double symbol
            else if (a == u){
            //  # check if u in special characters
              if ((u == L'+')||
                  (u == L'|')||
                  (u == L'&')){
               // # if that right, return splitted keys
                t = wc(u);
                t += wc(u);
                return new Token(t, MATH, line, position);
              }
        //      # check if we meet < twice
              if (u == L'<'){
         //       # if right, return << or <<=
                u = f.get();
         //       # check if u is empty
                if (u == -1)
                    return new Token(L"<<", MATH, line, position);
            //    #
                else if (u == L'=')
              //  #
                    return new Token(L"<<=", MATH, line, position);
     //           #
                else{
       //         #
        f.back(1);//            f.seekg((long long)f.tellg() - 1);
         //       #
                    return new Token(L"<<", MATH, line, position); 
                }
           //     #
              }
    //          # check if we meet > twice or three times
              else if (u == L'>'){
           //       # check if we meet > again
                  a = f.get();
                  b = f.get();
                  
             //     # check if we meet >=
                  if ((a == L'>')&&(b == L'='))
                      return new Token(L">>>=", MATH, line, position);
  //                # set length of u
    //              # if j == 0, then return
                  if (a == -1)
                      return new Token(L">>", MATH, line, position);
        //          # check if we meet > or '='
                  if ((a == L'>')||(a == L'=')){
                     // f.seekg((long long)f.tellg() 
                    f.back(((b == -1) ? 0 : 1));
                    
                      t = L">>";
                      t += wc(a);
                      return new Token(t, MATH, line, position);
                  }
          //        # else return >>
                  f.back( (((b == -1) ? 0 : 1) + ((a == -1) ? 0 : 1)));
                  return new Token(L">>", MATH, line, position);
              }
            }
            
            //# else, go to back
      //      wcout<<"CHAR: "<<a<<endl;
            
            f.back(1);// 1);
            
            Token * k  =  new Token(L"", MATH, line, position);
                        
            *k += wc(u);
            
            
            
            return k;
        }
        if (u == L'.'){
      //      # check if .. is next elements
            int a = f.get();
        //    # if got none
            if (a == -1)
                return new Token(L".", SPECIAL, line, position);
    //        # else, check next element
            int b = f.get();
   //         # if got none, then 
            if (b == -1){
             f.back(1);//   f.seekg((long long )f.tellg() - 1);
                return new Token(L".", SPECIAL, line, position);
            }
 //           # check, if b == a and it is dot
            if ((a == b)&&(a == '.')){
    //            # then return '...'
                return new Token(L"...", SPECIAL, line, position);
            }
            else{
                f.back(2);//f.seekg((long long )f.tellg() - 2);
                return new Token(L".", SPECIAL, line, position);
            }
        }
        if (u == L':'){
            // check if next element is :
            int a = f.get();
            // if got none, then just return;
            if (a == -1){
                return new Token(L":", SPECIAL, line, position);
            }
            // else
            if (a == L':'){
                return new Token(L"::", SPECIAL, line, position);
            }
            f.back(1);//f.seekg(f.tellg() - 1);
            return new Token(L":", SPECIAL, line, position);
        }
    }
    Token * k = new Token(L"", SPECIAL, line, position);
    *k += wc(u);
    return k;
    #undef isdigit 
    #undef isletter 
    #undef wc
}

static set<wstring> bsk  {L"@interface", L"abstract", L"assert", 
L"break", L"case", L"catch", L"class", L"const", 
L"continue", L"default", L"do", L"else", L"enum", 
L"extends", L"false", L"final", L"finally", L"for", 
L"goto", L"if", L"implements", L"import", L"instanceof", 
L"interface", L"native", L"new", L"null", L"package", 
L"private", L"protected", L"public", L"return", L"static",
L"strictfp", L"super", L"switch", L"synchronized", 
L"this", L"throw", L"throws", L"transient", L"true", 
L"try", L"var", L"volatile", L"while"};

static set<wstring> b_ops {L"!", L"+", L"++", L"-", L"--", L"~"};

static set<wstring> f_ops {L"++", L"--"};

static vector<set<wstring>> lvl_ops{
{L"||"}, {L"&&"}, {L"|"}, {L"^"},
{L"&"}, {L"==", L"!="}, {L"<", L"<=", L">", L">="}, 
{L"<<", L">>", L">>>"}, {L"+", L"-"}, {L"%", L"*", L"/"}
};


static set<wstring> s_ops {L"%=",L"&=",L"*=",
    L"+=",L"-=",L"/=", 
L"<<=",L"=",L">>=",
L">>>=",L"^=",L"|="};

//# this function checks an level of the operator
static int lvl_check(wstring & d){
    // # check all lvl_ops
    for (int i = 0; i < 10; i++){
        //  # if operand there
        if (contains(lvl_ops[i], d))
        {
            // then return level
            return i + 3;
        }; 
    }
    // if there no operators, return 13
    return 13;
}
// # this method checks if token is valid name
static bool is_name(Token f){
    if (f.type == 0) return false;
    if (contains(bsk, f)) return false;
    return (f.type == WORD);
}

//# this method checks if it is constant
static bool is_c(Token f){
  //  # check if it's an string, number or keyword
    int i = f.type;
    if (i == 0) return false;
    if (
    (f == L"false")|| 
    (f == L"true")|| 
    (f == L"null")
    ) return true;
    if (contains(bsk, f)) return false;
    return (
    (i == STRING_)|| 
    (i == NUMBER)|| 
    (i == CHARACTER)
    );
}

// # this method checks if it is value
static bool is_v(Token f){
  //  # check if it is an string, number or word
    if (
    (f == L"false")|| 
    (f == L"true")|| 
    (f == L"null")||
    (f == L"super")||
    (f == L"this")
    ) return true;
    if (contains(bsk, f)) return false;
    int i = f.type;
    return (
    (i == STRING_)|| 
    (i == NUMBER)|| 
    (i == WORD)|| 
    (i == CHARACTER)
    );
}

//# this method checks if token is not comment or space
//# or other token, than will be ignored by the compiler
static bool right(Token t){
    int i = t.type;
    if (i == COMMENT) return false;
    if (i == SPACE) return false;
    return true;
}

struct ex : public std::exception
{
    public :
	const char * what () const throw ()
    {
    	return "";
    }
};

// this fields handles an information for syntax error


// this function throws an error
static void throw_error(wstring er)
{
    
    std::wostringstream str;
    if (source_file != 0)
    {
        str<<s2ws(source_file).c_str();
        str<<L":";
    }
    if (error_token.line > 0)
    {
        str<<error_token.line<<L":"<<error_token.position<<L":";
    }
         str<<L" Syntax Error: "<<er.c_str()<<endl;
        
        er = str.str();
        int len = er.size();
    
    syntax_error t;
    
        t.name = new char[len]; 
        wcstombs(t.name, er.c_str(), len);
    
    
    throw t;
}

// this function throws an token error
static void throw_token_error(int line, int position, wstring er)
{
    std::wostringstream str;
    if (source_file != 0)
    {
        str<<s2ws(source_file).c_str();
        str<<L":";
    }
    if (error_token.line > 0)
    {
        str<<error_token.line<<L":"<<error_token.position<<L":";
    }
    str<<L" Invalid Token: "<<er.c_str()<<endl;
    
    invalid_token j;
    
    j.name = new char[256]; 
    wcstombs(j.name, str.str().c_str(), 256);
    throw j;
}

static Token null_token = Token(L"", NULL_);
// iterator

class it{
    public:
    int i;
    vector<Token> * f;
    virtual Token get() 
    {
        return null_token;
    };
    virtual Token peek(int i)
    {
        return null_token;
    };
    
    virtual Token peek()
    {
        return null_token;
    };
    
    virtual Token get(int i)
    {
        return null_token;
    };
    virtual void back(int k) {};
    it () {};
};

class it3: public it{
    public:
    int d;
    int size;
    // iterator constructor
    it3( vector<Token>  *f, int i = 0, int s = -1)

    {
        this->f = f;
        this->i = i;
        this->d = i;
        if (s == -1) size = f->size();
        else size = s;
    }
    // this function returns an next Token element
    Token get(){
        int i = this->i;
        //    # if stop-index exceedes length of the array
        if (i >= this->size) return null_token;
        //# else then increment stop-index and return element by index
        Token t = this->f->at(i);
        error_token = t;
        this->i++;
        return t;
    };
    
    Token peek(int i)
    {
        return this->f->at(this->i + i);
    };
    Token peek()
    {
        return this->f->at(this->i);
    }
    Token get(int i)
    {
        return this->f->at(i);
    }
    // # this function seeks back
    void back(int a){
        
     //   wcout<<"back got ";
        int i = this->i;
        i -= a;
        int d = this->d;
        this->i = (d > i)? d : i;
        return;
    }
};


typedef map<Token, Token (*)()> func_map;
// iterator with stop_functions
class it2: public it{
    public:
    // map
    int d;
    func_map b;
    // initializer
    it2(vector<Token> * f, int i, map<Token, Token (*)()> b)
    {
        this->f = f;
        this->i = i;
        this->d = i;
        this->b = b;
    };
    Token peek(int i)
    {
        return this->f->at(this->i + i);
    };
    Token peek()
    {
        return this->f->at(this->i);
    }
    Token get(int i)
    {
        return this->f->at(i);
    }
//    # this function returns an next element
//        # or None if bracket here
    Token get(){
            
       //     wcout<<"HI";
           
            int i = this->i;
            
            auto f = this->f;
            
     //       wcout<<"all okay, fuck rus"<<endl;
            
            
            Token t = f->at(i);
            
            error_token = t;
     //       # if stop-index exceedes length of the array
            if (i >= f->size())
        //        # then raise error
                throw_error(L"the one of the closing elements is expected");
  //          # else then check if next element is equals to bracket
     //       # if yes, then return None
     
            func_map::iterator m = this->b.find(t);
               
  //             wcout<<"FUNN"<<endl;
   //  return null_token;
            if (m != b.end()){
        //        # perform finish action
                   return m->second();
     //    #       print("none")
            }
     //       # increment stop-index and return element by index
            this-> i++;
            return t;
    }
    void back(int a){
        
     //   wcout<<"back got ";
        int i = this->i;
        i -= a;
        int d = this->d;
        this->i = (d > i)? d : i;
        return;
    }
};

inline Token get(it &x){
    return x.get();
}
inline void back(it & x, const int y){
    x.back(y);
}
#define token_list vector<Token>

template<typename T>
static void vector_extend(std::vector<T> &vec, const std::vector<T> &ext) {
    vec.reserve(vec.size() + ext.size());
    vec.insert(std::end(vec), std::begin(ext), std::end(ext));
}

template <class T>
static bool vector_contains(std::vector<T> const &v, T const &x) { 
    return ! (v.empty() ||
              std::find(v.begin(), v.end(), x) == v.end());
}

// this is an list class
class list{
    
};
// this is an initial element
class init{
public:

long double b1;
Token b2;
int line = 0;
int position = 0;
vector<init> b3;
char type = 0; 
// 1 - число
// 2 - строка
// 3 - массив

// функция строкового представления объекта
friend auto operator<<
(std::wostream& os,init m) -> std::wostream& { 
    switch(m.type){
        case 1:
        return os<<m.b1;
        
        case 2:
        return os<<L"\""<<m.b2<<L"\":("<<m.line<<L","<<m.position<<L")";
        
        case 3:
        os << L"[";
        
        for(std::vector<init>::iterator it = m.b3.begin(); it != m.b3.end(); ++it) {
            os <<(const init) *it;
            os <<L", ";
        }
        os << L"]";
        
        return os;
    }
    os<<L"NULL";
    return os;
}

// функция сравнения объекта
friend bool operator == (const init a, const init b){
    if (b.type == a.type){
        switch(b.type){
            case 1:
            return b.b1 == a.b1;
            case 2:
            return b.b2 == a.b2;
            case 3:
            return b.b3 == a.b3;
        }
    }
    else{
        return false;
    }    
}


operator wstring() {
    assert(type == 2);
    return (wstring)b2;
}

operator Token(){
    assert(type == 2);
    return b2;
}

operator double(){
    assert(type == 1);
    return (double)b1;
}

operator long double(){
    assert(type == 1);
    return b1;
}

operator int(){
    assert(type == 1);
    return (int)b1;
}

operator vector<init>(){
    assert(type == 3);
    return b3;
}

init(Token n){
    type = 2;
    line = n.line;
    position = n.position;
    b2 = n;
}

init(wchar_t * name, int l, int n){
    type = 2;
    line = l;
    position = n;
    b2 = wstring(name);
}

init(wchar_t * name){
    type = 2;
    b2 = wstring(name);
}

init(long double n){
    type = 1;
    b1 = n;
}

init(int n){
    type = 1;
    b1 = n;
}

init(const init * n, int len){
    b3 = vector<init>(n, n + len);
    type = 3;
}

static init lst(){
    init k;
    k.type = 3;
    return k;
}

static init num(){
    init k;
    k.type = 1;
    return k;
}

static init str(){
    init k;
    k.type = 2;
    return k;
}
    
init()
{
    this->type = 0;
};

init(vector<init> k){
    type = 3;
    b3 = vector<init>(k);
}

init(std::initializer_list<init> c){
    type = 3;
    b3 = vector<init>(c);
}


friend bool operator ==(const init s, Token t){
    if (s.type != 2) return false;
    return s.b2 == t;
}

friend bool operator ==(const init s, const wchar_t * t){
    if (s.type != 2) return false;
    return s.b2 == t;
}

friend bool operator ==(const init s, vector<init> t){
    if (s.type != 3) return false;
    return s.b3 == t;
}

friend bool operator ==(const init s, long double t){
    if (s.type != 1) return false;
    return s.b1 == t;
}

init & operator[]( int n){
    assert(type == 3);
    return b3.at(n);
}

void append(const init n){
    assert(type == 3);
    b3.push_back(n);
}

void extend(const init n){
    assert(type == 3);
    assert(n.type == 3);
    vector_extend(b3, n.b3);
}

void extend(vector<init> n){
    assert(type == 3);
    vector_extend(b3, n);
}

friend void operator +=(init a, const init n){
    if (a.type == 3){
        a.append(n);
        return;
    }
    assert(a.type == n.type);
    if (a.type == 2){
        a.b2 += n.b2;
    }
    else{
        a.b1 += n.b1;
    }
}

bool contains(const init x){
    assert(type == 3);
    return vector_contains(b3, x);
}

friend void operator+=(init t, long double i){
    if (t.type == 3){
        t.append(init(i));
        return;
    }
    assert(t.type == 1);
    t.b1+=i;
}

friend void operator+=(init t, wstring i){
    if (t.type == 3){
        t.append(init(i));
        return;
    }
    assert(t.type == 2);
    t.b2+=i;
}

init copy(){
    assert(type == 3);
    return init(b3);
}

int size(){
    assert(type == 3);
    return (int)b3.size();
}

};


// # this is type-cast class
static init tp_{
    init(L"$type"), 
   // # name
    init::lst(),
   // # ext
    init(), 
 //   # generic
    init::lst(),
 //   # array level
    init(0),
//    # annotations
    init::lst(),
//    flags
    init(0)
};

static init cls{
    // flags
    init(0),
    // name
    init(),
    // generic cast
    init::lst(),
    // superclass path
    init::lst(),
    // interfaces
    init::lst(),
    // body
    init{init::lst(), init::lst(), init::lst(), init::lst(), init::lst(), init::lst(), init::lst() },
    //    inner        method        field       const         enum          note        order
    // annotations
    init::lst()
};     

// # this is array for type-cast class
static init ar_(init t, int l){
  //      # if l is None
    if (l == 0)
        return t;
    //    # else, return an copy of object
    else{
        init v = t.copy();
        v[4] += l;
        return v;
    }
}

// this method returns an name, or raises an error
static Token gt_n(it & l){
//        # 
        Token t = get(l);
        // if got name, then return
        if (is_name(t))
            return t;
        // else raise an error
        else
            throw_error(L"invalid name");
}

// this method returns an name, or raises an error
static Token gt_c(it  &l){
//        # 
        Token t = get(l);
        // if got name, then return
        if (is_c(t))
            return t;
        // else raise an error
        else
            throw_error(L"invalid name");
}
    
// this function checks if element contains in array
static bool contains(init & e, Token j)
{
    // if type is an array
    if (e.type == 3)
    {
        // then return if element contains in array
        return e.contains(j);
    }    
    // else
    else
    {
        return e == j;
    }
}
//    # this method returns an expected symbol or name
static Token gt_e(it & l, init e){
// get element
    Token t =  get(l);
    
    if (t.type == 0){
        throw_error(L"an token is expected");
    }
// check if e contains an t
    if (contains(e, t))
        return t;
// if not, then raise an error
    else{
     wstring fin = L"the ";
     fin += (t);
     fin += L" is not expected here";
     
     throw_error(fin);
    }
}

//    # this method returns an expected symbol or name
static Token gt_e_n(it & l, init e){
// get element
    Token t =  get(l);
    
    if (t.type == 0){
        return null_token;
    }
// check if e contains an t
    if (contains(e, t))
        return t;
// if not, then raise an error
    else{
     wstring fin = L"the ";
     fin += t;
     fin += L" is not expected here";
     
     throw_error(fin);
    }
}

//# this method returns an class name
static init cls_name(it & f){
    init n = init::lst();
  //  # get next element
    Token k = get(f);
    
    #ifdef check_buf
    wcout<<"$$class name getting here$$"<<k<<endl;
    #endif
  //  # repeat until reaches end
    while(k.type != NULL_){
    //    # if k is None, then end is reached
    //    # check if k is an name
        if (!is_name(k)){
    #ifdef check_buf
            wcout<<"K"<<k<<endl;
    #endif
         //   # if not, then raise error
            throw_error(L"invalid name");
        }
        else{
    #ifdef check_buf
            wcout<<"K"<<k<<endl;
    #endif
         //   # else, add name to n
            n.append(k);
         //   # check if next element is an dot
            k = get(f);
         //   # if k is dot, then continue
            if (k == L".")
            {
                k = get(f);
                continue;
            }
         //   # else, back
            if (k.type != NULL_) 
                 back(f, 1);
            return n;
        }
    }
}       

namespace glb{
    
    
    static int cur_line = 0;
    static int cur_position = 0;
    static bool note = false;
    static map<int, int> brcs;
   // static map<int, int> brcs2;
   // static map<int, int> brcs3;
    static int q = 0;
    static int t = 0;
    static bool s = true;
    static bool bound = false;
    static bool allow_variables = false;
    
    static void refresh()
    {
        glb::cur_line = 0;
        glb::cur_position = 0;
        glb::q = 0;
        glb::t = 0;
        glb::brcs.clear();
        glb::note = false;
        glb::s = true;
        glb::allow_variables = false;
        glb::bound = false;
    }
    
    static Token s_ch(){
        glb::s = false;
        return null_token;
    }
    
    // this method clears an map object
    
    // 
    static void jumpb(it & l)
    {
        // get jump int from brcs
        int i = brcs[l.i];
        // check if i is greater that zero
        if (i > 0)
        {
            // if yes, then jump
            l.i = i;
            return;
        }
        
        // else
        else
        {
            
        //
            // iterate until ) meet
            // create pull
            vector<int> poll;
            // add element to poll
            poll.push_back(l.i);
            // get token and iterate
            Token j;
            // begin iteration
            while (poll.size() > 0)
            {
                j = get(l);
                #ifdef check_buf
                wcout<<L"jumpb_searching "<<j<<endl;
                #endif
                // if j is null, then raise error
                if (j.type == NULL_)
                {
                    throw_error(L"the ) is expected");
                    return;
                }
                // if j is an closing bracket
                else if (j.type == BR_close)
                {
                // else, check if j equals to ) 
                if (j == L")")
                {
                    // if yes, then add to brcs
                    brcs[poll[poll.size()-1]] = l.i;
                    poll.pop_back();
                    
                    // fill out
         //           wcout<<poll.size()<<endl;
                }
                }
                
                else if (j.type == BR_open)
                {
                if (j == L"(")
                {
                    // if got ( symbol, then add to poll
                    poll.push_back(l.i);
                }
                }
            }
            return;
        }
    }
    
    /*/ 
    static void jumpb2(it & l)
    {
        // get jump int from brcs
        int i = brcs2[l.i];
        // check if i is greater that zero
        if (i > 0)
        {
            // if yes, then jump
            l.i = i;
            return;
        }
        
        // else
        else
        {
            
        //
            // iterate until ) meet
            // create pull
            vector<int> poll;
            // add element to poll
            poll.push_back(l.i);
            // get token and iterate
            Token j;
            // begin iteration
            while (poll.size() > 0)
            {
                j = get(l);
                #ifdef check_buf
                wcout<<L"jumpb_searching "<<j<<endl;
                #endif
                // if j is null, then raise error
                if (j.type == NULL_)
                {
                    throw_error(L"the ] is expected");
                    return;
                }
                // if j is an closing bracket
                else if (j.type == BR_close)
                {
                // else, check if j equals to ) 
                if (j == L"]")
                {
                    // if yes, then add to brcs
                    brcs[poll[poll.size()-1]] = l.i;
                    poll.pop_back();
                    
                    // fill out
         //           wcout<<poll.size()<<endl;
                }
                }
                
                else if (j.type == BR_open)
                {
                if (j == L"[")
                {
                    // if got ( symbol, then add to poll
                    poll.push_back(l.i);
                }
                }
            }
            return;
        }
    }
    
    // 
    static void jumpb3(it & l)
    {
        // get jump int from brcs
        int i = brcs3[l.i];
        // check if i is greater that zero
        if (i > 0)
        {
            // if yes, then jump
            l.i = i;
            return;
        }
        
        // else
        else
        {
            
        //
            // iterate until ) meet
            // create pull
            vector<int> poll;
            // add element to poll
            poll.push_back(l.i);
            // get token and iterate
            Token j;
            // begin iteration
            while (poll.size() > 0)
            {
                j = get(l);
                #ifdef check_buf
                wcout<<L"jumpb_searching "<<j<<endl;
                #endif
                // if j is null, then raise error
                if (j.type == NULL_)
                {
                    throw_error(L"the } is expected");
                    return;
                }
                // if j is an closing bracket
                else if (j.type == BR_close)
                {
                // else, check if j equals to ) 
                if (j == L"}")
                {
                    // if yes, then add to brcs
                    brcs[poll[poll.size()-1]] = l.i;
                    poll.pop_back();
                    
                    // fill out
         //           wcout<<poll.size()<<endl;
                }
                }
                
                else if (j.type == BR_open)
                {
                if (j == L"{")
                {
                    // if got ( symbol, then add to poll
                    poll.push_back(l.i);
                }
                }
            }
            return;
        }
    }*/
}

inline bool operator==(const Token j, const init jk){
    if (jk.type == 2)
    {
        return j == jk.b2;
    }
    else
    {
        return false;
    };
}

// declare here
static void _pass(it &l);
static void cls_fill(it &l, init& ret);
static void cls_fill(it &l, init& ret, bool is_anonumous);
static void cls_body(it &l, init& ret, int k, init nt, int flags);
static void cls_body(it &l, init& ret);
static void cls_body_anonumous(it &l, init & bb);
static bool chk_mth( int f, bool a);
static init c_func(it &l, int f, init nt, bool is_abstract, init gen, init type, init name);
static init c_cnst(it &l, int f, init nt);
static Token c9(it &l, init &nt);
static init b3(it &l);
static init super_call(it &l);
static init this_call(it &l);
static Token b4(it &l);
static init b5(it &l);
static void f_add(it &l , init &c ,Token j);
static init b1(it &l);
static init brk(it& l, int ign);
static init spc(it &l, init b, Token j, int ign);
static init null_init = init();
static init _at(it &g);
static init arr(it &l, Token j);
static init tcall(it &l, Token j); 
static init mth(it &l, init b, Token j, int ign);
static init call(it& l, Token j);
static Token gt_e_b(it &l, init e); 
static init cnew(it& l); 
static init tnew(it& l); 
static init cls_b(int l, it &l1, bool l2, init token);
static init cls_p(int cl, it &l);
static init a5(it& l);
static init gt_var(it &l);
static init a7(it &l );
static init a6(it& l);
static init get_args(it &l);
static init a5(it& l, init a, init b);
static init f1(it& l);
static init f2(it& l);
static init fnc_b(it& l);
static init arg_eq(it &l);
static init arg_e(it &l, bool a, bool b);
static init arg_e(it &l);
static init enm(it &l);
static bool c3(it &l, init &n, bool b, init s);
static bool ca(it &l, init &n);
static bool cv(it &l, init &n);
static bool c5(it &l, int f, init& n);
static bool c6(it &l, int f, init t,init n, init &x, bool a, init q, init nt);
static bool c4(it &l, int f, init &x, init nt);
static bool c66(int f);

// # the b2 calls b1 function and raises error
//    # if an exception occured
static init b2(it &k){
        it &l =* new it3(k.f, k.i); 
        glb::q = 0;
        try{
  //          # call a1
            init u = b1(l);
            
            if (u[0] == L"var")
            {
                
            #ifdef check_buf
                wcout<<L"var element is returned by b2 function\n";
            #endif
            }
            
    //        wcout<<u<<endl;
            else
             if (u[1][0] == L"?")
            {
                throw ex();
            }
   //         # displace index
            k.i = l.i;
  //          # return type
            return u;
        }
        catch(ex ere){
  //          # raise syntax error
            throw_error(L"invalid type");
            return null_init;
        }
}


// the b22 get's implementation of type for the cnew function
static init b22(it & lk)
{
    
    it &l =* new it3(lk.f, lk.i); 
        glb::q = 0;
    //   # create an type
        init t = tp_.copy();
   //     #this is an annotation array
        init &nt = t[5];
      //  # get name and annotations
        Token nm = c9(l, nt);
        
//        wcout<<L"b1<"<<endl;
//        wcout<<nm<<endl;
        //
        //
        init &n = t[1];
        //
//        wcout<<nm<<L" name "<<endl;
     //   # if n is not valid 
        
 //     #  print(nt)
  //      # set annotations
        //t[5] = nt;
   //     # extension
   //     # check if n is ?
        
        Token c;
        Token d;
        // if not name
        if (!is_name(nm))
       // #    print('name::', n)
            {
                    #ifdef check_buf
                    #endif
                throw_error(L"invalid type");
                return null_init;
            }
   //     # else, check if dots here
        
        else{
             #ifdef check_buf
            #endif
//            wcout<<"hello "<<endl;
            n.append(init(nm));
//            wcout<<"HELLL"<<endl;
            d = gt_e_b(l, init{init(L"."), init(L"<")});
            
            
             #ifdef check_buf
                    wcout<<" "<<d<<endl;;
                    #endif
       //     # compare d-variable with '.'
            while (d.type != NULL_){
                
             #ifdef check_buf
                    wcout<<L" 2169?\n";
                    #endif
                if (d == L"<")
                {
                    #ifdef check_buf
                    #endif
                    // continue here
                    t[3].append(b3(l));
                    d = gt_e_b(l, init{init(L"."), init(L"<")});
                    if (d.type == NULL_) break;
                }
                else
                {
                    t[3].append(null_init);
                }
                
                c = get(l);
            // check if c is < element 
                
            //    # check if c is an name
                if (!is_name(c))
                {
       //           //  # if not, then go back
                    back(l, (c.type == NULL_) ? 1 : 2);
                    break;
                }
                d = gt_e_b(l, init{init(L"."), init(L"<")});
                
      //          wcout<<d<<endl;
                
                n.append(c);
            }
        }
 //       # set typename
  //      t[1] = n;
//        wcout<<t<<endl;
//        wcout<<n<<endl;
//        wcout<<"HELL@"<<endl;
//  #      print(t.name)
 //   #    print(n)
   //     # try to get typename generic
        int i = l.i;
        
        #ifdef check_buf
       wcout<<L"hit here CCC::::"<<c<<endl;
       #endif
//      get next element, should be < or [
        
       #ifdef check_buf
       wcout<<L"hit here"<<endl;
       #endif
        // if got <, then get b5
        
        lk.i = l.i;
        return t;
}

// the b22 get's implementation of type for the cnew function
static init b23(it & lk)
{
    
    it &l =* new it3(lk.f, lk.i); 
        glb::q = 0;
    //   # create an type
        init t = tp_.copy();
   //     #this is an annotation array
        init &nt = t[5];
      //  # get name and annotations
        Token nm = c9(l, nt);
        
//        wcout<<L"b1<"<<endl;
//        wcout<<nm<<endl;
        //
        //
        init &n = t[1];
        //
//        wcout<<nm<<L" name "<<endl;
     //   # if n is not valid 
        
 //     #  print(nt)
  //      # set annotations
        //t[5] = nt;
   //     # extension
   //     # check if n is ?
        
        Token c;
        Token d;
        // if not name
        if (!is_name(nm))
       // #    print('name::', n)
            {
                    #ifdef check_buf
                    wcout<<L"fuck that chechen\n";
                    #endif
                throw_error(L"invalid type");
                return null_init;
            }
   //     # else, check if dots here
        
        else{
             #ifdef check_buf
                    wcout<<L"HII!, i'm farida, i think usup is better that you\n";
                    #endif
//            wcout<<"hello "<<endl;
            n.append(init(nm));
//            wcout<<"HELLL"<<endl;
            d = gt_e_b(l, init{ init(L"<")});
            
            
             #ifdef check_buf
                    wcout<<L"dear "<<d<<endl;;
                    #endif
       //     # compare d-variable with '.'
            while (d.type != NULL_){
                
             #ifdef check_buf
                    wcout<<L"escaban: how are you?\n";
                    #endif
                if (d == L"<")
                {
                    #ifdef check_buf
                    wcout<<L"HII!, i am farida, your past love(fuck, ahahhaha, i have girl better)\n";
                    #endif
                    // continue here
                    t[3].append(b3(l));
                    d = gt_e_b(l, init{init(L"."), init(L"<")});
                    if (d.type == NULL_) break;
                }
                else
                {
                    t[3].append(null_init);
                }
                
                c = get(l);
            // check if c is < element 
                
            //    # check if c is an name
                if (!is_name(c))
                {
       //           //  # if not, then go back
                    back(l, (c.type == NULL_) ? 1 : 2);
                    break;
                }
      //          wcout<<d<<endl;
                n.append(c);
                break;
            }
        }
 //       # set typename
  //      t[1] = n;
//        wcout<<t<<endl;
//        wcout<<n<<endl;
//        wcout<<"HELL@"<<endl;
//  #      print(t.name)
 //   #    print(n)
   //     # try to get typename generic
        int i = l.i;
        
        #ifdef check_buf
       wcout<<L"hit here CCC::::"<<c<<endl;
       #endif
//      get next element, should be < or [
        c = gt_e_b(l, init(L"<"));
        
       #ifdef check_buf
       wcout<<L"hit here"<<endl;
       #endif
        // if got <, then get b5
        
        lk.i = l.i;
        return t;
}

//# this function check if here an expected element, if not, then goes back
static Token gt_e_b(it &l, init e){
         
         
         #ifdef check_buf
         wcout<<"here gt e b"<<endl;
         #endif
    
   //     # get element
        Token u = get(l);
     //   # check if u in e
        #ifdef check_buf
         wcout<<u<<endl;
         wcout<<u.type<<endl;
        #endif
        if (u.type == 0)
        {
            return null_token;
        }
     
        if (contains(e, u))
        {
        //    # if true, then return
            return u;
        }
   ///     # if not, then go back and return None
        else{
            if (u.type != NULL_) back(l, 1);
            return null_token;
        }
}

//# this function will return an name 
//    # and process if here is generic type
static init b1(it& l){
    
    #ifdef check_buf
                    wcout<<L"fuck that chechen\n";
                    #endif
   //   # create an type
        init t = tp_.copy();
   //     #this is an annotation array
        init &nt = t[5];
      //  # get name and annotations
        Token nm = c9(l, nt);
        
//        wcout<<L"b1<"<<endl;
//        wcout<<nm<<endl;
        //
        #ifdef check_buf
        wcout<<L"HELL\n";
        #endif
        //
        init &n = t[1];
        //
//        wcout<<nm<<L" name "<<endl;
     //   # if n is not valid 
        
 //     #  print(nt)
  //      # set annotations
        //t[5] = nt;
   //     # extension
   //     # check if n is ?
        
        Token c;
        Token d;
        if (nm.type == NULL_) throw_error(L"the name of type is expected");
        
        // if var keyword is allowed, then check, if nm is equals to var
        if (glb::allow_variables)
        {
            #ifdef check_buf
            wcout<<L"i'm here ::" << nm<<endl;
            #endif
            glb::allow_variables = false;
            
            if (nm == L"var")
            {
                #ifdef check_buf
                wcout<<L"set var type here"<<endl;
                #endif
                
                t[0] = L"var";
                return t;
            }
        }
  //  if (!glb::bound){
        if (nm == L"?")
        {
    if (glb::bound)
        {
             // fuck
     //        wcout<<"fukd"<<endl;
 //         #  print("? _______")
  //          # if true, then check for 'extends' keyword
            Token nf = get(l);
            
    //        #
            if (nf.type == NULL_) goto l1;
            if ((nf == L"extends")||
                (nf == L"super")){
   //      #       print("ext")
    //            # here must be an type
                t[2] = b1(l);
                
      ///    #      print(ext)
         //       # return
           //     # set name
                t[1] = init{init(L"?"), init(nf)};
             //   # set extension
             
               // # return 
                return t;
            }
      //      # else, go back and return '?'
            else{

        //        # go back
                if (nf.type == NULL_) back(l, 1);
       l1:     //      # set type ?
                n.append(init(L"?"));
            //    # return 
                return t;
            }
    }
    else
    {
        throw_error(L"required class or interface without bounds");
        throw ex();
        return null_init;
    }
            
        }
        // if not name
        else if (!is_name(nm))
       // #    print('name::', n)
            {
                
                    #ifdef check_buf
                    wcout<<L"fuck that chechen\n";
                    #endif
                throw ex();
                return null_init;
            }
   //     # else, check if dots here
        
        else{
             #ifdef check_buf
                    wcout<<L"HII!, i'm farida, i think usup is better that you\n";
                    #endif
//            wcout<<"hello "<<endl;
            n.append(init(nm));
//            wcout<<"HELLL"<<endl;
            d = gt_e_b(l, init{init(L"."), init(L"<")});
            
            
             #ifdef check_buf
                    wcout<<L"dear "<<d<<endl;;
                    #endif
       //     # compare d-variable with '.'
            while (d.type != NULL_){
                
             #ifdef check_buf
                    wcout<<L"escaban: how are you?\n";
                    #endif
                if (d == L"<")
                {
                    #ifdef check_buf
                    wcout<<L"HII!, i am farida, your past love(fuck, ahahhaha, i have girl better)\n";
                    #endif
                    // continue here
                    t[3].append(b3(l));
                    d = gt_e_b(l, init{init(L"."), init(L"<")});
                    if (d.type == NULL_) break;
                }
                else
                {
                    t[3].append(null_init);
                }
                c = get(l);
            // check if c is < element 
                
            //    # check if c is an name
                if (!is_name(c))
                {
       //           //  # if not, then go back
                    back(l, (c.type == NULL_) ? 1 : 2);
                    break;
                }
                d = gt_e_b(l, init{init(L"."), init(L"<")});
                
      //          wcout<<d<<endl;
                
                n.append(c);
            }
        }
 //   }
    
 //       # set typename
  //      t[1] = n;
//        wcout<<t<<endl;
//        wcout<<n<<endl;
//        wcout<<"HELL@"<<endl;
//  #      print(t.name)
 //   #    print(n)
   //     # try to get typename generic
        int i = l.i;
//      get next element, should be < or [
        c = gt_e_b(l, init{init(L"<"), init(L"[")});
        
       
        
        // get level integer
        long double lvl = t[4].b1;
        // if got [, then continue for arrays
        while (c == L"["){
            c = get(l);
            
            // hell
            
            if (c != L"]")
            {
                l.i = i;
                return t;
            }
            else 
            {
                i = l.i;
                lvl ++;
            }
            c = gt_e_b(l, init{init(L"[")});
        } 
        t[4].b1 = lvl;
        // return t
        return t;
     //     #  print("what?")
     //       # return to the old position and return type
    
}

// this function checks an type for static keyword
static init b2_static(it &l)
{
    // reserve stop point
    int i = l.i;
    // 
    init nt = init::lst();
    // check for static keyword
    Token j = c9(l, nt);
    
    #ifdef check_buf
    wcout<<"expected static modifier meet here: "<<j<<endl; 
    #endif
    // if j is no static keyword, then just return b2 type
    if (j != L"final")
    {
        l.i = i ;
        return b2(l);
    }
    else
    {
        // if got static keyword, then get b2 type, set static and got annotations
        init ret = b2(l);
        // set flags
        ret[6] = init(_final);
        // set annotations
        ret[5].extend(nt);
        // return ret object
        return ret;
    }
}

// this function checks an type for static keyword
static init b2_static_var(it &l)
{
    bool s = glb::allow_variables;
    
    glb::allow_variables = true;
    
    init ret = b2_static(l);
    
    glb::allow_variables = s;
    
    
    
    return ret;
}

//# this function will return an generic cast
static init b5(it& l)
{    
        glb::q = 0;
 //       # iterate until meet >
        init c = init::lst();
        init t;
            
        init nt;
     //   # begin iteration
        while (true)
        {
            // create array for annotations
            nt = init::lst();
      //      # /get name and annotations
            if (c9(l, nt).type != NULL_) back(l, 1);
            Token n = gt_n(l);
    //        # get extends 
            if (gt_e_b(l, init{init(L"extends")}).type != NULL_)
          //      # then get type
                t = b2(l);
            else
                t = null_init;
      //      # add pair
            c.append(init{init(n), t, nt});
       //     # get next element, , or >
            Token j = b4(l);
        //    # if j is >, then return
            if (j == L">")
                return c;
          //  # if j is not ',', then raise error
            if (j != L",")
            {
                throw_error(L"the > symbol is expected");
                return null_init;
            }
        }
}
     

//# this function will return an element
static Token b4(it &l){
 //       # q - count of > left
 //       # check if q != 0
        if (glb::q > 0){
            glb::q -=1; 
            return Token(L">", MATH);
        }
 //       # else, get next element
        else{
            Token g = get(l);
            
          //  # check if an n is >> or >>>
            if (g == L">>"){
                glb::q = 1;
                return Token(L">", MATH);
            }
            else if (g == L">>>"){
                glb::q = 2;
                return Token(L">", MATH);
            }
            else 
               return g;
        }
        
}

static init b3(it& l){
   //     # check if next symbol is >
    //    # if yes, then return []
        Token j = b4(l);
        if (j == L">") return init::lst();
        else{
      //  # else, go back and continue
            if (j.type != NULL_) back(l, 1);
      //  # parse for type gen
            auto c = init::lst();
            while (true){
                c.append(b1(l));
             //   # there only 3 symbols allowed: , and < and >
             //   # the symbol > must be after symbol <
             //   # that followed to the name
                auto g = b4(l);
                if (g == L",") {continue;}
                else if (g == L">") {break;}
                else
                {
                    #ifdef check_buf
                    wcout<<g<<endl;
                    wcout<<l.peek()<<endl;
                    #endif
            //    #    print('ff::', g)
                    throw ex();
                    
                }
            }
        //    # return c
            return c;
        }
}



//# this code will parse for ast syntax tree
static init a1(it &l, init b = null_init, int ign = 0){
  //      # get element
        Token j = get(l);
        
        #ifdef check_buf
        wcout<<L"a1::"<<j<< "of type"<<j.type<<endl;
        #endif
    //    # check element type
   //     # if j is None
        if (j.type == 0){
            
         //   wcout<<"got b"<<b<<endl;
   //     #    print('here is none')
     //       # then check if b is None
       //     # if raise SyntaxError
         //   # else return b
            if (b.type == 0)
            {
                throw_error(L"1482the operator is expected");
            }
            else
                return b;
        }
        // check if annotations is allowed here
        if (glb::note)
        { 
        #ifdef check_buf
          wcout<<L"annotation is allowed here"<<endl;
        #endif
          if (b.type == 0)
          {
            // check if j is equals to @
            if (j == L"@")
            {
                
                glb::cur_line = j.line;
                glb::cur_position = j.position;
                // return annotation type
                return a1(l, _at(l), ign); 
            }
          }
        }
  //      # get element type 
        int i = j.type;
     //   # if there are bracket
        if (i == BR_open){
            
    //        wcout<<"FUCK"<<endl;
            
          //  # if not b is none, then raise SyntaxError
            if (b.type != 0){
                if (j == L"[")
                //    # return [ cast
                {
                    #ifdef check_buf
                    wcout<<L"here is an loop \n";
                    #endif
                    init i = arr(l, j);
                    int ll = i.size();
                    if (ll != 1)
                    {
                        throw_error(L"dimension size must be 1");
                    }
                    if (ll == 0)
                    {
                        return a1(l, b, ign);
                    }
                    else
                    {
                        return a1(l, init{init(j), b, i[0]}, ign);
                    }
                }
            }
    //        # if [ here 
            if (j == L"[")
                throw_error(L"the operand is expected");
       //     # else if j is (:
            else if (j == L"(")
            {
      //          wcout<<"hell here, fukc you"<<endl;
         //       # it's may be parentheses or cast
                glb::cur_line = j.line;
                glb::cur_position = j.position;
         
                return brk(l, ign);
            }
         //   # else if j is {
            else if (j == L"{")
            {
                throw_error(L"array types is not supported by this version\
 of ejavac");
             //   # it's will be an array, so return array
                return a1(l, init{init(L"$array", j.line, j.position), arr(l, j)}, ign);
            }
            else
            {
                throw_error(L"the ; or , is expected here");
            }
        }
 //       #
   //     #print('got', j)
      //  # if j is math
        if (i == MATH){
      //      # check if j == '<'f
      //      # and b is none
            if (b.type == NULL_)
            {
               if (j == L"<")
               {
                   
                glb::cur_line = j.line;
                glb::cur_position = j.position;
                   
                    return a1(l, tcall(l, j), ign);
                }
            }
            
            #ifdef check_buf
                wcout<<":MATH"<<endl;
            #endif
    //        # else perform operations with math
                return mth(l, b, j, ign);
            
        }
   //     # if j is special operator
        if (i == SPECIAL)
       //     # then perform operations with special 
            return spc(l, b, j, ign);
   //     # if j is value
        if (is_v(j)){
     //       # if j is value, then check for other operations
       //     # if b is None, then return forward operations
         //   # else raise SyntaxError
 //           wcout<<fibMonaccianSearch<wstring, wstring>(bsk, 
 //   (wstring)j, bsk_len)<<j<<endl;
         
            if (b.type == 0)
            {
                init ib;
                #define elif else if
                if (j == L"false") ib = init{init(L"%k", j.line, j.position), init(L"false")}; 
                elif (j == L"true") ib = init{init(L"%k", j.line, j.position), init(L"true")}; 
                elif (j == L"null") ib = init{init(L"%k", j.line, j.position), init(L"null")}; 
                elif (j == L"super")
                { 
                    ib = init{init(L"%k", j.line, j.position), init(L"super")};
                    if (l.peek()!=L".")
                    {
                        throw_error(L"the . is expected here");
                    }
         //       glb::cur_line = j.line;
         //       glb::cur_position = j.position;
                    
           //         ib = super_call(l);
                } 
                elif (j == L"this") 
                { 
                    
       //         glb::cur_line = j.line;
     //           glb::cur_position = j.position;
        ib = init{init(L"%k", j.line, j.position), init(L"this")}; 
               
              //      ib = this_call(l);
                } 
                elif (i == STRING_){
                    ib = init{init(L"%s", j.line, j.position), init(j)};
                }
                elif (i == NUMBER){
                    ib = init{init(L"%d", j.line, j.position), init(j)};
                }
                elif (i == WORD)
                {
                    
                glb::cur_line = j.line;
                glb::cur_position = j.position;
                    
                    ib = call(l, j);
                }
                elif (i == CHARACTER){
                    ib = init{init(L"%c", j.line, j.position), init(j)};
                }
                #undef elif
        //        wcout<<"a1: none"<<endl;;
        //        wcout<<j<<endl;
                // check if here an special symbol
                
             //   # check for necessary operations
                return a1(l, ib, ign);
            }
            else
            {
    //      #      print('b', b)
        #ifdef check_buf
        wcout<< b<<endl;
        #endif
      //     #     print('j', j)
                throw_error(L"1545the operator is expected");
            }
        }
     //   # check for keyword
        if (j == L"new"){
            //
        #ifdef check_buf
            wcout<<"fun new"<<endl;
        #endif
            if (b.type != NULL_)
                throw_error(L"the operator 'new' is invalid");
                
                glb::cur_line = j.line;
                glb::cur_position = j.position;
                
            return a1(l, cnew(l), ign);
        }
        
  //      # check for keyword 
        if (j == L"instanceof"){
        //    # if b is none
            if (b.type == 0)
          //      # then raise SyntaxError
                throw_error(L"illegal start of expression");
        //    # if level is higher that expected
            if (ign > 9){
          //      # then go back
                back(l, 1);
                return b;
            }
            else{
             //   # return type
                return a1(l, {init(L"$instanceof", j.line, j.position), b, b2(l)}, ign);
            }
        }
    //    wcout<<is_v(j)<<endl;
        wstring errorr = L"1780the ";
        errorr += j;
        errorr += L" is not expected here";
        throw_error(errorr);
};


static Token none(){
    return null_token;
}
 //   # this function parentheses
static init prt(it& l){
//        it & g =* new it2(l.f, l.i, {
//   {Token(L")", BR_open), none}
//});
        int i = l.i;
        // get pointer
        glb::jumpb(l);
        // set pointer
        
        it & g = *new it3(l.f, i, l.i - 1); 
  //      # get an expression
        auto exp = a1(g);
   //     # displace index 
        l.i = g.i + 1;
   //     # return expression
        return exp;
}

// this function checks an '('
static init brk(it &ll, int ign)
{
    it & l = *new it3(ll.f, ll.i);
    int i = l.i;
    // jump to the next ) symbol
    
  //  wcout<<get(ll)<<endl;
  // check for cur_line and cur_position
  int line = glb::cur_line;
  int position = glb::cur_position;
  glb::cur_line = 0;   
  glb::cur_position = 0;   
  //  exit(0);
    
    glb::jumpb(l);
#ifdef check_buf
    wcout<<L"JUmpB"<<endl;
#endif

    // 
 //   wcout<<"here is an hell";
    // get token 
    Token j = get(l);
    // if token is an value or ( element, then 
    l.i = i;
    
#ifdef check_buf
    wcout<<j<<endl;
    wcout<<is_v(j)<<endl;
#endif
    // it is an type casting
    if (is_v(j) || (j == L"(") 
    ||
    (
    j == L"new" ||
    j == L"this" || 
    j == L"super"
    )
    
    )
    {
        // get type
        init t = b2(l);
        // get ) element
        gt_e(l, init(L")"));
        // l
#ifdef check_buf
        wcout<<"$c#ast: "<<t<<endl;
#endif
        ll.i = l.i;
        // return cast type
        return a1(ll,init{init(L"$cast", line, position), t, a1(ll, null_init, 14)}, ign);
    }
    // check if -> here
    else if (j == L"->")
    {
#ifdef check_buf
        wcout<<L"calling an lambda"<<endl;
#endif
        // then perform some operation with lambda
        init ar = get_args(l);
        // get -> token
        gt_e(l, init(L"->"));
        #ifdef check_buf
        wcout<<L"_>"<<endl;
    #endif
        ll.i = l.i;
        // get function body
        init bd = f2(ll);
        #ifdef check_buf
        wcout<<L"got bd"<<endl;
        #endif
        // return lambda expression
        throw_error(L"lambdas is not supported by this version of ejavac");
        return a1(ll, init{init(L"$lambda", line, position), ar, bd}, ign);
    }
    else
    {
        init pr = prt(l);
        ll.i = l.i;
        return a1(ll, pr, ign);
    }
 //   while 
}

// this function gets an lambda expression arguments
static init get_args(it &l )
{
    // save position
    int i = l.i;
    // get token
    Token j = get(l);
    //check if got token is an closing ) token
    if (j == L")")
    {
        return null_init;
    }
    // check if got token is an name
    else if (is_name(j))
    {
        // check if , or ) after name got
        Token n = get(l);
        // if got ')', then just return an array with one parameters
        if (n == L")")
        {
            return init{init(0), init(j)};
        }
        // if got ',', then get list of names
        else if (n == L",")
        {
            // create an array
            init ar = init{init(0), init(j)};
            // add names to an array
            // iterate until ) reaches
            while (n == L",")
            {
                // get name
                j = gt_n(l);
                // add name to array
                ar.append(init(j));
                // get , or ) element
                n = gt_e(l, init{init(L")"), init(L",")});
            }
            return ar;
        }
        // else, go back and try with parameters
    }
    // go back
    l.i = i;
    #ifdef check_buf
    wcout<<"demo\n";
    #endif
    // iterate until ) met 
    init ar = init{init(1)};
    do
    {
        // add type and name
       ar.append(init{b2(l), gt_n(l)});
       // iterate until ) met, get , or )  
    } while(gt_e_b(l, init{init(L","), init(L")")}).type != BR_close);
    
    // return
    return ar;
};
/*
//# this function checks an '(' 
static init  brk( it& l, int ign){
 //       # this function will try two times:
 //       # firstly, parentheses
 //       # if got an error, then typecast
        init i = l.i;
        try{
  //          # check with parentheses
 //    #       print('calling')
            return a1(l, prt(l), ign);
        }
        catch(syntax_error ere){
 //       try{
        //    # check with typecast
            it & g = *new it2(l.f, i, {
   {Token(L")", BR_open), none}
});
   //         # get type
            init t = b2(g);
 //    #       print ('casting')
   //         # check if ) forward
     //       # if not, raise SyntaxError
            if (get(g).type != NULL_)
                throw syntax_error("the ) is expected");
   //         # forward
 //     #      print('f')
            l.i = g.i + 1;
            return a1(l,init{init(L"$cast"), t, a1(l, null_init, 14)}, ign);
 //       }
        
        }
}
*/
//# special operations
static init spc(it& l, init b, Token j, int ign){
      //  # if b is None, then raise an Error
        Token cc;
        if (b.type == 0)
        {
            throw_error(L"2648 invalid start of expression");
            return null_init;
        }
     //   # there may be ternary operation or dot operation
        else{
            

// check if :: here
        if (j == L"::"){
    throw_error(L"scope resolutions is not supported by this version of ejavac");
       //     check for an name 
            j = get(l);
            // check for new keyword
            if (j == L"new"){
                return a1(l, init{init(L"::new", j.line, j.position), b}, ign);
            }
            else{
                #ifdef check_buf
                wcout<<L" :: "<<j<<endl;
#endif
                // check for name, else return "name is expected"
                if (is_name(j))
                {
                    return a1(l, init{init(L"::", j.line, j.position), b, init(j)}, ign);
                }
                else
                {
                    throw_error(L"the name is expected after ::");
                    return null_init;
                }
            }
        }
        
        // check if -> here
        if (j == L"->")
        {
            // if L"->" here, then check if back operator is an name
            if (b.type == 2)
            {
                if (is_name(b.b2))
                {
                    // then get an lambda expression
                    #ifdef check_buf
                    wcout<<L"type and name "<<b.b2<<endl;
                  #endif
                  throw_error(L"lambdas is not supported by this version of ejavac");
        
                    return init{init(L"$lambda"), init{init(0), init{b}}, f2(l)};
                }
            }
    #ifdef check_buf        
            wcout<<L"type is for -> "<<b.type<<endl;
            wcout<<L"name is for -> "<<b.b2<<endl;
            wcout<<L"name is for type -> "<<b.b2.type<<endl;
    #endif
            // else, throw error
            throw_error(L"the -> is not expected here");
            return null_init;
        }
            
        //            # if there are an ternaty condition 
            if (j == L"?"){
                
                throw_error(L"condition operator is not supported by \
this version of ejavac");
                #ifdef check_buf
                wcout<<"????????????????????????"<<endl;
                #endif
                
            //    # add new operation
                glb::t ++ ;
                init c = a1(l, null_init, ign);
                #ifdef check_buf
                wcout<<"$$$$$$$$$$$$$$$$$$$$$$"<<c<<endl;
                #endif
                init d = a1(l, null_init, ign);
                
                
                
                #ifdef check_buf
                wcout<<"$$$$$$$$$$$$$$$$$$$$$$"<<d<<endl;
                #endif
                
        //        # return
                return init{init(L"?"), b, c, d};
            }
            if (j == L":"){
                
                #ifdef check_buf
                wcout<<":::::::::::::::::::::::::::::"<<endl;
                #endif
                
         //       # if there :
                if (glb::t == 0)
                {
                    throw_error(L"the symbol ':' is not expected here");
                    return null_init;
                }
                if (ign < 1)
                {
                    glb::t--;
                }
                else
                {
                    back(l, 1);
                }
                return b;
            }
            if (j == L",")
            {
          //      # if there an comma
           //     # then raise error
                throw_error(L"comma is not expected here");
                return null_init;
            }
            if (j == L".")
            {
                  // check for cur_line and cur_position
        //        # if there an dot
        //        # then raise error
                cc = get(l);
  glb::cur_line = cc.line;
  glb::cur_position = cc.position;
      //          # if c is <, then make tcall
                if (cc == L"<")
                    return a1(l, init{j, b, tcall(l, cc)}, ign);
       //         # else if c is not name, then raise error
       //         # if here is keyword
                if (cc == L"class")
                    return a1(l, init{j, b, init{init(L"%k", glb::cur_line, glb::cur_position), init(L"class")}}, ign);
                if (cc == L"new")
                {
                    return a1(l, init{j, b, tnew(l)}, ign);
                }
           //     # if here is name 
                if (is_name(cc)){
             //       # 
               //     # try without typecast
                    return a1(l, init{j, b, call(l, cc)}, ign);
                }
                else 
                {
                    
  glb::cur_line = 0;   
  glb::cur_position = 0;  
                    
                    throw_error(L"invalid name");
                    return null_init;
                }
            }
        }
        
        wstring out = L"the ";
        out+= j;
        out+= L" is not expected here";
        throw_error(out);
        return null_init;
}
// # this function returns an [] conditions
static init mlc(it &l, init & type){
    // create malloc list
    
    init k = init::lst();
    
    int level = 0;
    
    Token j;
    // repeat until [ here
    while ((j = gt_e_b(l, init(L"["))).type != NULL_)
    {
        // get an array
        if (gt_e_b(l, init(L"]")).type != NULL_)
        {
            // if ] here
            back(l, 2);
            break;
        }
        else
        {
            // else, get array and append
            init i=(arr(l, j));
            //
                k.append(i[0]);
            if (i.size() != 1)
            {
                throw_error(L"dimension size must be 1");
            }
        }
        level ++;
    }
    
    #ifdef check_buf
    wcout<<L"array DIMENSION: "<<level<<endl;
    #endif
    //
    while ((j = gt_e_b(l, init(L"["))).type != NULL_)
    {
        // get an array
        gt_e(l, init(L"]"));
        //
        level ++;
    }
    k.append(level);
    // print k
    #ifdef check_buf
    wcout<<L"array: "<<k<<endl;
    #endif
    return k;
}

#define SyntaxError(x) syntax_error(x);
#define raise throw
static inline auto operator ==(wstring n, wchar_t i){
    if (n.size() == 1){
        return (n[0] == i);
    }
    return false;
}

static inline auto operator !=(wstring n, wchar_t i){
    if (n.size() == 1){
        return (n[0] != i);
    }
    return true;
}
//# this function releases an cnew conditions



int i;

//# this function releases an cnew conditions
static init cnew(it &l){
    
      // check for cur_line and cur_position
  int line = glb::cur_line;
  int position = glb::cur_position;
  glb::cur_line = 0;   
  glb::cur_position = 0;  
 //   wcout<<"hello cnew"<<endl;;
        init ar = init::lst();
 //   wcout<<"funck"<<endl;
 //       # get type of class
        init n = b22(l);
        
        #ifdef check_buf
        wcout<<"your head will be cutted"<<endl;
        wcout<<n<<endl;
        #endif
   //     # get type of arr
        Token b = get(l);
        #ifdef check_buf
        wcout<<b<<endl;
        #endif
        init bb;
     //   # raise error if b is neither [ nor ( 
        if ((b != L"[")&&(b != L"("))
            throw_error(L"'(' or '[' expected");
       // # get array
//        # check for class reimplementation
        if (b == L"(")
        {
  //          wcout<<"passed"<<endl;
            ar = arr(l, b);
            
   //         wcout<<l.f[l.i]<<endl;
  //          wcout<<"passed2"<<endl;
            if (gt_e_b(l, init{init(L"{")}).type!=NULL_)
            {
                throw_error(L"the anonumous classes are not supported \
by this version of ejavac");
            //    wcout<<"anonumous class"<<endl;
  //          # get class body
                bb = cls[5].copy();
                it &jk = * new it3(l.f, l.i);
                
                #ifdef check_buf
                wcout<<L"THERE IS ANONUMOUS CLASS DEFINITION"<<endl;
                #endif
                
                cls_body_anonumous(jk, bb);
                l.i = jk.i;
    //            # return 
                return init{init(L"$anonumous", line, position), n, ar, bb};
            }
            return init{init(L"$new", line, position), n, ar};
        }
      //  # else, then process
        else{
        //    # go back
            back(l, 1);
          //  # get array
            ar = mlc(l, n);
            
            #ifdef check_buf
            wcout<<l.peek()<<endl;
            #endif
            
            Token bbt = gt_e_b(l, init{init(L"{")});
//            # get body
            if (bbt.type!=0)
            {
                 #ifdef check_buf
            wcout<<"WHAT?"<<endl;
            #endif
                if (ar.size() > 1)
                {
                    throw_error(L"array creation with both dimension\
 expression and initialization is illegal");
                }
  //              # get array body
                bb = arr(l, Token(L"{", BR_open));
                return init{init(L"$new_arr", line, position), n, ar[0], bb};
            }
            else
            {
            #ifdef check_buf
            wcout<<"passed?"<<endl;
            #endif
                if (ar.size() == 1)
                {
                    throw_error(L"array dimension missing");
                }
                    return init{init(L"$malloc", line, position), n, ar};
            }
            
        }
}

//# this function releases an cnew conditions
static init tnew(it &l){
    
      // check for cur_line and cur_position
  int line = glb::cur_line;
  int position = glb::cur_position;
  glb::cur_line = 0;   
  glb::cur_position = 0;  
 //   wcout<<"hello cnew"<<endl;;
        init ar = init::lst();
 //   wcout<<"funck"<<endl;
 //       # get type of class
        init n = b23(l);
        
        #ifdef check_buf
        wcout<<"your head will be cutted"<<endl;
        wcout<<n<<endl;
        #endif
   //     # get type of arr
        Token b = get(l);
        #ifdef check_buf
        wcout<<b<<endl;
        #endif
        init bb;
     //   # raise error if b is neither [ nor ( 
        if (b != L"(")
        {
        //    # go back
            throw_error(L"the ( is expected here");
            return null_init;
        }
       // # get array
//        # check for class reimplementation
        else
        {
  //          wcout<<"passed"<<endl;
            ar = arr(l, b);
            
   //         wcout<<l.f[l.i]<<endl;
  //          wcout<<"passed2"<<endl;
            if (gt_e_b(l, init{init(L"{")}).type!=NULL_)
            {
                throw_error(L"the anonumous classes are not supported \
by this version of ejavac");
            //    wcout<<"anonumous class"<<endl;
  //          # get class body
                bb = cls[5].copy();
                it &jk = * new it3(l.f, l.i);
                
                #ifdef check_buf
                wcout<<L"THERE IS ANONUMOUS CLASS DEFINITION"<<endl;
                #endif
                
                cls_body_anonumous(jk, bb);
                l.i = jk.i;
    //            # return 
                return init{init(L"$anonumous", line, position), n, ar, bb};
            }
            return init{init(L"$new", line, position), n, ar};
        }
      //  # else, then process
}

//# this function releases an call conditions or returns just value pair
static init call(it &l, Token j){
    // check for cur_line and cur_position
  int line = glb::cur_line;
  int position = glb::cur_position;
  glb::cur_line = 0;   
  glb::cur_position = 0; 
 //       # if j maybe an method name
  //          # then get next element to compare 
    //        # if it's an '(' symbol
            Token n = get(l);
        //    # if n is none, then just return j
            if (n.type == NULL_)
            {
                return init{init(L"%v", line, position), init(j)};
            }
       //     # if n is not '(', then back and return
            if (n != '(')
            {
                back(l, 1);
                return init{init(L"%v", line, position), init(j)};
            }
     //       # else return expression
            return init{init(L"$call",  line, position), init(j), 
                   arr(l, Token(L"(", BR_open)), init::lst()};
}

//# this function releases an super call conditions or returns just value pair
static init super_call(it &l){
 //       # if j maybe an method name
   int line = glb::cur_line;
  int position = glb::cur_position;
  glb::cur_line = 0;   
  glb::cur_position = 0; 
  //          # then get next element to compare 
    //        # if it's an '(' symbol
            Token n = get(l);
        //    # if n is none, then just return j
            if (n.type == NULL_)
            {
                return null_init;
            }
       //     # if n is not '(', then back and return
            if (n != '(')
            {
                back(l, 1);
                return null_init;
            }
     //       # else return expression
            return init{init(L"$scall", line, position), arr(l, Token(L"(", BR_open))};
}

//# this function releases an this call conditions or returns just value pair
static init this_call(it &l){
 //       # if j maybe an method name
   int line = glb::cur_line;
  int position = glb::cur_position;
  glb::cur_line = 0;   
  glb::cur_position = 0; 
  //          # then get next element to compare 
    //        # if it's an '(' symbol
            Token n = get(l);
        //    # if n is none, then just return j
            if (n.type == NULL_)
            {
                return null_init;
            }
       //     # if n is not '(', then back and return
            if (n != '(')
            {
                back(l, 1);
                return null_init;
            }
     //       # else return expression
            return init{init(L"$tcall", line, position), 
                arr(l, Token(L"(", BR_open))};
}

//static inline  init call(it& l, Token j){
 //   return call (l, j, false);
//}


static init tcall(it &l, Token j){
//   #     print('tcall')
//   #     print(j)
//        # for typecast call there must be 
//        # an name, 
//        # a typecast operator <>
//        # and a call operator ()
    // check for cur_line and cur_position
  int line = glb::cur_line;
  int position = glb::cur_position;
  glb::cur_line = 0;   
  glb::cur_position = 0; 
  //
        if (j != L"<")
            throw_error(L"illegal start of expression");
   //     #
        glb::q = 0;
    
        init gen = b3(l);
//     #   print(gen)
#ifdef check_buf
        wcout<<gen<<endl;
  //      # 
#endif
        j = get(l);
  //  #    print(j)
        if (!is_name(j))
            throw_error(L"illegal start of expression");
    //    # 
        
        glb::q = 0;
  //      #
        // check if an opening bracket here
        Token jk = get(l);
   
        if (jk.type != BR_open || jk != L"(" )
            throw_error(L"illegal start fo expression");
//        # 
// #       print("no error")
        return init{init(L"$call", line, position), init(j),
             arr(l, Token(L"(", BR_open)), gen};
}

namespace cnt{
    vector<bool> t;
    static Token c_stop (){
        t[t.size()-1] = false;
        return null_token;
    };
    static bool tt(){
        return t[t.size()-1];
    }
}

//# this function returns an array
static init arr(it& l, Token j){
 //       wcout<<"array"<<endl;
     
        // get closing array bracket
        if (j == L"("){
            j = Token(L")", BR_close);
        }
        else if (j == L"["){
            j = Token(L"]", BR_close);
        }
        else if (j == L"{"){
            j = Token(L"}", BR_close);
        }
        // if no closing bracket here, then raise error
        else{
            (throw_error(L"no allowed bracket here"));
            return null_init;
        }
        // create new token
        // if next symbol is an bracket, then return 
        if (l.peek() == j)
        {
            l.i += 1;
            return init::lst();
        }
    //    # create an new iterator
        it &g = * new it2(l.f, l.i, {
            {j, none}, 
{Token(L",", SPECIAL), none}
} );

        // create an list 
        init ar = init::lst();
        // iterate with all awailable elements
        label12:
        // append an expression
        ar.append(a1(g));
        // check if next element is equals to ,
        // if yes, then go back
        if (g.peek() == L",")
        {
            g.i ++;
            // go back
            goto label12;
        }
        // if got an closing bracket, then return
        else if (g.peek() == j && g.peek().type==BR_close)
        {
            l.i = g.i;
            l.i ++;
            // return
            return ar;
        } 
        // else, raise error
        else{
            throw_error(L"the comma or closing bracket is expected here");
            return ar;
        }
}
  
static inline Token br_close(Token jj){
    wchar_t jk = jj[0];
    //    # get closing bracket
    jk = br_close(jk);
        // create new token
    Token j;
    j = wstring(1, jk);
    return j;
} 


  
#define cnt_start cnt::t.push_back(true);
#define cnt_stop cnt::t.pop_back();
#define c_stop cnt::c_stop
#define c_t (cnt::tt())
//# this function returns an dictionary for the annotation
static init dct_note(it &l){
    bool note;
    // create new iterator
    it & g = * new it2(l.f, l.i, {
        {Token(L")", BR_close), none},
        {Token(L",", BR_close), none},
        });
    
    Token n;
    // create new array
    init ar = init::lst();
    // check next element, if got ), then return iterator
    
    #ifdef check_buf
    wcout<<L"@@@"<<g.peek()<<endl;
    #endif
   
   
    if (g.peek() == L")")
    {
        goto ret_label;
    }
   
    
    // iterate until ) meet
    back_label:
    // get name element
    n = gt_n(g);
    // get = element
    gt_e(g, init(L"="));
    // get expression
            note = glb::note;
            glb::note = true;
                ar.append(init{init(n), a1(g, null_init, 0)});
            glb::note = note;
    // check if peek element is equals to ,
    // if yes, then go back
    // else, return
    if (g.peek() == L",")
    {
        // displace g position
        g.i ++;
        // go back
        goto back_label;
    };
      //   #   print('done')
        // #   print(g.f[g.i])

 //       # displace element index
    // return label
    ret_label:
    //
    l.i = g.i;
    l.i ++;
   #ifdef check_buf     
        wcout<<"next element is: "<<l.peek()<<endl;
    #endif
   //     # return array of elements
        return ar;
}

// this function returns an math expression
static init mth(it &l, init b, Token j, int ign)
{
    // check for b, if none, then there will be an back operator
    if (b.type == NULL_)
    {
        init nt = init::lst();
        // check if there an back operator
        do
        {
            
            if (j == L"++")
            {
                throw_error(L"operator ++ is not supported by this version of \
ejavac");
            }
            
            if (j == L"--")
            {
        throw_error(L"operator -- is not supported by this version of \
ejavac");
            }
            // check if operator is back operator
            if (contains(b_ops, j))
            {
                nt.append(j);
            }
            // else, throw an error
            else
            {
                wstring jj = L"the ";
                jj += (j);
                jj += L" is not an back operator";
                throw_error(jj.c_str());
            }
            // get next token
            j = get(l);
        }
        while (j.type == MATH);
        // go back and get an expression
        if (j.type != NULL_)back(l, 1);
        // get an expression
        init zx = a1(l, null_init, 15);
        // return 
        return a1(l, init{init(L"$b_ops"), nt, zx}, ign);
    }
    else
    { 
        init nt = init::lst();
        // if j in forward operands
        if (contains(f_ops, j))
        {
            throw_error(L"postfix operators are not\
 supported by this version of ejavac");
            do
            {
                // if not in f_ops, then raise error
                if (!contains(f_ops, j))
                {
                     wstring jj = L"the ";
                     jj += j;
                     jj += L" is not an forward operator";
                     throw_error(jj.c_str());
                }
                // append to the array 
                nt.append(j);
                // get next element
                j = get(l);
            } while (j.type == MATH);
            // go back and get an expression
            if (j.type != NULL_)back(l, 1);
            // return 
            return a1(l, init{init(L"$f_ops"), nt, b}, ign);
        }
        //      #else, check if here an assigment operator
        if (contains(s_ops, j))
        {
//            # then perform with assigment operator
        //    if (expr_type(b) != expr_flags.variable:
     //           raise SyntaxError("variable is expected")
  //          # 
            return init{init(L"$assign"), init(j), b, a1(l)};
        }
        //       # check level of j operator
        int i = lvl_check(j);
   //     # if i exceedes an ign, the back and return
        if (ign > i){
     //       wcout<<"back";
            back (l, 1);
            return b;
        }
      //  # else, process
        else{
  //      wcout<<L"got: "<<j<<endl;;
            init dj = init{init(L"$math"), init(j), b, a1(l, null_init, i + 1)};
            //         wcout<<dj;
            return a1(l, dj, ign);
        }
    }
}

#define zr 0



static vector<vector<int>>flg_pool{
{
    0
}    
};
static vector<vector<init>>note_pool{
    {
        init::lst()
    }    
};

//#define True true;
#define elif else if
#define True true
#define False false
#define None null_init
  //  # this function sets an flag

static map<wstring, int> c1_map_values{
    {L"public", _public},
    {L"final", _final},
    {L"abstract", _abstract},
    {L"interface", _interface|_abstract},
    {L"@interface", _annotation|_interface|_abstract},
    {L"enum", _enum},
    {L"class", _class}
};

static map<int, int> c1_map_error{
    {_public, _public|_private|_protected},
    {_final, _final|_abstract},
    {_abstract, _final|_abstract|_native|_default},
    {_interface|_abstract, _final},
    {_annotation|_interface|_abstract, _final},
    {_enum, _final|_abstract},
    {_class, 0}
};

static set<int> c1_map_false{
    _interface|_abstract, 
    _enum,
    _class,
    _annotation|_interface|_abstract
};
  
static bool c1(int& f, Token n){
// if n  public;
        // get value
        int value = c1_map_values[((wstring)n)];
        // check if value is 0
        if (value == 0) goto label1234;
   #ifdef check_buf     
        wcout<<value<<L" "<<f<<endl;
    #endif
        // if not 0, then check in error table
        if ((f & c1_map_error[value])==0)
        {
            // set modifier
            f |= value;
            // return if value contains in false map
            return (!contains(c1_map_false, value));
        }
        else
        {
            // throw an error
            throw_error(L"illegal combination of modifiers");
            return (!contains(c1_map_false, value));
        }
        
       ///* if (n == L"public"){
            //// if flag is not set, then set;
            //if ((f & (_public|_private|_protected)) == zr)
            //{
                //f |= _public;
                //return True;
            //}
            //raise SyntaxError("illegal combination of modifiers");
        //}
        //// if n is final ;
        //elif (n == L"final"){
            //// if flags are not set, then set;
            //if ((f & (_final|_abstract)) == zr){
                //f |= _final;
                //return True;
            //}
            //raise SyntaxError("illegal combination of modifiers");
        //}
        //// if n is abstract;
        //elif (n == L"abstract"){
            //// if flags are not set, then set;
            //if ((f & (_final|_abstract|_native)) == zr)
            //{
                    //f |= _abstract;
                    //return True;
            //}
            //raise SyntaxError("illegal combination of modifiers");
        //}
        //// if n is interface;
        //elif (n == L"interface"){
            //// interface cannot be final;
            //if ((f & _final) == zr){
                //f |= _interface;
                //f |= _abstract ;
                //return False;
            //}
            //raise SyntaxError("interface cannot be final");
        //}
        //// if n is annotation;
        //elif (n == L"@interface"){
            //// interface cannot be final;
            //if ((f & _final) == zr)
            //{
                //f |= _annotation;
                //return False;
            //}
            //raise SyntaxError("annotation cannot be final");
        //}
        //// if n is enum;
        //elif (n == L"enum")
        //{
            //// enum cannot be final or abstract;
            //if ((f & (_final|_abstract)) == zr)
            //{
                //f |= _enum;
                //return False;
            //}
            //raise SyntaxError("enum cannot be final or abstract");
        //}
        //// if n is class;
        //elif (n == L"class"){
            //f |= _class;
            //return False;
        //}*/
        // if we got just name;
        label1234:
        if (is_name(n))
        {
            return false;
        }
        else{
        // raise syntaxerror;
            throw ex();
        }
}

static map<wstring, int> c11_map_values{
    {L"private", _private},
    {L"protected", _protected},
    {L"native", _native},
    {L"strictfp", _strictfp},
    {L"static", _static},
    {L"volatile", _volatile},
    {L"transient", _transient},
    {L"synchronized", _synchronized}
};

static map<int, int> c11_map_error{
    {_private, (_public|_private|_protected|_default) },
    {_protected, (_public|_private|_protected) },
    {_native, (_abstract|_native|_strictfp) },
    {_strictfp, (_native|_strictfp) },
    {_static, (_native|_default) },
    {_volatile, _volatile },
    {_transient, _transient },
    {_synchronized, _synchronized }
};

static bool c11(int& f, Token n){
     // get value
     
        int value = c11_map_values[((wstring)n)];
        // check if value is 0
        if (value == 0) throw ex();
        // if not 0, then check in error table
        if ((f & c11_map_error[value])==0)
        {
            // set modifier
            f |= value;
            // return if value contains in false map
            return true;
        }
        else
        {
            // throw an error
            throw_error(L"illegal combination of modifiers");
            return true;
        }
        // if ( n in ["private", "protected"];
        //if ( n == L"private"){
            //// if ( flag is not set, then set;
            //if ((f&(_public|_private|_protected)) == zr)
            //{
                //f |= _private;
                //return True;
            //}
            //// else (, raise erro;
            //raise SyntaxError("illegal combination of modif (iers");
        //}
        
        //if ( n == L"protected"){
            //// if ( flag is not set, then set;
            //if ((f&(_public|_private|_protected)) == zr)
            //{
                //f |= _protected;
                //return True;
            //}
            //// else (, raise erro;
            //raise SyntaxError("illegal combination of modif (iers");
        //}
        //// these flags will be set once;
        //// the ast generator will ;
        //// choose what flag is correct;
        //else if ( n == L"native"){
            //if ((f&(_abstract|_native|_strictfp)) == zr)
            //{
                //f |= _native;
                //return True;
            //}
            //raise SyntaxError("illegal combination of modif (iers");
        //}
        //else if (n==L"strictfp"){
            //if ( (f &(_native|_strictfp)) == zr){
                //f |= _strictfp;
                //return True;
            //}
            //raise SyntaxError("illegal combination of modif (iers");
        //}
        //else if ( n == L"static"){
            //if ( (f &(_native|_default)) == zr){
                //f |= _static;
                //return True;
            //}
            //raise SyntaxError("illegal combination of modif (iers");
        //}
        //else if (n == L"volatile"){
     /////   { "transient", "synchronized"},
            //// get flag;
            //int g = _volatile;
            //// check if ( here an this flag;
            //if ( (f & g) == zr){
                //f |= g;
                //return True;
            //}
            //raise SyntaxError("illegal combination of modif (iers");
        //}
        //else if (n == L"transient"){
     /////   { "transient", "synchronized"},
            //// get flag;
            //int g = _transient;
            //// check if ( here an this flag;
            //if ( (f & g) == zr){
                //f |= g;
                //return True;
            //}
            //raise SyntaxError("illegal combination of modif (iers");
        //}
        //else if (n == L"synchronized"){
     /////   { "transient", "synchronized"},
            //// get flag;
            //int g = _synchronized;
            //// check if ( here an this flag;
            //if ( (f & g) == zr){
                //f |= g;
                //return True;
            //}
            //raise SyntaxError("illegal combination of modif (iers");
        //}*/
}

static bool glb_sf(int &f, Token n){
        try{
            return c1(f, n);
        }
        catch(ex l){
            wstring k = L"the ";
            k += (wstring)n;
            k += L" is not expected here";
            throw_error(k);
        }
}

static bool fld_sf(int &f, Token n){
      ///  if (is_name (n)) return false;
            if ((n == L"(")|| 
                (n == L"{")||
                (n == L"<")||
                (n == L":")||
                (n == L"}"))
            return false;
        try{
        //    wcout<<n<<endl;
            return c11(f, n);
        }
        catch (ex l){
            try
            {
#ifdef check_buf
                wcout<<"c1:"<<endl;
#endif
                return c1(f, n);
            }
          catch(ex l){
            wstring k = L"+the ";
            k += (wstring)n;
            k += L" is not expected here";
            throw_error(k);
          }
        } 
}

static bool fld_sf2(int & f, Token n){
    #ifdef check_buf
   wcout <<L"funny load "<<n<<endl;
    #endif
    if (n == L"default"){
        if ((f & (_default|_static|_abstract|_private)) == 0){
            f |= _default | _public;
            return true;
        }
        else{
            throw_error(L"illegal combination of modifiers");
        }
    }
    else{
        return fld_sf(f, n);
    }
}

static Token c9(it& l, init& n)
        // get next element
{
        Token f = get(l);
        
  //      wcout<<f<<endl;
        // repeat while @ is here
        while ( f == L"@")            // if next element is "interface", then return
        {
            if ( gt_e_b(l,init{init(L"interface")}).type != NULL_){
                return Token(L"@interface", WORD, f.line, f.position);
            //continue
            };
            glb::cur_line = f.line;
            glb::cur_position = f.position;
    #ifdef check_buf
    wcout<<"@annotation "<<endl;
    #endif 
            n.append(_at(l));
            f = get(l);
        // return f 
        };
    return f;
}

inline bool eq(int f, int y)
{
    return (f & y) != 0;
}

// this function gets an interface parents
static void cls_parents(it &l, init& ret)
{
    // get token
    Token j = get(l);
    
    // if j is equals to < 
    // then set generic 
    if (j == L"<")
    {
        // set generic
        ret[2] = b5(l);
        j = get(l);
    }
    #ifdef check_buf
    wcout<<"hell: "<<j<<endl;
    #endif
    // if j is equals to extends 
    if (j == L"extends")
    {
        // get generic cast
        // then get super class nametype
        glb::bound = false;
        ret[3] = b22(l);
        glb::bound = true;
        // get next token
        j = get(l);
    }
    // if j is equals to implements 
    if (j == L"implements")
    {
        // then fill array with names
        // get array point 
        init & nt = ret[4];
        // fill array 
        do
        {
            glb::bound = false;
            nt.append(b22(l));
            glb::bound = true;
        } 
        while (gt_e_b(l, init(L",")).type != NULL_);
        // finish
    }
    // if not equals, then go back and return
    else
    {
        // go back
        if (j.type != NULL_)
        {
            // 
            back(l, 1);
        }
    }
    // return 
    return;
}


#define _recall 0x0001
#define _interface  0x0200
#define _is_abstract 0x1000
#define _is_anonumous 0x0010
#define _annotation 0x2000

static void chk_anonumous(int f, int flags)
{
    if ((flags & _is_anonumous)!= 0)
    {
        if ((f & _static) != 0)
        {
            throw_error(L"modifier static not allowed here");
        }
    }
    
    if ((flags & _interface) != 0)
    {
        if ((f & (_private|_protected)) != 0)
        {
            throw_error(L"modifier private and protected not allowed here");
        }
    }
    if ((flags & _annotation) != 0)
    {
        if ((f & (_default)) != 0)
        {
            throw_error(L"modifier default is not allowed here");
        }
    }
}

static bool chk_mth(int f, int flags)
{
    
    if ((flags & _interface) != 0)
    {
        //throw_error(L"constructor is not allowed in interfaces");
        
if (
(
(f & 
(
_native |
_synchronized|
_volatile |
_transient)) != 0
)
 ){
    throw_error(L"modifiers \
volatile, native, synchronized, \
transient is not allowed here");
}

else if 
(
((f & _static) == 0) & ((f & _protected) != 0)
)
{
    throw_error(L"modifiers \
protected is not allowed here");
}
        //
        return ((f & (_default|_static|_private)) == 0);
    }
    else
{
    chk_anonumous(f, flags);
    bool a = (flags & _is_abstract) != 0;
    if (a){

if ((f & 
(
_volatile |
_transient)) != 0 ){
    throw_error(L"modifiers \
volatile, \
transient is not allowed here");
}

}
else
{
  if ((f & 
(
_volatile |
_abstract |
_transient)) != 0 ){
    throw_error(L"modifiers \
abstract, \
volatile, \
transient is not allowed here");
}  
}
}
#ifdef check_buf
wcout<<(f & (_abstract|_native))<<endl;
#endif

if (((f & _abstract) * (f & _private)) > 0)
{
    throw_error(L"illegal combination of modifiers: \
abstract and private");
}

return ((f & (_abstract|_native))!= 0);

}

static void rjct_mdf(int f, vector<int> jk, wstring otu)
{
    int z = 0;
    // 
    int l = jk.size();
    // while l is > 0
    while (l > 0)
    {
        l --;
        z |= (f & jk[l]);
        #ifdef check_buf
        wcout<<(f & jk[l])<<endl;
        #endif
    }
    // if z > 0, then raise error
    if (z != 0) throw_error(otu);
    return;
}

static void chk_fld(int f, int flags)
{
        chk_anonumous(f, flags);
        
        if ( 
        (
        flags & _interface
        )
         != 0
        )
        {
            if (f & 
            (
            _transient|
            _private|
            _protected
            )
            )
            {
                throw_error(
                L"modifier transient, private, protected is not allowed in interface methods"
                );
            }
        } 
    // here0
    if ((f &(
_strictfp|
_native|
_synchronized|
_abstract|
_default)) != 0)
                {
throw_error(L"modifier \
strictfp, native, \
synchronized, abstract, default is \
not allowed here");
}
}

static void chk_cls(int f, int flags)
{
        chk_anonumous(f, flags);

if ((f &(
_strictfp|
_native|
_synchronized|
_volatile |
_transient |
_default)) != 0)

{
throw_error(L"modifier \
strictfp, native, \
synchronized, abstract, volatile, transient, default is \
not allowed here");
}

    return;
}

static void chk_note(int f)
{

if ((f &(
_strictfp|
_native|
_synchronized|
_abstract|
_static|
_private|
_protected|
_volatile |
_transient |
_default)) != 0)

{
throw_error(L"modifier \
strictfp, static, native, private, protected, \
synchronized, abstract, volatile, transient, default is \
not allowed here");
}

    return;
}

static void chk_cnst(int f, int flags)
{
        chk_anonumous(f, flags);
    
    if ((flags & _interface) != 0)
    {
        throw_error(L"constructor is not allowed in interfaces");
    }
        
    // 
    if ((flags & _enum) != 0)
    {
        if ((f & _public) != 0) throw_error(L"public modifier is not allowed in enumerations");
    }
    // here
    if ((flags &  (_is_anonumous)) != 0)
    {
        throw_error(L"constructors is not allowed in anonumous classes");
    }
    #ifdef check_buf
    wcout<<L"error here"<<endl;
    #endif
    if ((flags & _interface) == 0)
    { 
    rjct_mdf(f, 
                {
                    _abstract,
                    _volatile,
                    _transient,
                    _final,
                    _static,
                    _native
                }, 
                L"the modifiers 'abstract' \
'volatile', \
'transient', \
'final', \
'static', 'native' is not allowed here"
            );
            
    }
    else
    {
        
    }
    #ifdef check_buf        
    wcout<<L"constructor here\n";
    #endif
}

static void chk_s_cnst(int f, int flags, int nt_size)
{
   /* if ((flags & _interface) != 0)
    {
        throw_error(L"constructor is not allowed in interfaces");
    }*/
    if ((flags & _recall) != 0)
        {
            throw_error(L"illegal start of type");
        }
    else
        {
            if (nt_size>0)
            {
                throw_error(L"illegal start of type");
            }
            else if (f != 0 && f != _static)
            {
                throw_error(L"illegal start of type");
            }
            else if (((flags & _is_anonumous)!=0) && (f == _static))
            {
                throw_error(L"static initializers is not allowed here");
            }
        }
}

// this function gets an class body
static void cls_body_recall(it &l, init & ret, int f, init nt, int flags)
{
    flags = flags | _recall;
    // get flags 
//    int f = (int)ret[0];
    // check if class is abstract and return 
    return cls_body(l, ret, f, nt, flags);
}

// this function returns an constructor element
static init c_init(it &l, int f)
{
    return init{init(f), fnc_b(l)};
}

// this function defines an anonumous class
static void cls_body_anonumous(it &l, init &ret)
{
    
    #ifdef check_buf
    wcout<<L"i am calling now::ANONUMOUS CLS_BODY"<<ret<<endl;
    #endif
    cls_body(l, ret, 0, init::lst(), _is_anonumous);
}

static void cls_body(it &l, init &ret, int f_copy, init nt_copy, int flags, init name);

static void cls_body(it &l, init &ret, int f_copy, init nt_copy, int flags)
{
    cls_body(l, ret, f_copy, nt_copy, flags, null_init);
};

static void cls_body(it &l, init &ret, int f_copy, init nt_copy, int flags, init name2)
{
    int num = -1;
    Token j ;
    
  //  bool recall = (flags & _recall) != 0;
  //  bool is_abstract = (flags & _is_abstract) != 0;
  //  bool is_anonumous = (flags & _is_anonumous) != 0;
  //  bool interface = (flags & _interface) != 0;
    
    init nt;
    init name;
    init type;
    init gen;
    int f;

    #define ret5 ret
    
    // get all class fields
    label123:
    num ++;
    
    // set modifiers
    nt = nt_copy.copy();
    f = f_copy;
    #ifdef check_buf
    wcout<<L"first modifier: "<<f<<endl;
    #endif
    // pass semicolons
    _pass(l);
    // get token
    j  = c9(l, nt);
    
    // if not interface, then not ignore default modifier
    if ((flags & _interface) == 0)
    {
    // repeat until field modifier here
    while (fld_sf(f, j))
    {
        // get next token
        j = c9(l, nt);
    };
    }
    else
    {
    // repeat until field modifier here
    while (fld_sf2(f, j))
    {
        // get next token
        #ifdef check_buf
        wcout<<j<<endl;
        #endif
        j = c9(l, nt);
    };    
    };
    #ifdef check_buf
    wcout<<L"finish cls body"<<endl;
    wcout<<j<<endl;
    #endif
    // check token
    // if got class token
    if ((j.type == WORD) && (contains(bsk, j))) 
        {
             #ifdef check_buf
            wcout<<L"CHECKING ??"<<ret5<<endl;
    #endif     
                    chk_cls(f, flags);
            // then create class field and fill
            init cl = cls.copy();
            // get name for class
            
                     
     #ifdef check_buf
            wcout<<L"ALL OKAY&&?"<<ret5<<endl;
    #endif       
            
            cl[1] = gt_n(l);
            cl[0] = f;
            cl[6] = nt.copy();
            // fill cls body
            
     #ifdef check_buf
            wcout<<L"FILLING?"<<ret5<<endl;
    #endif       
            
            if (((flags & _interface) == 0) && ((f & _static) == 0)) 
                          cls_fill(l, cl, true);
            else cls_fill(l, cl);
            // add field to the class body
        //    ret5.append(init{init(0), cl});
    #ifdef check_buf
            wcout<<L"ERROR HERE?"<<ret5<<endl;
    #endif
            ret5[0].append(cl);
            ret5[6].append(0);
            goto label123;
        }
        
# define a is_abstract
    // if got an < , then create tcall method
    else if (j == L"<")
    {
        #ifdef check_buf
            wcout<<L"what hell? 4333?"<<endl;
    #endif
        // check if is annotation here
            
        // get generics
         gen = b5(l);
         
         #ifdef check_buf
            wcout<<L"i got generics? 4341?"<<gen<<L" FLY "<<l.peek()<<endl;
    #endif
    
           
         // check if next element is an constructor
         Token k = gt_e_b(l, init{name2, init(L"(")});
         // compare with name
         
         if (k == L"(")
         {
             goto add_const;
         }
         else if ((k == name2)  && (gt_e_b(l, init(L"(")).type == BR_open))
         {
             add_const:
             back(l, 1);
             // if equal, then get initializer
             // check constructor modifiers
                         chk_cnst(f, flags);
             ret5[1].append(c_func(l, f, nt.copy(), false, gen, null_init, 
                        init(Token(L"$initializer", STRING_, k.line, k.position))));
            ret5[6].append(1);
         }
         else
         {
             if (k.type != NULL_)
             {
                 back(l, 1);
             }
        // get type
             type = b2(l);
        // get name 
             name = gt_n(l);
        // create function
        if ((flags & _interface) != 0)
        {
            if ((f & (_default|_static|_private)) == 0)
            {
                f = (f | _public) | _abstract;
            } 
        }
             ret5[1].append(c_func(l, f, nt.copy(), chk_mth(f, flags), gen, type, name));
            ret5[6].append(1);
         }
    }
    // if got an (, then return constructor
    else if (j == L"(")
    {
        back(l, 1);
        // check constructor modifiers
        chk_cnst(f, flags);
        // append
        ret5[1].append( c_func(l, f, nt.copy(), false, init::lst(), null_init, 
             init(Token(L"$initializer", STRING_, j.line, j.position))));
            ret5[6].append(1);
    }
    // if got an } ,then return
    else if (j == L"}")
    {
        return;
    }
    else if (j == L"{")
    {
        chk_s_cnst(f, flags, nt.size());
        ret5[3].append(c_init(l, f));
            ret5[6].append(3);
    }
    // if got an :, then continue for body
    else if (j == L":")
    {
        // get { symbol
        gt_e(l, init(L"{"));
        // call themself
        cls_body_recall(l, ret, f, nt.copy(), flags);
    }
    // if got name
    else if (is_name(j))
    {
        
        #ifdef check_buf
            wcout<<L"CHECKING NAME??"<<ret5<<endl;
    #endif     
        // then compare with name
        //  if equals, then return constructor
        if ((j == name2) && (gt_e_b(l, init(L"(")).type == BR_open))
        {
            back(l, 1);
            // check constructor modifiers
            chk_cnst(f, flags);
            // return constructor
            ret5[1].append(c_func(l, f, nt.copy(), false, init::lst(), null_init, 
            init(Token(L"$initializer", STRING_, j.line, j.position))));
            // 
            
            ret5[6].append(1);
        }
        // else, go back and get type and name
        else
        {
            // go back
            back(l, 1);
            #ifdef check_buf
            wcout<<L"type??"<<l.peek()<<endl;
    #endif     
            // get type
            type = b2(l);
            // get name
            name = gt_n(l);
            // if next element is (, then return function
            if (gt_e_b(l, init(L"(")).type == BR_open)
            {
                back(l, 1);
                
        if ((flags & _interface) != 0)
        {
            if ((f & (_default|_static|_private)) == 0)
            {
                f = (f | _public) | _abstract;
            } 
        }
                ret5[1].append(c_func(l, f, nt.copy(), chk_mth( f, flags), init::lst(), type, name));
            ret5[6].append(1);
            }
            else
            {
                // else get fields
                back(l, 1);
                // check field flags
                chk_fld(f, flags);
                
                // appending 
                #ifdef check_buf
                wcout<<L"FIELDS HERE ::::"<<endl;
                #endif
                // append
                ret5[2].append(init{ init(f), type, arg_e(l, ((f & _interface) != 0) ,true), nt.copy()});
            ret5[6].append(2);
                #ifdef check_buf
                wcout<<ret5<<endl;
                #endif
            }
        }
    }
    else
    {
        if (j.type == NULL_)
        {
            throw_error(L"the } is expected");
            return;
        }
        else
        {
            wstring name = L"the ";
            name += j;
            name += L" is not expected here";
            throw_error(name);
        }
    }
    goto label123;
}
# undef a
# undef ret5

// this function gets an class body
static void cls_body(it &l, init & ret)
{
    // i 
    #ifdef check_buf
    wcout<<L"i am calling now:: CLS_BODY"<<endl;
    #endif
    // get flags 
    int flg = (int)ret[0];
    // check if class is abstract and return 

#ifdef check_buf    
    wcout<<ret[5]<<endl;
#endif
    
    return cls_body(l, ret[5], 0, init::lst(), (eq(flg, _abstract)) ? _is_abstract : 0, ret[1]);
}


// this fucntion gets an interface body
static void int_body(it &l, init & ret)
{
    // i 
    #ifdef check_buf
    wcout<<L"i am calling now (interface)"<<endl;
    #endif
    // return 
    return cls_body(l, ret[5], 0, init::lst(), _interface, ret[1]);
}
// this fucntion gets an class parents
static void int_parents(it &l, init&ret)
{
    // get token
    Token j = get(l);
    
    // if j is equals to < 
    // then set generic 
    if (j == L"<")
    {
        // set generic
        ret[2] = b5(l);
        j = get(l);
    }
    // if j is equals to extends
    if (j == L"extends")
    {
        // then fill array with names
        // get array point 
        init & nt = ret[4];
        // fill array 
        do
        {
            nt.append(b22(l));
        } while (gt_e_b(l, init(L",")).type != NULL_);
        // finish
    }
    // if not equals, then go back and return
    else
    {
        // go back
        if (j.type != NULL_)
        {
            // 
            back(l, 1);
        }
    }
    // return 
    return;
}

static void enm_parents(it &l, init&ret)
{
    // get token
    Token j = get(l);
    
    // if j is equals to < 
    // then set generic 
 //   if (j == L"<")
 //   {
        // set generic
 //       ret[2] = b5(l);
 //       j = get(l);
 //   }
    // if j is equals to extends
    if (j == L"implements")
    {
        // then fill array with names
        // get array point 
        init & nt = ret[4];
        // fill array 
        do
        {
            nt.append(b22(l));
        } while (gt_e_b(l, init(L",")).type != NULL_);
        // finish
    }
    // if not equals, then go back and return
    else
    {
        // go back
        if (j.type != NULL_)
        {
            // 
            back(l, 1);
        }
    }
    // return 
    return;
}

static void enm_fill(it &l, init &ret5, init nt_copy, int & num)
{
    // get enumeration array
    init & ret4 = ret5[4];
    // fill enumeration array
    init nt;
    Token tk;
    
    bool dot = false;
    
    int exp_size = nt_copy.size();
    
    back123:
    // 
    // get annotation copy
    nt = nt_copy.copy();
    
    // get identifier
    tk = c9(l, nt);
    // if got name, then add enumeration element
    if (is_name(tk))
    {
        if (dot)
        {
            throw_error(L"an comma is expected here");
        }
        dot = true;
        // set name
        init name = init(tk);
        // this is an array init
        init ar;
        // check if next element is (
        if ((tk = gt_e_b(l, init(L"("))).type != NULL_)
        {
            // then set array
            ar = arr(l, tk);
        }
        // else, set an empty array
        else
        {
            ar = init::lst();
        }
        // if next element is {, then get anonumous class 
        if (gt_e_b(l, init(L"{")).type == NULL_)
        {
            ret4.append(init{init(L"$set_enum"),  name, ar, init(num)});
            
            ret5[6].append(4);
        }
        else
        {
            throw_error(L"the anonumous classes are not supported \
by this version of ejavac");
            //cun
            // 
            init body = cls[5].copy();
            cls_body_anonumous(l, body);
            // 
            ret4.append(init{init(L"$new_enum"), name, ar, body});
            
            ret5[6].append(4);
        }
    }
    else if (tk == L",")
    {
        if (!dot)
        {
            throw_error(L"an comma is not expected here");
        }
        dot = false;
    }
    // if got :, then check for : element
    else if (tk == L":")
    {
        // get { element
        gt_e(l, init{init(L"{")});
        // recall
        enm_fill(l, ret5, nt, num);
        // check for } element
        gt_e(l, init{init(L"}")});
    }
    // if got ';' or '}', then check if annotations there, if yes, then throw error
    else// if ((tk == L";") || (tk == L"}"))
    {
        back(l, 1);
        // throw error
        if (nt.size() > exp_size) throw_error(L"an identifier expected");
        
        return;
    }
    num ++;
    goto back123;
    // repeat until ; or } meet
    
}

static void enm_body(it &l, init &ret5, init & name)
{
    int i = 0;
    // get an enumeration expression
    enm_fill(l, ret5, init::lst(), i);
    // get expression
    Token tk = gt_e(l, init{init(L"}"), init(L";")});
    // if got } expression, then finish
    if (tk == L"}") return;
    #ifdef check_buf
          wcout<<l.peek()<<endl;
    #endif
    // if got ; expression, then continue
    cls_body(l, ret5, 0, init::lst(), _enum, name);
    
}

// fill enum body
static void enm_body(it &l, init &ret)
{
    enm_body(l, ret[5], ret[1]);
    return;
}

static void ant_body_recall(it &l, init &ret5, int f_copy, init nt_copy)
{
    
    throw_error(L"annotations is not supported\
 by this version of ejavac");
    
     Token j ;
    
  //  bool recall = (flags & _recall) != 0;
  //  bool is_abstract = (flags & _is_abstract) != 0;
  //  bool is_anonumous = (flags & _is_anonumous) != 0;
  //  bool interface = (flags & _interface) != 0;
    
    init nt;
    init name;
    init type;
    init gen;
    int f;
    
    // get all class fields
    label123:
    
    // set modifiers
    nt = nt_copy.copy();
    f = f_copy;
    #ifdef check_buf
    wcout<<L"first modifier: "<<f<<endl;
    #endif
    // pass semicolons
    _pass(l);
    // get token
    j  = c9(l, nt);
    
    
    // repeat until field modifier here
    while (fld_sf(f, j))
    {
        // get next token
        j = c9(l, nt);
    };
    
    #ifdef check_buf
    wcout<<L"finish _ant body recall"<<endl;
    wcout<<j<<endl;
    #endif
    // check token
    // if got class token
    if ((j.type == WORD) && (contains(bsk, j))) 
    {
            chk_cls(f, _interface);
            // then create class field and fill
            init cl = cls.copy();
            // get name for class
            cl[1] = gt_n(l);
            cl[0] = f;
            cl[6] = nt.copy();
            // fill cls body
            cls_fill(l, cl);
            // add field to the class body
            ret5[0].append(cl);
            ret5[6].append(0);
            goto label123;
    }
        
    // if got an } ,then return
    else if (j == L"}")
    {
        return;
    }
    // if got an :, then continue for body
    else if (j == L":")
    {
        // get { symbol
        gt_e(l, init(L"{"));
        // call themself
        ant_body_recall(l, ret5, f, nt.copy());
    }
    // if got name
    else if (is_name(j))
    {
        // then go back and get type and name
        {
            // go back
            back(l, 1);
            // get type
            type = b2(l);
            // get name
            name = gt_n(l);
            // if next element is (, then return note
            if (gt_e_b(l, init(L"(")).type == BR_open)
            {
                chk_note(f);
                
                gt_e(l, init(L")"));

                // check if next element is default
                if (gt_e_b(l, init{init(L"default"), init(L";")}) == L"default")
                {
                    // check if next element is @
                    // if yes, append an annotation
                    bool temp = glb::note;
                    glb::note = true;
                    {
                    // append
                        ret5[5].append(init{/*init(5)*/ nt.copy(), type, name, a6(l)});
            ret5[6].append(5);
                    }
                    glb::note = temp;
                }
                // else, just append an set
                else
                {
                    // append
                    ret5[5].append(init{/*init(5)*/ nt.copy(), type, name, null_init});
            ret5[6].append(5);
                }
            }
            else
            {
                // else get fields
                back(l, 1);
                // check field flags
                chk_fld(f, _interface);
                
                // appending 
                #ifdef check_buf
                wcout<<L"FIELDS HERE ::::"<<endl;
                #endif
                // append
                ret5[2].append(init{ init(f), type, arg_e(l, ((f & _interface) != 0) ,true), nt.copy()});
            ret5[6].append(2);
                #ifdef check_buf
                wcout<<ret5<<endl;
                #endif
            }
        }
    }
    else
    {
        if (j.type == NULL_)
        {
            throw_error(L"the } is expected");
            return;
        }
        else
        {
            wstring name = L"the ";
            name += j;
            name += L" is not expected here";
            throw_error(name);
        }
    }
    goto label123;
}

static void ant_body(it &l, init &ret)
{
    init & nt = ret[5];
    // this function calls an annotation class builder
    return ant_body_recall(l, nt, 0, init::lst());
};

static void cls_fill(it &l, init &ret)
{
    cls_fill(l, ret, false);
}
// this method fills an class body
static void cls_fill(it &l, init& ret, bool is_anonumous)
{
  //  it & l = *new it3(l.f, l.i);
    #ifdef check_buf
    wcout<<L"yes, processing"<<endl;
    #endif
    // check class type
    int f = (int)ret[0];
    // if type is class
    if (eq(f, _class))
    {
        // then fill an class
        cls_parents(l, ret);
    }
    else if (eq(f, _enum))
    {
        enm_parents(l, ret);
    }
    else if (eq(f, _interface))
    {
        // then fill an interface
        int_parents(l, ret);
    }
    #ifdef check_buf
    wcout<<ret<<endl;
    wcout<<l.peek()<<endl;
    #endif
    // get { symbol
    gt_e(l, init(L"{"));
    if (eq (f, _class))
    {
#ifdef check_buf
        wcout<<L"you here c?"<<endl;
#endif
        if (is_anonumous) cls_body_anonumous(l, ret[5]);
        else cls_body(l, ret);
    }
    else if (eq(f, _annotation))
    {
#ifdef check_buf
        wcout<<L"you here a?"<<endl;
#endif
        ant_body(l, ret);
    }
    else if (eq(f, _interface))
    {
#ifdef check_buf
        wcout<<L"you here i?"<<endl;
#endif
        int_body(l, ret);
    }
    else if (eq(f, _enum))
    {
#ifdef check_buf
        wcout<<L"you here e?"<<endl;
#endif
        enm_body(l, ret);
    }
}    
// this method creates an global class
static init c2(it& l)
        // create object class
{
    // get copy of class
    init ret = cls.copy();
    // bind annotation list
    init &nt = ret[6];
    // create flag int
    int f = 0;
    // get token
    Token j = c9(l, nt);
    // 
    while(glb_sf(f, j))
    {
        j = c9(l, nt);
    }
    //..
    #ifdef check_buf
    wcout<<j<<endl;
    wcout<<L"nt"<<nt<<endl;
    #endif
    // if got name, then raise an error
    if (is_name(j))
    {
        throw_error(L"class, interface or enum expected");
    }
    // else, get name
    else
    {
        j = gt_n(l);
    }
    //
    ret[0] = init(f);
    // add name
    ret[1] = init(j);
    // modify class
    cls_fill(l, ret);
    // return
    return ret;
    // this method returns an parent classes
};
/*
static init cls_p(int f, it& l)
        // f is flags
        // l is iterat||
        // cl[0] is an generic
        // cl[1] is an superclass
        // cl[2] is an interfaces
{
        init cl {null_init, null_init, init::lst()};
        
        Token j = get(l);
        // if j is <
        if ((j == L"<") 
        && ((f & (_enum|_annotation)) == zr)) 
        // then get type cast
        {
            cl[0] = b5(l);
            j = get(l);
        // if j == extends
        };
        if (j == L"extends" 
        && (f & (_enum|_annotation)) == zr)            // then get type
            // if interface
        {
            if ((f & _interface) != zr) 
            {
                while (true)
                {
                    cl[2].append(b2(l));
                    j = get(l);
                    // if j is comma, then continue
                    if ( j == L",")
                    {
                        continue;
                    }
                    else
                    {
                        break;
            // if class
                    };
                };
            }
            else
            {
                cl[1] = b2(l);
                j = get(l);
            };
        };
        // if j is implements
        // implements supp||tes only f|| classes
        if((f & (_enum|_interface|_annotation)) == zr 
        && j == L"implements")
        {
          while ( True){
            cl[2].append(b2(l));
            j = get(l);
            // if j is comma, then continue
            if ( j == L","){
                continue;
            }
            else
            {
                break;
        // if j is none
            };
          };
        };
        if (!(j.type == NULL_))  back(l, 1);
        return cl;
};
*/
static init _st(it &l)
{
        //    print("first", l.f[i])
            if (gt_e_b(l, init(L";")).type != NULL_)
            {
                return null_init;
            }
            else
            {
                it & b = *new it2(l.f, l.i, {
                    {Token(L";", SPECIAL), none}
                //    {Token(L")", BR_close), none}
                    });
                init a = a1(b);
                l.i = b.i + 1;
                return a;
            }
};

static init _st2(it &l)
{
        //    print("first", l.f[i])
            if (gt_e_b(l, init(L")")).type != NULL_)
            {
                return null_init;
            }
            else
            {
                it & b = *new it2(l.f, l.i, {
              //      {Token(L";", SPECIAL), none}
                    {Token(L")", BR_close), none}
                    });
                init a = a1(b);
                l.i = b.i + 1;
                return a;
            }
};

static init k1(Token j, it &l)
{
    
    map<wstring, int> k {
        {L"assert",1},
        {L"break",2},
        {L"continue",3},
        {L"do",4},
        {L"if",5},
    //    {L"final",13},
        {L"for",6},
        {L"goto",7},
        {L"return",8},
        {L"switch",9},
        {L"throw", 11},
        {L"try",10},
    //    {L"var", 11},
        {L"while",12},
        {L"this", 13},
        {L"super",14}
    };
    
    // check if token in map
    int i = k[(wstring)j];
    // if i is 0, then raise error and return null
    if (i == 0)
    {
        // if not keyword here, then return null init
        return null_init;
        // throw error
     //   wstring out = L"the ";
     //   out += j;
     //   out+= L" is not expected here";
     //   throw_error(out);
    } 
    else if (i == 13)
    {
    glb::cur_line = j.line;
    glb::cur_position = j.position;
        return this_call(l);
    }
    else if (i == 14)
    {
    glb::cur_line = j.line;
    glb::cur_position = j.position;
        return super_call(l);
    }
    else
    {
        Token n;
        init a, b, c;
        bool t;
        switch(i)
        {
            // assert
            case 1:
        {
            throw_error(L"assert is not supported by this version of \
ejavac");
            // create an iterator
            it & jfd = * new it2(l.f, l.i, 
            {
                
            {Token(L";"), none}, 
                
            {Token(L":"), none}
            
            });
            // get expression
            a = a1(jfd);
            l.i = jfd.i + 1; 
            // check if here an ':'
            if (l.get(jfd.i) == L":")
            // if true, then get string
            {
                n = get(l);
                // check type of element, raise error if not string
                if ( n.type != STRING_){
                    throw_error(L"the string is expected");
                    n = null_token;
                // continue
            // else, set string as None
                }
                else if (l.peek() != L";")
                {
                    throw_error(L"the ; is expected here");
                }
            }
            else
                n = null_token;
            // return array
            return init{init(L"$assert"), a, init(n)};
        }    
            // break
            case 2:
        {
            a = init(L"$break");
            
            // label
            case2:
            
            n = get(l);
            // if j is name, then get ;, else, check if 
            // j is an ;, else raise error
            if ( is_name(n))
            {
                a = init{a, init(n)};
                // get an semicolon
                n = get(l);
            // check if j is an semicolon
            }
            else
            {
                a = init{a, null_init};
            }
            if ( n != L";")            
            {
                throw_error(L"the ; is expected here");
            // return
            };
            return a;
        }   
            // continue
            case 3:
        {
            a = init(L"$continue");
            goto case2;
        }
            // do 
            case 4:
        {
 //           gt_e(l, init(L"{"));
            // get body
            a = f1(l);
            // get while and condition
            gt_e(l, init(L"while"));
            gt_e(l, init(L"("));
            c = prt(l);
            // create do_while object and return            
            return init{init(L"$do"), c, a};
        }
            // if 
            case 5:
        {
            gt_e(l, init(L"("));
      //      print("got (")
            // get condition element
            a = prt(l);
//  wcout<<L"got "<< a;
            // get function element
            b = f1(l);
      //      print("got b")
            // if "else" is next, then
       //     print("got", c)
            if (gt_e_b(l, init(L"else")).type != NULL_)       
        //         print("else meet")
            {
                c = f1(l);
            // if not, then go back
            }
            else
            {
                c = init::lst();
            // return if element
      //      print("ret ")
            };
            return init{init(L"$if"), a, b, c};
        }
             
            // for
            case 6:
        {
            // 
            #ifdef check_buf
            wcout<<L"FOR OBJECT CALLING HERE"<<endl;
        #endif
            gt_e(l, init(L"("));
            // get token
            n = get(l);
            // if got ;, then create for object
            if (n == L";")
            {
                // return for object
                a = _st(l);
                b = _st2(l);
        //        gt_e(l, init(L"{"));
                return init{init(L"$for"), init(1), null_init, a, b, f1(l)};
            }
            else if (n == L"@")
            {
                // go back
                back(l, 1);
                goto case61;
            }
            else if (n == L"var")
            {
                // get name
                n = gt_n(l);
                // get : or = Token
                j = gt_e(l, init{
                    init(L":"),
                    init(L"=")
                    });
                // if j is an :, then get expression and return init
                if (j == L":")
                {
                    a = prt(l);
      //              gt_e(l, init(L"{"));
                    // return
    throw_error(L"for-each is not supported by this version of \
ejavac");
                    return init{init(L"$for"), init(3), n, a, f1(l)};
                }
                else
                {
                    c = init{init(L"$var"), n, a6(l)};
                    // return for object
                    a = _st(l);
                    b = _st2(l);
    //                gt_e(l, init(L"{"));
                    return init{init(L"$for"), init(1), c, a, b, f1(l)};
                }
            }
            else if (is_name(n))
            {
                #ifdef check_buf
                wcout<<"name got (for)\n";
    #endif
                // go back
                back(l, 1);
                // get type
                a = b2(l);
                // check if name here
                n = get(l);
                // if got name, then continue
                if (is_name(n))
                {
                    #ifdef check_buf
                    wcout<<"wcout is name\n";
                    #endif
                    goto case62;
                }
                else
                {
                    l.i = i;
                    goto case63;
                }
            }
            case63:
            a = _st(l);
            b = _st(l);
            c = _st2(l);
  //          gt_e(l, init(L"{"));
            return init{init(L"$for"), init(1), a, b, c, f1(l)};
            
            case61:
            
            // get type
            a = b2(l);
            // get name
            n = gt_n(l);
            
            case62:
            // check if next element is :
            if (gt_e_b(l, init(L":")).type != NULL_)
            {
                case64:
                #ifdef check_buf
                wcout<<L"::::\n";
                #endif
                #ifdef check_buf
                //
                wcout<<L"for -> prt";
     //           gt_e(l, init(L"{"));
                // if yes, then return for object
                #endif
                b = prt(l);
                #ifdef check_buf
                //
                wcout<<L"opopopopopop\n";
     //           gt_e(l, init(L"{"));
                // if yes, then return for object
                #endif
                throw_error(L"for-each is not supported by this version of \
ejavac");
                return init{init(L"$for"), init(2), a, n, b, f1(l)};
            }
            else
            {
                // go back and get operators
                back(l, 1);
                a = (init{init(L"$set"), a, arg_e(l)});
                b = _st(l);
                c = _st2(l);
   //             gt_e(l, init(L"{"));
                return init{init(L"$for"), init(1), a, b, c, f1(l)};
            }
        }
            // goto
            case 7:
        {
            n = gt_n(l);
            gt_e(l, init(L";"));
            return init{init(L"$goto"), init(n)}; 
        }
            // return
            case  8:
        {
            return init{init(L"$return"), _st(l)};
        }
            // switch
            case 9:
        {
            throw_error(L"switch is not supported by this \
version of ejavac");
            gt_e(l, init(L"("));
            // get switch expression
            
            a = prt(l);
            // get { element
            gt_e(l, init(L"{"));
            // get case or default
            j = get(l);
            // do
            while( (j != L"default")&&(j != L"case")&&(j != L"}"))
            {
                throw_error(L"case, default or } is expected");
                j = get(l);
                if (j. type == NULL_)
                {
                    return null_token;
                }
            }
            // create list
            c = init::lst();
            // if got {, then finish
            if (j == L"}")
            {
                return init{init(L"$switch"), a, c};
            }
            // else, iterate
            else
            {
                t = false;
                do
                {
                    // check token
                    if (j == L"case")
                    {
                        // if got case, 
                        // then get an expression
                        // and append case element
                        a = a7(l);
                        c.append(init{init(L"$case"), a});
                    }
                    // 
                    else if (j == L"default")
                    {
                        // if got default
                        // then get : token 
                        // and check if default is appended already
                        gt_e(l, init(L":"));
                        // check if default is defined already
                        if (t)
                        {
                            throw_error(L"duplicate default label");
                        }
                        else
                        {
                            t = true;
                            c.append(init{init{L"$default"}});
                        }
                    }
                    // 
                    else if (j.type == NULL_)
                    {
                        throw_error(L"the closing } is expected");
                    }
                    else
                    {
                        f_add(l, c, j);
                    }
                    
                    // get token
                    j = get(l);
                } while (j != L"}");
            }
            return init{init(L"$switch"), a, c};
        }
            // try
            case 10:
        {
            #ifdef check_buf
            
            wcout<<L"hello try world"<<endl;
            
            #endif
            
            // if ( here, then get try-with-resources 
            if (gt_e_b(l, init(L"(")).type == BR_open)
            {
                throw_error(L"try-with-resources is not supported by \
this version of ejavac");
                a = arg_eq(l);
                // 
                #ifdef check_buf
                wcout<<"got"<<endl;
                wcout<<a<<endl;
                
                #endif
                
            }
            // if not ( here, then continue
            else
            {
                a = null_init;
            }
            a = init{a, f1(l)};
            // get all catch elements 
            c = init::lst();
            while (gt_e_b(l, init(L"catch")).type != NULL_)
            {
                gt_e(l, init(L"("));
                b = init{b2(l), gt_n(l)};
                gt_e(l, init(L")"));
                c.append(init{b, f1(l)});
            } 
            // get finally element
            if (gt_e_b(l, init(L"finally")).type != NULL_)
            {
                return init{init(L"$try"), a, c, f1(l)};
            }
            else
            {
                if (c.size() == 0)
                {
                    if (a.type == NULL_)
                    {
                        throw_error(L"'try' without 'catch', 'finally' or resource declarations");
                    }
                }
                return init{init(L"$try"), a, c, null_init};
            }
        }
            // throw
            case 11:
        {
            // get an throw expression
            return init{init(L"$throw"), _st(l)};
        }
            case 12:
        {
            // get while object
            gt_e(l, init(L"("));
            // get condition
            a = prt(l);
            // get body
       //     gt_e(l, init(L"{"));
            b = f1(l);
            
            if (gt_e_b(l, init(L"else")).type != NULL_)       
        //         print("else meet")
            {
                c = f1(l);
            // if not, then go back
            }
            else
            {
                c = init::lst();
            // return if element
      //      print("ret ")
            };
            // return
            return init{init(L"$while"), a, b, c};
        }
           // case 13:
            
        }
    }
}

static init gt_var(it &l)
{
    // get name
    Token h = gt_n(l);
    gt_e(l, init(L"="));
    // get expression
    // and return
    return init{init(L"$var"), h, a6(l)};
}
/*
static init k1(Token j, it &l){
    void *s[] = {
        &&_assert,
        &&_break,
        &&_continue,
        &&_do,
        &&_if,
        &&_for,
        &&_goto,
        &&_return,
        &&_switch,
        &&_try,
        &&_while
    };
    map<wstring, int> k {
        {L"assert",1},
        {L"break",2},
        {L"continue",3},
        {L"do",4},
        {L"if",5},
        {L"for",6},
        {L"goto",7},
        {L"return",8},
        {L"switch",9},
        {L"try",10},
        {L"while",11}
    };
    
    init a, b, c, d, r;
    Token c1, n;
    
    int jfr = k[(wstring)j];
    
    jfr --;
    
    wcout<<j<<" "<<jfr<<endl;
    
    if ( jfr < 0){
    //    wcout<<"fuck, not ";
    //    wcout<<j<<endl;
         return null_init;
     }
    else goto *s[jfr];
    
    _if:
    
    wcout<<L"FUNNY LOAD"<<endl;
    
            gt_e(l, init{init(L"(")});
      //      print("got (")
            // get condition element
            a = prt(l);
  wcout<<L"got "<< a;
            // get function element
            b = f1(l);
      //      print("got b")
            // if "else" is next, then
            c1 = get(l);
       //     print("got", c)
            if ( c1 == L"else")       
        //         print("else meet")
            {
                c = f1(l);
            // if not, then go back
            }
            else
            {
                c = null_token;
                back(l, 1);
            // return if element
      //      print("ret ")
            };
            return init{init(L"$if"), a, b, c};
    _do:
            gt_e(l, init{init(L"{")});
            // get body
            a = f1(l);
            // get while and condition
            gt_e(l,init{init(L"while")});
            gt_e(l, init{init(L"(")});
            c = prt(l);
            // create do_while object and return            
            return init{init(L"$do"), c, a};
            
            
    _while:
            // get condition
            gt_e(l, init{init(L"(")});
            a = prt(l);
            // get body
            b = f1(l);
            // create while object and return
            return init{init(L"$while"), a, b};
        
    _assert:
            // create counter
            cnt_start
            // create an iterator
            it & jfd = * new it2(l.f, l.i, 
            {
                
            {Token(L";"), none}, 
                
            {Token(L":"), c_stop}
            
            });
            // get expression
            r = a1(jfd);
            l.i = jfd.i + 1; 
            // check counter
            if ( c_t)                // if true, then get string
            {
                n = get(l);
                // check type of element, raise error if not string
                if ( n.type != STRING_){
                    cnt_stop;
                    raise SyntaxError("the string is expected");
                // continue
            // else, set string as None
                };
            }
            else
                n = null_token;
            // return array
            cnt_stop
            return init{init(L"$assert"), r, n};
        
    _goto:
            // get label, semicolon and return
        {
            n = gt_n(l);
            gt_e(l, init{init(L";")});
            return init{init(L"$goto"), n};
        };

    _break:
            // get label(optional), semicolon and return
        {
            n = get(l);
            a = null_init;
            // if j is name, then get ;, else, check if 
            // j is an ;, else raise error
            if ( is_name(n)){
                a = init(j);
                // get an semicolon
                j = get(l);
            // check if j is an semicolon
            };
            if ( j != L";")            {
                raise SyntaxError("the \";\" is expected");
            // return
            };
            return init{init(L"$break"), init(n)};
        
        };
    _continue:
            // get label(optional), semicolon and return
        {
            j = get(l);
            a = null_init;
            // if j is name, then get ;, else, check if 
            // j is an ;, else raise error
            if ( is_name(j)){
                a = init(j);
                // get an semicolon
                j = get(l);
            // check if j is an semicolon
            };
            if ( j != L";"){
                raise SyntaxError("the \";\" is expected");
            // return
            };
            return init{init(L"$continue"), a};
        
        // this function checks for duplicate case label
        };/*
        def chk(x, c)
        {
            j = x[0];
            z = x[1];
            i = c.type;
            i = NUMBER if i == CHARACTER else i;
            // check if got element in constant pool
            if ( c in z)            {
                raise SyntaxError("duplicate case label");
            };
            elif ( j is None)            {
                x[0] = i;
            };
            elif ( j != i)            {
                raise SyntaxError("incompatible types");
            };
            else
            {
                z.append(c);
            };
            return c;
        
        };/**/
 /* //      @add;
    _switch:
          // l is an iterator
          // c is an body
          // z is an constant pool for check if an constant is already defined
        {
          c = init::lst();
          init z = init{null_init, init::lst()};
          bool zd = false;
          // get switch operand
          gt_e(l, init{init(L"(")});
          init v = prt(l);
          // get { element
          gt_e(l, init{init(L"{")});
          // get case or default for first
          j = gt_e(l, init{init(L"case"), init(L"default")});
          
          // repeat
          while ( True)            // check element
          {
            if ( j == L"default"){
                if (zd == true){
                    raise SyntaxError("duplicate default label");
                };
                c.append(init(L"$default"));
                gt_e(l, init{L":"});
                zd = true;
            }
            elif ( j == L"case"){
                // 
                Token g = gt_c(l);
                // get : or comma
                j = gt_e(l, init{init(L":"), init(L",")});
                // repeat until , meet
                while ( j == L",")                    // append other constants
                {
                    g.append(init{init(L"$case"), gt_c(l)});
                    j = gt_e(l, init{init(L":"), init(L",")});
                // if j is :
                };
                if ( j == L":"){
                    c.extend(g);
                };
            }
            elif ( j == L";"){
                continue;
            }
            elif ( j == L"{"){
                c.append(fnc_b(l));
            }
            elif ( j == L"}"){
                return c;
            // if j is None
            // then raise Error
            }
            elif ( j.type == NULL_){
                raise SyntaxError("the symbol } is expected");
            // if j is an keyword
            }
            elif ((r = k1(j, l)).type != NULL_)         //       print(j)
            {
                c.append(r);
            // if j is not an keyword
            }
            else
                // if j is name 
            {
                if ( is_name(j))                    // then get :
          //          print(j)
                    // if got, then add label
                {
                    b = get(l);
                    //
           //         print(b) 
                    //
                    if ( b == L":"){
                        c.append(init{init(L":"), init(j)});
                        continue;
                    // if j is none 
                    }
                    elif ( j.type==NULL_) {
                        raise SyntaxError("the symbol ; is expected");
                    // go back
                    };
                    back(l, 1);
                // go back
                };
                back(l, 1);
                int i = l.i;
                try
                    // make two attempts 
                    // first, with setters
                    // second, with expression
                {
                    r = a5(l);
                    c.extend(r);
                }
                catch(syntax_error ere)
                {
                    l.i = i;
                    r = a6(l);
                    c.append(r);
            
            // get element
                };
            j = get(l);
            };
            return init{init(L"$switch"), v, c};
          };
        }
    _return:
    
        wcout<<"HI, WHAT HAPPENED?"<<endl;
            // return return object
        {
            return init{init(L"$return"), a6(l)};
        // this will return an 
        };
    
     _for:
     
     wcout<<"HI, WHAT HAPPENED? (for)"<<endl;
            // create an counter class
        {
            it * kk;
            cnt_start
            init ff;
   //         l = [];
            // for object type
            int f = 0;
            // 0 - expr, expr, expr
            // 1 - set, expr, expr
            // 2 - type-and-name, expr
            // try to get 3 statements
            gt_e(l, init{init(L"(")});
            
            wcout<<"for (\n";
            
            try
                // create an iterator
            {
                kk = new it2(l.f, l.i, 
                {                     
            {Token(L";"), none},      
            {Token(L")"), c_stop}     
                }                   );
                // first statement may be an set object
                // if not, then may be an expression
                // try with set object
                int i = kk->i;
                try
             //       print("a")
                {
                    a = a5(*kk);
            //        print("b")
                    f = 1;
                // if false, then try with expression
                }
                catch(syntax_error ere)
                {
                    kk->i = i;
                    a = _st(*kk, init(L";"));
                    f = 0;
                };
                // get other two statements
                // get second statement
                b = _st(*kk, init(L";"));
        //        print("bb", b)
                // get third statement
                d = _st(*kk, init(L")"));
                // return for body
                ff = init {init(f), a, b, d};
            // then try with for-each
            }
            catch (syntax_error ere)
  //              print("type 2")
                // get for-each statement
            {
                wcout<<":\n";
                
                kk =new it2(l.f, l.i, {
        {Token(L")", BR_close), c_stop}
                                      }
                );
                
                wcout<<"name : "<<l.f[l.i]<<endl;
                // get var, if not got, then go back
                Token t = gt_e_b(*kk, init{init(L"var")});
                init tt;
                int f = 3;
                if (t.type == NULL_)                    // get type, and name
                {
                     tt = b2(*kk);
                     f = 2;
       //         print("got an type: ", t)
                };
                
                wcout<<tt<<endl;
                
                n = gt_n(*kk);
                
                wcout<<L"got an name: "<<n<<endl;
        //        print("got an name: ", n)
                // get :
                gt_e(*kk, init{init(L":")});
                //
        //        print("got an : element")
        //        print("the first element is:", l.f[l.i])
                // get statement
                a = a1(*kk);
                
                wcout<<a<<endl;
                // displace l
                // return for body
                if (f == 2){
                    ff = init{init(2), tt, init(n), a};
                }
                else
                {
                    ff = init{init(3), init(n), a};
            // check for c.t
                };
            wcout<<kk->f[kk->i]<<endl;
            if (kk->f[kk->i] != L")")       //         print("c.t")
                raise SyntaxError("the ) is expected here");
            // displace k position
            };
            cnt_stop
            l.i = kk->i + 1;
            // get body
            gt_e(l, init{init(L"{")});
            
            wcout<<"for got function"<<endl;
            
            b = fnc_b(l);
            // return
            return init{init(L"$for"), ff, b};
        };
//    #undef k
    /*
        @add;
        def*//**
    _try:
            // get resources
        {
            Token tt = gt_e(l, init{init(L"("), init(L"{")});
            // this is an array with resources
            auto res = init::lst();
            // check element
            if ( tt == L"(")                // if there an (, then there must be some resources
                // get all them
            {
                res = arg_eq(l);
                gt_e(l, init{init(L"{")});
            // get body
            };
            a = fnc_b(l);
            // get catch elements
            init t;
            init cth = init::lst();
            // get next symbol
            Token j = gt_e(l, init{init(L"catch"), init(L"finally")});
            // check got element
            while ( j == L"catch")      //          print("catch")
                // then process until finally met or met nothing
            {
                gt_e(l, init{init(L"(")});
                // get type
                t = b2(l);
                // get name
                n = gt_n(l);
                // get closing bracket
                gt_e(l, init{init(L")")});
                // get body
                gt_e(l, init{init(L"{")});
                // append element
                cth.append(
init{init(L"$catch"), init{init(L"$set"), 
    init(t), init(n)}, fnc_b(l)});
                // get next element
                j = get(l);
            };
            if ( j == L"finally")                // append body
            {
                gt_e(l, init{init(L"{")});
                cth.append(init{init(L"$finally"), fnc_b(l)});
            }
            else
            {
                if (res.size()==0)
                {
                    if (cth.size()==0)
                    {
                        raise SyntaxError("error: \"try\" without \"catch\", \"finally\" or resource declarations");
                    };
                back(l, 1);
                };
            };
            return init{init(L"$try"), res, a, cth};
    };

 ///
}*/

static void f_add(it &l, init &c, Token j)
{
    // check element
            init a;
            // if got semicolon, then pass
            if (j == L";")
            {
                return;
            }
            else if (j == L"{")
            {
                // append body
                c.append(init{init(L"$body"), fnc_b(l)});
            }
            else if (contains(bsk, j))
            {
                // if name is an keyword, then make operations with keyword
             /* init */ a = k1(j, l);
                // if got null_token, then go back and try to append an expression
                if (a.type == NULL_)
                {
                    // back 
                    back(l, 1);
                    // check if final or var keyword here
                    if (j == L"final")
                    {
                        goto label5860;
                    }
                    else if (j == L"var")
                    {
                        goto label5860;
                    }
                    // get token
                    c.append(a6(l));
                }
                // else, append an got expression
                else
                {
                    c.append(a);
                #ifdef check_buf
                wcout<<L"got message "<<j<<endl;
                #endif
                }
            }
            
            else if (j == L"@")
            {
                // if an annotation here, then it will be an type set
                // go back
                back(l, 1);
        label5860:
                // get an type and names
                a = b2_static_var(l);
                if (a[0] == L"var")
                {
                    #ifdef check_buf
                    wcout<<L"checking buffer\n";
                    wcout<<l.peek()<<endl;
                    #endif
    // get name
    
    Token h = gt_n(l);
    gt_e(l, init(L"="));
    // get expression
    // and return
    c.append(init{init(L"$var"), h, a6(l), a[5], a[6]});
                }
                else
                {
                // get names
                    c.append(init{init(L"$set"), a, arg_e(l)});
                }
            }
            else if (is_name(j))
            {
                // try to get : element
                if (gt_e_b(l, init(L":")).type != NULL_)
                {
                    // if got, then perform action with label
                    c.append(init{init(L"$label"), init(j)});
                }
                else
                {
                // try to get type and name, if false, then continue
                    back(l, 1);
                    
                    int i = l.i;
                    // get type
                /*init*/ a = b2_static_var(l);
                    // 
                    #ifdef check_buf
                    wcout<<"an name is getting"<<endl;
                    #endif
                    // get name
                    j = get(l);
                    // check if j is an name, if yes, then go back and continue
                    if (is_name(j))
                    {
                        back(l, 1);
                        
                        if (a[0] == L"var")
                {
                    
                    #ifdef check_buf
                    wcout<<L"checking buffer\n";
                    wcout<<l.peek()<<endl;
                    #endif
    // get name
    Token h = gt_n(l);
    gt_e(l, init(L"="));
    // get expression
    // and return
    c.append(init{init(L"$var"), h, a6(l), a[5], a[6]});
                }
                else
                {
                // get names
                #ifdef check_buf
                    wcout<<L"error here?:"<<endl;
                    wcout<<c<<endl;
                    #endif
                    c.append(init{init(L"$set"), a, arg_e(l)});
                #ifdef check_buf
                    wcout<<L"error no here?:"<<endl;
                    wcout<<c<<endl;
                    #endif
                }
                        
                    }
                    else
                    {
                        l.i = i;
                        // get expression
                        c.append(a6(l));
                    }
                }
            }
            else
            {
                // go back
                back(l, 1);
                // append expression and continue
                c.append(a6(l));
            }
}

static init fnc_b(it& l)
        // l is an iterator
        // c is an body
{
        init r;
        Token j;
        init c = init::lst();
        #ifdef check_buf
        wcout<<"function body here"<<endl;
        #endif
        while (true)           
        {
             // get element
            j = get(l);
            #ifdef check_buf
            wcout<<l.peek(-2)<<endl;
            
            wcout<<L"fnc_b: " <<j<<endl;
            wcout<<L"fnc_b: " <<(j == L"}")<<endl;
            #endif
            if (j == L"}")
            {
                // 
                #ifdef check_buf
                wcout<<"got closing element\n";
                #endif
                // if got closing element, then return
                return c;
            }
            else if (j.type == NULL_)
            {
                throw_error(L"the } is expected");
            }
            // add element
            #ifdef check_buf
            wcout<<"here error?"<<endl;
            #endif
            f_add(l, c, j);
        }
};


// this method returns an resources for thy-with-resources
static init arg_eq(it & k)
        // create an array for return
{
        init t ,a;
        Token n;
        init ret = init::lst();
        // create an iterator 
        it &l = *new it2(k.f, k.i, 
        {
        {Token(L","), none}, 
        {Token(L";"), none}, 
        {Token(L")"), none}
        });
        // iterate until ) meet
        while (l.peek()!=L")")            // get an type
        {
            t = b2(l);
            #ifdef check_buf
            wcout<<L"got type of: "<<t<<endl;
            #endif
            // iterate until ; or ) meet
            while (l.peek()!=L";")                // get name
            {
                n = gt_n(l);
                #ifdef check_buf
                wcout<<n<<endl;
                #endif
                // get = symbol
                gt_e(l, init{init(L"=")});
                // get expression
                a = a1(l);
                #ifdef check_buf
                wcout<<a<<endl;
                #endif
                // append elements
                ret.append(init{init(L"$set_one"), 
                    t, init(n), a});
                    #ifdef check_buf
                wcout<<L"sot"<<endl;
                wcout<<ret<<endl;
                
                #endif
                
                if (l.peek() == L")") goto break1;
            // reset a, displace l
            };
            l.i += 1;
        // return 
        };
        break1:
        k.i = l.i;
        k.i += 1;
        return ret;
};
// this function returns an cycle body
static init f1(it & l)
{
        Token v = get(l);
        init r;
        // if v is an special keyword
        if (contains(bsk, v))        //    print("kk:", v)
        {
            r = k1(v, l);
            // if got null, then continue
            if (r.type != NULL) return r;
        }
        // if v is an beginning of the block
        if (v == L"{")       //     print("got sd")
        {
            return fnc_b(l);
        }
        // else, return just expression
        else
        {
            if (v.type != NULL_) back(l, 1);
            return a6(l);
        };
};

// this function returns an lambda expression body
static init f2(it &l )
{
    // if next token is {
    if (gt_e_b(l, init(L"{")).type == BR_open)
    {
        it & ll = *new it3(l.f, l.i);
        // then return an function body
        init nt =  init{init(1), fnc_b(ll)};
        // displace l index and return
        l.i = ll.i;
        return nt;
    }
    // else return an expression
    else
    {
        return init{init(0), a1(l)};
    }
}

static init a6( it & l){
        // get iterator
        it &e =*new it2(l.f, l.i, 
        {
            {Token(L";"), none}
        }
        );
        // get expression
        init b = a1(e, null_init, 0);
        // displace pointer
        l.i = e.i + 1;
        return b;
};

static init a7( it & l){
        // get iterator
        it &e =*new it2(l.f, l.i, 
        {
            {Token(L":"), none}
        }
        );
        // get expression
        init b = a1(e, null_init, 0);
        // displace pointer
        l.i = e.i + 1;
        return b;
};
        
static init a5(it& l){
    return a5(l, init(L"$set"), init(L"$var"));
}
    // this function returns an type and fields
static init a5(it &l, init s, init v)
        // create body
{
        init c = init::lst();
        // 
        if (gt_e_b(l, init{init(L"var")}).type!=NULL_) 
         // get fields
        {
            c.append(v);
            init ar = arg_e(l, true, false);
            // return fields 
            for(int i = 0; i < ar.size(); i++)
            {
                c.append(init{ar[i][0], ar[i][1]});
            // return
            };
            return c;
   //     print("got")
        // get type
        };
        init h = b2(l);
        
    //    print("got", h)
        // get fields
        init ar = arg_e(l);
    //    print(ar)
        // fill body
        for (int i = 0; i < ar.size(); i++)
            // set name
        {
            c.append(s);
            init x = ar[i][0];
            // 
            init j = ar[i][2];
    //        print("set", h, x)
            c.append(init{ar_(h, j), x, ar[i][1]});
        // return body
   //     print("c", c)  
        };
        return c;
    
    // this method returns an class body
};
/*
static init cls_b(int f, it &k, bool a = False, init n = null_init)
        // f is flags
        // k is iterator
        // c is an body
        // a - is class is abstract
        // n - if class is not anonumous, then string
        // else, None
{
       wcout<<"class ->"<<endl;
        
        init c = init::lst();
        //
        it &l =*new it3(k.f, k.i);
        // check an class type
        if ((f & _enum) != 0)            // if got enum, then continue
        {
  //          wcout<<"eNUM"<<endl;
            c = enm(l);
        // else, raise error
        }
        elif( (f & _class) != 0)            // if got class, then continue
        {
            
            wcout<<"HI, class here"<<endl;
            while ( c3(l, c, a, n))
            {
                
            };
        }
        elif ( (f & _interface) != 0)            // if we got interface, then continue
        {
            while ( cv(l, c)){
            }
        }
        elif ((f & _annotation) != 0 )           // if we got interface, then continue
        {
            while ( ca(l, c)){
        // displace
            }
        }
        k.i = l.i;
        // return 
        return c;    
    // create body for enum
};
*/
static init enm(it &l)
        // the array that will be returned
{
        init ar = init::lst();
        // iterate while we meet an } symbol
        Token i = get(l);
        // begin iteration
        while (True)            // check for annotations
        {
            init nt = init::lst();
            // while meet @, check all annotations
            while ( i == L"@"){
                nt.append(_at(l));
                i = get(l);
            // check if it"s an valid name
            };
            
     //       wcout<<"I"<<i<<endl;
            
            if (!is_name(i))                // else, raise error
            {
                throw_error(L"the name is expected here");
            // append an operator
            };
            ar.append(init{i, nt});
            // check next element, must be an comma
            i = get(l);
            
     //       wcout<<":I:"<<i<<endl;
            // if comma, then continue
            while ( i == L",")                // repeat until meet an comma
            {
                i = get(l);
            // if got }, then break
            };
            if ( i == L"}") 
                break;
        };
        //
        //return an array
        return ar;
};
    // create class body
static bool c3(it &t, init &x){
    return c3(t, x, False, null_init);
}
   /* 
static bool c3(it &l, init &x, bool a, init n)
{
        // if n is None, then set anon as true
        // else set anon as false
        #ifdef check_buf
        wcout<<"c3"<<endl;
        #endif
        
        bool anon;
        if ( n.type == NULL_){
            anon = True;
        }
        else
        {
            anon = False;
        // get acc field
        };
  //      wcout<<"hi"<<endl;
        int f = flg_pool.back().back();
        // get note field
        init nt = note_pool.back().back().copy();
        //iterate with acc and note fields
        #ifdef check_buf
        wcout<<"hi343"<<endl;
        #endif
        
        Token j = c9(l, nt);
        
  //      wcout<<"hi43"<<endl;
    //    print(j)
        // check if an semicolon here
        if ( j == L";"){
            return True;
        // check, if an "}" here
        }
        elif (j == L"}"){
            return False;
        // check, if None here
        }
        elif (j.type==NULL_)            // then raise Error
        {
            raise SyntaxError("the } is expected");
        // check, if an ")" here
        }
        elif ( j == L")"){
            if (flg_pool.back().size() == 1){
                raise SyntaxError("the ) is not expected here");
            // delete last element from pool
            };
            flg_pool.back().pop_back();
            note_pool.back().pop_back();
            return True;
        // check, if we got 
        };
        while (fld_sf(f, j)){
    //        wcout<<"se5"<<endl;
            j = c9(l, nt);
 //           wcout<<j<<endl;
   //         wcout<<j.type<<endl;
     //       wcout<<is_name(j)<<endl;
        //
        // print("j", j)
        
        // check, if an "(" here
        };
        
//        wcout<<"I'M here"<<endl;
  //      wcout<<is_name(j)<<endl;
  //      exit(0);
        if ( j == L"(")            // if yes, then add flag to the pool
        {
            flg_pool.back().push_back(f);
            note_pool.back().push_back(nt);
            return True;
        // check if an constructor here
        }
        elif ( j == L"{")            // if nt, then raise illegal start of type
        {
            if ( nt.size() > 0){
                raise SyntaxError("illegal start of type");
            // if class is anonumous, then there will
            // not an static modifier
            };
            if ( anon) {
                if ((f & _static)!= 0){
                    raise SyntaxError("illegal static declaration in anonumous class") ;
            // then iterate with constructor
                };
            };
            c5(l, f, x);
            return True;
        // check if an constructor here
        }
        elif ((n.type == 2)&&((n == j) || (j == L"const")))
              // then create an constructor
        {
            gt_e(l, init{init(L"(")});
            // create an callable constructor
            c6(l, f, null_init, init(L"$initializer"), x, False, null_init, nt.copy() );
            return True;
        // check if j is an < expression
        }
        elif ( j == L"<")            // check for generic
        {
      //      wcout<<"got"<<endl;
            
            init q = b5(l);
            
//            wcout<<"b5"<<endl;
            // chech for type
            init t = b2(l);
            
//            wcout<<"b2"<<endl;
            // check for name
            Token n = gt_n(l);
            
//            wcout<<"here"<<endl;
            // check for ( symbol
            gt_e(l, init{init(L"(")});
            
//            wcout<<"HERE"<<endl;
            // if class is anonumous, then there will
            // not an static modifier
            if (anon){
                if (( f & _static) != 0)                 
                {
                    raise SyntaxError("illegal static declaration in anonumous class");
            // return function
                };
            }
            c6(l, f, t, n, x, a, q, nt.copy());
            return True;
        // check if j is an name here
        }
        elif (is_name(j))            // go back to get an type
        {
//            wcout<<"name got\n";
            back(l, 1);
            // get type
            init t = b2(l);
            #ifdef check_buf
            wcout<<t<<endl;
            #endif
            // get name
            Token n = gt_n(l);
            
//            wcout<<n<<endl;
            // get symbol
            j = gt_e(l, init{
                init(L"="),
                init(L";"), 
                init(L","), 
                init(L"("), 
                init(L"[")});
            // check if symbol is equals to (
            // if yes, then declare method
            // and return 
            if ( j == L"(")                
            // if class is anonumous, then there will
            // not an static modifier
            {
                if (anon){
                  if ((f & _static)!= 0){
                    raise SyntaxError("illegal static declaration in anonumous class");
                  };
                }
                #ifdef check_buf
                wcout<<"here METHOD"<<endl;
                #endif
                c6(l, f, t, n, x, a, null_init, nt.copy());
         //       wcout<<"sun"<<endl;
                return True;
            }
            // else, get all fields and iterate with them
            else
                // check acc modifier
            {
                if ((f &  
                ( _strictfp|_native| 
                  _synchronized| _abstract )) !=0)
    throw syntax_error("modifiers strictfp,\
     native, synchronized, abstract are\
     not allowed here");
                // go back to the two symbols
                
                back(l, 2);
                
  //              wcout<<l.f[l.i]<<endl;
                // get all assigments
                init fls = arg_e(l);
                // iterate with them
                int d = 0, l = fls.size();
                while(d < l){
                    init i = fls[d];
                    x.append(init
{init(1), init(f), ar_(t, i[2]), i[0], i[1], nt.copy()});
                    d ++;
                };
                return True;
            };
        }
        else
        {
            // if not name, then it"s an class implementation
            // then create an class
            // 
            // if class is anonumous, then there will
            // not an static modifier
            if ( anon){
                
    
               if (
               (f & 
               (
_static|_public|_protected|_private
               )
               ) != zr){
                raise SyntaxError("modifiers \
static, public,\
protected, privated is not allowed here");
                
               };
            }
            c4(l, f, x, nt.copy());
            return True;
        }
}
 */     /*      
    // create interface body
static bool cv(it & l, init &x){
        // a check if interface is annotation
        // get acc field
        int f = flg_pool.back().back();
        // get note field
        init nt = note_pool.back().back().copy();
        //iterate with acc field
        Token j = c9(l, nt);
        
    //    print(j)
        // check if an semicolon here
        if ( j == L";")
        {
            return True;
        // check, if an "}" here
        }
        elif ( j ==  L"}")        
        {
            return False;
        // check, if None here
        }
        elif ( j.type == NULL_)            // then raise Error
        {
            raise SyntaxError("the } is expected");
        // check, if an ")" here
        }
        elif ( j == L")")        
        {
            if ( flg_pool.back().size() == 1)            
            {
                raise SyntaxError("the ) is not expected here");
            // delete last element from pool
            };
            flg_pool.back().pop_back();
            note_pool.back().pop_back();
            return True;
        // check, if we got modifier
        };
        while ( fld_sf2(f, j))        
        {
            j = c9(l, nt);
        // print("j", j)
        
        // check, if an "(" here
        };
        if ( j == L"(")            // if yes, then add flag to the pool
        {
            flg_pool.back().push_back(f);
            note_pool.back().push_back(nt.copy());
            return True;
        // check if an constructor here
        }
        elif ( j == L"{")            // then raise error 
        {
            raise SyntaxError("error: initializers not allowed in interfaces");
            
        // check if j is an < expression
        }
        elif ( j == L"<")            // check for generic
        {
            init q = b5(l);
            // chech for type
            init t = b2(l);
            // check for name
            init n = gt_n(l);
            // check for ( symbol
            gt_e(l, init{init(L"(")});
            // return function
            c66(f);
            c6(l, f, t, n, x, True, q, nt);
            return True;
        // check if j is an name here
        }
        elif ( is_name(j))            // go back to get an type
        {
            
            wcout<<L"interface method"<<endl;
            
            back(l, 1);
            // get type
            init t = b2(l);
            // get name
            init n = gt_n(l);
            
            
            // get symbol
            Token j = gt_e(l, 
            init {
                init(L"="), 
                init(L";"), 
                init(L","), 
                init(L"("), 
                init(L"[")
                }
                );
            // check if symbol is equals to (
            // if yes, then declare method
            // and return 
            if ( j == L"(")
            {
                wcout<<L"call interface method"<<endl;
                
                c66(f);
                c6(l, f, t, n, x, True, None, nt.copy());
                return True;
            // else, get all fields and iterate with them
            }
            else
                // check acc modifier
            {
                if ((f &(
_strictfp|_native|_volatile|
_transient|_synchronized|_abstract|_default)) != 0)
                {
raise SyntaxError("modifier \
strictfp,native,volatile,\
transient,synchronized,abstract,default\
not allowed here");
                // go back to the two symbols
                };
                back(l, 2);
                // get all assigments
                init fls = arg_e(l);
                // iterate with them
                int d = 0, l = fls.size();
                while(d < l){
                    init i = fls[d];
                    x.append(init
{init(1), init(f), ar_(t, i[2]), i[0], i[1], nt.copy()});
                    d ++;
                };
                return True;
            };
        }
        else
        {
            // if not name, then it"s an class implementation
            // then create an class
            // 
        //    for i in [ flg_.private, flg_.protected, flg_.default ]
            if ((f &(_protected|_private|_default)) != 0)
            {
raise SyntaxError("modifier \
private,protected,default\
not allowed here");
               c4(l, f, x, nt.copy());
            };
            return True;
    
    // this method returns an annotate
        };
    }
    * */
static init _at(it &l)
{
    
    throw_error(L"annotations is not supported by this version of ejavac");
    // check for cur_line and cur_position
  int line = glb::cur_line;
  int position = glb::cur_position;
  glb::cur_line = 0;   
  glb::cur_position = 0;   
        // get name of annotate
        init ret = init{init(L"@", line, position), cls_name(l), init::lst()};
        
        
        // this is an element for an dictionary
    //    init & l = ret[2];
    #ifdef check_buf
    wcout<<L"@@NOTE HERE"<<endl;
    wcout<<ret[1]<<endl;
    #endif
        // check if bracket here, if no bracket here, then return
        if (gt_e_b(l, init(L"(")).type == BR_open)
        {
            i = l.i;
            // check peek element
            if (l.peek() ==  L")")
            {
                l.i ++;
                return ret;
            }
            // check for name and assignment
            Token j = get(l);
            // if no = after
            if (gt_e_b(l, init(L"=")).type == NULL_)
            {
                l.i = i;
                 bool note = glb::note;
            glb::note = true;
                ret[2] = init{init(L"value"), prt(l)};
            glb::note = note;
                return ret;
            }
            // else, return dict
            else
            {
                #ifdef check_buf
                wcout<<"well"<<endl;
                #endif
                l.i = i;
                ret[2] = dct_note(l);
                return ret;
            }
        }
        else
        {
            return ret;
        }
};
  
    // create annotation body
static bool ca(it &l,init &x)
{
        // a check if interface is annotation
        // get acc field
        int f = flg_pool.back().back();
        // get note field
        init nt = note_pool.back().back().copy();
        //iterate with acc field
        Token j = get(l);
    //    print(j)
        // check if an semicolon here
        if (j == L";")        
        {
            return True;
        // check, if an "}" here
        }
        elif (j == L"}")        
        {
            return False;
        // check, if None here
        }
        elif (j.type == NULL_)            // then raise Error
        {
            raise SyntaxError("the } is expected");
        // check, if an ")" here
        }
        elif ( j == L")")        
        {
            if (flg_pool.back().size() == 1)            
            {
                raise SyntaxError("the ) is not expected here");
            // delete last element from pool
            };
            flg_pool.back().pop_back();
            note_pool.back().pop_back();
            return True;
        // check, if we got modifier
        };
        while (fld_sf2(f, j))        
        {
            j = get(l);
        // print("j", j)
        
        // check, if an "(" here
        };
        if ( j == L"(")            // if yes, then add flag to the pool
        {
            flg_pool.back().push_back(f);
            flg_pool.back().push_back(nt);
            return True;
        // check if an constructor here
        }
        elif ( j == L"{")            // then raise error 
        {
            raise SyntaxError("error: initializers not allowed in interfaces");
            
        // check if j is an < expression
        }
        elif ( j == L"<")            // if yes, then raise an error
        {
            raise SyntaxError("the < is not expected here");
        // check if j is an name here
        }
        elif (is_name(j))            // go back to get an type
        {
            back(l, 1);
            // get type
            init t = b2(l);
            // get name
            Token n = gt_n(l);
            // get symbol
            Token j = gt_e(l, 
            init{
                init(L"="), 
                init(L";"), 
                init(L","), 
                init(L"("), 
                init(L"[")
            });
            // check if symbol is equals to (
            // if yes, then declare method
            // and return 
            if ( j == L"(")                // check acc modifier
            {
                if ((f &
                (_volatile|
                 _default|
                 _final|
                 _native|
                 _private|
                 _protected|
                 _static|
                 _strictfp|
                 _synchronized|
                 _transient
                )
                ) == 0
                )
                {
raise SyntaxError("modifier \
volatile, \
default, \
final, \
native, \
private, \
protected, \
static, \
strictfp, \
synchronized, \
transient\ not allowed here");
                // then get ) element
                };
                init a;
                gt_e(l, init{init(L")")});
                // get next element: ";" or "default"
                if ((gt_e(l, 
                init{
                    init(L"default"), 
                    init(L";")
                }) == L"default"))                // get next element, if got default, then get expression
                {
                    if (gt_e_b(l, init{init(L"@"
                        )}).type == NULL_
                    )
                    {
                        a = _at(l);
                    }
                    else
                    {
                        a = a6(l);
                // else, set none
                    };
                }
                else{
                    a = null_init;
                };
                x.append(init{init(5), 
                    t, 
                    init(n), 
                    a, 
                    nt.copy()});
                return True;
            }    
            // else, get all fields and iterate with them
            else
                // check acc modifier
            {
                if ((f &(
_strictfp|_native|_volatile|
_transient|_synchronized|_abstract|_default)) == 0)
                {
raise SyntaxError("modifier \
strictfp,native,volatile,\
transient,synchronized,abstract,default\
not allowed here");
                // go back to the two symbols
                };
                back(l, 2);
                // get all assigments
                init fls = arg_e(l);
                // iterate with them
                int d = 0, l = fls.size();
                while(d < l){
                    init i = fls[d];
                    x.append(init
{init(1), init(f), ar_(t, i[2]), i[0], i[1], nt.copy()});
                    d ++;
                };
                return True;
            };
        }
        else{
            // if not name, then it"s an class implementation
            // then create an class
            // 
       //     for i in [  ]
         //   {
            if ( (f& 
            (
_private|_protected|_default
            )
            
            ) != 0)
            {
raise SyntaxError("modifier private, \
protected, default not allowed here");
                        
            };
            c4(l, f, x, nt.copy());
            return True;
        }
}
    // this method returns an array of arguments 
static init arg_t(it &l){
     //   print("hell ")
    // wcout<<"im here"<<endl;
        int st = 0;
        
        //
#ifdef check_buf
                wcout<<"i'm called"<<endl;
#endif
        
        init args = init::lst();
        bool k = true;
        while ( k)            // get type
        {
            
      //      wcout<<"HI"<<endl;
            // check for static extension
            
            
            
            init t = b2_static(l);
            // get name
            #ifdef check_buf
            wcout<<"METHOD ARGUMENTS ";
            wcout<<t<<endl;
            #endif
            
            Token n = get(l);
            // if got dot, then there should be 3 dots and ")"
            if (!is_name(n))            
            {
                if ( n == L"...")   
                {
                    n = gt_n(l);
                    k = False;
                    // modify type
                    t[0] = init(L"$vararg");
                    // else, raise an error
                }
                else
                {
                    throw_error(L"invalid name");
            // add arg
                };
                
            // get element
         //   print("type ", t, n)
         //   print(args)
            };
            args.append(init{t, init(n) });
            Token b = gt_e(l, 
            init{init(L")"), 
                 init(L","), 
                 init(L"[")
                 }
            );
            
            if (b == L"[")
            {
            // if got [ element, then check 
            // if got an variable-arity parameter
                if (!k)
                {
                    throw_error(L"legacy array notation not allowed on variable-arity parameter");
                }                 
                else
                {
               //  array level :   t[4];
                   int lvl= 0;
                  //  check all [] elements
                  do 
                  {
                      // get ] element
                      gt_e(l, init(L"]"));
                      // add level
                      lvl ++;
                      // get next token
                       b = gt_e(l, 
            init{init(L")"), 
                 init(L","), 
                 init(L"[")
                 }           );
                    // check if token is equals to ]
                  }
                   while (b == L"[");
                   
                   t[4] += lvl;
                   
                   // if got ) 
                };
            };
         //   print(b)
            //check element
            if (b == L")")         
            //       print("ret")
            {
                return args;
        // raise Error
            };
        }
        throw_error(L"error: varargs parameter must be the last parameter");
}
    // this method returns an assign arguments

static init arg_e(it & l){
    return arg_e(l, false, true);
}
static init arg_e(it &z, bool t, bool r)
         // store current check
{
         // t - check if after name must be an = sign
         // r - check if after name mustn't be an [ sign
         // counter
         bool s = glb::s;
         // tokens
         Token e, n;
         // set check
         glb::s = true;
         // an array of elements
         init ar = init::lst();
         // an new iterator
         it &l = *new it2(z.f, z.i,
         {
         {Token(L",", SPECIAL),  none}, 
         {Token(L";", SPECIAL),  glb::s_ch}
         }
         );
         // iterate with array
         while ( glb::s)             // get name 
         {
             #ifdef check_buf
             wcout<<"some"<<l.peek()<<endl;
             #endif
     //        wcout<<"got"<<get(l)<<endl;
             //
             n = gt_n(l);
             //
    //         wcout<<n<<endl;
             // lv is an array level
             int lv = 0;
             if (r) // repeat until [ meet
             {
               while (gt_e_b(l, init{init(L"[")}).type!=NULL_)               
               {
                 gt_e(l, init{init(L"]")});
                 lv += 1;
             // get element
               };
             }
             e = gt_e_n(l, init{init(L"=")});
             // if e is none, then return and continue
             if (e.type == NULL_)
             {
                 if (t){
                     throw_error(L"the = is expected");
                 };
                 
             #ifdef check_buf
             wcout<<"array add"<<ar<<endl;
             #endif
                 ar.append(init{init(n), null_init, init(lv)});
             // else, iterate
             }
             else
             {
                 
             #ifdef check_buf
             wcout<<"array add"<<ar<<endl;
             #endif
                 ar.append(init{init(n), a1(l), init(lv)});
             // add
             };
             l.i += 1;
             
             #ifdef check_buf
             wcout<<"some pass"<<l.peek()<<endl;
             #endif
         // if array is empty, then raise empty array error
         };
         if (ar.size() == 0)         {
             throw_error(L"the name is expected");
         // displace element
         };
         z.i = l.i;
         glb::s = s;
         return ar;
}
    
static bool c4(it &l, int f, init &x, init nt = null_token)
{
        // check acc modifier
        if ((f &
                (
_strictfp|
_native|
_synchronized|
_transient|
_volatile
                )
                ) == 0
                )
                {
throw_error(L"modifier \
strictfp, \
native, \
synchronized, \
transient, \
volatile \
not allowed here");
                // then get ) element
                };
        // l is an token iterator
        // f is an flag
        // x is an main body
        // n is an name
        Token n = gt_n(l);
        // get parent
        // get body
        vector<int> flg_;
        flg_.push_back(0);
        // add to pool
        flg_pool.push_back(flg_);
        // get parent types
        init p = cls_p(f, l);
        // get begin of body
        gt_e(l, init{init(L"{")});
        // get an body
        init b = cls_b(f, l, ((f & _abstract) == _abstract), n);
        // append to x
        x.append(init{init(3), 
            init(f), 
            init(n), 
            p[0], 
            p[1], 
            p[2], 
            b, 
            nt
        }
        );
        flg_pool.pop_back();
        return true;
    // this is for an constructor
};
    
    
static bool c5(it &l, int f, init &x)
    {
        if ( f == zr)        {
        }
        else if ( f == _static)
        {
        }
        else
        {
            SyntaxError("only static \
modifier is allowed here");
        // l is an token iterator
        // f is an flag
        // x is an main body
        };
        x.append(init{init(4), init(f), fnc_b(l)});
        return true;
};
        
    // this function returns just an list

static init arg_l( it &l)
        // repeat until names here
{
        Token j;
        init c = init::lst();
        while ( True)            // get type
        {
            c.append(b2(l));
            // get symbol and check
            j = get(l);
            // compare with ,
            if ( j == L",")            
            {
                continue;
            }
            else
            {
                back(l, 1);
                return c;
            }
        }
};
    // this method returns an package information
            
static init _pkg(it &l, init source)
{
        // array for package name
        init n = init::lst();
        
        // array for annotations
        init nt = init::lst();
        
        // set return index for case if no package modifier here
        int i = l.i;
        // get all annotations
        Token j = c9(l, nt);
        // if j is not package modifier, then back to the return index
        if ( j != L"package")        
        {
            l.i = i;
            return init{n, init::lst(), source};
        // else, continue
        };
        n.append(gt_n(l));
        // repeat until here ;
        j = gt_e(l, init{init(L"."), init(L";")});
        // repeat
        while ( j == L".")        
        // get name and append
        {
            n.append(gt_n(l));
        // get next element
            j = gt_e(l, init{init(L"."), init(L";")});
        // return
        };
        return init{n, nt, source};
};        
    // this is for an interface method
    /*
static bool c66(int f){
    #ifdef check_buf
    wcout<<f<<endl;
    #endif
    int i = (_native |
_synchronized |
_final |
_volatile |
_transient);
#ifdef check_buf
    wcout<<i<<endl;
#endif
    i = (f&i);
    #ifdef check_buf
    wcout<<i<<endl;
#endif
    if (i == 0) return true;
    else
    {
        #ifdef check_buf
        wcout<<L"fukc"<<endl;
        throw_error(L"modifiers \
_native, \
_synchronized, \
_final, \
_volatile, \
_transient is not allowed here");
    };
    return false;

}

*/
//static init c_func(it &l, int f, init nt bool is_abstract, init gen, init type, init name);
//static init c_cnst(it &l, int f);
    // this is for an method                
static init c_func(it & l,int f, init nt, bool a, init gen, init t, init jn)
{
        // l is an token iterator
        // f is an flag
        // gen is generics
        // t is an type
        // jn is an name
        // g is a list of throws exceptions
        // x is an main body
        // a checks is method abstract
        // nt is an annotations
/*if (a){

if ((f & 
(
_volatile |
_transient)) != 0 ){
    throw_error(L"modifiers \
_volatile, \
_transient is not allowed here");
}

}
else
{
  if ((f & 
(
_volatile |
_abstract |
_transient)) != 0 ){
    throw_error(L"modifiers \
_abstract, \
_volatile, \
_transient is not allowed here");
}  
}
*/

        Token j = get(l);
        // if br is not (, then raise error
        if (j.type != BR_open)
        {
            throw_error(L"the ( operator is expected");
        }
        if (j != L"(")
        {
            throw_error(L"the ( operator is expected");
        }
  #ifdef check_buf      
        wcout<<L"method here is going \n";
#endif
        init ar, g;
        
        if (gt_e_b(l, init(L")")).type != NULL_)        
        {
            ar = init::lst();
#ifdef check_buf
            wcout<<"()"<<endl;
#endif
        }
        else
        {
            ar = arg_t(l);
            
#ifdef check_buf
            wcout<<ar<<endl;
            wcout<<"(0)"<<endl;
      //  print(ar)
      wcout<<ar<<endl;
        // check element
#endif
        };
        j = gt_e(l, init{init(L"throws"), 
            init(L"{"), init(L";")});
        // if j is throws, then get list of throws exceptions
        if ( j == L"throws")        
        {
            g = arg_l(l);
            j = gt_e(l, init{init(L"{"), init(L";")});
        // else, set g = []
        }
        else
        {
            g = init::lst();
        // check element
        };
        
   //     wcout<<ar<<endl;
   //     wcout<<g<<endl;
        
        if ( j == L";")            // only abstract methods haven"t an body
            // check if method is final
        {
            if ( a )
            {
                return ( init{init(f), 
                    gen, t, jn, 
                ar, g, null_init ,nt});
            }
            else
            {
                    throw_error(L"missing method body");
                    return ( init{init(f), 
                    gen, t, jn, 
                ar, g, null_init ,nt});
            }
                // f[0] - modifiers
                // t - type
                // jn - name
                // g - arguments
                // b - body
                // gen - generics
        }
        else if ( j == L"{")            // check if an method is abstract
        {
            init b;
            
            if ( a )
            {
                throw_error(L"this method cannot have a body");
            }
            else 
            {
                
                
#ifdef check_buf
                wcout<<j<<endl;
             //   wcout<<get(l)<<endl;
                wcout<<"call body"<<endl;
                wcout<<l.peek()<<endl;
                wcout<<jn<<endl;
#endif
                b = fnc_b(l);
            // return
            }
            return (init{
    init(f), gen, t, 
    jn, ar, g, b ,nt});
        // else
        }
        else
        {
            wstring fin = L"the ";
            fin += j;
            fin += L" is not expected here";
     
            throw_error(fin);
            
            return (init{
    init(f), gen, t, 
    jn, ar, g, null_init ,nt});
        };
};

#undef None
#undef elif 
#undef True
#undef False
#undef zr
#undef SyntaxError
#undef token_list
#undef raise
#undef cnt_start 
#undef cnt_stop 
#undef c_stop 
#undef c_t



//preprocess file
static vector<Token> _pr(wistream &f)
{
    
  //  wcout<<L"::"<<endl;
    
        buffer buf(f);
        
    vector<Token> v;

    Token i = *get_token(buf);

#ifdef check_buf
    wcout<<i<<endl;
#endif
 //   exit(0);
 //   wcout<<i;
    while ( i.type != NULL)//    print(i.type, "<", i, ">")
    {
        if (right(i)) v.push_back(i);
        
        
        i = * get_token(buf);
      #ifdef check_buf  
        
        wcout<<L" :: TOKEN :: "<<i<<L" :: OF TYPE :: "<<i.type<<endl;;
 #endif
    };
    
 //   wcout<<"\n\n<ret>"<<endl;
    return v;
}
//
static init _imp(it &l){
 //   # this is name
    init n;
    
//    wcout<<"hello world\n";
 //   # repeat until here ;
    Token j; //= gt_e(l, init{
  //      init(L"."), 
  //      init(L";"), 
  //      init(L"as")
 //   });
  //  # get static modifier if here
    j = get(l);
    bool k = (j == L"static");
    
    if (k) j = get(l);
    
    // check if got an name
    // if not, then raise "name is expected"
    if (!is_name(j))
    {
        throw_error(L"the name is expected here");
        return null_init;
    }
    // get . element
    gt_e(l, init(L"."));
    // create list
    n = init{init(j)};
    // get name or * element
    label123:
    // 
    j = get(l);
    // if equals to '*', then get ';' and return
    if (j == L"*")
    {
        // return
        gt_e(l, init(L";"));
        return init{n, init(j), init((int)k)};
    }
    // if got name, then get 
    else if (is_name(j))
    {
        // then add to the name list and get ';' or 'as' or '.'
        n.append(init(j));
        // get 
        j = gt_e(l, init{init(L";"), init(L"."), init(L"as")});
        // check if equals to '.'
        if (j == L".")
        {
            goto label123;
        }
        // if equals to ';'
        else if (j == L";")
        {
            return init{n, n.b3.back(), init((int)k)}; 
        }
        // if equals to 'as' 
        else if (j == L"as")
        {
            // get name and return 
            j = gt_n(l);
            // get ; element
            gt_e(l, init(L";"));
            // return 
            return init{n, init(j), init((int)k)};
        }
    }
}

// this function passes an semicolons
static void _pass(it & l)
{
    // check if an semicolon here
    Token j;
    do {
        j = get(l);
    }while (j == L";");
    if (j.type == NULL_) return;
    back(l, 1);
    return;
}

//# returns an syntax tree
static init _ast(vector<Token> & v, const char * name)
{
  //  # get an token list 'v'
 //   vector<Token> v = _pr(f);
    

//    # an iterator
    glb::refresh();
    
//    wcout<<L"\n\n"<<endl;
    
    it &l = *new it3(&v);
    //
    _pass(l);
//    # an ast tree
//    # if package identifier here
    init ast = init{_pkg(l, init(s2ws(string(name))))};
//    # release imports
//    # there are some import libraries
    init im = init::lst();
    
 //   wcout<<ast<<endl;
    
    _pass(l);
//    # fill array
    while (gt_e_b(l,init{init(L"import")}).type!=NULL_)
    {
        im.append(_imp(l));
        _pass(l);
    //    wcout<<L"Limport\n";
    }
//    # add import libraries to the ast
    ast.append(im);

#ifdef check_buf
    wcout<<im<<endl;
 //   wcout<<im<<endl;
    
//#print("end:",vars(c2(it(v))))
#endif
    int a = v.size() - 1;
//    # iterate all classes
    while (l.i < a)
    {
        #ifdef check_buf
        wcout<<"begin\n";
        #endif
        im = c2
        (l);
   //     wcout<<im<<endl;
        ast.append(im);
        _pass(l);
    #ifdef check_buf
        wcout<<im<<endl;
    #endif
    }
//    # cast classes
//#    print(ast)
    return ast;
}

static init load_ast_from_file(const char * path)
{
    
 //  cout<<path<<endl;
 //   wcout<<L"LOADING\n";
//    setlocale(LC_ALL, "");
    source_file = (char* )path;
 // const char *path = "task.java";
  
  std::wifstream fs; 
   
   fs.imbue(std::locale(std::locale(), new std::codecvt_utf8<wchar_t>));
   fs.open(path);
   
 //  int i = fs.get();
  // while (i > 0)
   {
 //      wcout<<(wchar_t)i;
 //      i  = fs.get();
   }
  vector<Token> pr = _pr(fs);
 // it & iter = *new it3( & pr);
  
  init rt = _ast(pr, path);
  
  // wcout
  return rt; 
}

#ifndef library



int main(){
    
    init l = load_ast_from_file(
"example.java");
     wcout<<endl<<endl<<endl<<endl<<l;;
 
 
    
    
  
 /* vector<Token> pr = _pr( * new 
  wifstream
  (
  "txt.txt"
  ) ) ;
  
  wcout<<a1(*new it3(& pr))<<endl;
  */
}

#endif

#define Constant_Utf8_info 1
#define Constant_Int_info 3
#define Constant_Float_info 4
#define Constant_Long_info 5
#define Constant_Double_info 6
#define Constant_Class_info 7
#define Constant_String_info 8
#define Constant_Field_info 9
#define Constant_Method_info 10
#define Constant_InterfaceMethod_info 11
#define Constant_NameAndType_info 12
#define Constant_MethodHandle_info 15
#define Constant_MethodType_info 16
#define Constant_Dynamic_info 17
#define Constant_InvokeDynamic_info 18
#define Constant_Module_info 19
#define Constant_Package_info 20

#ifdef library

#include <jni.h>

extern "C" {
/*
 * Class:     ClassLoader
 * Method:    loadFromBytecode
 * Signature: ([B)LClassData;
 */


#define load_class(x, y) jclass x = env->FindClass(y);     
#define load_object(x, y) jobject x = env->AllocObject(y);
#define load_method(x, cl, n, s) jmethodID x = env->GetMethodID(cl, n, s);
#define newobj(a, x) jobject a = env->NewObject(x, env->GetMethodID(x, "<init>", "()V"));
#define call_bool(x, id, ...) env->CallBooleanMethod(x, id, __VA_ARGS__)

namespace glb{
    
jclass obj;
//jclass arr;
jclass str;
jclass tok;
jclass flt;
jmethodID initD;
jmethodID initO;
jmethodID initT;
//jmethodID addO;
//jobject source;
JNIEnv * env = 0;
// this function converts init into java object
jobject get(init &i, jstring source)
{
    if (i.type == 1){
        return glb::env->NewObject(glb::flt, 
        glb::initD, (double)i);
    }
    else if (i.type == 2){
        jobject j = glb::env->NewStringUTF(
        ws2s(i.b2).c_str()); 
        int line = i.line;
        int position = i.position;
        jobject ret = glb::env->NewObject(glb::tok, glb::initT, source, j, line, position);

        return ret;
    }
    else if (i.type == 3){
        
        
    //    wcout<<"ERROR3"<<endl;
        // return an array
        vector<init> * vec = new vector<init>[1];
        *vec = i.b3;
        int l = vec->size();
        jobject byteBuffer = env->NewDirectByteBuffer((void*)vec, sizeof(vector<init>));

        jobject ret = glb::env->NewObject(glb::obj, glb::initO, l, byteBuffer, source);
//        cout << &i << endl;
//        cout << "store" << endl;
//        int i = 0;
         
//        while (i < l){
//            call_bool(ret, addO, get(vec[i]));
//            i++;
//        }
        return ret;
    }
    else{
        return 0;
    }
}
// this function load necessary elements 
static void on_load(JNIEnv * env)
{
  //  if (glb::env != env)
    {
        
//load_class (obj,"java/lang/Object")
load_class (obj,"com/ejavac/InitList")
load_class (str,"java/lang/String")
load_class (tok,"com/ejavac/Token")
load_class (flt,"java/lang/Double")
glb::obj = obj;
glb::flt = flt;
glb::tok = tok;
glb::str = str;
//glb::arr = arr;
glb::env = env;
//load_object(ret, obj)
load_method(initD, flt, "<init>", "(D)V");
glb::initD = initD;
load_method(initO, obj, "<init>", "(ILjava/lang/Object;Ljava/lang/String;)V");
load_method(initT, tok, "<init>", "(Ljava/lang/String;Ljava/lang/String;II)V");
//load_method(addO, obj, "add", "(Ljava/lang/Object;)Z");
glb::initO = initO;
glb::initT = initT;
//glb::addO = addO;
    
    }
}


}

JNIEXPORT jobject JNICALL Java_com_ejavac_Compiler_fileToAST
  (JNIEnv * env, jclass cls, jstring name){

glb::on_load(env);

const char *path = env->GetStringUTFChars(name, 0);

// wcout<<L"funn"<<endl;
//cout<<path<<endl;
try
{
  init AST = (load_ast_from_file(path));
  return glb::get(AST, name);
}
catch (const std::exception& e)                                 
  {                                                               
    /* unknown exception */             
    jclass jc = env->FindClass("java/lang/Exception");                
    if(jc) env->ThrowNew (jc, e.what());      
    return 0;
  } 

};

// Implement the native methods
JNIEXPORT jobject JNICALL Java_com_ejavac_InitList_get(JNIEnv *env, jobject obj, jint index) {

    glb::on_load(env);
    // Retrieve the bytebuffer field from the Java object
    jfieldID bytebuffer_field = env->GetFieldID(env->GetObjectClass(obj), "bytebuffer", "Ljava/lang/Object;");
    jobject bytebuffer = env->GetObjectField(obj, bytebuffer_field);


    jfieldID source_field = env->GetFieldID(env->GetObjectClass(obj), "source", "Ljava/lang/String;");
    jstring source = static_cast<jstring>(env->GetObjectField(obj, source_field));

    // Perform the desired operations on the bytebuffer
    auto ptr = ((vector<init>*)env->GetDirectBufferAddress(bytebuffer));
    int idx = static_cast<int>(index);
    init& ast = ptr->at(idx);
    // Return the result as a Java object;
    return glb::get(ast, source);
}

}
#undef call_method
#undef load_class
#undef load_object
#undef load_method
#undef newobj

#endif

