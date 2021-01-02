package org.right.ntlang.compile;
import java.util.*;
import org.right.ntlang.*;
import org.right.ntlang.interfaces.*;

import static org.right.ntlang.compile.NTToken.TokenType;
import org.right.ntlang.exception.*;
import org.right.ntlang.runtime.*;
public class NTBindRules {
    // 绑定权值 枚举类。
    public static enum BindPower {
        NONE(0),
        LOWEST(1),
        ASSIGN(2),
        CONDITION(3),
        LOGIC_OR(5),
        LOGIC_AND(7),
        EQUAL(8),
        CMP(9),
        BIT_OR(10),
        BIT_AND(11),
        BIT_SHIFT(12),
        RANGE(13),
        TERM(14),
        FACTOR(15),
        POW(16),
        UNARY(17),
        CALL(18),
        HIGHEST(255);
        public final byte power;
        BindPower(int p) {
            power = (byte)p;
        }
    }
    // 算法绑定规则类
    public static class SymbolBindRule {
        public String id;
        public BindPower lbp;
        DenotationFn nud;
        DenotationFn led;
        public SymbolBindRule(String id,BindPower lbp,DenotationFn nud,DenotationFn led) {
            this.id = id;
            this.lbp = lbp;
            this.nud = nud;
            this.led = led;
        }
    }
    
    /**********************运算符的.led()与.nud()方法*********************/
    // 数字与字符串的.nud()，编译字面量。
    private static final DenotationFn literal = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) {
            NTVM vm = cu.curParser.getVM();
            cu.writeOpcodeLoadConstant(vm.addConstant(cu.curParser.curToken.value));
            // System.out.println("data " + cu.curParser.getVM().s.peek());
            
        }
        
        
    };
    // 中缀运算符.led()方法。
    private static final DenotationFn infixOperator = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            SymbolBindRule rule = rules.get(cu.curParser.preToken.type);
            BindPower rbp = rule.lbp;// 中缀运算符对左右操作数的绑定权值一样。
            cu.expression(rbp);
            NTVM vm = cu.curParser.getVM();
            cu.writeOpcodeCall(rule.id,2);
            //fn.addInstruction(NTOpcode.CALL,vm.vars.get(rule.id).toString());
            // cu.curParser.getVM().vars.get(rule.id).call(cu.curParser.getVM());
          
        }
    };
    // 前缀运算符的.nud()方法。
    private static final DenotationFn unaryOperator = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            SymbolBindRule rule = rules.get(cu.curParser.preToken.type);
            // System.out.println("id " + cu.curParser.preToken.type);
            cu.expression(BindPower.UNARY);
            // System.out.println("id2 " +  cu.curParser.getVM().vars.containsKey(rule.id + " nud"));
                
            NTValue v = cu.curParser.getVM().getVars().get(rule.id + " nud");
            assert v != null:"hhh";
            // System.out.println(v == null);
            cu.writeOpcodeCall(rule.id + " nud",1);
            //  v.call(cu.curParser.getVM());
        }
    };
    // 标识符的.nud()方法。
    private static final DenotationFn id = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            NTToken name = cu.curParser.preToken;
            // 给变量赋值
            if (canAssign && cu.curParser.matchToken(NTToken.TokenType.ASSIGN)) {
                cu.expression(BindPower.LOWEST);
                cu.writeOpcodeStoreVar(cu.curParser.sourceCode.substring(name.start,name.start + name.length));
            } else {
                cu.writeOpcodeLoadVar(cu.curParser.sourceCode.substring(name.start,name.start + name.length));
            }
            
        }
    };
    // 「||」的.led()方法。
    private static final DenotationFn logicOr = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            // 判断条件在栈顶
            cu.writeOpcodeLogicOr();
            // 获取当前指令地址用于"回填"
            int oldInstructionIndex = cu.fn.getCurInstrIndex();
            // 生成计算右操作数的指令
            cu.expression(BindPower.LOGIC_OR);
            // 回填
            int offset = cu.fn.getCurInstrIndex() - oldInstructionIndex;
            cu.fn.patchPlaceInstruction(oldInstructionIndex,offset);
            
            
        }
    };
    // 「&&」的.led()方法。
    private static final DenotationFn logicAnd = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            // 判断条件在栈顶
            cu.writeOpcodeLogicAnd();
            // 获取当前指令地址用于"回填"
            int oldInstructionIndex = cu.fn.getCurInstrIndex();
            // 生成计算右操作数的指令
            cu.expression(BindPower.LOGIC_AND);
            // 回填
            int offset = cu.fn.getCurInstrIndex() - oldInstructionIndex;
            cu.fn.patchPlaceInstruction(oldInstructionIndex,offset);
        }
    };
    // 「?:」的.led()方法。
    private static final DenotationFn condition = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            // 判断条件在栈顶
            cu.writeOpcodeJumpIfFalse();
            // 获取当前指令地址用于"回填"
            int falseBranchStart = cu.fn.getCurInstrIndex();
            // 生成计算左边结果的指令
            cu.expression(BindPower.LOWEST);
            cu.curParser.consumeCurToken(NTToken.TokenType.COLON,"expect ':' after true branch!");
            // 执行完true分支后需要跳过false分支
            cu.writeOpcodeJump();
            int falseBranchEnd = cu.fn.getCurInstrIndex();
            // 先回填true
            int offset = cu.fn.getCurInstrIndex() - falseBranchStart;
            cu.fn.patchPlaceInstruction(falseBranchStart,offset);
            
            // 随后编译false分支
            cu.expression(BindPower.LOWEST);
            
            // 再回填false分支
            offset = cu.fn.getCurInstrIndex() - falseBranchEnd;
            cu.fn.patchPlaceInstruction(falseBranchEnd,offset);
            
        }
    };
    // 「(」的.nud()方法。
    private static final DenotationFn parentheses = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            cu.expression(BindPower.LOWEST);
            cu.curParser.consumeCurToken(NTToken.TokenType.RIGHT_PAREN, "expect ')' after expression!");
        }
    };
    // 「(」的.led()方法
    private static final DenotationFn callable = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            NTParser p = cu.curParser;
            // TODO.....
            int argNum = 0;
            if (!p.matchToken(NTToken.TokenType.RIGHT_PAREN)) {
                argNum = cu.processArgList();
                p.consumeCurToken(NTToken.TokenType.RIGHT_PAREN,"expect ')' after argument list!");
            }
            cu.writeOpcodeCallFunction(argNum);      
        }
    };
