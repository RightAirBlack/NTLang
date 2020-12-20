package org.right.ntlang;

import java.util.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;

public class NTValueCallable extends NTValue {
     // Callable的fields
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
                               if (n == null || !n.canCall())
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
    
    public NTValueCallable(NTCallable num) {
        super(num);
    }

    public static NTValue getField(String name) {
        return fields.get(name);
    }
    
    
}
