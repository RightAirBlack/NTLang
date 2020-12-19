package org.right.ntlang.interfaces;
import org.right.ntlang.*;
import org.right.ntlang.exception.*;

public interface NTCallable
{

    public int call(NTVM vm) throws RunningException;

}
