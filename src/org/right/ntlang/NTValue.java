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
    public static final NTValue NIL = new NTValueNil();
    public static final NTValue TRUE = new NTValue(ValueType.TRUE);
    public static final NTValue FALSE = new NTValue(ValueType.FALSE);
    protected ValueType type;
    protected Object userdata = null;
    protected String str = null;
    protected Map<String,NTValue> map = null;
    protected double num = Double.NaN;
    protected NTCallable caller = null;
    
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
    public NTValue(Map<String,NTValue> c) {
        this(ValueType.MAP);
        map = c;
    }
    
    public NTValue(double n) {
        this(ValueType.NUM);
        setDouble(n);
    }
    
    public ValueType type() {
        return type;
    }
    
    
    // STRING
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
                return NIL;
            case FALSE:
                return FALSE;
            case TRUE:
                return TRUE;
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
    
    
    
    public void setValue(String str) {
        this.str = str;
    }
    
    public void setValue(Map<String,NTValue> map) {
        this.map = map;
    }
    
    // CALLABLE
    public void setCallable(NTCallable c) {
        caller = c;
    }
    
    public boolean canCall() {
        return caller != null;//type == ValueType.CALLABLE 
    }
    
    public boolean call(NTVM vm,int varNum) throws RunningException {
        if (!canCall()) throw new RunningException("cannot be callable!");
        return caller.call(vm,varNum);
    }
    
    // NUM
    public double toNum() {
        switch (type) {
            case NIL:
            case FALSE:
                return 0.0;
            case TRUE:
                return 1.0;
            case NUM:
                return num;
            case USERDATA:
                return userdata instanceof Number ? (Double)(Number)userdata : Double.NaN;
            default:
                return Double.NaN;
        }
    }
    public void setInt(int n) {
        num = n;
    }

    public void setInt(Integer n) {
        num = n;
        userdata = n;
    }
    
    public int toInt() throws RunningException {
           if (num != Double.NaN) return (int)num;
           if (userdata instanceof Number)
               return userdata;
           return Integer.MIN_VALUE;
    }
    
    public void setDouble(double n) {
        num = n;
    }

    public void setDouble(Double n) {
        num = n;
        userdata = n;
    }
    
    public double getDouble() {
        if (num != Double.NaN) return num;
        if (userdata instanceof Number)
            return userdata;
        return Double.NaN;
    }

    public void setFloat(float n) {
        num = n;
    }

    public void setFloat(Float n) {
        num = n;
        userdata = n;
    }
    
    public float toFloat() {
        if (num != Double.NaN) return  (float)num;
        if (userdata instanceof Number)
            return userdata;
        return Float.NaN;
    }
    
    public void setLong(long n) {
        num = n;
    }

    public void setLong(Long n) {
        num = n;
        userdata = n;
    }

    public long toLong() {
        if (num != Double.NaN) return  (long)num;
        if (userdata instanceof Number)
            return userdata;
        return Long.MIN_VALUE;
    }
    
    public void setShort(short n) {
        num = n;
    }

    public void setShort(Short n) {
        num = n;
        userdata = n;
    }

    public short toShort() {
        if (num != Double.NaN) return  (short)num;
        if (userdata instanceof Number)
            return userdata;
        return Short.MIN_VALUE;
    }
    
    public void setByte(byte n) {
        num = n;
    }

    public void setByte(Byte n) {
        num = n;
        userdata = n;
    }

    public byte toByte() {
        if (num != Double.NaN) return  (byte)num;
        if (userdata instanceof Number)
            return userdata;
        return Byte.MIN_VALUE;
    }
    
    // MAP
    // 原生数据类型的一些字段
//    private Map<String,NTValue> nilFields;
//    private Map<String,NTValue> trueFields;
//    private Map<String,NTValue> falseFields;
//    private Map<String,NTValue> numFields;
    public void setField(String name,NTValue field) throws RunningException {
        // 仅允许map及部分userdata使用此函数
        switch (type) {
            case MAP:
                if (map == null) throw new RunningException("[" + type.name() + "] this map is null!");
                map.put(name,field);
                break;
            case USERDATA:
                if (userdata == null)  throw new RunningException("[" + type.name() + "] this userdata is null!");
                if (userdata instanceof Map<String,NTValue>) 
                    ((Map<String,NTValue>)userdata).put(name,field);
                default:
                 throw new RunningException("[" + type.name() + "] cannot getField: " + name + " !");
        }
    }
    
    public NTValue getField(String name) {
        switch (type) {
            case NIL:
                return NIL.getField(name);
            case FALSE:
                return FALSE;
            case TRUE:
                return TRUE;
            case NUM:
                return NTValueNum.getField(name);
            case STRING:
                return NTValueString.getField(name);
            case CALLABLE:
                return NTValueCallable.getField(name);
            case MAP:
                return map.get(name);
            default:
                return userdata instanceof Map<String,NTValue> ? ((Map<String,NTValue>)userdata).get(name) : NIL;
        }
    }
    
    // USERDATA
    public Object getUserdata(){
        return userdata;
    }
    public void setUserdata(Object u){
        userdata = u;
    }
}
