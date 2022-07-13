package com.compiler;

/**
 * 终结符号
 */
public class Terminal extends Token {

    private Terminal(){}

    public static Terminal of(String name) {
        Terminal result = new Terminal();
        result.name = name;
        return result;
    }

}
