package org.right.ntlang;
import static org.right.ntlang.compile.NTToken.TokenType;

import org.right.ntlang.exception.*;
import org.right.ntlang.compile.*;
import java.util.*;
public class Main
{
    public static void main(String[] args) {
        NTVM vm = new NTVM();
        //NTParser p = new NTParser(null,"bbb.a = i?a+1^3:\"hh\\nhh\";tmp a = bbb[\"a\",1.2,3000.0]; /*vvvv*/");
        StringBuffer str = new StringBuffer();
        Scanner s = new Scanner(System.in);
            System.out.println("**********欢迎使用MOYU表达式语言************\t");
        vm.vars.put("a",new NTValue(23));
        Map<String,NTValue> m = new HashMap<>();
        m.put("a",new NTValueNum(114514));
        vm.vars.put("m",new NTValue(m));
        while (true) {
            
            System.out.println("*************请输入表达式*************\t");
            str.append(s.nextLine());
            if (str.toString().compareTo("exit") == 0) return;
            NTParser p = new NTParser(null,str.append(';').toString());
            vm.setParser(p);
            try {
                vm.compile();
                vm.call();
                System.out.println("SOURCE CODE: " + vm.getParser().sourceCode + "\f");
                System.out.println(vm.getThis());
            } catch (LexException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            str = new StringBuffer();
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
}
