package com.tcl.mybatislearning;

import com.tcl.mybatislearning.entity.Author;
import com.tcl.mybatislearning.entity.Blog;
import com.tcl.mybatislearning.mapper.BlogMapper;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;

@SpringBootTest
class GetStartedTests {

    @Test
    void getStarted() throws IOException {
        // 1.读取配置构建sqlSessionFactory
        String configPath = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(configPath);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        // 2.创建sqlSession
        try (SqlSession sqlSession = sessionFactory.openSession(true)){
            BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);

            // 插入
            Author author = new Author();
            author.setName("author-1");
            Blog b = new Blog();
            b.setTitle("Blog-2");
            b.setContent("Content-2");
            b.setAuthor(author);
            mapper.insertBlog(b);
            System.out.println(b);

            // 查询
            Blog blog = mapper.selectById(b.getId());
            System.out.println(blog);

            // 修改
            author.setName("author-3");
            blog.setAuthor(author);
            mapper.updateBlog(blog);
            blog = mapper.selectById(blog.getId());
            Assertions.assertEquals(author, blog.getAuthor());

            // 删除
//            mapper.deleteBlogById(blog.getId());
//            int count = mapper.count();
//            Assertions.assertEquals(1, count);

        }

    }


    @Test
    void sqlSessionFactoryJavaConfig() {
        // 1.构建数据源，对应于xml配置中的<dataSource type="POOLED">标签
        PooledDataSource dataSource = new PooledDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis_learning?serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");

        // 2.构建Configuration实例
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        // <environment id="development">标签
        Environment environment = new Environment("development", transactionFactory, dataSource);
        // <configuration>标签
        Configuration configuration = new Configuration(environment);

        // <mappers/>标签
        configuration.addMappers("com.tcl.mybatislearning.mapper");
        // 或者添加mapper接口
//        configuration.addMapper(BlogMapper.class);

        // 3.构建出SqlSessionFactory实例
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
            mapper.selectAll().forEach(System.out::println);
        }
    }

    @Test
    void selectList() throws IOException {
            // 1.读取配置构建sqlSessionFactory
            String configPath = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(configPath);
            SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            // 2.创建sqlSession
            try (SqlSession sqlSession = sessionFactory.openSession(true)) {
                BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
                mapper.selectAll().forEach(System.out::println);
            }
    }


    @Test
    void testC3P0DataSource() throws IOException {
        selectList();
    }
}
