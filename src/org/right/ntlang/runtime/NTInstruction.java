package org.right.ntlang.runtime;

//一条指令，即"字节码"
public class NTInstruction {
    public NTInstruction(NTOpcode opcode) {
        this(opcode, 0, 0);
    }
    public NTInstruction(NTOpcode opcode, int a) {
        this(opcode, a, 0);
    }
    public NTInstruction(NTOpcode opcode, int a, int b) {
        this.opcode = opcode;
        this.a = a;
        this.b = b;
    }
    
    public NTOpcode opcode = NTOpcode.forValue(0); //指令类型
    public int a; //指令值1
    public int b; //指令值2
}
