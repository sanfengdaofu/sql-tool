package com.anso.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ConnectionAction {
    void close(PreparedStatement[] preparedStatements, ResultSet rs) throws SQLException; //关闭连接

    Connection open() throws Exception;  //获取连接
}
