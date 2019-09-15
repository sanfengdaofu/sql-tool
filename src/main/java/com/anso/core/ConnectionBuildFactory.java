package com.anso.core;

import com.anso.core.conf.ConfigRead;
import com.anso.core.pool.SQLConnection;
import com.anso.core.util.Cache;


public class ConnectionBuildFactory {
    private final SQLConnection sqlConnection;

    public ConnectionBuildFactory(String path, int size) {
        Cache.doInit(size);      //初始化缓存大小
        try {
            this.sqlConnection = new ConfigRead(path).getSQLConnection();
        } catch (Exception e) {
            throw new RuntimeException("创建连接失败,检查文件路径以及参数名");
        }
    }

    /**
     * 获取SQL连接
     *
     * @return
     */
    public SQLConnection getSqlConnection() {
        return sqlConnection;
    }
  /*  //获取默认连接
    public Connection getConn() throws Exception {
        return getConn(null);
    }

    public Connection getConn(String name) throws Exception {
        return sqlConnection.getConn(name);
    }*/
}
