package com.tcl.mybatislearning.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * @author li
 * @version 1.0
 * @date 2020/8/12 10:19
 */
public class HikariDataSourceFactory extends UnpooledDataSourceFactory {
    public HikariDataSourceFactory() {
//        HikariConfig hikariConfig = new HikariConfig("D:\\developer\\java\\JetBrains\\IdeaProjects\\mybatis-learning\\mybatis-learning\\src\\main\\resources\\hikariCP.properties");
        this.dataSource = new HikariDataSource();
    }
}
