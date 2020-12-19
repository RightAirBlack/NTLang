package org.right.ntlang.compile;
import java.util.concurrent.*;
import java.nio.*;
import java.util.*;
import org.apache.http.util.*;
import org.right.ntlang.*;
import org.right.ntlang.runtime.*;

public class NTFn implements Callable {

    private Vector<NTInstruction> instrStream;
    private Vector<NTValue> constant;
    public NTFn() {
        instrStream = new Vector<NTInstruction>(1024);
        constant = new Vector<NTValue>();
    }
    @Override
    public NTVM.VMResult call() throws Exception
    {
        // TODO: Implement this method
        return null;
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
