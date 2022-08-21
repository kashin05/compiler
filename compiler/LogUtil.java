package com.compiler;

import java.util.LinkedList;

/**
 * @author kashin
 */
public abstract class LogUtil {

    public static final LinkedList<String> strList = new LinkedList<>();

    public static void log(String str) {
        strList.add(str + "\n");
    }

    public static void logNln(String str) {
        strList.add(str);
    }

    public static void flushLog() {
        for (String s : strList) {
            System.err.print(s);
        }
    }

}
