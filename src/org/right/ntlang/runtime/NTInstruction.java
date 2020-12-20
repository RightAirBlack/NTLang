package org.right.ntlang.runtime;

//一条指令即"字节码"
public class NTInstruction {
    public NTInstruction(NTOpcode opcode, int operand0) {
        this(opcode, operand0, 0);
    }
    public NTInstruction(NTOpcode opcode, int operand0, int operand1) {
        this.opcode = opcode;
        this.operand0 = operand0;
        this.operand1 = operand1;
    }
    public NTInstruction(NTOpcode opcode, String opvalue) {
        this.opcode = opcode;
        this.opvalue = opvalue;
    }
    public NTOpcode opcode = NTOpcode.forValue(0); //指令类型
    public int operand0; //指令值1
    public int operand1; //指令值2
    public String opvalue; //指令值
}
