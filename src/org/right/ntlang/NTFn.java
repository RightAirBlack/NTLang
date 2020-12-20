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
                    int varNum = instr.operand1;
                    NTValue caller = vm.vars.get(name);    
                    if (!caller.canCall()) throw new RunningException("can call this 'function'!");
                    caller.call(vm,varNum);
                    break;
                case CALL_METHOD:
                    // 形参数
                    int varNum2 = instr.operand0;
                    // 方法名
                    String methodName = vm.s.elementAt(vm.s.size() - varNum2).toString();
                    NTValue methodCaller = vm.vars.get(methodName);    
                    if (!methodCaller.canCall()) throw new RunningException("can call this 'method'!");
                    methodCaller.call(vm,varNum2);
                    break;
                case LOAD_CONSTANT:
                    NTValue i = vm.constantVars_IV.get(instr.operand0);
                    if (i == null) throw new RunningException("Error Operand0!");
                    vm.s.push(i);
                    break;
                 case LOAD_VAR:
                     String varName = instr.opvalue;
                     vm.s.push(vm.vars.get(varName));
                     break;
                 case LOAD_MAP:
                     String fieldName = instr.opvalue;
                     vm.s.push(vm.s.pop().getField(fieldName));
                     break;
//                case STORE_MAP:
//                    String fieldName2 = instr.opvalue;
//                    vm.s.pop().setField(fieldName2,vm.s.);
//                    break;       
                     
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
