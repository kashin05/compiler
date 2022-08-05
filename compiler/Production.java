package com.compiler;

import java.util.List;
import java.util.Objects;

/**
 * 产生式
 * @author kashin
 */
public class Production {

    protected NonTerminal head;
    protected List<Token> body;

    public Token getToken(int idx) {
        return body.get(idx);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Production that = (Production) o;
        return Objects.equals(head, that.head) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, body);
    }

    @Override
    public String toString() {
        String bodyStr = "";
        for (Token token : body) {
            bodyStr += token;
        }
        return head + " -> " + body;
    }
}
