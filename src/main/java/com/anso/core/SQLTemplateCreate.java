package com.anso.core;

import com.anso.core.annotation.SQLMapping;
import com.anso.core.enums.SQLModel;
import com.anso.core.util.Cache;
import com.anso.core.util.ClassIdentification;
import com.anso.core.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;

import java.util.*;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 语句生成类
 */
public class SQLTemplateCreate implements SQLCreate {
    @Override
    public Object[] insertObjectConvertSQL(Class<?> clazz) throws Exception {
        Object[] objects;
        synchronized (clazz) {  //锁住不同的对象,
            objects = Cache.insertTemplate.get(clazz.getSimpleName());
            //如果存在,就直接返回
            if (objects != null && objects.length == 4) {
                return objects;
            }
            objects = new Object[4];
            StringBuilder sb = new StringBuilder("INSERT INTO ");
            StringBuilder fields = new StringBuilder(" (");
            List<Method> methodList = new ArrayList<>();
            Map<String, Method> setMethods = new HashMap<>();
            StringBuilder preCompileSQLInsert = new StringBuilder(" values(");//预编译sql insert语句
            String methodName;    //get方法
            String setMethodName; //set方法
            String fieldName;
            String column;
            String tableName = clazz.getSimpleName();
            SQLMapping annotation = clazz.getAnnotation(SQLMapping.class);
            if (annotation != null) {
                String tableName1 = annotation.tableName();
                if (!StringUtil.isEmpty(tableName1))
                    tableName = tableName1;
            }
            sb.append(tableName);
            for (Field declaredField : clazz.getDeclaredFields()) {
                SQLMapping sqlMapping = declaredField.getAnnotation(SQLMapping.class);
                fieldName = declaredField.getName();
                methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);//拼接方法名
                setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);//拼接方法名
                Method method = clazz.getMethod(methodName);
                Method setMethod = clazz.getMethod(setMethodName, declaredField.getType());
                if (sqlMapping != null) {                        //如果存在注解
                    //如果不是忽略封装就加入.
                    column = sqlMapping.column();                  //获取注解的值
                    if (!"".equals(column)) {                     //如果不为空
                        fieldName = column;                       //字段名就等于列名
                    }
                    if (!sqlMapping.ignorePacking()) {
                        setMethods.put(fieldName, setMethod);
                    }
                    //如果是主键并且主键自增
                    if (sqlMapping.id()) {
                        continue;
                    }
                    fields.append(fieldName).append(",");
                } else {
                    setMethods.put(fieldName, setMethod);
                    fields.append(fieldName).append(",");
                }
                methodList.add(method);                        //将方法加入集合
                preCompileSQLInsert.append("?,");
            }
            //替换最后一个逗号
            fields.replace(fields.length() - 1, fields.length(), ")");
            preCompileSQLInsert.replace(preCompileSQLInsert.length() - 1, preCompileSQLInsert.length(), ")");
            objects[0] = sb.append(fields).append(preCompileSQLInsert).toString(); //sql
            objects[1] = Collections.unmodifiableList(methodList);     //get方法
            objects[2] = tableName;                                          //默认表名
            objects[3] = Collections.unmodifiableMap(setMethods);            //set方法
            Cache.insertTemplate.put(clazz.getSimpleName(), objects);
        }
        return objects;
    }

    @Override
    public PreparedStatement[] getPreparedStatement(Connection conn, SQLModel model, String sql, String tableName, String... otherParam) throws
            SQLException {
        if (StringUtil.isEmpty(sql))
            throw new RuntimeException("没有sql语句,当前sql语句为");
        int size = 0;
        if (otherParam != null && otherParam.length > 0) {
            size = otherParam.length;
        }
        PreparedStatement[] preparedStatements;
        if (size == 0) {
            preparedStatements = new PreparedStatement[1];
            preparedStatements[0] = conn.prepareStatement(sql);
            return preparedStatements;
        } else
            preparedStatements = new PreparedStatement[size];
        if (StringUtil.isEmpty(tableName))
            throw new RuntimeException("没有表名");
        switch (model) {
            case INSERT:
                for (int i = 0; i < otherParam.length; i++) {
                    String replace = sql.replaceFirst(tableName, otherParam[i]);
                    preparedStatements[i] = conn.prepareStatement(replace);
                }
                break;
            default:

                break;
        }
        return preparedStatements;
    }

    @Override
    public PreparedStatement[] fastBindParam(PreparedStatement[] preparedStatements, Iterable iterable, List<Method> methods) throws InvocationTargetException, IllegalAccessException, SQLException {
        Iterator iterator = iterable.iterator();
        Object o;     //当前对象
        while (iterator.hasNext()) {
            o = iterator.next();
            bindObject(o, methods, preparedStatements);
            for (PreparedStatement preparedStatement : preparedStatements) {
                preparedStatement.addBatch();
            }
        }
        return preparedStatements;
    }

    @Override
    public PreparedStatement[] fastBindParam(PreparedStatement[] preparedStatements, Object object, List<Method> methods) throws InvocationTargetException, IllegalAccessException, SQLException {
        bindObject(object, methods, preparedStatements);
        return preparedStatements;
    }

    @Override
    public PreparedStatement[] fastBindParam(PreparedStatement[] preparedStatements, Object[] object) throws InvocationTargetException, IllegalAccessException, SQLException {
        int i = 0;
        if (object.length > 0) {
            for (Object o : object) {
                setValue(preparedStatements, ++i, o);
            }
        }
        return preparedStatements;
    }

    @Override
    public <T> T fastPackingResult(ResultSet resultSet, Class<T> result, Map<String, Method> method, boolean more) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException {
        int i = 0;
        T t = null;
        List<T> list = null;
        if (more) {
            list = new ArrayList<>();
        }
        while (resultSet.next()) {
            ++i;
            if (ClassIdentification.isJDKClass(result)) {
                ResultSetMetaData metaData = resultSet.getMetaData(); //获取元数据
                int columnCount = metaData.getColumnCount();          //获取数据量
                if (columnCount > 1)
                    throw new RuntimeException("存在多列结果请核查,当前结果有:"+columnCount+"个");
                    t = (T) resultSet.getObject(1);
                if (more)
                    list.add(t);
            } else if (ClassIdentification.isMap(result)) {
                Map<String, Object> map = new HashMap<String, Object>();
                ResultSetMetaData metaData = resultSet.getMetaData(); //获取元数据
                int columnCount = metaData.getColumnCount();          //获取数据量
                for (int i1 = 0; i1 < columnCount; i1++) {
                    String columnName = metaData.getColumnName(i1 + 1);
//                    System.out.println(metaData.getColumnClassName(i + 1) + " " + metaData.getColumnName(i + 1));
                    map.put(columnName, resultSet.getObject(columnName));
                }
                t = (T) map;
                if (more)
                    list.add(t);
            } else if (!more && ClassIdentification.isList(result)) {
                throw new RuntimeException("查询单条请勿传入集合");
            } else {
                //为JavaBean
                t = result.newInstance();
                for (Map.Entry<String, Method> methods : method.entrySet()) {
                    methods.getValue().invoke(t, resultSet.getObject(methods.getKey()));
                }
                if (more)
                    list.add(t);
            }
        }
        if (i > 1 && !more) {
            throw new RuntimeException("结果有" + i + "条,请核查!");
        }
        if (more)
            return (T) list;
        return t;
    }


    private void bindObject(Object o, List<Method> methods, PreparedStatement[] preparedStatements) throws SQLException, InvocationTargetException, IllegalAccessException {
        int i = 0;
        for (Method method : methods) {
            setValue(preparedStatements, ++i, method.invoke(o));
        }
    }

    private void setValue(PreparedStatement[] preparedStatements, int i, Object value) throws SQLException {
        if (value instanceof Date) {
            Date d = (Date) value;
            value = new Timestamp(d.getTime());
        }
        for (PreparedStatement preparedStatement : preparedStatements) {
            preparedStatement.setObject(i, value);
        }
    }

}
