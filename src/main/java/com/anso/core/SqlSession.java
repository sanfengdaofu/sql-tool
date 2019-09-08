package com.anso.core;

public interface SqlSession {
    <T> T getMapper(Class<T> userClass, SQLTemplateCreate sqlTemplateCreate, DynamicDataSource dynamicDataSource);
}
