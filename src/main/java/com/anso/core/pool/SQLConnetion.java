package com.anso.core.pool;

import com.sun.istack.internal.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * 数据库连接池
 */
public class SQLConnetion {
    //多数据源
    private Map<String, DataSource> dataSourceMap = null;
    //默认数据源
    private DataSource defaultDataSource = null;
    private final static String DEFAULT = "default";

    public void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
        this.dataSourceMap = Collections.unmodifiableMap(dataSourceMap);
    }

    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    //如果存在多数据源,那就根据键获取名字
    public Connection getConn(String name) throws SQLException {
        if (name == null)
            name = DEFAULT;
        return dataSourceMap == null ? defaultDataSource.getConnection() : dataSourceMap.get(name).getConnection();
    }

    @Override
    public String toString() {
        return "SQLConnetion{" +
                "dataSourceMap=" + dataSourceMap +
                ", defaultDataSource=" + defaultDataSource +
                '}';
    }
}
