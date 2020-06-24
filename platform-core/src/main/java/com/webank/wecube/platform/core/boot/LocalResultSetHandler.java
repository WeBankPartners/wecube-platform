package com.webank.wecube.platform.core.boot;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface LocalResultSetHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}
