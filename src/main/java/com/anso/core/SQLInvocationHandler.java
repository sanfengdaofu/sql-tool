package com.anso.core;

import com.anso.core.enums.SQLModel;
import com.anso.core.util.Cache;
import com.anso.core.util.ClassIdentification;

import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SQLInvocationHandler implements InvocationHandler, ConnectionAction {
    private SQLCreate sqlCreate;
    private DynamicDataSource dynamicDataSource;

    public SQLInvocationHandler(SQLCreate sqlCreate, DynamicDataSource dynamicDataSource) {
        this.sqlCreate = sqlCreate;
        this.dynamicDataSource = dynamicDataSource;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (args[0] == null) {
            throw new RuntimeException("第一个参数不能为空!方法名:" + methodName + "  入参为:" + Arrays.toString(method.getParameters()));
        }
        Connection conn = open();
        ResultSet resultSet = null;
        PreparedStatement[] preparedStatements = null;
        try {
            if (ClassIdentification.isJDKClass(args[0].getClass())) {
                //是JDK数据类型
                if (methodName.contains("select")) {
                    Class<?> result = (Class<?>) args[1];
                    if (result == null) {
                        throw new RuntimeException("返回值不得为空");
                    }
                    //查询设置为只读
                    dynamicDataSource.setRead();
                    preparedStatements = sqlCreate.getPreparedStatement(conn, SQLModel.OTHER, (String) args[0], null);
                    Object[] objects;
                    Map<String, Method> methods = null;
                    //判断是否为javaBean类型
                    if (ClassIdentification.isJavaBean(result)) {
                        objects = getCache(result);
                        methods = (Map<String, Method>) objects[3];
                    }
                    resultSet = sqlCreate.fastBindParam(preparedStatements, (Object[]) args[args.length - 1])[0].executeQuery();
                    return sqlCreate.fastPackingResult(resultSet, result, methods, ClassIdentification.isList(method.getReturnType()));

                } else {
                    preparedStatements = sqlCreate.getPreparedStatement(conn, SQLModel.OTHER, (String) args[0], null);
                    return sqlCreate.fastBindParam(preparedStatements, (Object[]) args[args.length - 1])[0].executeUpdate();
                }
            } else {
                Object[] objects;
                //如果是集合类型
                Class clazz = args[0].getClass();
                if (ClassIdentification.isList(clazz)) {
                    Collection list = (Collection) args[0];
                    if (list.size() == 0)
                        throw new RuntimeException("集合中没有对象,方法名:" + methodName + "  方法入参:" + Arrays.toString(method.getParameters()));
                    try {
                        //设置手动提交事务
                        dynamicDataSource.setNoAutoCommit();
                        clazz = list.iterator().next().getClass();
                       objects = getCache(clazz);
                        preparedStatements = sqlCreate.getPreparedStatement(conn, SQLModel.INSERT, (String) objects[0], (String) objects[2], (String[]) args[args.length - 1]);
                        int result = 0;
                        for (PreparedStatement preparedStatement : sqlCreate.fastBindParam(preparedStatements, (Iterable) args[0], (List<Method>) objects[1])) {
                            result += preparedStatement.executeBatch().length;
                        }
                        dynamicDataSource.commit();
                        return result;
                    } catch (SQLException e) {
                        dynamicDataSource.rollback();
                        throw e;
                    }
                } else {
                    //设置手动提交
                    try {
                        dynamicDataSource.setNoAutoCommit();
                        clazz = args[0].getClass();
                        objects = getCache(clazz);
                        preparedStatements = sqlCreate.getPreparedStatement(conn, SQLModel.INSERT, (String) objects[0], (String) objects[2], (String[]) args[args.length - 1]);
                        int result = 0;
                        for (PreparedStatement preparedStatement : sqlCreate.fastBindParam(preparedStatements, args[0], (List<Method>) objects[1])) {
                            result += preparedStatement.executeUpdate();
                        }
                        dynamicDataSource.commit();
                        return result;
                    } catch (Exception e) {
                        dynamicDataSource.rollback();
                        throw e;
                    }
                }

            }
        } finally {
            close(preparedStatements, resultSet);
        }
    }


    @Override
    public void close(PreparedStatement[] preparedStatements, ResultSet rs) throws SQLException {
        try {
            if (rs != null) {
                rs.close();
            }
            if (preparedStatements != null && preparedStatements.length > 0) {
                for (PreparedStatement preparedStatement : preparedStatements) {
                    preparedStatement.close();
                }
            }
        } finally {
            dynamicDataSource.close();
        }
    }

    @Override
    public Connection open() throws Exception {
        return dynamicDataSource.getConnection();
    }


    public Object[] getCache(Class clazz) throws Exception {
        Object[] objects = Cache.insertTemplate.get(clazz.getSimpleName());
        if (objects == null)
            objects = sqlCreate.insertObjectConvertSQL(clazz);
        return objects;
    }

}
