package com.anso.core.util;

import java.util.*;

public class ClassIdentification {
    private final static Set<Class> clazzs = new HashSet<Class>() {
        {
            add(String.class);
            add(Boolean.class);
            add(Character.class);
            add(Date.class);
        }
    };


    public static boolean isJDKClass(Class clazz) {
        if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
            return true;
        }
        return clazzs.contains(clazz);
    }

    public static boolean isList(Class clazz) {
        return Iterable.class.isAssignableFrom(clazz);
    }

    public static boolean isMap(Class clazz) {
        return Map.class.isAssignableFrom(clazz);
    }


    public static boolean isJavaBean(Class clazz) {
        return !(isList(clazz) || isMap(clazz) || isJDKClass(clazz));
    }
}
