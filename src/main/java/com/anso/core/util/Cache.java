package com.anso.core.util;

import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    //根据类名寻找对应的sql模板语句,和模板方法.第一位是模板语句,第二位是方法集合,第三位是表名,第四位是set方法
    public static ConcurrentHashMap<String, Object[]> insertTemplate;

    public synchronized static void doInit(int size) {
        if (insertTemplate == null) {
            insertTemplate = new ConcurrentHashMap<>(size);
        }
    }
}
