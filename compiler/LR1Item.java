package com.compiler;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * LR(1)项
 * P166
 * @author kashin
 */
public class LR1Item extends Item {

    /** 向前看符号 */
    protected Set<Terminal> lookaheads;

    public LR1Item(Item item, Set<Terminal> lookForward) {
        this.pos = item.pos;
        this.production = item.production;
        this.lookaheads = lookForward;
    }

    public LR1Item(Production production, int pos, Set<Terminal> lookForward) {
        this.pos = pos;
        this.production = production;
        this.lookaheads = lookForward;
    }

    public LR1Item copy() {
        LR1Item result = new LR1Item(this.production, this.pos, new HashSet<>(this.lookaheads));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LR1Item lr1Item = (LR1Item) o;
        return Objects.equals(lookaheads, lr1Item.lookaheads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lookaheads);
    }

    @Override
    public String toString() {
        String result = production.head.toString();
        result += " -> ";
        for (int i=0; i<production.body.size(); i++) {
            if (i == pos) {
                result += "•";
            }
            result += production.body.get(i);
        }

        if (pos >= production.body.size()) {
            result += "•";
        }

        result += ",";
        result += this.lookaheads;

        return result;
    }
}
