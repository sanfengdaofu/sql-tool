package com.anso.core;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface SQLExecute {
    int insertAll(Iterable t, String... table); //默认注解的名字,可指定多个表名同时插入

    int insert(Object clazz, String... table);

    //object为根据对象生成sql,默认主键,可指定多个参数为where条件,默认and拼接
//    int update(Object clazz, String... fields);

//    int delete(Object clazz, String... fields);

    int insert(String sql, Object... values);

    int delete(String sql, Object... values);

    int update(String sql, Object... values);

    <T> T selectOne(String sql, Class<T> result, Object... values); //指定返回值类型

    <E> List<E> selectMany(String sql, Class<E> result, Object... values);//查询多条


}
