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

    public void compileProgram() throws LexException, CompileException {
        // TODO....
        if (curParser.matchToken(NTToken.TokenType.TMP)) {
            
        } else if (curParser.matchToken(NTToken.TokenType.SHARP)) {
            compileCommand();
        } else {
            expression(NTBindRules.BindPower.LOWEST);
        }
    }
    
    @Override
    public NTVM.VMResult call() throws Exception {
        // TODO: Implement this method
        return fn.call();
    }
    
    // 解析"宏命令"，即「#命令名 {命令参数|命令参数|命令参数|…};」
    public void compileCommand() throws LexException, CompileException {
        // 此时curToken是「#」后面的「命令名」
        // 先定义下命令名
        String command;
        // 「命令名」可能是ID也可能是STRING
        if (curParser.matchToken(NTToken.TokenType.ID)) {
            NTToken name = curParser.preToken;
            command = curParser.sourceCode.substring(name.start,name.start + name.length);
        } else if (curParser.matchToken(NTToken.TokenType.STRING)) {
            command = curParser.preToken.value.toString();
        } else {
            throw new CompileException("the type of the command name must be id or string!");
        }
        // 随后加载命令函数
        writeOpcodeLoadCommand(command);
        // 其中命令参数个数最多64个
        int argNum = 0;
        // 无参不能有 「{}」
        if (!curParser.matchToken(NTToken.TokenType.COLON)) {
            //if (curParser.curToken.type == NTToken.TokenType.COLON)
                
            argNum = processCommandArgList();
            curParser.consumeCurToken(NTToken.TokenType.RIGHT_BRACE,"expect '}' after command argument list!");
        }
        writeOpcodeEmitCommand(command,argNum);      
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
        if (curParser.curToken.type == NTToken.TokenType.RIGHT_PAREN   || 
            curParser.curToken.type == NTToken.TokenType.RIGHT_BRACKET)
            throw new CompileException("empty argument list!");
        int argNum = 0;
        do {
            if (++argNum > 16)
                throw new CompileException("the max number of argument if 16!");
            expression(NTBindRules.BindPower.LOWEST);
        } while (curParser.matchToken(NTToken.TokenType.COMMA));
        return argNum;
    }
    // 为命令参数列表中的各个参数生成加载指令
    public int processCommandArgList() throws LexException, CompileException {
        // 由主调用方保证参数不为空
        if (curParser.curToken.type == NTToken.TokenType.RIGHT_BRACE)
            throw new CompileException("empty command argument list!");
        int argNum = 0;
        do {
            if (++argNum > 64)
                throw new CompileException("the max number of command argument is 64!");
            //expression(NTBindRules.BindPower.LOWEST);
            if (curParser.matchToken(NTToken.TokenType.NUM)    || 
                curParser.matchToken(NTToken.TokenType.STRING) ||
                curParser.matchToken(NTToken.TokenType.TRUE)   ||
                curParser.matchToken(NTToken.TokenType.FALSE)   ||
                curParser.matchToken(NTToken.TokenType.NIL)   )  {
                // 加载字面量
                NTBindRules.rules.get(curParser.curToken.type).nud.call(this,false);
            } else if (curParser.matchToken(NTToken.TokenType.ID)) {
                // 加载id
                NTToken name = curParser.preToken;             
                writeOpcodeLoadConstant(curParser.getVM().
                 addConstant(new NTValue(curParser.sourceCode.
                 substring(name.start,name.start + name.length))));
            } else {
                throw new CompileException("the type of command arguments should be id,string,number,true,false or nil!");
            }
        } while (curParser.matchToken(NTToken.TokenType.BIT_OR));
        return argNum;
    }
    // "字节码"相关
    public void writeOpcodeCall(String fnName,int varNum) {
        fn.addInstruction(NTOpcode.CALL,curParser.getVM().addConstant(new NTValue(fnName)),varNum);
    }
    
    public void writeOpcodePushNil() {
        fn.addInstruction(NTOpcode.PUSH_NIL);
    }
    public void writeOpcodePushFalse() {
        fn.addInstruction(NTOpcode.PUSH_FALSE);
    }
    public void writeOpcodePushTrue() {
        fn.addInstruction(NTOpcode.PUSH_TRUE);
    }
    public void writeOpcodeLoadThis() {
        fn.addInstruction(NTOpcode.LOAD_THIS);
    }
    public void writeOpcodeLoadConstant(int valuePtrInConstant) {
        fn.addInstruction(NTOpcode.LOAD_CONSTANT,valuePtrInConstant);
    }
    public void writeOpcodeLoadVar(String varNameInVars) {
        fn.addInstruction(NTOpcode.LOAD_VAR,curParser.getVM().addConstant(new NTValue(varNameInVars)));
    }
    public void writeOpcodeStoreVar(String varNameInVars) {
        fn.addInstruction(NTOpcode.STORE_VAR,curParser.getVM().addConstant(new NTValue(varNameInVars)));
    }
    public void writeOpcodeLoadMap(String n) {
        fn.addInstruction(NTOpcode.LOAD_MAP,curParser.getVM().addConstant(new NTValue(n)));
    }
    public void writeOpcodeStoreMap(String n) {
        fn.addInstruction(NTOpcode.STORE_MAP,curParser.getVM().addConstant(new NTValue(n)));
    }
    public void writeOpcodeLoadMapBySubscript() {
        fn.addInstruction(NTOpcode.LOAD_MAP_BY_SUBSCRIPT);
    }
    public void writeOpcodeStoreMapBySubscript() {
        fn.addInstruction(NTOpcode.STORE_MAP_BY_SUBSCRIPT);
    }
    public void writeOpcodeCallFunction(int argNum) {
        fn.addInstruction(NTOpcode.CALL_FUN,argNum);
    }
    public void writeOpcodeLoadCommand(String commandVarNameInCommandLib) {
        fn.addInstruction(NTOpcode.LOAD_COMMAND,curParser.getVM().addConstant(new NTValue(commandVarNameInCommandLib)));
    }
    public void writeOpcodeEmitCommand(String commandName,int varNum) {
        fn.addInstruction(NTOpcode.EMIT_COMMAND,curParser.getVM().addConstant(new NTValue(commandName)),varNum);
    }
    public void writeOpcodeDup() {
        fn.addInstruction(NTOpcode.DUP);
    }
    public void writeOpcodeLogicOr() {
        fn.addInstruction(NTOpcode.LOGIC_OR,-1);
    }
    public void writeOpcodeLogicAnd() {
        fn.addInstruction(NTOpcode.LOGIC_AND,-1);
    }
    public void writeOpcodeJumpIfFalse() {
        fn.addInstruction(NTOpcode.JUMP_IF_FALSE,-1);
    }
    public void writeOpcodeJump() {
        fn.addInstruction(NTOpcode.JUMP,-1);
    }
}
