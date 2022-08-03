package com.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文法
 * context-free grammar 上下文无关文法
 * @author kashin
 */
public class Grammar {

    /** 开始符号 */
    protected NonTerminal start;
    /** 产生式 */
    protected List<Production> productionList = new ArrayList<>();
    /** 所有符号 */
    protected Set<Token> tokens = new HashSet<>();


    /**
     * P140 FIRST(X)集合
     * @param tokens
     * @return
     */
    public Set<Terminal> first(List<Token> tokens) {

        // 是否全部包含ε
        boolean allContainsEpsilon = true;
        Set<Terminal> result = new HashSet<>();
        for (Token token : tokens) {

            Set<Terminal> firstSet = first(token);
            for (Terminal terminal : firstSet) {
                if (terminal.equals(Terminal.of("ε"))) {
                    continue;
                }
                result.add(terminal);
            }

            if (!firstSet.contains(Terminal.of("ε"))) {
                allContainsEpsilon = false;
                break;
            }
        }

        if (allContainsEpsilon) {
            result.add(Terminal.of("ε"));
        }

        return result;
    }

    /**
     * P140 FIRST(X)集合
     * @param token
     * @return
     */
    public Set<Terminal> first(Token token) {

        Set<Terminal> result = new HashSet<>();
        if (token instanceof Terminal) {
            result.add((Terminal) token);
            return result;
        }

        List<Production> productionList = getProduction((NonTerminal) token);
        for (Production production : productionList) {

            // X->ε
            if (production.body.size() == 1 && production.body.get(0).equals(Terminal.of("ε"))) {
                result.add(Terminal.of("ε"));
            }

            Set<Terminal> firstSet = first(production.body);
            result.addAll(firstSet);
        }

        return result;
    }

    /**
     * 获得所有head开头的产生式
     * get productions that start with head
     * @param head
     * @return
     */
    public List<Production> getProduction(NonTerminal head) {
        List<Production> result = new ArrayList<>();
        for (Production production : productionList) {
            if (production.head.equals(head)) {
                result.add(production);
            }
        }
        return result;
    }

}
