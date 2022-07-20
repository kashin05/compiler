package com.compiler;

import java.util.*;

/**
 * 项集
 */
public class SetOfItem {

    /** 项集的编号 */
    protected int id;
    /** 项 */
    protected Set<Item> items = new HashSet<>();

    /**
     * 删除非内核项
     * @param start symbol 开始符号
     */
    public void delNonkernel(NonTerminal start) {
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            // 点在最左端的所有项 dots are not at the left end
            if (item.pos != 0) {
                continue;
            }
            // E'->•E除外
            if (item.production.head.equals(start)) {
                continue;
            }
            iterator.remove();
        }

    }

    /**
     * 项集的闭包
     * P156 Closure的计算
     * @param grammar 文法
     */
    public void closure(Grammar grammar) {
        boolean addNew;
        do {
            addNew = false;

            Set<Item> allItems = new HashSet<>(items);
            for (Item item : allItems) {

                // 点在产生式体的最右边
                if (item.isDotAtRight()) {
                    continue;
                }

                // 判断是否终结符
                Token token = item.production.getToken(item.pos);
                if (token instanceof Terminal) {
                    continue;
                }

                // 获取所有产生式
                List<Production> productionList = grammar.getProduction((NonTerminal) token);

                for (Production production : productionList) {

                    Item newItem = new Item();
                    newItem.production = production;
                    newItem.pos = 0;

                    if (items.contains(newItem)) {
                        continue;
                    }

                    items.add(newItem);
                    addNew = true;
                }
            }
        } while (addNew);
    }


    /**
     * GOTO
     * p156
     * @param grammar
     * @param token 文法符号
     * @return
     */
    public SetOfItem gotoSetOfItem(Grammar grammar, Token token) {

        System.err.println("计算GOTO " + this + "     " + token);

        SetOfItem result = new SetOfItem();
        for (Item item : items) {

            if (item.isDotAtRight()) {
                continue;
            }

            Token token1 = item.production.getToken(item.pos);
            if (token1.equals(token)) {
                Item item1 = new Item();
                item1.pos = item.pos+1;
                item1.production = item.production;
                result.items.add(item1);
            }
        }

        result.closure(grammar);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetOfItem item = (SetOfItem) o;
        return Objects.equals(items, item.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        return "SetOfItem{" + "id=" + id + ", " + items + '}';
    }
}
