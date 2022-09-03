package com.compiler;

import java.util.LinkedList;

/**
 * @author kashin
 */
public abstract class LogUtil {

    public static final LinkedList<String> strList = new LinkedList<>();

    public static void log(String str) {
        System.out.print(str + "\n");
    }

    public static void logNln(String str) {
        System.out.print(str);
    }

    public static void logNln(String str, int colSize) {
        String actionName = str;
        while (actionName.toCharArray().length < colSize) {
            actionName += ' ';
        }
        System.out.print(actionName);
    }

}
