package org.right.ntlang.compile;
import java.util.*;
import org.right.ntlang.*;

public class NTToken implements Cloneable {
    public static enum TokenType {
        UNKNOWN(0),
        NUM(1),STRING(2),ID(3),
        TMP(4)/*tmp a = xxxx*/,FUN(5),IF(6),ELSE(7),TRUE(8),FALSE(9),WHILE(10),FOR(11),BREAK(12),CONTINUE(13),RETURN(14),NIL(15),
        THIS(16),
        /**分隔符**/
        COMMA(17), // ,
        COLON(18), // :
        LEFT_PAREN(19), // (
        RIGHT_PAREN(20), // )
        LEFT_BRACKET(21), // [
        RIGHT_BRACKET(22), // ]
        LEFT_BRACE(23), // {
        RIGHT_BRACE(24), // }
        DOT(25), // .
        DOT_DOT(26), //..
        ADD(27), // +
        SUB(28), // -
        MUL(29), // *
        DIV(30), // /
        MOD(31), // %
        
        ASSIGN(32), // =
        
        BIT_AND(33), // &
        BIT_OR(34), // |
        BIT_NOT(35), // ~
        BIT_SHIFT_RIGHT(36), // >>
        BIT_SHIFT_LEFT(37), // <<
        
        LOGIC_AND(38), // &&
        LOGIC_OR(39), // ||
        LOGIC_NOT(40), // !
        EQUAL(41), // ==
        NOT_EQUAL(42), // !=
        GREATE(43), // >
        GREATE_EQUAL(44), // >=
        LESS(45), // <
        LESS_EQUAL(46), // <=
        
        QUESTION(47), // ?
        SEMI_COLON(48), // ;
        POW(49), // **
        EOF(255); // "EOF" 文件结尾
        
        private final byte id;
        private static HashMap<Integer, TokenType> mappings;
        private synchronized static HashMap<Integer, TokenType> getMappings() {
            if (mappings == null) {
                mappings = new HashMap<Integer, TokenType>();
            }
            return mappings;
        }
        
        TokenType(int id) {
            this.id = (byte)id;
            TokenType.getMappings().put(id, this);
        }
        
        public byte getId() {
            return id;
        }
    }
    
    public static Map<String,TokenType> keywordsToken 
      = new HashMap<String,TokenType>() {{
          put("tmp",TokenType.TMP);
          put("fn",TokenType.FUN);
          put("if",TokenType.IF);
          put("else",TokenType.ELSE);
          put("true",TokenType.TRUE);
          put("false",TokenType.FALSE);
          put("while",TokenType.WHILE);
          put("for",TokenType.FOR);
          put("break",TokenType.BREAK);
          put("continue",TokenType.CONTINUE);
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
