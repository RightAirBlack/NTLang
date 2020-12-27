package org.right.ntlang.runtime;

//一条指令，即"字节码"
public class NTInstruction {
    public NTInstruction(NTOpcode opcode) {
        this(opcode, 0, 0);
    }
    public NTInstruction(NTOpcode opcode, int operand0) {
        this(opcode, operand0, 0);
    }
    public NTInstruction(NTOpcode opcode, int operand0, int operand1) {
        this.opcode = opcode;
        this.operand0 = operand0;
        this.operand1 = operand1;
    }
    
    public NTOpcode opcode = NTOpcode.forValue(0); //指令类型
    public int operand0; //指令值1
    public int operand1; //指令值2
}
