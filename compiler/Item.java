package com.compiler;

import java.util.Objects;

/**
 * 项
 * A -> •XYZ (pos:0)
 * A -> X•YZ (pos:1)
 * A -> XY•Z (pos:2)
 * A -> XYZ• (pos:3)
 */
public class Item {

    /** 产生式 */
    protected Production production;
    /** 点的位置(0开始，产生式体的size结束) */
    protected int pos;

    /**
     * 点在产生式体的最右边
     * @return
     */
    public boolean isPosRight() {
        if (pos >= production.body.size()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return pos == item.pos && Objects.equals(production, item.production);
    }

    @Override
    public int hashCode() {
        return Objects.hash(production, pos);
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

        return result;
    }
}
