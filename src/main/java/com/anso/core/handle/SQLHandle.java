package com.anso.core.handle;

import com.anso.core.*;
import com.anso.core.pool.SQLConnetion;

import java.sql.SQLException;
import java.util.List;

//其实我应该动态代理执行阶段,sql预处理和绑定阶段应该在其他地方搞出
public class SQLHandle {
    private SQLExecute sqlExecute;
    private DynamicDataSource dynamicDataSource;
    //数据默认配置路径,多数据源的配置用.来切分,如果单数据源就不要切分,默认数据源前缀为default
    private static final String defaultPath = "pool/druid.properties";
    private final DataSourceChoose dataSourceChoose;

    public SQLHandle() {
        this(defaultPath);
    }

    //这里的size是你domain的数量
    public SQLHandle(String path) {
        this(path, 10);
    }

    public SQLHandle(int size) {
        this(defaultPath, size);
    }

    public SQLHandle(DataSourceChoose dynamicSource) {
        this(defaultPath, 10, dynamicSource);
    }

    public SQLHandle(int size, DataSourceChoose dataSourceChoose) {
        this(defaultPath, size, dataSourceChoose);
    }

    public SQLHandle(String path, int size) {
        this(path, size, null);
    }

    public SQLHandle(String path, int size, DataSourceChoose dataSourceChoose) {
        SQLConnetion sqlConnetion = new ConnectionBuildFactory(path, size).getSqlConnetion();
        //默认处理方式,如果你有更好的什么操作的话,那就用你的,
        if (dataSourceChoose == null) {
            dataSourceChoose = new DataSourceChoose() {
                ThreadLocal<String> threadLocal = new ThreadLocal<>();

                @Override
                public void set(String name) {
                    threadLocal.set(name);
                }

                @Override
                public String get() {
                    return threadLocal.get();
                }

                @Override
                public void remove() {
                    threadLocal.remove();
                }
            };
        }
        this.dataSourceChoose = dataSourceChoose;
        this.dynamicDataSource = new DynamicDataSource(sqlConnetion, dataSourceChoose);
        this.sqlExecute = new DefaultSqlSession().getMapper(SQLExecute.class, new SQLTemplateCreate(), dynamicDataSource);
    }

    //设置数据源
    public void setDataSource(String dataSource) {
        dataSourceChoose.set(dataSource);
    }

    //准备开始事务
    public void startTransaction() throws SQLException {
        dynamicDataSource.startTransaction();
    }

    //准备提交,即使没开启,被人瞎操作也没事.
    public void commit() throws SQLException {
        try {
            dynamicDataSource.doCommit();
        } finally {
            dynamicDataSource.close();
        }
    }

    //准备回滚,即使没先开启手动,被误操作也没事
    public void rollback() throws SQLException {
        try {
            dynamicDataSource.rollback();
        } finally {
            dynamicDataSource.close();
        }
    }

    //根据对象来插入JavaBean,可以同时插入多表,全部成功才成功
    public int insertPro(Object o, String... table) throws SQLException {
        return sqlExecute.insert(o, table);
    }

    //插入单条sql,非JavaBean使用,由于可变数组只能一个,不能给多个表名了,暂不支持
    public int insertPro(String sql, Object... values) throws SQLException {
        return sqlExecute.insert(sql, values);
    }

    //更新
    public int update(String sql, Object... values) throws SQLException {
        return sqlExecute.update(sql, values);
    }

    /**
     * 删除
     *
     * @param sql    一条sql
     * @param values 所需要的值
     * @return 成功条数
     * @throws SQLException
     */
    public int delete(String sql, Object... values) throws SQLException {
        return sqlExecute.delete(sql, values);
    }

    /**
     * 插入多条,如果你想同时插入很多表,那就全部填上,如果你想插入当前JavaBean所在的表,也需要填写上该表名
     * 必须全部成功才算成功,否则失效
     *
     * @param iterable  一个迭代器
     * @param tableName 多个表明
     * @return 成功的条数
     * @throws SQLException
     */
    public int insertAll(Iterable iterable, String... tableName) throws SQLException {
        return sqlExecute.insertAll(iterable, tableName);
    }

    /**
     * 查询一条
     *
     * @param sql    sql语句
     * @param clazz  期望返回的结果类型
     * @param values 有参数就填上你的参数
     * @return 返回值类型将是你期望的返回类型
     * @throws SQLException
     */
    public <T> T selectOne(String sql, Class<T> clazz, Object... values) throws SQLException {
        return sqlExecute.selectOne(sql, clazz, values);
    }

    public <T> List<T> selectMany(String sql, Class<T> clazz, Object... values) throws SQLException {
        return sqlExecute.selectMany(sql, clazz, values);
    }
}
