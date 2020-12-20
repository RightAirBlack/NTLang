package org.right.ntlang;

import java.util.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;

public class NTValueNum extends NTValue {
     // Num的fields
    private static Map<String,NTValue> fields = new HashMap<String,NTValue>();
    
    static {
        fields.put("toString", new NTValue(new NTCallable(){
                              @Override
                              public boolean call(NTVM vm,int varNum) throws RunningException {
                                  NTValue n = vm.s.pop();
                                  vm.popNumTimes(varNum);
                                  vm.s.push(new NTValue(n.toString()));
                                  return false;
                              }
       }));
       fields.put("toBoolean", new NTValue(new NTCallable(){
                           @Override
                           public boolean call(NTVM vm,int varNum) throws RunningException {
                               NTValue n = vm.s.pop();
                               vm.popNumTimes(varNum);
                               if (n.getDouble() == 0 || n.getDouble() == Double.NaN || !(n.getUserdata() instanceof Number) )
                                   vm.s.push(NTValue.FALSE);
                               vm.s.push(NTValue.TRUE);
                               return false;
                           }
        }));
//        fields.put("", new NTValue(new NTCallable(){
//                           @Override
//                           public boolean call(NTVM vm,int varNum) throws RunningException {
//                               vm.popNumTimes(varNum);
//                               vm.s.push(new NTValue("nil"));
//                               return false;
//                           }
//        }));          
    }
    
    public NTValueNum(double num) {
        super(num);
    }

    public static NTValue getField(String name) {
        return fields.get(name);
    }
    
    
}