// TODO.....
    // 「[」的.nud()方法。
    private static final DenotationFn arrayLiteral = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            // curToken是key
            // 用「__arrayNew」新建array对象
            cu.writeOpcodeCall("__arrayNew",0);
            do {
                // 可以创建空array
                if (cu.curParser.curToken.type == NTToken.TokenType.RIGHT_BRACKET) 
                    break;
                cu.writeOpcodeDup();
                // 读取element
                cu.expression(BindPower.LOWEST);
                // 将element添加到array中
                cu.writeOpcodeCall("__arrayAdd",2);
            } while (cu.curParser.matchToken(NTToken.TokenType.COMMA));

            cu.curParser.consumeCurToken(NTToken.TokenType.RIGHT_BRACKET, "array literal should end with ']'!");
            

        }
    };
    // 「[」的.led()方法
    private static final DenotationFn subscript = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            // curToken是「[」后面的token
            // 确保[]之间不空
            if (cu.curParser.matchToken(NTToken.TokenType.RIGHT_BRACKET))
                throw new CompileException("need argument in '[]'!");
            // 解析[]里的参数并加载入栈，且参数个数原则上不能大于1
            if (cu.processArgList() > 1) 
                throw new CompileException("the max number of subscript argument is 1!");
            cu.curParser.consumeCurToken(NTToken.TokenType.RIGHT_BRACKET,"expect ']' after argument list!");
            // 给变量赋值
            if (canAssign && cu.curParser.matchToken(NTToken.TokenType.ASSIGN)) {
                cu.expression(BindPower.LOWEST);
                cu.writeOpcodeStoreMapBySubscript();
            } else {
                cu.writeOpcodeLoadMapBySubscript();
            }
            
        }
    };
    
    // 「.」的.led()方法
    private static final DenotationFn callEntry = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            // 此时curParser是「.」，而且「.」前面的value已经加载到栈里去了，所以直接生成加载「.」后面的名字所对应的field就行了
            cu.curParser.consumeCurToken(NTToken.TokenType.ID,"expect name after '.'!");
            NTToken name = cu.curParser.preToken;
            // 给变量赋值
            if (canAssign && cu.curParser.matchToken(NTToken.TokenType.ASSIGN)) {
                cu.expression(BindPower.LOWEST);
                cu.writeOpcodeStoreMap(cu.curParser.sourceCode.substring(name.start,name.start + name.length));
            } else {
                cu.writeOpcodeLoadMap(cu.curParser.sourceCode.substring(name.start,name.start + name.length));
            }         
        }
    };
    // 「{」的.nud()方法，即map直接量。
    private static final DenotationFn mapLiteral = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            // curToken是key
            // 用「__mapNew」新建map对象
            cu.writeOpcodeCall("__mapNew",0);
            do {
                // 可以创建空map
                if (cu.curParser.curToken.type == NTToken.TokenType.RIGHT_BRACE) 
                    break;
                cu.writeOpcodeDup();
                // 读取key
                cu.expression(BindPower.UNARY);
                // 读取key后面的冒号
                cu.curParser.consumeCurToken(NTToken.TokenType.COLON,"expect ':' after key!");
                // 读取value
                cu.expression(BindPower.LOWEST);
                // 将「key:value」添加到map中
                cu.writeOpcodeCall("__mapAdd",3);
            } while (cu.curParser.matchToken(NTToken.TokenType.COMMA));
            
            cu.curParser.consumeCurToken(NTToken.TokenType.RIGHT_BRACE, "map literal should end with '}'!");


        }
    };
    // 「nil」的.nud()方法。
    private static final DenotationFn pushNil = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            cu.writeOpcodePushNil();
        }
    };
    // 「true」的.nud()方法。
    private static final DenotationFn pushTrue = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            cu.writeOpcodePushTrue();
        }
    };
    // 「false」的.nud()方法。
    private static final DenotationFn pushFalse = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            cu.writeOpcodePushFalse();
        }
    };
    // 「this」的.nud()方法。
    private static final DenotationFn loadThis = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) throws LexException, CompileException {
            cu.writeOpcodeLoadThis();
        }
    };
    
    /*******************分割线*******************/
    // 绑定规则表
    public static final Map<TokenType,SymbolBindRule> rules
        = new HashMap<TokenType,SymbolBindRule>(){{
        
        DenotationFn unusednud = new DenotationFn(){   @Override  public void call(NTCompileUnit cu, boolean canAssign) throws LexException {}};
        SymbolBindRule unused = new SymbolBindRule(null,BindPower.NONE,unusednud,null);
        put(TokenType.UNKNOWN,unused);
        put(TokenType.EOF,unused);
        put(TokenType.COLON,unused);
        put(TokenType.COMMA,unused);
        SymbolBindRule prefixSymbol = new SymbolBindRule(null,BindPower.NONE,literal,null);
        put(TokenType.NUM,prefixSymbol);
        put(TokenType.STRING,prefixSymbol);
        put(TokenType.ID, new SymbolBindRule(null,BindPower.NONE,id,null));
        put(TokenType.TRUE, new SymbolBindRule(null,BindPower.NONE,pushTrue,null));
        put(TokenType.FALSE, new SymbolBindRule(null,BindPower.NONE,pushFalse,null));
        put(TokenType.NIL, new SymbolBindRule(null,BindPower.NONE,pushNil,null));
        put(TokenType.THIS, new SymbolBindRule(null,BindPower.NONE,loadThis,null));
        put(TokenType.SUB, new SymbolBindRule("-",BindPower.TERM,unaryOperator,infixOperator));
        put(TokenType.ADD, new SymbolBindRule("+",BindPower.TERM,unaryOperator,infixOperator));
        put(TokenType.MUL, new SymbolBindRule("*",BindPower.FACTOR,null,infixOperator));
        put(TokenType.DIV, new SymbolBindRule("/",BindPower.FACTOR,null,infixOperator));
        put(TokenType.MOD, new SymbolBindRule("%",BindPower.FACTOR,null,infixOperator));
        put(TokenType.POW, new SymbolBindRule("**",BindPower.POW,null,infixOperator));
        put(TokenType.LEFT_PAREN, new SymbolBindRule(null,BindPower.CALL,parentheses,callable));
        put(TokenType.RIGHT_PAREN, unused);
        put(TokenType.LEFT_BRACKET, new SymbolBindRule(null,BindPower.CALL,arrayLiteral,subscript));
        put(TokenType.RIGHT_BRACKET, unused);
        put(TokenType.LEFT_BRACE, new SymbolBindRule(null,BindPower.NONE,mapLiteral,null));
        put(TokenType.RIGHT_BRACE, unused);
        put(TokenType.LOGIC_OR, new SymbolBindRule(null,BindPower.LOGIC_OR,null,logicOr));
        put(TokenType.LOGIC_AND, new SymbolBindRule(null,BindPower.LOGIC_AND,null,logicAnd));
        put(TokenType.QUESTION, new SymbolBindRule(null,BindPower.ASSIGN,null,condition));
        put(TokenType.DOT, new SymbolBindRule(null,BindPower.CALL,null,callEntry));
    }};
}
