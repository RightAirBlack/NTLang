package org.right.ntlang.compile;
import java.util.concurrent.*;
import java.nio.*;
import java.util.*;
import org.apache.http.util.*;
import org.right.ntlang.*;
import org.right.ntlang.runtime.*;
import org.right.ntlang.exception.*;

public class NTFn implements Callable {

    private Vector<NTInstruction> instrStream;
    private Vector<NTValue> constant;
    private NTVM vm;
    public NTFn(NTVM vm) {
        instrStream = new Vector<NTInstruction>(1024);
        constant = new Vector<NTValue>();
        this.vm = vm;
    }
    @Override
    public NTVM.VMResult call() throws Exception
    {
        for (NTInstruction instr : instrStream) {
            switch (instr.opcode) {
                case MOV:
                    // TODO......
                case PUSH_NIL:
                    vm.s.push(NTValue.NIL);
                    break;
                case PUSH_TRUE:
                    vm.s.push(NTValue.TRUE);
                    break;
                case PUSH_FALSE:
                    vm.s.push(NTValue.FALSE);
                    break;
                case CALL:
                    String name = vm.constantVars_IV.get(instr.operand0).toString();
                    NTValue caller = vm.vars.get(name);    
                    if (!caller.canCall()) throw new RunningException("can call this 'method'!");
                    caller.call(vm);
                    break;
                case LOAD_CONSTANT:
                    NTValue i = vm.constantVars_IV.get(instr.operand0);
                    if (i == null) throw new RunningException("Error Operand0!");
                    vm.s.push(i);
                    break;
                    
            }
        }
        return NTVM.VMResult.SUCCESS;
    }
    
    // 加载一条字节码
    public boolean addInstruction(NTOpcode opcode, int operand0) {
        return addInstruction(opcode,operand0,0);
    }
    public boolean addInstruction(NTOpcode opcode, int operand0, int operand1) {
        return instrStream.add(new NTInstruction(opcode,operand0,operand1));
    }
    public boolean addInstruction(NTOpcode opcode, String operand) {
        return instrStream.add(new NTInstruction(opcode,operand));
    }
}
