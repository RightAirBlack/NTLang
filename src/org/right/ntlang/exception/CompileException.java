package org.right.ntlang.exception;

public class CompileException extends NTException {
    @Override
    public CompileException(String str) {
        super("COMPILE_ERROR:  " + str);
    }
}
