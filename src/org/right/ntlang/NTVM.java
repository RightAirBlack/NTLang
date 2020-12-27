package org.right.ntlang;
import java.util.*;
import java.util.concurrent.*;
import org.right.ntlang.compile.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;
import org.right.ntlang.runtime.*;
import java.io.*;

public class NTVM implements Callable {

    
    // 虚拟机返回值
    public static enum VMResult {
        SUCCESS,ERROR;
    }
    // 当前编译单元
    NTCompileUnit cu;
    // 如果此虚拟机用于命令行
    public final boolean isCli;
    public int lastCommandEndPtr = -1;
    // 当前词法解析器
    private NTParser curParser;
    // 当前指令流
    private NTFn curFn;
    // 常量表
    public  Vector<NTValue> constantVars_IV;
    public  Map<String,Integer> constantVars_VI;
    // 标准输入输出
    private PrintStream out;
    private InputStream in;
    // "栈"
    public Stack<NTValue> s = new Stack<NTValue>();
    // "全局"变量
    private Map<String,NTValue> vars = new HashMap<String,NTValue>(){{
            // 内置运算符
            put("- nud", new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = vm.s.peek();
                            v.getField("- nud").call(vm,varNum);
                            return false;
                        }              
            }));
            addConstant(new NTValue("- nud"));
            put("+ nud", new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = vm.s.peek();
                            v.getField("+ nud").call(vm,varNum);
                            return false;
                        }              
                    }));
            addConstant(new NTValue("+ nud"));
            put("+",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = vm.s.peek();
                            v.getField("+").call(vm,varNum);
                            return false;
                        }
             }));
            addConstant(new NTValue("+"));
            put("-",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = vm.s.peek();
                            v.getField("-").call(vm,varNum);
                            return false;
                        }
                    }));
            addConstant(new NTValue("-"));
            put("*",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = vm.s.peek();
                            v.getField("*").call(vm,varNum);
                            return false;
                        }
                    }));
            addConstant(new NTValue("*"));
            put("/",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = vm.s.peek();
                            v.getField("/").call(vm,varNum);
                            return false;
                        }
                    }));
            addConstant(new NTValue("/"));
            put("%",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = vm.s.peek();
                            v.getField("%").call(vm,varNum);
                            return false;
                        }
                    }));
            addConstant(new NTValue("%"));
            put("**",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = vm.s.peek();
                            v.getField("**").call(vm,varNum);
                            return false;
                        }
                    }));
           addConstant(new NTValue("**"));
           put("__mapNew",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = new NTValue(new HashMap<String,NTValue>());
                            vm.s.push(v);
                            return false;
                        }
                    }));
           addConstant(new NTValue("__mapNew"));
           put("__mapAdd",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue[] vars = popNumTimes(varNum);
                            vars[2].setField(vars[1].toString(),vars[0]);
                            return false;
                        }
                    }));
           addConstant(new NTValue("__mapAdd"));
           put("__arrayNew",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue v = new NTValue(new Vector<NTValue>());
                            vm.s.push(v);
                            return false;
                        }
                    }));
           addConstant(new NTValue("__arrayNew"));
           put("__arraySet",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue[] vars = popNumTimes(varNum);
                            vars[2].setElement(vars[1].toInt(),vars[0]);
                            return false;
                        }
                    }));
           addConstant(new NTValue("__arraySet"));
            put("__arrayAdd",    new NTValue(new NTCallable() {
                        @Override
                        public boolean call(NTVM vm,int varNum) throws RunningException {
                            NTValue[] vars = popNumTimes(varNum);
                            vars[1].addElement(vars[0]);
                            return false;
                        }
                    }));
            addConstant(new NTValue("__arrayAdd"));
            
            
    }};
    // 宏命令库
    private Map<String,NTCallable> commandLib;
    
    public NTVM() {
        this(false);
    }
    public NTVM(InputStream in,PrintStream out) {
        this(false,in,out);
    }
    public NTVM(boolean isCli) {
        this(isCli,null,null);
    }
    public NTVM(boolean isCli,InputStream in,PrintStream out) {
        this.isCli = isCli;
        this.in = in;
        this.out = out;
    }

    // 添加一个常量并加入变量表中(????)
    public  int addConstant(NTValue v) {
        if (constantVars_IV == null)
            constantVars_IV = new Vector<>(1024);
        if (constantVars_VI == null)
            constantVars_VI = new HashMap<>(1024);
        // 已存在此常量则返回已存在的
        if (constantVars_VI.containsKey(v)) 
            return constantVars_VI.get(v);
        // 否则增加此常量
        constantVars_IV.add(v);
        constantVars_VI.put(v.toString(),constantVars_IV.size() - 1);
        return constantVars_IV.size() - 1;
    }
    
    public void setParser(NTParser p) {
        curParser = p;
        p.setVM(this);
    }
    public NTParser getParser() {
        return curParser;
    }
    
    public void setCurFn(NTFn curFn) {
        this.curFn = curFn;
    }
    public NTFn getCurFn() {
        return curFn;
    }
    public void setVars(Map<String, NTValue> vars) {
        this.vars = vars;
    }
    public Map<String, NTValue> getVars() {
        return vars;
    }
    // commandLib
    public void setCommandLib(Map<String, NTCallable> commandLib) {
        this.commandLib = commandLib;
    }
    public Map<String, NTCallable> getCommandLib() {
        return commandLib;
    }
    public NTCallable addCommand(String name,NTCallable command) {
        return commandLib.put(name,command);
    }
    public NTCallable getCommand(String name) {
        return commandLib.get(name);
    }
    public void setIn(InputStream in) {
        this.in = in;
    }

    public InputStream getIn() {
        return in;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public PrintStream getOut() {
        return out;
    }
    /*********一些方法********/
    public void addLastCommandEndPtr(int length) {
        lastCommandEndPtr += length;
        curParser.fixNextCharPtr(1);
    }
    public void compile() throws LexException, CompileException {
        if (!isCli || cu == null)
             cu = new NTCompileUnit(curParser);
        
//        if (lastCommandEndPtr >= 0)
//            curParser.initCurChar(lastCommandEndPtr);
        curParser.getNextToken();
        while (!curParser.matchToken(NTToken.TokenType.EOF)) {
           cu.compileProgram();
        }     
    }
    // 编译完后调用vm.call()，并以递归的方式调用当前编译单元的.call()
    @Override
    public VMResult call() throws Exception {
        this.curFn = this.curParser.curCompileUnit.fn;
        VMResult result = curParser.curCompileUnit.call();
        return result;
    }
    // 用于跳过函数或方法中"不要"的实参，并返回(逆序)
    public NTValue[] popNumTimes(int times) {
        // 最多支持16个形参
        NTValue[] values = new NTValue[times];
        for (int i = 0;i < times;i++)
            values[i] = s.pop();
        return values;
    }
    
    /*********DEBUG***********/
    public static void dumpStack(Stack<NTValue> s) {
        System.out.println("dumpStack: " + s);
    }
}
