package org.right.ntlang.compile;
import java.util.*;
import org.right.ntlang.*;

public class NTToken implements Cloneable {
    public static enum TokenType {
        UNKNOWN,
        NUM,
        STRING,
        ID,
        TMP/*tmp a = xxxx*/,
        /*常量*/
        TRUE,
        FALSE,
        RETURN,
        NIL,
        THIS,
        SHARP, // #
        /**分隔符**/
        COMMA, // ,
        COLON, // :
        LEFT_PAREN, // (
        RIGHT_PAREN, // )
        LEFT_BRACKET, // [
        RIGHT_BRACKET, // ]
        LEFT_BRACE, // {
        RIGHT_BRACE, // }
        DOT, // .
        DOT_DOT, //..
        ADD, // +
        SUB, // -
        MUL, // *
        DIV, // /
        MOD, // %
        
        ASSIGN, // =
        
        BIT_AND, // &
        BIT_OR, // |
        BIT_NOT, // ~
        BIT_SHIFT_RIGHT, // >>
        BIT_SHIFT_LEFT, // <<
        
        LOGIC_AND, // &&
        LOGIC_OR, // ||
        LOGIC_NOT, // !
        EQUAL, // ==
        NOT_EQUAL, // !=
        GREATE, // >
        GREATE_EQUAL, // >=
        LESS, // <
        LESS_EQUAL, // <=
        
        QUESTION, // ?
        SEMI_COLON, // ;
        POW, // **
        EOF; // "EOF" 文件结尾
        private static HashMap<Integer, TokenType> mappings;
        private synchronized static HashMap<Integer, TokenType> getMappings() {
            if (mappings == null) {
                mappings = new HashMap<Integer, TokenType>();
            }
            return mappings;
        }
        
        public int getId() {
            return this.ordinal();
        }
    }
    
    private static Map<String,TokenType> keywordsToken 
      = new HashMap<String,TokenType>() {{
          put("tmp",TokenType.TMP);
          put("true",TokenType.TRUE);
          put("false",TokenType.FALSE);
          put("return",TokenType.RETURN);
          put("nil",TokenType.NIL);
          put("this",TokenType.THIS);
      }};
      
    public static TokenType isIdOrKeyword(String str) {
        if (keywordsToken.containsKey(str)) return keywordsToken.get(str);
        return TokenType.ID;
    }
      
    
    public TokenType type;
    //public String str;
    public int start;
    public int length;
    public int lineNo;
    public NTValue value;
    public NTToken(TokenType t,int s,int len,int line) {
        type = t;
        start = s;
        length = len;
        lineNo = line;
    }
    
    // 复制一份token
    @Override
    public NTToken clone()
    {
        return new NTToken(type,start,length,lineNo);
    }
    
    
}
