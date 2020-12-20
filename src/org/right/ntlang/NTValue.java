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
    
    //一些常量
    public static final NTValue NaN = new NTValue(Double.NaN);
    public static final NTValue NIL = new NTValue(ValueType.NIL);
    public static final NTValue TRUE = new NTValue(ValueType.TRUE);
    public static final NTValue FALSE = new NTValue(ValueType.FALSE);
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
    public NTValue(String c) {
        this(ValueType.STRING);
        str = c;
    }
    public NTValue(double n) {
        this(ValueType.NUM);
        setValue(n);
    }
    
    public ValueType type() {
        return type;
    }
    
    public int toInt() throws RunningException {
        switch (type) {
            case NUM:
                return  (int)num;
            default:
                throw new RunningException("[" + type.name() + " " + this.toString() + "] cannot call method toInt()!");
        }
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

    @Override
    public int hashCode() {
        switch (type) {
            case NIL:
                return "nil".hashCode();
            case FALSE:
                return "false".hashCode();
            case TRUE:
                return "true".hashCode();
            case NUM:
                return new Double(num).hashCode();
            case STRING:
                return str.hashCode();
            case CALLABLE:
                return caller.hashCode();
            case MAP:
                return map.hashCode();
            default:
                return userdata.hashCode();
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
    
//    public void setValue(boolean b) {
//        if (type != ValueType.NIL || type != ValueType.FALSE || type != ValueType.TRUE)
//            return;
//        type = b ? ValueType.TRUE : ValueType.FALSE;
//        num = b ? 1.0 : 0.0;
//    }
//    
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
