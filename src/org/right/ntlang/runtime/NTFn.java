package org.right.ntlang.runtime;
import java.util.*;
import org.right.ntlang.*;
import org.right.ntlang.compile.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;
import java.util.concurrent.*;

public class NTFn implements Callable {

    private Vector<NTInstruction> instrStream;
    private Vector<NTValue> constant;
    private NTVM vm;
    
    // 记录上次运行时表达式的值，初始是nil
    private NTValue _THIS = NTValue.NIL;
    public void setThis(NTValue tHIS)  {
        _THIS = tHIS;
    }
    public NTValue getThis() {
        return _THIS;
    }
    
    public NTFn(NTVM vm) {
        instrStream = new Vector<NTInstruction>(1024);
        constant = new Vector<NTValue>();
        this.vm = vm;
    }
    @Override
    public NTVM.VMResult call() throws Exception {
        int length = instrStream.size();
        NTInstruction instr;
        for (int index = 0;index < length;index++) {
            instr = instrStream.elementAt(index);
            switch (instr.opcode) {
                case PUSH_NIL:
                    vm.s.push(NTValue.NIL);
                    break;
                case PUSH_TRUE:
                    vm.s.push(NTValue.TRUE);
                    break;
                case PUSH_FALSE:
                    vm.s.push(NTValue.FALSE);
                    break;
                case LOAD_THIS:
                    vm.s.push(this._THIS);
                    break;
                case CALL:
                    String name = vm.constantVars_IV.get(instr.operand0).toString();
                    int varNum = instr.operand1;
                    NTValue caller = vm.getVars().get(name);    
                    if (caller == null || !caller.canCall()) throw new RunningException("can call this 'function'!");
                    caller.call(vm,varNum);
                    break;
                case CALL_FUN:
                    // 形参数
                    int varNum2 = instr.operand0;
                    // 方法名
                    NTValue methodCaller = vm.s.elementAt(vm.s.size() - 1 - varNum2);  
                    if (methodCaller == null || !methodCaller.canCall()) throw new RunningException("can call this 'method'!");
                    methodCaller.call(vm,varNum2);
                    break;
                case LOAD_COMMAND:
                    String commandVarName = vm.constantVars_IV.get(instr.operand0).toString();
                    // 库是null应返回nil，并"warning"
                    if (vm.getCommandLib() == null) {
                        vm.s.push(NTValue.NIL);
                        vm.getOut().println("WARNING: the command lib is nil!");
                        break;
                    }           
                    // 命令不存在应返回nil，并"warning"
                    if (!vm.getCommandLib().containsKey(commandVarName)) {
                        vm.s.push(NTValue.NIL);
                        vm.getOut().println("WARNING: there is a command which is nil!");
                        break;
                    }              
                    vm.s.push(new NTValue(vm.getCommand(commandVarName)));
                    break;
                case EMIT_COMMAND:
                    String commandName = vm.constantVars_IV.get(instr.operand0).toString();
                    int commandVarNum = instr.operand1;
                    NTValue commandCaller = vm.getVars().get(commandName);    
                    if (commandCaller == null || !commandCaller.canCall()) throw new RunningException("can call this 'command'!");
                    commandCaller.call(vm,commandVarNum);
                    break;
                case LOAD_CONSTANT:
                    NTValue i = vm.constantVars_IV.get(instr.operand0);
                    if (i == null) throw new RunningException("Error Operand0!");
                    vm.s.push(i);
                    break;
                 case LOAD_VAR:
                     String varName = vm.constantVars_IV.get(instr.operand0).toString();
                     // 变量不存在返回nil
                     if (!vm.getVars().containsKey(varName)) {
                         vm.s.push(NTValue.NIL);
                         break;
                     }              
                     vm.s.push(vm.getVars().get(varName));
                     break;
                 case STORE_VAR:
                     String varName2 = vm.constantVars_IV.get(instr.operand0).toString();
                     NTValue newVarValue = vm.s.pop();  
                     vm.getVars().put(varName2,newVarValue);
                     vm.s.push(newVarValue);
                     break;
                 case LOAD_MAP:
                     String fieldName = vm.constantVars_IV.get(instr.operand0).toString();
                     NTValue map = vm.s.pop();
                     if (map == NTValue.NIL) 
                         throw new RunningException("the map must be not a nil variable!");
                     NTValue field = map.getField(fieldName);
                     // 变量不存在返回nil
                     if (field == null) {
                         vm.s.push(NTValue.NIL);
                         break;
                     }              
                     vm.s.push(field);
                     break;
                case STORE_MAP:
                    String fieldName2 = vm.constantVars_IV.get(instr.operand0).toString();
                    NTValue newFieldValue = vm.s.pop();
                    NTValue map2 = vm.s.pop();
                    map2.setField(fieldName2,newFieldValue);
                    vm.s.push(newFieldValue);
                    break; 
                case LOAD_MAP_BY_SUBSCRIPT:
                    NTValue fieldName3 = vm.s.pop();
                    NTValue map3 = vm.s.pop();
                    if (map3 == NTValue.NIL) 
                        throw new RunningException("the map or array must be not a nil variable!");
                    NTValue field2;
                    if (fieldName3.type() == NTValue.ValueType.NUM)
                         field2 = map3.getElement(fieldName3.toInt());
                    else field2 = map3.getField(fieldName3.toString());
                    // 变量不存在返回nil
                    if (field2 == null) {
                        vm.s.push(NTValue.NIL);
                        break;
                    }              
                    vm.s.push(field2);
                    break;
                case STORE_MAP_BY_SUBSCRIPT:
                    NTValue newFieldValue2 = vm.s.pop();
                    NTValue fieldName4 = vm.s.pop();
                    NTValue map4 = vm.s.pop();
                    if (fieldName4.type() == NTValue.ValueType.NUM)
                         map4.setElement(fieldName4.toInt(),newFieldValue2);
                    else map4.setField(fieldName4.toString(),newFieldValue2);
                    vm.s.push(newFieldValue2);
                    break; 
                case DUP:
                    vm.s.push(vm.s.peek());
                    break;
            }
        }
        if (vm.s.size() > 0)
            this._THIS = vm.s.pop();
        else this._THIS = NTValue.NIL;
        vm.popNumTimes(vm.s.size());
        return NTVM.VMResult.SUCCESS;
    }
    
    // 加载一条字节码
    public boolean addInstruction(NTOpcode opcode) {
        return instrStream.add(new NTInstruction(opcode));
    }
    public boolean addInstruction(NTOpcode opcode, int operand0) {
        return instrStream.add(new NTInstruction(opcode,operand0));
    }
    public boolean addInstruction(NTOpcode opcode, int operand0, int operand1) {
        return instrStream.add(new NTInstruction(opcode,operand0,operand1));
    }
    // 重置字节流
    public void resetInstrStream() {
        instrStream.clear();
    }
}
