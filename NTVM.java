package org.right.ntlang;
import java.util.*;
import java.util.concurrent.*;
import org.right.ntlang.compile.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;

public class NTVM implements Callable
{

    
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
            put("+ nud", new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            // NTValue v = vm.s.pop();
                            // vm.s.push(new NTValue(-((double)(Double)v.getValue())));
                            return 0;
                        }              
                    }));
            put("+",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() + (double)(Double)b.getValue()));
                            return 0;
                        }
             }));
            put("-",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() - (double)(Double)b.getValue()));                      
                            return 0;
                        }
                    }));
            put("*",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() * (double)(Double)b.getValue()));                       
                            return 0;
                        }
                    }));
            put("/",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() / (double)(Double)b.getValue()));                        
                            return 0;
                        }
                    }));
            put("%",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue((double)(Double)a.getValue() % (double)(Double)b.getValue()));                        
                            return 0;
                        }
                    }));
            put("**",    new NTValue(new NTCallable() {
                        @Override
                        public int call(NTVM vm) throws RunningException {
                            Stack<NTValue> s = vm.s;
                            NTValue b = s.pop(),a = s.pop();
                            s.push(new NTValue(Math.pow( (double)(Double)a.getValue() , (double)(Double)b.getValue()) ));                        
                            return 0;
                        }
                    }));
    }};
    public static enum VMResult {
        SUCCESS,ERROR;
    }
    @Override
    public VMResult call() throws Exception
    {
        //return curParser.curCompileUnit.call();
        //return VMResult.ERROR;
        System.out.println(s.peek());
        return VMResult.SUCCESS;
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

    
    
    /********************/
}
