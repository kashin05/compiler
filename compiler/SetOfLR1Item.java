package com.compiler;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * LR(1)项集
 * @author kashin
 */
public class SetOfLR1Item extends SetOfItem {

    /** LR(1)项 */
    protected Set<LR1Item> lr1items = new HashSet<>();

    public SetOfLR1Item() {}

    public static SetOfLR1Item of(int id, LR1Item ... items) {
        SetOfLR1Item result = new SetOfLR1Item();
        result.id = id;
        for (LR1Item item : items) {
            result.lr1items.add(item);
        }
        return result;
    }

    public static SetOfLR1Item of(SetOfLR1Item setOfItem) {
        SetOfLR1Item result = new SetOfLR1Item();
        result.id = setOfItem.id;
        setOfItem.lr1items.forEach(lr1Item -> {
            result.lr1items.add(lr1Item.copy());
        });
        return result;
    }

    public static SetOfLR1Item of(SetOfItem lr0Set, Terminal lookForward) {
        SetOfLR1Item result = new SetOfLR1Item();
        result.id = lr0Set.id;
        result.buildLR1(lr0Set.items, lookForward);
        return result;
    }

    public void buildLR1(Set<Item> items, Terminal lookForward) {
        if (!lr1items.isEmpty()) {
            return;
        }

        if (lookForward == null) {
            throw new RuntimeException("向前看符号不能为空");
        }

        for (Item item : items) {
            Set<Terminal> lookForwards = new HashSet<>();
            lookForwards.add(lookForward);
            lr1items.add(new LR1Item(item, lookForwards));
        }
    }

    public void printLR1Items() {
        lr1items.forEach(item->{
            System.err.println(item);
        });
    }

    /**
     * LR1项集的闭包
     * @param grammar 文法
     */
    public void closureLR1(Grammar grammar) {

        boolean addNew;
        do {
            addNew = false;
            Set<LR1Item> allItems = new HashSet<>(lr1items);
            for (LR1Item lr1item : allItems) {

                // 点在产生式体的最右边
                if (lr1item.isDotAtRight()) {
                    continue;
                }

                // 判断是否终结符
                Token token = lr1item.production.getToken(lr1item.pos);
                if (token instanceof Terminal) {
                    continue;
                }

                Set<Terminal> first = new HashSet<>();
                if (lr1item.pos + 1 >= lr1item.production.body.size()) {
                    first.addAll(lr1item.lookaheads);
                } else {
                    List<Token> tokens = lr1item.production.body.subList(lr1item.pos+1, lr1item.production.body.size());
                    Set<Terminal> firstSymbols = grammar.first(tokens);
                    if (firstSymbols.contains(Terminal.of("ε"))) {
                        first.addAll(lr1item.lookaheads);
                    }
                    first.addAll(firstSymbols);
                }

                // 获取所有产生式
                List<Production> productionList = grammar.getProduction((NonTerminal) token);
                for (Production production : productionList) {
                    LR1Item lr1Item = new LR1Item(production, 0, first);
                    if (lr1items.contains(lr1Item)) {
                        continue;
                    }
                    lr1items.add(lr1Item);
                    addNew = true;
                }
            }
        } while (addNew);
    }

    @Override
    public String toString() {
        return "LR1SetOfItem{" + "id=" + id + ", " + lr1items + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetOfLR1Item that = (SetOfLR1Item) o;
        return Objects.equals(lr1items, that.lr1items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lr1items);
    }
}