package org.right.ntlang;

import java.util.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;

public class NTValueNil extends NTValue {
     // NIL的fields
    private static Map<String,NTValue> fields = new HashMap<String,NTValue>();
    
    static {
        fields.put("toString", new NTValue(new NTCallable(){
                              @Override
                              public boolean call(NTVM vm,int varNum) throws RunningException {
                                  vm.popNumTimes(varNum);
                                  vm.s.push(new NTValue("nil"));
                                  return false;
                              }
       }));
       fields.put("toNum", new NTValue(new NTCallable(){
                           @Override
                           public boolean call(NTVM vm,int varNum) throws RunningException {
                               vm.popNumTimes(varNum);
                               vm.s.push(new NTValue(0));
                               return false;
                           }
       }));
       fields.put("toBoolean", new NTValue(new NTCallable(){
                           @Override
                           public boolean call(NTVM vm,int varNum) throws RunningException {
                               vm.popNumTimes(varNum);
                               vm.s.push(NTValue.FALSE);
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
    
    public NTValueNil() {
        super(NTValue.ValueType.NIL);
    }

    @Override
    public NTValue getField(String name) {
        return fields.get(name);
    }
    
    
}
