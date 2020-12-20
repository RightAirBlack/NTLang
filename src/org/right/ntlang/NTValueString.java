package org.right.ntlang;

import java.util.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;

public class NTValueString extends NTValue {
     // Num的fields
    private static Map<String,NTValue> fields = new HashMap<String,NTValue>();
    
    static {
        fields.put("toNum", new NTValue(new NTCallable(){
                              @Override
                              public boolean call(NTVM vm,int varNum) throws RunningException {
                                  NTValue n = vm.s.pop();// 此时栈顶是a.b(c)中的a
                                  vm.popNumTimes(varNum);// 跳过c
                                  double t = Double.valueOf(n.toString());
                                  vm.s.push(new NTValueNum(t));
                                  return false;
                              }
       }));
       fields.put("toBoolean", new NTValue(new NTCallable(){
                           @Override
                           public boolean call(NTVM vm,int varNum) throws RunningException {
                               NTValue n = vm.s.pop();
                               vm.popNumTimes(varNum);
                               if (n.toString().compareTo("") == 0)
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
    
    public NTValueString(String str) {
        super(str);
    }

    public static NTValue getField(String name) {
        return fields.get(name);
    }
    
    
}
