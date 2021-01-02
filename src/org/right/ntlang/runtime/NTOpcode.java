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
    LOAD_MAP_BY_SUBSCRIPT,
    STORE_MAP_BY_SUBSCRIPT,
    LOAD_VAR,
    STORE_VAR,
    LOAD_THIS,
    JUMP,
    LOOP,
    JUMP_IF_FALSE,
    LOGIC_AND,
    LOGIC_OR,
    RETURN, // 返回表达式
    POP, // 丢掉栈顶
    DUP, // 复制栈顶
    TMP, // 申请一个临时变量
    CALL, // 调用全局变量中函数
    CALL_FUN, // 调用栈中函数
    LOAD_COMMAND, // 加载宏命令(宏函数)入栈
    EMIT_COMMAND, // 调用宏命令(宏函数)
    END;

    public int getValue() {
        return this.ordinal();
    }

    public static NTOpcode forValue(int value) {
        return values()[value];
    }
}
