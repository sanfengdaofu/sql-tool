package com.anso.core;

import com.anso.core.enums.SQLModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public interface SQLCreate {
    Object[] insertObjectConvertSQL(Class<?> clazz) throws Exception; //对象转sql语句

    PreparedStatement[] getPreparedStatement(Connection conn, SQLModel model, String sql, String tableName, String... otherParam) throws SQLException;

    PreparedStatement[] fastBindParam(PreparedStatement[] preparedStatements, Iterable iterable, List<Method> methods) throws InvocationTargetException, IllegalAccessException, SQLException;

    PreparedStatement[] fastBindParam(PreparedStatement[] preparedStatements, Object object, List<Method> methods) throws InvocationTargetException, IllegalAccessException, SQLException;

    PreparedStatement[] fastBindParam(PreparedStatement[] preparedStatements, Object[] object) throws InvocationTargetException, IllegalAccessException, SQLException;

    <T> T fastPackingResult(ResultSet resultSet, Class<T> result, Map<String, Method> method, boolean more) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException;


}
