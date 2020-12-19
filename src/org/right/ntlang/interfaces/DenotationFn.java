package org.right.ntlang.interfaces;
import org.right.ntlang.compile.*;
import org.right.ntlang.exception.*;

public interface DenotationFn {
    public void call(NTCompileUnit cu,boolean canAssign) throws LexException,CompileException;
}
