package org.right.ntlang.interfaces;
import org.right.ntlang.*;
import org.right.ntlang.exception.*;

public interface NTCallable
{

    public boolean call(NTVM vm,int varNum) throws RunningException;

}
