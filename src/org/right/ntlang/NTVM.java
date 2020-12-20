package org.right.ntlang;
import java.util.*;
import java.util.concurrent.*;
import org.right.ntlang.compile.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;

public class NTVM implements Callable
{
    // 常量表
    private int constantCount = 0;
    public  Map<Integer,NTValue> constantVars_IV;
    public  Map<String,Integer> constantVars_SI;
    // 添加一个常量并加入变量表中(????)
    public  int addConstant(NTValue v) {
        if (constantVars_IV == null)
            constantVars_IV = new HashMap<>();
        if (constantVars_SI == null)
            constantVars_SI = new HashMap<>();
        if (constantVars_SI.containsKey(v)) 
            return constantVars_SI.get(v);
        constantVars_IV.put(constantCount,v);
        constantVars_SI.put(v.toString(),constantCount);
        return constantCount++;
    }
    public Stack<NTValue> s = new Stack<NTValue>();
    public Map<String,NTValue> vars = new HashMap<String,NTValue>(){{
            put("- nud", new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            NTValue v = vm.s.pop();
                            vm.s.push(new NTValue(-((double)(Double)v.getValue())));
                            return 0;
                        }              
            }));
            addConstant(new NTValue("- nud"));
            put("+ nud", new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            NTValue v = vm.s.pop();
                            vm.s.push(new NTValue(-((double)(Double)v.getValue())));
                            return 0;
                        }              
                    }));
            addConstant(new NTValue("+ nud"));
            put("+",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            // dumpStack(s);
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() + (double)(Double)b.getValue()));
                            return 0;
                        }
             }));
            addConstant(new NTValue("+"));
            put("-",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() - (double)(Double)b.getValue()));                      
                            return 0;
                        }
                    }));
            addConstant(new NTValue("-"));
            put("*",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() * (double)(Double)b.getValue()));                       
                            return 0;
                        }
                    }));
            addConstant(new NTValue("*"));
            put("/",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() / (double)(Double)b.getValue()));                        
                            return 0;
                        }
                    }));
            addConstant(new NTValue("/"));
            put("%",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() % (double)(Double)b.getValue()));                        
                            return 0;
                        }
                    }));
            addConstant(new NTValue("%"));
            put("**",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue(Math.pow( (double)(Double)a.getValue() , (double)(Double)b.getValue()) ));                        
                            return 0;
                        }
                    }));
           addConstant(new NTValue("**"));
    }};
    
 
    // 虚拟机返回值
    public static enum VMResult {
        SUCCESS,ERROR;
    }
    // 编译完后调用vm.call()，并以尾递归的方式调用当前编译单元的.call()
    @Override
    public VMResult call() throws Exception
    {
        return curParser.curCompileUnit.call();
//        return VMResult.ERROR;
//        System.out.println(s.peek());
//        return VMResult.SUCCESS;
    }
    
    private NTParser curParser;
    public void setParser(NTParser p) {
        curParser = p;
        p.setVM(this);
    }
    public NTParser getParser() {
        return curParser;
    }
    
    public void compile() throws LexException, CompileException {
        NTCompileUnit cu = new NTCompileUnit(curParser);
        curParser.getNextToken();
        while (!curParser.matchToken(NTToken.TokenType.EOF)) {
           cu.compileProgram();
        }
        
        
    }

    
    
    /*********DEBUG***********/
    public static void dumpStack(Stack<NTValue> s) {
        System.out.println("dumpStack: " + s);
    }
}
