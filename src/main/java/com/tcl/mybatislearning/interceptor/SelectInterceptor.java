package com.tcl.mybatislearning.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * @author li
 * @version 1.0
 * @date 2020/8/11 19:07
 */
@Intercepts(value = {
        @Signature(
                type = Executor.class,
                method = "query",
                args = {
                        MappedStatement.class,
                        Object.class,
                        RowBounds.class,
                        ResultHandler.class
                }
        )
})
public class SelectInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 调用前
        System.out.println("==========查询前日志=========");
        // 执行查询
        Object object = invocation.proceed();
        // 调用后
        System.out.println("==========查询后日志=========");
        return object;
    }
}
