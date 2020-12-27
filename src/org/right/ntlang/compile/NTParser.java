package org.right.ntlang.compile;
import org.right.ntlang.*;
import org.right.ntlang.exception.*;

import static org.right.ntlang.compile.NTToken.TokenType;
// 注释是不可能写的，这辈子都不可能的(
public class NTParser {
    private NTVM vm;
    
    public StringBuffer sourceCode;
    private String fileName;
    private int nextCharPtr;
    private char curChar;
    public NTToken curToken;
    public NTToken preToken;
    public NTCompileUnit curCompileUnit;
    public NTParser(String fn,StringBuffer sc) {
        fileName = fn;
        sourceCode = sc;
        nextCharPtr = 1;
        curToken = new NTToken(TokenType.UNKNOWN,0,0,1);
        preToken = curToken;
    }
    public void initCurChar(int i) {
        curChar = sourceCode.charAt(i);
        curToken = new NTToken(TokenType.UNKNOWN,0,0,1);
        preToken = curToken;
    }
    public void fixNextCharPtr(int i) {
        nextCharPtr += i;
    }
    public void setVM(NTVM v) {vm = v;}
    public NTVM getVM() {return vm;}
    private char lookAheadChar() {
        return this.sourceCode.charAt(this.nextCharPtr);
    }
    
    private void getNextChar() {
        if (nextCharPtr >= sourceCode.length()) {
            curChar = '\0';
            return;
        }
        this.curChar = this.sourceCode.charAt(this.nextCharPtr++);
    }
    
    private boolean matchNextChar(char expectedChar) {
        if (lookAheadChar() == expectedChar) {
            getNextChar();
            return true;
        }
        return false;
    }
    
    private void skipBlanks() {
        while (Character.isWhitespace(curChar)) {
            if (curChar == '\n') curToken.lineNo++;
            getNextChar();
        }
    }
    
    private void parseId(TokenType type) {
        while (Character.isJavaIdentifierPart(curChar)) 
            getNextChar();
        int len = nextCharPtr - curToken.start - 1;
        if (type != TokenType.UNKNOWN) 
            curToken.type = type;
        else curToken.type = NTToken.isIdOrKeyword(
           sourceCode.substring(curToken.start,curToken.start + len));
        curToken.length = len;
    }
    
    private void parseDecNum() {
        while (Character.isDigit(curChar))
            getNextChar();
        if (curChar == '.' && Character.isDigit(lookAheadChar())) {
            getNextChar();
            while (Character.isDigit(curChar))
                getNextChar();
        }
    }
    
    private void parseNum() {
        if (curChar == '0' && matchNextChar('x')) {
            // TODO
        } else if (curChar == '0' && Character.isDigit(lookAheadChar())) {
            // TODO
        } else {
            parseDecNum();
            curToken.value = new NTValue(NTValue.ValueType.NUM);        
            curToken.value.setDouble(Double.valueOf(sourceCode.substring(curToken.start,nextCharPtr - 1)));         
        }
        curToken.length = nextCharPtr - curToken.start - 1;
        curToken.type = TokenType.NUM;
        
    }
    
    // 解析字符串，仅支持「"」即半角双引号扩起来的。
    private void parseString() throws LexException {
        StringBuffer sb = new StringBuffer();
        while (true) {
            getNextChar();
            // 检测到「"」则完成解析
            if (curChar == '"') {
                curToken.type = TokenType.STRING;
                break;
            }
            
            if (curChar == '\\') {
                getNextChar();
                switch (curChar) {
                    case '0':
                        sb.append('\0');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        throw new LexException(String.format("unsupport escape \\%c",curChar));
                }
            } else {
                sb.append(curChar);
            }
            String str = sb.toString();
            curToken.value = new NTValue(NTValue.ValueType.STRING);
            curToken.value.setValue(str);
        }
    }
    
    private void skipAline() {
        getNextChar();
        while (nextCharPtr - 1 < sourceCode.length() && curChar != '\0') {
            if (curChar == '\n') {
                curToken.lineNo++;
                getNextChar();
                break;
            }
            getNextChar();
        }
    }
    
    private void skipComment() throws LexException {
        char nextChar = lookAheadChar();
        if (curChar == '/') {
            skipAline();
        } else {
            while (nextChar != '*' && nextCharPtr < sourceCode.length() && curChar != '\0') {
                getNextChar();
                if (curChar == '\n') curToken.lineNo++;
                nextChar = lookAheadChar();
            }
            if (matchNextChar('*')) {
                if (!matchNextChar('/')) throw new LexException("expect '/' after '*'!");
                getNextChar();           
            } else throw new LexException("except '*/' before file end!");
        }
        skipBlanks();
    }
    
