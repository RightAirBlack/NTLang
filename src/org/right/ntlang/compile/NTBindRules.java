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
        LOGIC_OR(4),
        LOGIC_AND(5),
        EQUAL(6),
        CMP(7),
        BIT_OR(8),
        BIT_AND(9),
        BIT_SHIFT(10),
        RANGE(11),
        TERM(12),
        FACTOR(13),
        POW(14),
        UNARY(15),
        CALL(16),
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
    
    /**********************算符的.led()与.nud()方法*********************/
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
//            Stack<NTValue> s = cu.curParser.getVM().s;
//            NTValue b = s.pop() ,a = s.pop();
//            s.push(new NTValue((double)(Double)a.getValue() + (double)(Double)b.getValue()));
            Integer tmp = vm.constantVars_SI.get(rule.id);
            cu.writeOpcodeCall(tmp,2);
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
            NTVM vm = cu.curParser.getVM();
            // System.out.println("id2 " +  cu.curParser.getVM().vars.containsKey(rule.id + " nud"));
                
            NTValue v = cu.curParser.getVM().vars.get(rule.id + " nud");
            assert v != null:"hhh";
            // System.out.println(v == null);
            cu.writeOpcodeCall(vm.constantVars_SI.get(new NTValue(rule.id)),1);
            //  v.call(cu.curParser.getVM());
        }
    };
    // 标识符的.nud()方法。
    private static final DenotationFn id = new DenotationFn() {
        @Override
        public void call(NTCompileUnit cu, boolean canAssign) {
            NTToken name = cu.curParser.preToken;
            NTVM vm = cu.curParser.getVM();
            Map<String,NTValue> vars = vm.vars;
            if (!vars.containsKey(cu.curParser.sourceCode.substring(name.start,name.start + name.length))) {
                cu.writeOpcodePushNil();
                return;
            }
            cu.writeOpcodeLoadVar(cu.curParser.sourceCode.substring(name.start,name.start + name.length));
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
    /*******************分割线*******************/
    // 绑定规则表
    public static final Map<TokenType,SymbolBindRule> rules
        = new HashMap<TokenType,SymbolBindRule>(){{
        
        DenotationFn unusednud = new DenotationFn(){   @Override  public void call(NTCompileUnit cu, boolean canAssign) throws LexException {}};
        SymbolBindRule unused = new SymbolBindRule(null,BindPower.NONE,unusednud,null);
        put(TokenType.UNKNOWN,unused);
        put(TokenType.EOF,unused);
        put(TokenType.COLON,unused);
        SymbolBindRule prefixSymbol = new SymbolBindRule(null,BindPower.NONE,literal,null);
        put(TokenType.NUM,prefixSymbol);
        put(TokenType.STRING,prefixSymbol);
        put(TokenType.ID, new SymbolBindRule(null,BindPower.NONE,id,null));
        put(TokenType.SUB, new SymbolBindRule("-",BindPower.TERM,unaryOperator,infixOperator));
        put(TokenType.ADD, new SymbolBindRule("+",BindPower.TERM,unaryOperator,infixOperator));
        put(TokenType.MUL, new SymbolBindRule("*",BindPower.FACTOR,null,infixOperator));
        put(TokenType.DIV, new SymbolBindRule("/",BindPower.FACTOR,null,infixOperator));
        put(TokenType.MOD, new SymbolBindRule("%",BindPower.FACTOR,null,infixOperator));
        put(TokenType.POW, new SymbolBindRule("**",BindPower.POW,null,infixOperator));
        put(TokenType.LEFT_PAREN, new SymbolBindRule("(",BindPower.NONE,parentheses,null));
        put(TokenType.RIGHT_PAREN, unused);
    }};
    
}
