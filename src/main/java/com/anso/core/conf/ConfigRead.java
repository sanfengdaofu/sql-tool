package com.anso.core.conf;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.anso.core.pool.SQLConnection;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 读取文件配置类
 */
public class ConfigRead {
    //默认配置
    private final Properties p = new Properties() {
        {
            put("initialSize", "5");
            put("maxActive", "10");
        }
    };

    public ConfigRead(String path) throws IOException {
        try (InputStream in = ConfigRead.class.getClassLoader().getResourceAsStream(path)) {
            this.p.load(in);
        }
    }

    //默认druid,未来考虑切换多连接池,配置规则:多数据源用.来隔开
    public SQLConnection getSQLConnection() throws Exception {
        SQLConnection sqlConnection = new SQLConnection();
        Set<String> names = this.p.stringPropertyNames();
        Map<String, DataSource> map = new HashMap<>();
        Map<String, Properties> moreProperties = new HashMap<>();
        boolean flag = false;  //是否为多数据源的标志
        for (String key : names) {
            String[] moreDataSource = key.split("\\.");
            if (moreDataSource.length == 2) {
                flag = true;
                String name = moreDataSource[0];
                Properties properties = moreProperties.get(name);
                if (properties == null) {
                    properties = new Properties();
                    //配置默认的池
                    properties.put("initialSize", "5");
                    properties.put("maxActive", "10");
                    moreProperties.put(name, properties);
                }
                properties.setProperty(moreDataSource[1], String.valueOf(this.p.get(key)));
            }
        }
        if (flag) {
            //多数据源的设置
            for (Map.Entry<String, Properties> more : moreProperties.entrySet()) {
                map.put(more.getKey(), DruidDataSourceFactory.createDataSource(more.getValue()));
            }
            sqlConnection.setDataSourceMap(map);
        } else {
            //单数据源
            sqlConnection.setDefaultDataSource(DruidDataSourceFactory.createDataSource(this.p));
        }
        return sqlConnection;
    }


}
