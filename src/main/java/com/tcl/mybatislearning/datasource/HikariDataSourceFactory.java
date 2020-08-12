package com.tcl.mybatislearning.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author li
 * @version 1.0
 * @date 2020/8/12 10:19
 */
public class HikariDataSourceFactory extends UnpooledDataSourceFactory {
    public HikariDataSourceFactory() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream("hikariCP.properties");
        Properties props = new Properties();
        props.load(inputStream);
        HikariConfig hikariConfig = new HikariConfig(props);
        this.dataSource = new HikariDataSource(hikariConfig);
    }
}
