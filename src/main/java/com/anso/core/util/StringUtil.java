package com.anso.core.util;

public class StringUtil {
    private StringUtil() {
    }

    public static boolean isEmpty(String value) {
        return value == null || "".equals(value);
    }
}
