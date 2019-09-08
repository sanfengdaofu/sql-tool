package com.anso.core;

import com.anso.core.conf.ConfigRead;
import com.anso.core.pool.SQLConnetion;
import com.anso.core.util.Cache;


public class ConnectionBuildFactory {
    private final SQLConnetion sqlConnetion;

    public ConnectionBuildFactory(String path) {
        this(path, 10);
    }


    public ConnectionBuildFactory(String path, int size) {
        Cache.doInit(size);      //初始化缓存大小
        try {
            this.sqlConnetion = new ConfigRead(path).getSQLConnection();
        } catch (Exception e) {
            throw new RuntimeException("创建连接失败,检查文件路径以及参数名");
        }
    }

    /**
     * 获取SQL连接
     *
     * @return
     */
    public SQLConnetion getSqlConnetion() {
        return sqlConnetion;
    }
  /*  //获取默认连接
    public Connection getConn() throws Exception {
        return getConn(null);
    }

    public Connection getConn(String name) throws Exception {
        return sqlConnetion.getConn(name);
    }*/
}
