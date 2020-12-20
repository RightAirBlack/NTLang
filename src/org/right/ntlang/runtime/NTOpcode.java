package org.right.ntlang.runtime;

// 指令类型
public enum NTOpcode {

    LOAD_CONSTANT,
    PUSH_NIL,
    PUSH_FALSE,
    PUSH_TRUE,
    LOAD_TMP,
    STORE_TMP,
    LOAD_MAP,
    STORE_MAP,
    LOAD_VAR,
    STORE_VAR,
    JUMP,
    LOOP,
    JUMP_IF_FALSE,
    AND,
    OR,
    RETURN,
    POP,
    MOV, // 赋值操作
    TMP, // 申请一个临时变量
    CALL, // 调用函数
    CALL_METHOD, // 
    END; // 伪代码，仅表示文件结尾

    public int getValue() {
        return this.ordinal();
    }

    public static NTOpcode forValue(int value) {
        return values()[value];
    }
}
