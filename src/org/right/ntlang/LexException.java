package org.right.ntlang.exception;

public class LexException extends NTException {
    @Override
    public LexException(String str) {
        super("LEX_ERROR:  " + str);
    }
}
