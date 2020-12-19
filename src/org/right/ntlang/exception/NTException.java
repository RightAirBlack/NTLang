package org.right.ntlang.exception;

public class NTException extends Exception
{

    private String message;
    @Override
    public NTException(String str) {
        this.message = str;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
   
    
}
