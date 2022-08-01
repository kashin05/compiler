package com.compiler;

/**
 * 语法分析表动作
 * @author kashin
 */
public class Action {

    protected ActionType type;
    protected Integer state;
    protected Production production;

    public static Action of(ActionType type) {
        Action result = new Action();
        result.type = type;
        return result;
    }

    public static Action of(ActionType type, Integer state) {
        Action result = new Action();
        result.type = type;
        result.state = state;
        return result;
    }

    public static Action of(ActionType type, Production production) {
        Action result = new Action();
        result.type = type;
        result.production = production;
        return result;
    }

    @Override
    public String toString() {
        return "Action{" +
                "type=" + type +
                ", state=" + state +
                ", production=" + production +
                '}';
    }
}
