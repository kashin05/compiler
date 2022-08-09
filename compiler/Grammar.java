package com.compiler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文法
 * context-free grammar 上下文无关文法
 * @author kashin
 */
public class Grammar {

    private final AtomicInteger idGen = new AtomicInteger();

    /** 开始符号 */
    protected NonTerminal start;
    /** 产生式 */
    protected List<Production> productionList = new ArrayList<>();
    /** 所有符号 */
    protected Set<Token> tokens = new HashSet<>();

    /** LR0项集族 */
    protected Set<SetOfItem> setOfItemsLR0 = new HashSet<>();
    /** LR1项集族 */
    protected Set<LR1SetOfItem> setOfItemsLR1 = new HashSet<>();

    /** 项集的GOTO关系 */
    protected Map<Integer, Map<Token, Integer>> itemGoToItem = new HashMap<>();

    /**
     * LR0 项集族计算
     * P157 规范LR(0)项集族的计算
     * The canonical collection of sets of LR(0) items for an augmented grammar
     */
    public void buildLR0SetOfItem() {
        // 初始化第一个项集
        SetOfItem first = new SetOfItem();
        first.id = nextId();
        first.items = new HashSet<>();
        List<Production> productionList = getProduction(start);
        for (Production production : productionList) {
            Item item = new Item();
            item.pos = 0;
            item.production = production;
            first.items.add(item);
        }
        first.closure(this);

        setOfItemsLR0.add(first);

        boolean newAdd;
        do {
            newAdd = false;

            Set<SetOfItem> allItemsLR0 = new HashSet<>(setOfItemsLR0);
            for (SetOfItem setOfItem : allItemsLR0) {
                for (Token token : tokens) {
                    SetOfItem setOfItem1 = setOfItem.gotoSetOfItem(this, token);
                    // GOTO集合为空
                    if (setOfItem1.items.isEmpty()) {
                        continue;
                    }

                    // 判断GOTO集合是否已经存在
                    SetOfItem old = null;
                    for (SetOfItem item : setOfItemsLR0) {
                        if (item.equals(setOfItem1)) {
                            old = item;
                            break;
                        }
                    }

                    SetOfItem gotoTarget;
                    if (old != null) {// 已经有相同的项集
                        gotoTarget = old;
                    } else {
                        setOfItem1.id = nextId();
                        setOfItemsLR0.add(setOfItem1);
                        newAdd = true;
                        gotoTarget = setOfItem1;
                    }

                    // 记录GOTO关系
                    Map<Token, Integer> gotoSet = itemGoToItem.computeIfAbsent(setOfItem.id, id-> new HashMap<>());
                    gotoSet.put(token, gotoTarget.id);
                }
            }
        } while (newAdd);

    }

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

        // X 是一个终结符号
        // if X is a terminal, then FIRST(X) = {X}
        if (token instanceof Terminal) {
            result.add((Terminal) token);
            return result;
        }

        List<Production> productionList = getProduction((NonTerminal) token);
        for (Production production : productionList) {

            // X->ε
            if (production.body.size() == 1 && production.body.get(0).equals(Terminal.of("ε"))) {
                result.add(Terminal.of("ε"));
            } else {
                Set<Terminal> firstSet = first(production.body);
                result.addAll(firstSet);
            }
        }

        return result;
    }

    public List<Production> getProduction(NonTerminal head) {
        List<Production> result = new ArrayList<>();
        for (Production production : productionList) {
            if (production.head.equals(head)) {
                result.add(production);
            }
        }
        return result;
    }

    public int nextId() {
        return idGen.incrementAndGet();
    }
}