    public void getNextToken() throws LexException {
        preToken = curToken.clone();
        skipBlanks();
        curToken.type = TokenType.EOF;
        curToken.length = 0;
        curToken.start = nextCharPtr -1;
        while (nextCharPtr - 1 < sourceCode.length() && curChar != '\0') {
            switch (curChar) {
                case ';':
                    curToken.type = TokenType.COLON;
                    break;
                case '^':
                    curToken.type = TokenType.POW;
                    break;
                case ':':
                    curToken.type = TokenType.COLON;
                    break;
                case ',':
                    curToken.type = TokenType.COMMA;
                    break;
                case '(':
                    curToken.type = TokenType.LEFT_PAREN;
                    break;
                case ')':
                    curToken.type = TokenType.RIGHT_PAREN;
                    break;
                case '{':
                    curToken.type = TokenType.LEFT_BRACE;
                    break;
                case '}':
                    curToken.type = TokenType.RIGHT_BRACE;
                    break;
                case '[':
                    curToken.type = TokenType.LEFT_BRACKET;
                    break;
                case ']':
                    curToken.type = TokenType.RIGHT_BRACKET;
                    break;
                case '.': 
                    if (matchNextChar('.'))
                         curToken.type = TokenType.DOT_DOT;
                    else curToken.type = TokenType.DOT;
                    break;
                case '=':
                    if (matchNextChar('='))
                        curToken.type = TokenType.EQUAL;
                    else curToken.type = TokenType.ASSIGN;
                    break;
                case '+':
                    curToken.type = TokenType.ADD;
                    break;
                case '-':
                    curToken.type = TokenType.SUB;
                    break;
                case '*':
                    if (matchNextChar('*'))
                        curToken.type = TokenType.POW;
                    else curToken.type = TokenType.MUL;
                    break;
                case '/':
                    if (matchNextChar('/') || matchNextChar('*')) {
                        skipComment();
                        curToken.start = nextCharPtr - 1;
                        continue;
                    }
                    else curToken.type = TokenType.DIV;
                    break;
                case '%':
                    curToken.type = TokenType.MOD;
                    break;
                case '&':
                    if (matchNextChar('&'))
                        curToken.type = TokenType.LOGIC_AND;
                    else curToken.type = TokenType.BIT_AND;
                    break;
                case '|':
                    if (matchNextChar('|'))
                        curToken.type = TokenType.LOGIC_OR;
                    else curToken.type = TokenType.BIT_NOT;
                    break;
                case '!':
                    if (matchNextChar('='))
                        curToken.type = TokenType.NOT_EQUAL;
                    else curToken.type = TokenType.LOGIC_NOT;
                    break;
                case '>':
                    if (matchNextChar('='))
                        curToken.type = TokenType.GREATE_EQUAL;
                    else if (matchNextChar('>')) curToken.type = TokenType.BIT_SHIFT_RIGHT;
                    else curToken.type = TokenType.GREATE;
                    break;
                case '<':
                    if (matchNextChar('='))
                        curToken.type = TokenType.LESS_EQUAL;
                    else if (matchNextChar('<')) curToken.type = TokenType.BIT_SHIFT_LEFT;
                    else curToken.type = TokenType.LESS;
                    break;
                case '?':
                    curToken.type = TokenType.QUESTION;
                    break;
                case '~':
                    curToken.type = TokenType.BIT_NOT;
                    break;
                case '#':
                    curToken.type = TokenType.SHARP;
                    break;
                case '"':
                    parseString();
                    break;
                default:
                
                    
                    if (Character.isJavaIdentifierStart(curChar)) {
                        parseId(TokenType.UNKNOWN);
                    } else if (Character.isDigit(curChar)) {
                        parseNum();
                    }
                    return;
            }
            
            // 大部分case出口
            curToken.length = nextCharPtr - curToken.start;
            getNextChar();
            return;
        }
    }
    
    public boolean matchToken(TokenType expected) throws LexException {
        if (curToken.type == expected) {
            getNextToken();
            return true;
        }
        return false;
    }
    
    // 断言并读入
    public void consumeCurToken(TokenType expected,String errMsg) throws CompileException, LexException {
        if (curToken.type != expected) throw new CompileException(errMsg);
        getNextToken();
    }
    
    public void consumeNextToken(TokenType expected,String errMsg) throws CompileException, LexException {
       getNextToken();
        if (curToken.type != expected) throw new CompileException(errMsg);
    }
    
    public void setCompileUnit(NTCompileUnit cu) {
        curCompileUnit = cu;
        cu.curParser = this;
    }
    
}
