package com.compiler;

import com.compiler.model.Lookaheads;

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
    protected Map<String, Token> tokens = new HashMap<>();

    /** LR0项集族 */
    protected Set<SetOfItem> setOfItemsLR0 = new HashSet<>();
    /** LR1项集族 */
    protected Set<SetOfLR1Item> setOfItemsLR1 = new HashSet<>();

    private Map<Integer, SetOfLR1Item> id2itemsLR1 = new HashMap<>();

    /** 项集的GOTO关系 */
    protected Map<Integer, Map<Token, Integer>> itemGoToItem = new HashMap<>();

    /** 语法分析表 LR Parsing Table */
    protected Map<String, Action> parsingTable = new HashMap<>();

    public static void main(String[] args) {
        Grammar grammar = buildGrammarForExample4_45();
        grammar.algorithm_4_63();
        grammar.buildParsingTable();
        grammar.printParsingTable();
        // id * id + id
        grammar.LR_parsing(new LinkedList<>(Arrays.asList(Terminal.of("id"), Terminal.of("*"), Terminal.of("id"), Terminal.of("+"), Terminal.of("id"), Terminal.of("$"))));
    }



    /**
     * 例4.45
     * @return
     */
    private static Grammar buildGrammarForExample4_45() {
        Grammar grammar = new Grammar();
        grammar.start = NonTerminal.of("S'");
        grammar.add(NonTerminal.of("S'"), Arrays.asList(NonTerminal.of("S")));
        grammar.add(NonTerminal.of("S"), Arrays.asList(NonTerminal.of("S"), Terminal.of("+"), NonTerminal.of("T")));
        grammar.add(NonTerminal.of("S"), Arrays.asList(NonTerminal.of("T")));
        grammar.add(NonTerminal.of("T"), Arrays.asList(NonTerminal.of("T"), Terminal.of("*"), NonTerminal.of("F")));
        grammar.add(NonTerminal.of("T"), Arrays.asList(NonTerminal.of("F")));
        grammar.add(NonTerminal.of("F"), Arrays.asList(Terminal.of("("),NonTerminal.of("S"),Terminal.of(")")));
        grammar.add(NonTerminal.of("F"), Arrays.asList(Terminal.of("id")));
        grammar.tokens.put("$", Terminal.of("$"));

        return grammar;
    }

    /**
     * 构建例4.61增广文法 P173
     * @return
     */
    private static Grammar buildGrammarForExample4_61() {
        Grammar grammar = new Grammar();
        grammar.start = NonTerminal.of("S'");
        grammar.add(NonTerminal.of("S'"), Arrays.asList(NonTerminal.of("S")));
        grammar.add(NonTerminal.of("S"), Arrays.asList(NonTerminal.of("L"), Terminal.of("="), NonTerminal.of("R")));
        grammar.add(NonTerminal.of("S"), Arrays.asList(NonTerminal.of("R")));
        grammar.add(NonTerminal.of("L"), Arrays.asList(Terminal.of("*"), NonTerminal.of("R")));
        grammar.add(NonTerminal.of("L"), Arrays.asList(Terminal.of("id")));
        grammar.add(NonTerminal.of("R"), Arrays.asList(NonTerminal.of("L")));
        grammar.tokens.put("$", Terminal.of("$"));
        return grammar;
    }

    private static Grammar buildGrammarForExample4_55() {
        Grammar grammar = new Grammar();
        grammar.start = NonTerminal.of("S'");
        grammar.add(NonTerminal.of("S'"), Arrays.asList(NonTerminal.of("S")));
        grammar.add(NonTerminal.of("S"), Arrays.asList(NonTerminal.of("C"), NonTerminal.of("C")));
        grammar.add(NonTerminal.of("C"), Arrays.asList(Terminal.of("c"), NonTerminal.of("C")));
        grammar.add(NonTerminal.of("C"), Arrays.asList(Terminal.of("d")));
        grammar.tokens.put("$", Terminal.of("$"));
        return grammar;
    }


    public void putAction(int setId, Token symbol, Action action) {
        parsingTable.put(setId + "_" + symbol, action);
    }

    public Action getAction(int setId, Token symbol) {
        return parsingTable.get(setId + "_" + symbol);
    }

    public static final int colSize = 8;

    /**
     * 打印语法分析表
     */
    public void printParsingTable() {

        List<Integer> stateList = new ArrayList<>();
        List<Token> symbolList = new ArrayList<>();

        for (Map.Entry<String, Action> tableEntry : parsingTable.entrySet()) {
            String idSymbol = tableEntry.getKey();
            Integer state = Integer.valueOf(idSymbol.split("_")[0]);
            Token symbol = tokens.get(idSymbol.split("_")[1]);
            Action action = tableEntry.getValue();

            if (!stateList.contains(state)) {
                stateList.add(state);
            }
            if (!symbolList.contains(symbol)) {
                symbolList.add(symbol);
            }
        }

        Collections.sort(symbolList, (a, b)->{

            if (a.name.equals(b.name)) {
                return 0;
            }

            if (a instanceof Terminal) {
                if (b instanceof Terminal) {
                    return a.name.compareTo(b.name);
                }
                if (b instanceof NonTerminal) {
                    return -1;
                }
            }
            if (a instanceof NonTerminal) {
                if (b instanceof Terminal) {
                    return 1;
                }
                if (b instanceof NonTerminal) {
                    return a.name.compareTo(b.name);
                }
            }

            throw new RuntimeException("symbol illegal" + a + " = "+ b);
        });

        LogUtil.logNln("", 2);
        LogUtil.logNln(" ");
        for (Token token : symbolList) {
            LogUtil.logNln(token.toString(), colSize);
        }
        LogUtil.log("");
        for (Integer state : stateList) {
            LogUtil.logNln(state.toString(), 2);
            LogUtil.logNln(" ");

            for (Token token : symbolList) {
                Action action = getAction(state, token);
                if (action == null) {
                    LogUtil.logNln("", colSize);
                } else {
                    LogUtil.logNln(action.type.name(), colSize);
                }
            }

            LogUtil.log("");
        }

        LogUtil.log("==|=======|=======|=======|");
    }

    /**
     * 算法4.56 p169
     * 构造规范LR语法分析表
     * Construction of canonical-LR parsing tables
     */
    public void buildParsingTable() {

        LogUtil.log("print LR(1) kernels");
        for (SetOfLR1Item setOfLR1Item : setOfItemsLR1) {
            LogUtil.log(setOfLR1Item.toString());
        }

        // 对LR(1)内核求闭包
        for (SetOfLR1Item setOfLR1Item : setOfItemsLR1) {
            setOfLR1Item.closureLR1(this);
        }

        LogUtil.log("print LR(1) after closure");
        for (SetOfLR1Item setOfLR1Item : setOfItemsLR1) {
            LogUtil.log(setOfLR1Item.toStringOrder());
        }

        // Action
        for (SetOfLR1Item setOfLR1Item : setOfItemsLR1) {
            for (LR1Item lr1Item : setOfLR1Item.lr1itemList) {
                if (lr1Item.isDotAtRight()) {
                    if (lr1Item.production.head.equals(this.start)) {// S'->S•
                        if (lr1Item.lookaheads.contains(Terminal.of("$"))) {
                            // 接受
                            putAction(setOfLR1Item.id, Terminal.of("$"), Action.of(ActionType.Accept));
                        }
                    } else {
                        for (Terminal lookahead : lr1Item.lookaheads) {
                            // 规约
                            putAction(setOfLR1Item.id, lookahead, Action.of(ActionType.Reduce, lr1Item.production));
                        }
                    }
                } else {
                    Token token = lr1Item.production.getToken(lr1Item.pos);
                    Integer gotoSetId = getGoto(setOfLR1Item.id, token);
                    if (token instanceof Terminal) {// 终结符
                        // 移入
                        putAction(setOfLR1Item.id, token, Action.of(ActionType.Shift, gotoSetId));
                    } else {
                        // GOTO
                        putAction(setOfLR1Item.id, token, Action.of(ActionType.Goto, gotoSetId));
                    }
                }
            }
        }


    }



    public void add(NonTerminal head, List<Token> body) {
        Production production = new Production();
        production.head = head;
        production.body = new ArrayList<>(body);
        productionList.add(production);

        tokens.put(head.name, head);
        for (Token token : body) {
            tokens.put(token.name, token);
        }
    }

    /**
     * LALR(1)项集族的内核的高效计算方法
     * LALR(1) collection of sets of items
     * P174 Efficient computation of the kernels of the LALR(1) collection of sets of items
     */
    public void algorithm_4_63() {
        // Construct the kernels of the sets of LR(0) items for G
        buildLR0SetOfItem();
        // remove the nonkernel items
        removeNonKernelFormLR0SetOfItems();

        // determine which lookaheads are spontaneously,
        // and which items in lookaheads are propagated

        initLR1SetOfItem();

        // the first pass
        List<SetOfLR1Item> lr1SetOfItems = new ArrayList<>();
        for (SetOfItem item : this.setOfItemsLR0) {
            SetOfLR1Item lr1SetOfItem = SetOfLR1Item.of(item, Terminal.of("#"));
            lr1SetOfItems.add(SetOfLR1Item.of(lr1SetOfItem));
        }

        List<Lookaheads> lookaheadsList = determiningLookaheads(lr1SetOfItems);
        updateLR1ItemLookaheadOfSpontaneous(lookaheadsList);

        // the spontaneous lookahead $ for the initial item S'->•S
        BreakForSetOfLR1Item:
        for (SetOfLR1Item lr1SetOfItem : getSetOfItemsLR1()) {
            LogUtil.logNln("");
            for (LR1Item lr1Item : lr1SetOfItem.lr1items) {
                if (lr1Item.production.head.equals(start) && lr1Item.pos == 0) {
                    Set<Terminal> lookForwards = new HashSet<>();
                    lookForwards.add(Terminal.of("$"));
                    lr1Item.lookaheads = lookForwards;
                    break BreakForSetOfLR1Item;
                }
            }
        }

        System.err.println("-----------------------第"+ 0 +"趟扫描，初始值");
        printLR1();

        int i = 1;
        boolean propagation;// 是否有新的向前看符号被传播
        do {
            int scanPass = i++;
            System.err.println("-----------------------第"+ scanPass +"趟扫描，确定传播的向前看符号");

            // 复制一份当前的LR1项集族，防止当前扫描的结果，又被用于这次扫描
            List<SetOfLR1Item> newSetOfItem = new ArrayList<>();
            for (SetOfLR1Item lr1SetOfItem : getSetOfItemsLR1()) {
                SetOfLR1Item lr1SetOfItemCopy = SetOfLR1Item.of(lr1SetOfItem);
                newSetOfItem.add(lr1SetOfItemCopy);
            }

            List<Lookaheads> lookaheads = determiningLookaheads(newSetOfItem);
            propagation = updateLR1ItemLookaheadOfPropagation(lookaheads);

            printLR1();
        } while (propagation);
    }

    /**
     * 更新传播的向前看符号
     * @param lookaheads
     * @return
     */
    private boolean updateLR1ItemLookaheadOfPropagation(List<Lookaheads> lookaheads) {
        boolean propagation = false;
        for (Lookaheads lookahead : lookaheads) {
            if (lookahead.propagation.isEmpty()) {
                continue;
            }

            SetOfLR1Item gotoSetOfItem = getLR1SetOfItem(lookahead.gotoSetOfItem.id);
            for (LR1Item lr1item : gotoSetOfItem.lr1items) {
                if (lr1item.production.equals(lookahead.gotoItem.production)) {
                    lr1item.lookaheads.addAll(lookahead.propagation);
                    propagation = true;
                }
            }
        }
        return propagation;
    }

    /**
     * 更新自发生的向前看符号
     * @param lookaheads
     * @return
     */
    private boolean updateLR1ItemLookaheadOfSpontaneous(List<Lookaheads> lookaheads) {
        boolean propagation = false;
        for (Lookaheads lookahead : lookaheads) {
            if (lookahead.spontaneous.isEmpty()) {
                continue;
            }

            SetOfLR1Item gotoSetOfItem = getLR1SetOfItem(lookahead.gotoSetOfItem.id);
            for (LR1Item lr1item : gotoSetOfItem.lr1items) {
                if (lr1item.production.equals(lookahead.gotoItem.production)) {
                    lr1item.lookaheads.addAll(lookahead.spontaneous);
                    propagation = true;
                }
            }
        }
        return propagation;
    }

    /**
     * 算法4.62
     * 确定向前看符号
     * @param setOfItems LR1项集族，每个LR1项集必须计算过闭包
     */
    public List<Lookaheads> determiningLookaheads(List<SetOfLR1Item> setOfItems) {

        List<Lookaheads> result = new ArrayList<>();

        for (SetOfLR1Item sourceSet : setOfItems) {// 遍历项集

            for (LR1Item sourceItem : sourceSet.lr1items) {// 遍历项

                SetOfLR1Item lr1SetOfItem = SetOfLR1Item.of(sourceSet.id, sourceItem.copy());

                lr1SetOfItem.closureLR1(this);

                for (LR1Item lr1Item : lr1SetOfItem.lr1items) {

                    if (lr1Item.isDotAtRight()) {
                        continue;
                    }

                    Set<Terminal> propagation = new HashSet<>();// 传播
                    Set<Terminal> spontaneous = new HashSet<>();// 自发生

                    for (Terminal terminal : lr1Item.lookaheads) {
                        if (sourceItem.lookaheads.contains(terminal)) {
                            // 向前看符号是传播的
                            propagation.add(terminal);
                        } else {
                            // 向前看符号是自发生
                            spontaneous.add(terminal);
                        }
                    }

                    Token token = lr1Item.production.getToken(lr1Item.pos);
                    Integer nextSetId = getGoto(lr1SetOfItem.id, token);
                    if (nextSetId == null) {
                        throw new RuntimeException("项集[" + lr1SetOfItem.id + "]在符号[" + token + "]上没有跳转");
                    }

                    SetOfLR1Item gotoSetOfItem = getLR1SetOfItem(nextSetId);

                    // 找到相应的GOTO项
                    LR1Item gotoItem = null;
                    for (LR1Item temp : gotoSetOfItem.lr1items) {
                        if (temp.production.equals(lr1Item.production)) {
                            gotoItem = temp;
                            break;
                        }
                    }

                    Iterator<Terminal> iterator = propagation.iterator();
                    while (iterator.hasNext()) {
                        // 去掉旧的传播的向前看符号
                        if (gotoItem.lookaheads.contains(iterator.next())) {
                            iterator.remove();
                        }
                    }

                    Lookaheads e = new Lookaheads();
                    e.sourceSetOfItem = sourceSet;
                    e.sourceItem = sourceItem;
                    e.gotoSetOfItem = gotoSetOfItem;
                    e.gotoItem = gotoItem;
                    e.propagation = propagation;
                    e.spontaneous = spontaneous;
                    result.add(e);

                    e.print();
                }
            }
        }

        return result;
    }

    /**
     * p159
     * Algorithm4.44 LR-parsing algorithm
     * LR 语法分析算法
     */
    public void LR_parsing(LinkedList<Terminal> w) {

        int size = w.size();

        Stack<Integer> stateStack = new Stack<>();
        stateStack.push(1);// 放入初始状态

        Stack<Token> symbolStack = new Stack<>();// for print

        while (true) {
            Integer state = stateStack.peek();
            Terminal symbol = w.getFirst();
            final Action action = getAction(state, symbol);
            if (action == null) {
                throw new RuntimeException("state:" + state + " symbol:" + symbol + " 没有找到动作");
            }
            switch (action.type) {
                case Goto:
                    //
                    break;
                case Shift:

                    printParsingAction(stateStack, symbolStack, size, w);

                    stateStack.push(action.state);
                    w.removeFirst();
                    symbolStack.push(symbol);

                    LogUtil.logNln("移入 " + symbol + " push state " + action.state);
                    LogUtil.log("");
                    break;
                case Reduce:

                    printParsingAction(stateStack, symbolStack, size, w);

                    for (Token token : action.production.body) {
                        stateStack.pop();
                        symbolStack.pop();
                    }
                    Integer top = stateStack.peek();
                    Action actionGoto = getAction(top, action.production.head);
                    if (actionGoto == null) {
                        LogUtil.log("规约前状态 " + state);
                        LogUtil.log("规约符号  " + w.getFirst());
                        LogUtil.log("规约产生式  " + action.production);
                        LogUtil.log("规约后状态 " + top + "没有找到GOTO");
                        throw new RuntimeException("Reduce Exception");
                    }
                    stateStack.push(actionGoto.state);
                    symbolStack.push(action.production.head);

                    LogUtil.logNln("规约 " + state + " " + symbol + " push state " + actionGoto.state);
                    LogUtil.log("");
                    break;
                case Accept:

                    printParsingAction(stateStack, symbolStack, size, w);

                    LogUtil.logNln("接受，语法分析完成");
                    LogUtil.log("");
                    return;
            }
        }

    }

    /**
     * 打印语法分析过程
     * @param stateStack    状态栈
     * @param symbolStack   符号栈
     * @param size          输入串符号个数
     * @param w             输入串
     */
    private void printParsingAction(Stack<Integer> stateStack, Stack<Token> symbolStack, int size, LinkedList<Terminal> w) {
        {
            Integer[] states = stateStack.toArray(new Integer[0]);
            String stateStr = "";
            for (Integer stateId : states) {
                String str = LogUtil.format("" + stateId, 2);
                stateStr += str;
            }
            LogUtil.logNln(stateStr, 2*size);
        }
        {
            Token[] tokens = symbolStack.toArray(new Token[0]);
            String tokenStr = "";
            for (Token token : tokens) {
                tokenStr += LogUtil.format(token.toString(), 2);
            }
            LogUtil.logNln(tokenStr, 2*size);
        }
        {
            String symbolStr = "";
            for (Terminal terminal : w) {
                symbolStr += LogUtil.format(terminal.name, 3);
            }
            LogUtil.logNln(symbolStr, 3*size);
        }
    }

    /**
     * 删除LR(0)项集的非内核项
     */
    public void removeNonKernelFormLR0SetOfItems() {
        setOfItemsLR0.forEach(item->item.delNonkernel(start));
    }

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
                for (Token token : tokens.values()) {
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

    /**
     * 初始化LR1项集族<br/>
     * 没有确定向前看符号
     */
    public void initLR1SetOfItem() {
        for (SetOfItem setOfItem : setOfItemsLR0) {
            SetOfLR1Item lr1SetOfItem = new SetOfLR1Item();
            lr1SetOfItem.id = setOfItem.id;
            for (Item item1 : setOfItem.items) {
                LR1Item e = new LR1Item(item1, new HashSet<>());
                lr1SetOfItem.lr1items.add(e);
            }
            addLR1SetOfItem(lr1SetOfItem);
        }
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

    /**
     *
     * @param start 起始状态
     * @param token 文法符号
     * @return 目标状态
     */
    public Integer getGoto(Integer start, Token token) {
        Map<Token, Integer> idToken = itemGoToItem.get(start);
        return idToken.get(token);
    }

    public SetOfLR1Item getLR1SetOfItem(int id) {
        return id2itemsLR1.get(id);
    }

    public void addLR1SetOfItem(SetOfLR1Item e) {
        setOfItemsLR1.add(e);
        id2itemsLR1.put(e.id, e);
    }

    public Set<SetOfLR1Item> getSetOfItemsLR1() {
        return Collections.unmodifiableSet(this.setOfItemsLR1);
    }

    public int nextId() {
        return idGen.incrementAndGet();
    }

    public void printLR1() {
        setOfItemsLR1.forEach(lr1SetOfItem -> {
            System.err.println("id = " + lr1SetOfItem.id);
            lr1SetOfItem.lr1items.forEach(lr1Item -> {
                System.err.println("\t" + lr1Item);
            });
        });
    }
}
