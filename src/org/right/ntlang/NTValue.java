package org.right.ntlang;
import java.util.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;
import java.util.concurrent.*;

public class NTValue {
    public static enum ValueType {
        NIL(0),FALSE(1),TRUE(2),NUM(3),STRING(4),
        USERDATA(5),CALLABLE(6),MAP(7),LIST(8),RANGE(9),
        CLASS(10),INSTANCE(11);
        public final byte id;
        ValueType(int i) {
            id = (byte)i;
        }
    }
    
    private ValueType type;
    private Object userdata = null;
    private String str = null;
    private Map<String,NTValue> map = null;
    private double num = Double.NaN;
    private NTCallable caller = null;
    
    public NTValue(ValueType type) {
        this.type = type;     
    }
    public NTValue(NTCallable c) {
        this(ValueType.CALLABLE);
        caller = c;
    }
    public NTValue(double n) {
        this(ValueType.NUM);
        setValue(n);
    }
    
    public ValueType type() {
        return type;
    }
    
    @Override
    public String toString() {
        switch (type) {
            case NIL:
                return "nil";
            case FALSE:
                return "false";
            case TRUE:
                return "true";
            case NUM:
                return Double.toString(num);
            case STRING:
                return str;
            case CALLABLE:
                return caller.toString();
            case MAP:
                return map.toString();
            default:
                return userdata.toString();
        }
    }
    
    public Object getValue() {
        switch (type) {
            case NIL:
            case FALSE:
                return 0.0;
            case TRUE:
                return 1.0;
            case NUM:
                return num;
            case STRING:
                return str;
            case CALLABLE:
                return caller;
            case MAP:
                return map;
            default:
                return userdata;
        }
    }
    
    public void setValue(Object o) {
        userdata = o;
    }
    
    public void setValue(boolean b) {
        if (type != ValueType.NIL || type != ValueType.FALSE || type != ValueType.TRUE)
            return;
        type = b ? ValueType.TRUE : ValueType.FALSE;
        num = b ? 1.0 : 0.0;
    }
    
    public void setValue(double n) {
        num = n;
    }
    
    public void setValue(Double n) {
        num = n;
        userdata = n;
    }
    
    public void setValue(NTCallable c) {
        caller = c;
    }
    
    public void setValue(String str) {
        this.str = str;
    }
    
    public void setValue(Map<String,NTValue> map) {
        this.map = map;
    }
    
    public boolean canCall() {
        return caller != null;//type == ValueType.CALLABLE 
    }
    
    public int call(NTVM vm) throws RunningException {
        if (!canCall()) throw new RunningException("cannot be callable!");
        return caller.call(vm);
    }
}
