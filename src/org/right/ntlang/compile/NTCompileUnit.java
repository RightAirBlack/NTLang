package org.right.ntlang.compile;
import java.util.concurrent.*;
import org.right.ntlang.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;

public class NTCompileUnit implements Callable {

    public NTFn fn;
    public NTParser curParser;
    // 暂时只有一个作用域
    // public NTComplieUnit enclosingUnit;
    public NTCompileUnit(NTParser p) {
        curParser = p;
        p.curCompileUnit = this;
        fn = new NTFn();
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
    
}
