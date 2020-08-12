package com.tcl.mybatislearning.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author li
 * @version 1.0
 * @date 2020/8/12 11:12
 */
public class DruidDataSourceFactory extends UnpooledDataSourceFactory {

    public DruidDataSourceFactory() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream("druid.properties");
        Properties props = new Properties();
        props.load(inputStream);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.configFromPropety(props);
        this.dataSource = dataSource;
    }
}
