package com.tcl.mybatislearning.factory;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import java.util.List;

/**
 * @author li
 * @version 1.0
 * @date 2020/8/11 18:52
 */
public class MyObjectFactory extends DefaultObjectFactory {


    @Override
    public <T> T create(Class<T> type) {
        System.out.println("+++++++++调用无参构造器创建实例++++++++");
        return super.create(type);
    }

    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        System.out.println("==========调用有参构造器创建实例===========");
        return super.create(type, constructorArgTypes, constructorArgs);
    }

    @Override
    public <T> boolean isCollection(Class<T> type) {
        return super.isCollection(type);
    }
}
