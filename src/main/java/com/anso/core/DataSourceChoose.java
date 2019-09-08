package com.anso.core;

/**
 * 控制你的数据源,配合ThreadLocal
 */
public interface DataSourceChoose {
    void set(String name);

    String get();

    void remove();
}
