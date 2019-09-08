package com.anso.core;


import java.lang.reflect.Proxy;

public class DefaultSqlSession implements SqlSession {
    //一个默认的实现sqlsession的接口类.代理模式.

    @Override
    public <T> T getMapper(Class<T> clazz, SQLTemplateCreate sqlTemplateCreate, DynamicDataSource dynamicDataSource) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new SQLInvocationHandler(sqlTemplateCreate,dynamicDataSource));
    }

}
