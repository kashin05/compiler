package com.compiler.model;

import com.compiler.LR1Item;
import com.compiler.SetOfLR1Item;
import com.compiler.Terminal;

import java.util.HashSet;
import java.util.Set;

/**
 * 向前看符号的传播自发生信息
 * @author kashin
 */
public class Lookaheads {

    public SetOfLR1Item sourceSetOfItem;
    public LR1Item sourceItem;

    public SetOfLR1Item gotoSetOfItem;
    public LR1Item gotoItem;
    /** 传播 */
    public Set<Terminal> propagation = new HashSet<>();// 传播
    /** 自发生 */
    public Set<Terminal> spontaneous = new HashSet<>();// 自发生

    public void print() {

        if (this.propagation.isEmpty() && this.spontaneous.isEmpty()) {
            return;
        }

        System.err.println("自" + sourceSetOfItem + "    " + sourceItem);
        System.err.println("到" + gotoSetOfItem + "    " + gotoItem);
        System.err.println("传播" + propagation);
        System.err.println("自发生" + spontaneous);
    }

}
