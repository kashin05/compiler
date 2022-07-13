package com.compiler;

/**
 * 非终结符号
 */
public class NonTerminal extends Token {

    private NonTerminal(){}

    public static NonTerminal of(String name) {
        NonTerminal result = new NonTerminal();
        result.name = name;
        return result;
    }
}
