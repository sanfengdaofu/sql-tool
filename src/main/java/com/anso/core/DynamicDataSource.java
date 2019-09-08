package com.anso.core;

import com.anso.core.pool.SQLConnetion;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据源设定
 */
public class DynamicDataSource {
    private static ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Boolean> isAutoCommit = new ThreadLocal<>();
    private final SQLConnetion sqlConnetion;
    private DataSourceChoose dataSourceChoose;

    public DynamicDataSource(SQLConnetion sqlConnetion, DataSourceChoose dataSourceChoose) {
        this.dataSourceChoose = dataSourceChoose;
        this.sqlConnetion = sqlConnetion;
    }

    public void setConnection() throws SQLException {
        connectionThreadLocal.set(sqlConnetion.getConn(dataSourceChoose.get()));
    }

    public Connection getConnection() throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection == null) {
            setConnection();
            connection = connectionThreadLocal.get();
        }
        return connection;
    }

    public void close() throws SQLException {
        //如果有人设置了手动提交事务,那就必须手动关闭后才能实现关闭,当手动事务一提交,会将状态改变
        if (connectionThreadLocal.get() != null && (isAutoCommit.get() == null || !isAutoCommit.get())) {
            closeAll();
        }
    }

    private void closeAll() throws SQLException {
//        System.out.println("清除所有....");
        try {
            connectionThreadLocal.get().close();
        } finally {
            //用完需要移除,养成好习惯.
            connectionThreadLocal.remove();
            isAutoCommit.remove();
            dataSourceChoose.remove();
        }
    }

    public void startTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
        isAutoCommit.set(true);
    }

    public void doCommit() throws SQLException {
        if (isAutoCommit.get() != null) {
            isAutoCommit.set(false);
            commit();
        }
    }

    void commit() throws SQLException {
        if (connectionThreadLocal.get() != null && (isAutoCommit.get() == null || !isAutoCommit.get())) {
            connectionThreadLocal.get().commit();
        }
    }

    public void rollback() throws SQLException {
        if (isAutoCommit.get() != null && connectionThreadLocal.get() != null) {
            isAutoCommit.set(false);
            connectionThreadLocal.get().rollback();
        }
    }

    //设置不自动提交
    public void setNoAutoCommit() throws SQLException {
        //如果不是外部人员手动关闭的,那就不需要在操作了.
        if (isAutoCommit.get() == null)
            getConnection().setAutoCommit(false);
    }

    /**
     * 设置只读
     *
     * @throws SQLException
     */
    void setRead() throws SQLException {
        getConnection().setReadOnly(true);
        getConnection().setAutoCommit(false);
    }

}
