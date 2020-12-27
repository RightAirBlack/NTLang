package org.right.ntlang;
import java.util.*;
import org.right.ntlang.compile.*;
import org.right.ntlang.exception.*;
import org.right.ntlang.interfaces.*;
import static org.right.ntlang.compile.NTToken.TokenType;

public class Main {
    public static void main(String[] args) {
        NTVM vm = new NTVM(true,System.in,System.out);
        //NTParser p = new NTParser(null,"bbb.a = i?a+1^3:\"hh\\nhh\";tmp a = bbb[\"a\",1.2,3000.0]; /*vvvv*/");
        StringBuffer str = new StringBuffer();
        Scanner s = new Scanner(System.in);
        System.out.println("**********欢迎使用MOYU表达式语言************\t");
        loadVar(vm);
        System.out.println("*************请输入表达式*************\t");
        NTParser p = new NTParser("cli",str);
        vm.setParser(p);
        int oldLength;
        while (true) {
            
            System.out.print(">> ");
            oldLength = str.length();
            str.append(s.nextLine()).append(";");
            if (str.toString().compareTo("exit") == 0) System.exit(0);
            try {
                p.initCurChar(oldLength);
                vm.compile();
                vm.call();
                vm.addLastCommandEndPtr(str.length() - oldLength + 1);
//                System.out.println("SOURCE CODE: " + vm.getParser().sourceCode + "\f");
                System.out.flush();
                System.out.print("<< ");
                System.out.println(vm.getCurFn().getThis());
            } catch (NTException e) {
                System.out.flush();
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            vm.cu.fn.resetInstrStream();
            // str = str.delete(0,str.length());
        }
    
        /*
        while (p.curToken.type != TokenType.EOF) {
            try {
                p.getNextToken();
                System.out.println("[" + p.curToken.type.name() + "]  \t" + p.sourceCode.substring(p.curToken.start,p.curToken.start + p.curToken.length) + "\t  :  \t" + p.curToken.value);
            }
            catch (NTException e) {
                System.out.println(e.getMessage());
            }
        }*/
    }

    private static void loadVar(NTVM vm) {
        vm.getVars().put("a", new NTValue(23));
        Map<String,NTValue> m = new HashMap<>();
        m.put("a", new NTValueNum(114514));
        vm.getVars().put("m", new NTValue(m));
        vm.getVars().put("out", new NTValue(new NTCallable() {
                            @Override
                            public boolean call(NTVM vm, int varNum) throws RunningException {
                                NTValue[] vars = vm.popNumTimes(varNum);
                                if (vars.length == 1) {
                                    System.out.println(vars[0].toString());
                                    return false;
                                }
                                System.out.println(String.format(vars[vars.length - 1].toString(), Arrays.copyOfRange(vars, 0, vars.length - 2)));
                                return false;
                            }              
        }));
    }
}
