package org.right.ntlang.compile;
import java.util.concurrent.*;
import org.right.ntlang.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;
import org.right.ntlang.runtime.*;

public class NTCompileUnit implements Callable {

    public NTFn fn;
    public NTParser curParser;
    // 暂时只有一个作用域
    // public NTComplieUnit enclosingUnit;
    public NTCompileUnit(NTParser p) {
        curParser = p;
        p.curCompileUnit = this;
        fn = new NTFn(p.getVM());
    }

    public void compileProgram() throws LexException, CompileException
    {
        // TODO....
        expression(NTBindRules.BindPower.LOWEST);
    }
    
    @Override
    public NTVM.VMResult call() throws Exception
    {
        // TODO: Implement this method
        return fn.call();
    }
    
    // 使用TDOP(自上而下算符优先)解析代码
    public void expression(NTBindRules.BindPower rbp) throws LexException, CompileException {
        DenotationFn nud = NTBindRules.rules.get(curParser.curToken.type).nud;
        assert nud != null :"nud is null!";
        // System.out.println("expression..." + curParser.curToken.type);
        curParser.getNextToken();
        boolean canAssign = rbp.power < NTBindRules.BindPower.ASSIGN.power;
        nud.call(this,canAssign);
        
        while (rbp.power < NTBindRules.rules.get(curParser.curToken.type).lbp.power) {
            DenotationFn led = NTBindRules.rules.get(curParser.curToken.type).led;
            curParser.getNextToken();
            led.call(this,canAssign);
        }
    }
    // 为实参列表中的各个实参生成加载指令
    public int processArgList() throws LexException, CompileException {
        // 由主调用方保证参数不为空
        if (curParser.curToken.type == NTToken.TokenType.RIGHT_PAREN || 
           curParser.curToken.type == NTToken.TokenType.RIGHT_BRACKET ||
           curParser.curToken.type == NTToken.TokenType.RIGHT_BRACE)
            throw new CompileException("empty argument list!");
        int argNum = 0;
        do {
            if (++argNum > 16)
                throw new CompileException("the max number of argument if 16!");
            expression(NTBindRules.BindPower.LOWEST);
        } while (curParser.matchToken(NTToken.TokenType.COMMA));
        return argNum;
    }
    // "字节码"相关
    public void writeOpcodeCall(int fnName,int varNum) {
        fn.addInstruction(NTOpcode.CALL,fnName,varNum);
    }
    public void writeOpcodeMov(int from,int to) {
        fn.addInstruction(NTOpcode.MOV,from,to);
    }
    public void writeOpcodePushNil() {
        fn.addInstruction(NTOpcode.PUSH_NIL,0);
    }
    public void writeOpcodePushFalse() {
        fn.addInstruction(NTOpcode.PUSH_FALSE,0);
    }
    public void writeOpcodePushTrue() {
        fn.addInstruction(NTOpcode.PUSH_TRUE,0);
    }
    public void writeOpcodeLoadConstant(int valuePtrInConstant) {
        fn.addInstruction(NTOpcode.LOAD_CONSTANT,valuePtrInConstant);
    }
    public void writeOpcodeLoadVar(String varNameInVars) {
        fn.addInstruction(NTOpcode.LOAD_VAR,varNameInVars);
    }
    public void writeOpcodeStoreVar(String varNameInVars) {
        fn.addInstruction(NTOpcode.STORE_VAR,varNameInVars);
    }
    public void writeOpcodeLoadMap(String n) {
        fn.addInstruction(NTOpcode.LOAD_MAP,n);
    }
    public void writeOpcodeStoreMap(String n) {
        fn.addInstruction(NTOpcode.STORE_MAP,n);
    }
    public void writeOpcodeCallMethod(int argNum) {
        fn.addInstruction(NTOpcode.CALL_METHOD,argNum);
    }
}
