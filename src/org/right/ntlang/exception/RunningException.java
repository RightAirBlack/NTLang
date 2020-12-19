package org.right.ntlang.exception;

public class RunningException extends NTException {
    public RunningException(String str) {
        super("RUNNING_ERROR:  " + str);
    }
}
