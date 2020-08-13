# MyBatis学习笔记

## 1. 简介

MyBatis 是一款优秀的持久层框架，它支持自定义 SQL、存储过程以及高级映射。MyBatis 免除了几乎所有的 JDBC 代码以及设置参数和获取结果集的工作。MyBatis 可以通过简单的 XML 或注解来配置和映射原始类型、接口和 Java POJO（Plain Old Java Objects，普通老式 Java 对象）为数据库中的记录。 

## 2.快速入门

### 2.1 导入依赖

创建maven项目，并添加依赖：

```xml
<dependency>
  <groupId>org.mybatis</groupId>
  <artifactId>mybatis</artifactId>
  <version>3.5.5</version>
</dependency>
```

### 2.2 编写JavaBean和接口映射

`Blog.java`：

```java
@Data
public class Blog {

    private Long id;

    private String title;

    private String content;

    private String author;

    private Date createTime;

    private Date updateTime;
}
```

`BlogMapper.java`：Mapper映射接口

```java
@Mapper
public interface BlogMapper {

    Blog selectById(Long id);

    void insertBlog(Blog blog);

    void updateBlog(Blog blog);

    void deleteBlogById(Long id);

    List<Blog> selectAll();

    Integer count();

}
```

`BlogMapper.xml`：放在resources/mapper目录下

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.tcl.mybatislearning.mapper.BlogMapper">
    <insert id="insertBlog"
            parameterType="com.tcl.mybatislearning.entity.Blog"
            useGeneratedKeys="true"
            keyProperty="id">
        insert into blog (title, content, author)
        VALUES (#{title}, #{content}, #{author});
    </insert>

    <update id="updateBlog">
        update blog
        set
            <if test="title!=null">
                title=#{title},
            </if>
            <if test="content!= null">
                content=#{content},
            </if>
            <if test="author!=null">
                author = #{author}
            </if>
        where id = #{id};
    </update>

    <delete id="deleteBlogById">
        delete
        from blog
        where id = #{id};
    </delete>

    <select id="selectById" resultType="com.tcl.mybatislearning.entity.Blog">
        select *
        from Blog
        where id = #{id};
    </select>

    <select id="selectAll" resultType="com.tcl.mybatislearning.entity.Blog">
        select *
        from blog;
    </select>
    <select id="count" resultType="java.lang.Integer">
        select count(id)
        from blog;
    </select>
</mapper>
```



对于简单的语句，还可以使用注解来绑定sql语句：

```java
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```



### 2.3 编写xml配置

在`resources`目录下新建`mybatis-config.xml`文件，基本配置内容包含事物管理类型和数据源配置等。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/mybatis_learning?serverTimezone=Asia/Shanghai"/>
                <property name="username" value="root"/>
                <property name="password" value="root"/>
            </dataSource>
        </environment>
    </environments>
    
    <!--SQL映射文件-->
    <mappers>
        <!--或者指定映射接口包名-->
        <!--
        <package name="com.tcl.mybatislearning.mapper"/>
		-->
        <!--或者指定xml映射文件-->
        <mapper resource="mapper/BlogMapper.xml"/>
    </mappers>

</configuration>
```

**采坑点**：如果使用指定映射接口包名的方式，需要将对应的`*.xml`映射文件一同放在接口所在的位置。

**编译坑**：需要在pom.xml中加入下列配置才能将xml文件编译到target/classes目录中：

```xml
    <build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.xml</include>
                </includes>
            </resource>
        </resources>
    </build>
```



### 2.3 构建SqlSessionFactory

MyBatis以一个 SqlSessionFactory 实例为核心。可以通过 SqlSessionFactoryBuilder 创建SqlSessionFactory 的实例。而 SqlSessionFactoryBuilder 则可以有两种方法来构建出 SqlSessionFactory 实例 ：

-  输入流（XML 配置文件）

  ```java
  		// 1.读取配置构建sqlSessionFactory
          String configPath = "mybatis-config.xml";
          InputStream inputStream = Resources.getResourceAsStream(configPath);
          SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
  ```

  

- 预先配置的 Configuration 实例（Java代码配置）

  ```java
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
  	    // 映射接口包名 
          configuration.addMappers("com.tcl.mybatislearning.mapper");
  		// 或者添加mapper接口
  //        configuration.addMapper(BlogMapper.class);
  
          // 3.构建出SqlSessionFactory实例
          SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  
          try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
              BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
              mapper.selectAll().forEach(System.out::println);
          }
  ```

### 2.4 创建SqlSession执行查询

有了SqlSessionFactory实例后，我们便可以中获得 SqlSession 的实例。SqlSession 提供了在数据库执行 SQL 命令所需的所有方法。你可以通过 SqlSession 实例来直接执行已映射的 SQL 语句：

```java
        try (SqlSession sqlSession = sessionFactory.openSession(true)){
            BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);

            // 插入
            Blog b = new Blog();
            b.setTitle("Blog-1");
            b.setContent("Content-1");
            b.setAuthor("Author-1");
            mapper.insertBlog(b);
            System.out.println(b);

            // 查询
            Blog blog = mapper.selectById(b.getId());
            System.out.println(blog);

            // 修改
            blog.setAuthor("Author-2");
            mapper.updateBlog(blog);
            blog = mapper.selectById(blog.getId());
            Assertions.assertEquals("Author-2", blog.getAuthor());

            // 删除
            mapper.deleteBlogById(blog.getId());
            int count = mapper.count();
            Assertions.assertEquals(0, count);

        }

```



### 2.5 关键概念

#### 2.5.1 命名空间和命名解析

命名空间存在于XML映射文件中，其功能有两个：

```xml
<mapper namespace="com.tcl.mybatislearning.mapper.BlogMapper">
```

1. **接口绑定**
2. 通过限定名将不同语句分隔开

MyBatis 对所有具有名称的配置元素（包括语句，结果映射，缓存等）使用了如下的命名解析规则。 

- 全限定名（比如 “com.mypackage.MyMapper.selectAllThings）将被直接用于查找及使用。
- 短名称（比如 “selectAllThings”）如果全局唯一也可以作为一个单独的引用。 如果不唯一，有两个或两个以上的相同名称（比如 “com.foo.selectAllThings” 和 “com.bar.selectAllThings”），那么使用时就会产生“短名称不唯一”的错误，这种情况下就必须使用全限定名。

#### 2.5.2 作用域（Scope）和生命周期

理解不同作用域和生命周期类别非常重要，因为错误的使用会导致非常严重的并发问题。

**依赖注入框架** 

依赖注入框架可以创建线程安全的、基于事务的 SqlSession 和Mapper映射器实例，并将它们直接注入到你的 bean 中，因此可以直接忽略它们的生命周期。 

**最佳实践**

1. #### SqlSessionFactoryBuilder

   一旦创建了 SqlSessionFactory ，就可以丢弃该实例了。因此 SqlSessionFactoryBuilder 实例的最佳作用域是方法作用域（也就是局部变量） 。

2. #### SqlSessionFactory

   SqlSessionFactory 一旦被创建就应该在应用的运行期间一直存在，没有任何理由丢弃它或重新创建另一个实例。不要重复创建多次，因此 SqlSessionFactory 的最佳作用域是应用作用域。 可以使用单例模式或者静态单例模式。

3. #### SqlSession

   每个线程都应该有它自己的 SqlSession 实例。SqlSession 的实例不是线程安全的，因此是不能被共享的，所以它的最佳的作用域是请求或方法作用域。 最佳实践：

   ```java
   try (SqlSession session = sqlSessionFactory.openSession()) {
     // 应用逻辑代码
   }
   ```

4. **Mapper映射器实例**

   映射器是一些绑定映射语句的接口 。映射器接口的实例是从 SqlSession 中获得的。虽然从技术层面上来讲，任何映射器实例的最大作用域与请求它们的 SqlSession 相同。但**方法作用域才是映射器实例的最合适的作用域**。 



## 3. MyBatis配置

MyBatis 的配置文件包含了影响 MyBatis 行为的设置和属性信息。 配置文件的层级结构如下： 

```
configuration（配置） 
	properties（属性） 
	settings（设置）
	typeAliases（类型别名） 
	typeHandlers（类型处理器） 
	objectFactory（对象工厂）
	plugins（插件）
	environments（环境配置）
		environment（环境变量） 
			transactionManager（事务管理器） 
			dataSource（数据源） 
	databaseIdProvider（数据库厂商标识） 
	mappers（映射器）
```

### 3.1 Properties属性

这些属性可以通过三种方法进行设置：

1. 外部的url或者xx.properties文件
2. 使用properties 元素的子元素进行配置
3. 可以在 SqlSessionFactoryBuilder.build() 方法中传入属性值 

并可以进行动态替换。

```xml
    <properties resource="mybatis.properties">
        <property name="username" value="root"/>
        <property name="password" value="password"/>
        <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/mybatis_learning?serverTimezone=Asia/Shanghai"/>
    </properties>
```

设置好的属性可以在整个配置文件中用来替换需要动态配置的属性值。引用方法：`${varname}`

 ```xml
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${username}"/>
            </dataSource>
 ```

属性优先级（由高到低）：

1. 方法参数
2. url/resource外部属性
3. `<property>`元素

除此之外，还可以为`${}`占位符指定一个默认值，如`${username:root}`。该功能默认关闭，开启方法：

```xml
<properties resource="org/mybatis/example/config.properties">
  <!-- ... -->
  <property name="org.apache.ibatis.parsing.PropertyParser.enable-default-value" value="true"/> <!-- 启用默认值特性 -->
  <property name="org.apache.ibatis.parsing.PropertyParser.default-value-separator" value="?:"/> <!-- 修改默认值的分隔符 -->
</properties>
```

### 3.2 Setting设置

这些设置将会影响Mybatis的运行时行为。下表列出了所有设置项：

| 设置名                           | 描述                                                         | 有效值                                                       | 默认值                                                |
| -------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ----------------------------------------------------- |
| cacheEnabled                     | 全局性地开启或关闭所有映射器配置文件中已配置的任何缓存。     | true \| false                                                | true                                                  |
| lazyLoadingEnabled               | 延迟加载的全局开关。当开启时，所有关联对象都会延迟加载。 特定关联关系中可通过设置 `fetchType` 属性来覆盖该项的开关状态。 | true \| false                                                | false                                                 |
| aggressiveLazyLoading            | 开启时，任一方法的调用都会加载该对象的所有延迟加载属性。 否则，每个延迟加载属性会按需加载（参考 `lazyLoadTriggerMethods`)。 | true \| false                                                | false （在 3.4.1 及之前的版本中默认为 true）          |
| multipleResultSetsEnabled        | 是否允许单个语句返回多结果集（需要数据库驱动支持）。         | true \| false                                                | true                                                  |
| useColumnLabel                   | 使用列标签代替列名。实际表现依赖于数据库驱动，具体可参考数据库驱动的相关文档，或通过对比测试来观察。 | true \| false                                                | true                                                  |
| useGeneratedKeys                 | 允许 JDBC 支持自动生成主键，需要数据库驱动支持。如果设置为 true，将强制使用自动生成主键。尽管一些数据库驱动不支持此特性，但仍可正常工作（如 Derby）。 | true \| false                                                | False                                                 |
| autoMappingBehavior              | 指定 MyBatis 应如何自动映射列到字段或属性。 NONE 表示关闭自动映射；PARTIAL 只会自动映射没有定义嵌套结果映射的字段。 FULL 会自动映射任何复杂的结果集（无论是否嵌套）。 | NONE, PARTIAL, FULL                                          | PARTIAL                                               |
| autoMappingUnknownColumnBehavior | 指定发现自动映射目标未知列（或未知属性类型）的行为。`NONE`: 不做任何反应`WARNING`: 输出警告日志（`'org.apache.ibatis.session.AutoMappingUnknownColumnBehavior'` 的日志等级必须设置为 `WARN`）`FAILING`: 映射失败 (抛出 `SqlSessionException`) | NONE, WARNING, FAILING                                       | NONE                                                  |
| defaultExecutorType              | 配置默认的执行器。SIMPLE 就是普通的执行器；REUSE 执行器会重用预处理语句（PreparedStatement）； BATCH 执行器不仅重用语句还会执行批量更新。 | SIMPLE REUSE BATCH                                           | SIMPLE                                                |
| defaultStatementTimeout          | 设置超时时间，它决定数据库驱动等待数据库响应的秒数。         | 任意正整数                                                   | 未设置 (null)                                         |
| defaultFetchSize                 | 为驱动的结果集获取数量（fetchSize）设置一个建议值。此参数只可以在查询设置中被覆盖。 | 任意正整数                                                   | 未设置 (null)                                         |
| defaultResultSetType             | 指定语句默认的滚动策略。（新增于 3.5.2）                     | FORWARD_ONLY \| SCROLL_SENSITIVE \| SCROLL_INSENSITIVE \| DEFAULT（等同于未设置） | 未设置 (null)                                         |
| safeRowBoundsEnabled             | 是否允许在嵌套语句中使用分页（RowBounds）。如果允许使用则设置为 false。 | true \| false                                                | False                                                 |
| safeResultHandlerEnabled         | 是否允许在嵌套语句中使用结果处理器（ResultHandler）。如果允许使用则设置为 false。 | true \| false                                                | True                                                  |
| mapUnderscoreToCamelCase         | 是否开启驼峰命名自动映射，即从经典数据库列名 A_COLUMN 映射到经典 Java 属性名 aColumn。 | true \| false                                                | False                                                 |
| localCacheScope                  | MyBatis 利用本地缓存机制（Local Cache）防止循环引用和加速重复的嵌套查询。 默认值为 SESSION，会缓存一个会话中执行的所有查询。 若设置值为 STATEMENT，本地缓存将仅用于执行语句，对相同 SqlSession 的不同查询将不会进行缓存。 | SESSION \| STATEMENT                                         | SESSION                                               |
| jdbcTypeForNull                  | 当没有为参数指定特定的 JDBC 类型时，空值的默认 JDBC 类型。 某些数据库驱动需要指定列的 JDBC 类型，多数情况直接用一般类型即可，比如 NULL、VARCHAR 或 OTHER。 | JdbcType 常量，常用值：NULL、VARCHAR 或 OTHER。              | OTHER                                                 |
| lazyLoadTriggerMethods           | 指定对象的哪些方法触发一次延迟加载。                         | 用逗号分隔的方法列表。                                       | equals,clone,hashCode,toString                        |
| defaultScriptingLanguage         | 指定动态 SQL 生成使用的默认脚本语言。                        | 一个类型别名或全限定类名。                                   | org.apache.ibatis.scripting.xmltags.XMLLanguageDriver |
| defaultEnumTypeHandler           | 指定 Enum 使用的默认 `TypeHandler` 。（新增于 3.4.5）        | 一个类型别名或全限定类名。                                   | org.apache.ibatis.type.EnumTypeHandler                |
| callSettersOnNulls               | 指定当结果集中值为 null 的时候是否调用映射对象的 setter（map 对象时为 put）方法，这在依赖于 Map.keySet() 或 null 值进行初始化时比较有用。注意基本类型（int、boolean 等）是不能设置成 null 的。 | true \| false                                                | false                                                 |
| returnInstanceForEmptyRow        | 当返回行的所有列都是空时，MyBatis默认返回 `null`。 当开启这个设置时，MyBatis会返回一个空实例。 请注意，它也适用于嵌套的结果集（如集合或关联）。（新增于 3.4.2） | true \| false                                                | false                                                 |
| logPrefix                        | 指定 MyBatis 增加到日志名称的前缀。                          | 任何字符串                                                   | 未设置                                                |
| logImpl                          | 指定 MyBatis 所用日志的具体实现，未指定时将自动查找。        | SLF4J \| LOG4J \| LOG4J2 \| JDK_LOGGING \| COMMONS_LOGGING \| STDOUT_LOGGING \| NO_LOGGING | 未设置                                                |
| proxyFactory                     | 指定 Mybatis 创建可延迟加载对象所用到的代理工具。            | CGLIB \| JAVASSIST                                           | JAVASSIST （MyBatis 3.3 以上）                        |
| vfsImpl                          | 指定 VFS 的实现                                              | 自定义 VFS 的实现的类全限定名，以逗号分隔。                  | 未设置                                                |
| useActualParamName               | 允许使用方法签名中的名称作为语句参数名称。 为了使用该特性，你的项目必须采用 Java 8 编译，并且加上 `-parameters` 选项。（新增于 3.4.1） | true \| false                                                | true                                                  |
| configurationFactory             | 指定一个提供 `Configuration` 实例的类。 这个被返回的 Configuration 实例用来加载被反序列化对象的延迟加载属性值。 这个类必须包含一个签名为`static Configuration getConfiguration()` 的方法。（新增于 3.2.3） | 一个类型别名或完全限定类名。                                 | 未设置                                                |
| shrinkWhitespacesInSql           | 移除SQL中多余的空格. 注意：这个设置同样影响字符串常量。 (从3.5.5开始) | true \| false                                                | false                                                 |

settings示例：

```xml
<settings>
  <setting name="cacheEnabled" value="true"/>
  <setting name="lazyLoadingEnabled" value="true"/>
  <setting name="multipleResultSetsEnabled" value="true"/>
  <setting name="useColumnLabel" value="true"/>
  <setting name="useGeneratedKeys" value="false"/>
  <setting name="autoMappingBehavior" value="PARTIAL"/>
  <setting name="autoMappingUnknownColumnBehavior" value="WARNING"/>
  <setting name="defaultExecutorType" value="SIMPLE"/>
  <setting name="defaultStatementTimeout" value="25"/>
  <setting name="defaultFetchSize" value="100"/>
  <setting name="safeRowBoundsEnabled" value="false"/>
  <setting name="mapUnderscoreToCamelCase" value="false"/>
  <setting name="localCacheScope" value="SESSION"/>
  <setting name="jdbcTypeForNull" value="OTHER"/>
  <setting name="lazyLoadTriggerMethods" value="equals,clone,hashCode,toString"/>
</settings>
```

### 3.3 typeAliases类型别名

类型别名可为 Java 类型设置一个缩写名字。 它仅用于 XML 配置，意在降低冗余的全限定类名书写。 有两种配置方式：

1. 针对单个Java Bean

   ```xml
   <typeAliases>
     <typeAlias alias="Author" type="domain.blog.Author"/>
     <typeAlias alias="Blog" type="domain.blog.Blog"/>
     <typeAlias alias="Comment" type="domain.blog.Comment"/>
     <typeAlias alias="Post" type="domain.blog.Post"/>
     <typeAlias alias="Section" type="domain.blog.Section"/>
     <typeAlias alias="Tag" type="domain.blog.Tag"/>
   </typeAliases>
   ```

   这样，就可以使用“Blog”替代“domain.blog.Blog”。

2. 针对整个包下的Bean

   ```xml
   <typeAliases>
     <package name="domain.blog"/>
   </typeAliases>
   ```

   这样，在没有注解的情况下，会使用 Bean 的首字母小写的非限定类名来作为它的别名。 比如 `domain.blog.Author` 的别名为 `author`；若有注解，则别名为其注解值。 如：

   ```java
   @Alias("Blog")
   class Blog{
       
   }
   ```

   Java内建类型的别名：

   | 别名       | 映射的类型 |
   | ---------- | ---------- |
   | _byte      | byte       |
   | _long      | long       |
   | _short     | short      |
   | _int       | int        |
   | _integer   | int        |
   | _double    | double     |
   | _float     | float      |
   | _boolean   | boolean    |
   | string     | String     |
   | byte       | Byte       |
   | long       | Long       |
   | short      | Short      |
   | int        | Integer    |
   | integer    | Integer    |
   | double     | Double     |
   | float      | Float      |
   | boolean    | Boolean    |
   | date       | Date       |
   | decimal    | BigDecimal |
   | bigdecimal | BigDecimal |
   | object     | Object     |
   | map        | Map        |
   | hashmap    | HashMap    |
   | list       | List       |
   | arraylist  | ArrayList  |
   | collection | Collection |
   | iterator   | Iterator   |



### 3.4 typeHandlers类型处理器

MyBatis 在设置预处理语句（PreparedStatement）中的参数或从结果集中取出一个值时， 都会用类型处理器将获取到的值以合适的方式转换成 Java 类型。默认的处理器：

| 类型处理器                   | Java 类型                       | JDBC 类型                                                    |
| ---------------------------- | ------------------------------- | ------------------------------------------------------------ |
| `BooleanTypeHandler`         | `java.lang.Boolean`, `boolean`  | 数据库兼容的 `BOOLEAN`                                       |
| `ByteTypeHandler`            | `java.lang.Byte`, `byte`        | 数据库兼容的 `NUMERIC` 或 `BYTE`                             |
| `ShortTypeHandler`           | `java.lang.Short`, `short`      | 数据库兼容的 `NUMERIC` 或 `SMALLINT`                         |
| `IntegerTypeHandler`         | `java.lang.Integer`, `int`      | 数据库兼容的 `NUMERIC` 或 `INTEGER`                          |
| `LongTypeHandler`            | `java.lang.Long`, `long`        | 数据库兼容的 `NUMERIC` 或 `BIGINT`                           |
| `FloatTypeHandler`           | `java.lang.Float`, `float`      | 数据库兼容的 `NUMERIC` 或 `FLOAT`                            |
| `DoubleTypeHandler`          | `java.lang.Double`, `double`    | 数据库兼容的 `NUMERIC` 或 `DOUBLE`                           |
| `BigDecimalTypeHandler`      | `java.math.BigDecimal`          | 数据库兼容的 `NUMERIC` 或 `DECIMAL`                          |
| `StringTypeHandler`          | `java.lang.String`              | `CHAR`, `VARCHAR`                                            |
| `ClobReaderTypeHandler`      | `java.io.Reader`                | -                                                            |
| `ClobTypeHandler`            | `java.lang.String`              | `CLOB`, `LONGVARCHAR`                                        |
| `NStringTypeHandler`         | `java.lang.String`              | `NVARCHAR`, `NCHAR`                                          |
| `NClobTypeHandler`           | `java.lang.String`              | `NCLOB`                                                      |
| `BlobInputStreamTypeHandler` | `java.io.InputStream`           | -                                                            |
| `ByteArrayTypeHandler`       | `byte[]`                        | 数据库兼容的字节流类型                                       |
| `BlobTypeHandler`            | `byte[]`                        | `BLOB`, `LONGVARBINARY`                                      |
| `DateTypeHandler`            | `java.util.Date`                | `TIMESTAMP`                                                  |
| `DateOnlyTypeHandler`        | `java.util.Date`                | `DATE`                                                       |
| `TimeOnlyTypeHandler`        | `java.util.Date`                | `TIME`                                                       |
| `SqlTimestampTypeHandler`    | `java.sql.Timestamp`            | `TIMESTAMP`                                                  |
| `SqlDateTypeHandler`         | `java.sql.Date`                 | `DATE`                                                       |
| `SqlTimeTypeHandler`         | `java.sql.Time`                 | `TIME`                                                       |
| `ObjectTypeHandler`          | Any                             | `OTHER` 或未指定类型                                         |
| `EnumTypeHandler`            | Enumeration Type                | VARCHAR 或任何兼容的字符串类型，用来存储枚举的名称（而不是索引序数值） |
| `EnumOrdinalTypeHandler`     | Enumeration Type                | 任何兼容的 `NUMERIC` 或 `DOUBLE` 类型，用来存储枚举的序数值（而不是名称）。 |
| `SqlxmlTypeHandler`          | `java.lang.String`              | `SQLXML`                                                     |
| `InstantTypeHandler`         | `java.time.Instant`             | `TIMESTAMP`                                                  |
| `LocalDateTimeTypeHandler`   | `java.time.LocalDateTime`       | `TIMESTAMP`                                                  |
| `LocalDateTypeHandler`       | `java.time.LocalDate`           | `DATE`                                                       |
| `LocalTimeTypeHandler`       | `java.time.LocalTime`           | `TIME`                                                       |
| `OffsetDateTimeTypeHandler`  | `java.time.OffsetDateTime`      | `TIMESTAMP`                                                  |
| `OffsetTimeTypeHandler`      | `java.time.OffsetTime`          | `TIME`                                                       |
| `ZonedDateTimeTypeHandler`   | `java.time.ZonedDateTime`       | `TIMESTAMP`                                                  |
| `YearTypeHandler`            | `java.time.Year`                | `INTEGER`                                                    |
| `MonthTypeHandler`           | `java.time.Month`               | `INTEGER`                                                    |
| `YearMonthTypeHandler`       | `java.time.YearMonth`           | `VARCHAR` 或 `LONGVARCHAR`                                   |
| `JapaneseDateTypeHandler`    | `java.time.chrono.JapaneseDate` | `DATE`                                                       |



#### 1.创建自定义的类型处理器 

方法：实现 `org.apache.ibatis.type.TypeHandler` 接口， 或继承 `org.apache.ibatis.type.BaseTypeHandler`， 并且可以（可选地）将它映射到一个 JDBC 类型 。

```java
@MappedJdbcTypes(JdbcType.VARCHAR)  	// 关联jdbc类型
public class AuthorTypeHandler extends BaseTypeHandler<Author> {


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Author parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getName());
    }

    @Override
    public Author getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String name = rs.getString(columnName);
        Author author = new Author();
        author.setName(name);
        return author;
    }

    @Override
    public Author getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String name = rs.getString(columnIndex);
        Author author = new Author();
        author.setName(name);
        return author;
    }

    @Override
    public Author getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String name = cs.getString(columnIndex);
        Author author = new Author();
        author.setName(name);
        return author;
    }
}

```

注册处理器：

```java
    <typeHandlers>
        <typeHandler handler="com.tcl.mybatislearning.handler.AuthorTypeHandler"/>
    </typeHandlers>
```

当在 `ResultMap` 中决定使用哪种类型处理器时，此时 Java 类型是已知的（从结果类型中获得），但是 JDBC 类型是未知的。 因此 Mybatis 使用 `javaType=[Java 类型], jdbcType=null` 的组合来选择一个类型处理器。 这意味着使用 `@MappedJdbcTypes` 注解可以*限制*类型处理器的作用范围，并且可以确保，除非显式地设置，否则类型处理器在 `ResultMap` 中将不会生效。 如果希望能在 `ResultMap` 中隐式地使用类型处理器，那么设置 `@MappedJdbcTypes` 注解的 `includeNullJdbcType=true` 即可。 然而从 Mybatis 3.4.0 开始，如果某个 Java 类型**只有一个**注册的类型处理器，即使没有设置 `includeNullJdbcType=true`，那么这个类型处理器也会是 `ResultMap` 使用 Java 类型时的默认处理器。 

#### 2.测试代码

实体类：

```java
@Data
public class Author {
    private Long id;
    private String name;
}
```

```java
@Data
public class Blog {

    private Long id;

    private String title;

    private String content;

    private Author author;

    private Date createTime;

    private Date updateTime;
}

```

XML映射文件：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.tcl.mybatislearning.mapper.BlogMapper">

    <resultMap id="blogMap" type="com.tcl.mybatislearning.entity.Blog">
        <id property="id" column="id"/>
        <result property="title" column="title"/>
        <result property="content" column="content"/>
        <result property="author" column="author" typeHandler="com.tcl.mybatislearning.handler.AuthorTypeHandler"/>
        <result property="createTime" column="create_time"/>
        <result property="updateTime" column="update_time"/>
    </resultMap>

    <insert id="insertBlog"
            parameterType="com.tcl.mybatislearning.entity.Blog"
            useGeneratedKeys="true"
            keyProperty="id">
        insert into blog (title, content, author)
        VALUES (#{title}, #{content}, #{author, typeHandler=com.tcl.mybatislearning.handler.AuthorTypeHandler});
    </insert>

    <select id="selectById" resultMap="blogMap">
        select *
        from Blog
        where id = #{id};
    </select>
</mapper>
```



### 3.5 ObjectFactory对象工厂

每次 MyBatis 创建结果对象的新实例时，它都会使用一个对象工厂（ObjectFactory）实例来完成实例化工作。 默认的对象工厂需要做的仅仅是实例化目标类，要么通过默认无参构造方法，要么通过存在的参数映射来调用带有参数的构造方法。 如果想覆盖对象工厂的默认行为，可以通过创建自己的对象工厂来实现。

实现方法是实现`ObjectFactory`接口或者继承`DefaultObjectFactory`类：

```java
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

```

注册自定义的对象工厂：

```xml
    <objectFactory type="com.tcl.mybatislearning.factory.MyObjectFactory">
    </objectFactory>
```

### 3.6 plugins插件

Mybatis允许使用插件来拦截下列方法的调用，实现监控或者重写的目的。

- Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query)

 只需实现 Interceptor 接口，并指定想要拦截的方法签名即可实现简单的插件。

```java
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
```

注册插件：

```xml
    <plugins>
        <plugin interceptor="com.tcl.mybatislearning.interceptor.SelectInterceptor"/>
    </plugins>
```



### 3.7 environments环境配置

#### 3.7.1 环境列表

MyBatis可以通过多种environment配置来适应多种环境 ，如生产，测试，开发环境或者多个生产数据库。但每个SqlSessionFactory实例只能使用一种环境。也就是**每个数据库对应一个 SqlSessionFactory 实例** 。

为了指定创建哪种环境，只要将它作为可选的参数传递给 SqlSessionFactoryBuilder 即可：

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment, properties);
```

如果不指定环境参数，则使用默认环境：

```xml
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${username}"/>
            </dataSource>
        </environment>
    </environments>
```

配置解析：

1. `<environments default="development">`：环境列表，可以指定默认使用的环境，ID值必须对应于某个环境的ID
2. `<environment id="development">`：一种环境，ID是自定义的
3. `<transactionManager type="JDBC"/>`：事务管理器配置
4. `<dataSource type="POOLED">`：数据源配置

#### 3.7.2 事务管理器

Mybatis包含两种事务管理器类型：**JDBC**和**MANAGED**

1. JDBC：直接使用了 JDBC 的提交和回滚设施，它依赖从数据源获得的连接来管理事务作用域。 
2. MANAGED：它从不提交或回滚一个连接，而是让容器来管理事务的整个生命周期 。

两者都是类型别名，分别对应于`TransactionFactory`接口的`JdbcTransactionFactory`和`ManagedTransactionFactory`实现类。源码见配置类：

```java
// org.apache.ibatis.session.Configuration.java
public Configuration() {
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);
...
  }
```

你可以用 `TransactionFactory` 接口实现类的全限定名或类型别名代替它们。

#### 3.7.3 数据源

dataSource 元素使用 JDBC 数据源接口来配置 JDBC 连接对象的资源。Mybatis有三种内建的数据源类型 ：

1. **UNPOOLED**– 这个数据源的实现会每次请求时打开和关闭连接 。可配置属性：

   - `driver` – 这是 JDBC 驱动的 Java 类全限定名（并不是 JDBC 驱动中可能包含的数据源类）。
   - `url` – 这是数据库的 JDBC URL 地址。
   - `username` – 登录数据库的用户名。
   - `password` – 登录数据库的密码。
   - `defaultTransactionIsolationLevel` – 默认的连接事务隔离级别。
   - `defaultNetworkTimeout` – 等待数据库操作完成的默认网络超时时间（单位：毫秒）。查看 `java.sql.Connection#setNetworkTimeout()` 的 API 文档以获取更多信息。

   作为可选项，你也可以传递属性给数据库驱动。只需在属性名加上“driver.”前缀即可，例如：

   - `driver.encoding=UTF8`

2. **POOLED**– 利用“连接池”将 JDBC 连接对象缓存起来，避免了频繁创建新的连接实例时所必需的初始化和认证时间。 可配置属性：

   - `poolMaximumActiveConnections` – 在任意时间可存在的活动（正在使用）连接数量，默认值：10
   - `poolMaximumIdleConnections` – 任意时间可能存在的空闲连接数。
   - `poolMaximumCheckoutTime` – 在被强制返回之前，池中连接被检出（checked out）时间，默认值：20000 毫秒（即 20 秒）
   - `poolTimeToWait` – 这是一个底层设置，如果获取连接花费了相当长的时间，连接池会打印状态日志并重新尝试获取一个连接（避免在误配置的情况下一直失败且不打印日志），默认值：20000 毫秒（即 20 秒）。
   - `poolMaximumLocalBadConnectionTolerance` – 这是一个关于坏连接容忍度的底层设置， 作用于每一个尝试从缓存池获取连接的线程。 如果这个线程获取到的是一个坏的连接，那么这个数据源允许这个线程尝试重新获取一个新的连接，但是这个重新尝试的次数不应该超过 `poolMaximumIdleConnections` 与 `poolMaximumLocalBadConnectionTolerance` 之和。 默认值：3（新增于 3.4.5）
   - `poolPingQuery` – 发送到数据库的侦测查询，用来检验连接是否正常工作并准备接受请求。默认是“NO PING QUERY SET”，这会导致多数数据库驱动出错时返回恰当的错误消息。
   - `poolPingEnabled` – 是否启用侦测查询。若开启，需要设置 `poolPingQuery` 属性为一个可执行的 SQL 语句（最好是一个速度非常快的 SQL 语句），默认值：false。
   - `poolPingConnectionsNotUsedFor` – 配置 poolPingQuery 的频率。可以被设置为和数据库连接超时时间一样，来避免不必要的侦测，默认值：0（即所有连接每一时刻都被侦测 — 当然仅当 poolPingEnabled 为 true 时适用）。

3. **JNDI** – 这个数据源实现是为了能在如 EJB 或应用服务器这类容器中使用，容器可以集中或在外部配置数据源，然后放置一个 JNDI 上下文的数据源引用。可配置属性：

   - `initial_context` – 这个属性用来在 InitialContext 中寻找上下文（即，initialContext.lookup(initial_context)）。这是个可选属性，如果忽略，那么将会直接从 InitialContext 中寻找 data_source 属性。
   - `data_source` – 这是引用数据源实例位置的上下文路径。提供了 initial_context 配置时会在其返回的上下文中进行查找，没有提供时则直接在 InitialContext 中查找。

   和其他数据源配置类似，可以通过添加前缀“env.”直接把属性传递给 InitialContext。比如：

   - `env.encoding=UTF8`

#### 3.7.4 使用第三方数据源

只需实现接口 `org.apache.ibatis.datasource.DataSourceFactory`  即可使用第三方数据源实现。实际上，`org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory` 可被用作父类来构建新的数据源适配器。`PooledDataSourceFactory`就是继承自它，只是替换了数据源实现类。

```java
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

  public PooledDataSourceFactory() {
    this.dataSource = new PooledDataSource();
  }

}
```

##### 1.使用C3P0数据源实现：

依赖：

```xml
        <dependency>
            <groupId>com.mchange</groupId>
            <artifactId>c3p0</artifactId>
            <version>0.9.5.5</version>
        </dependency>
```

工厂类：

```java
public class C3P0DataSourceFactory extends UnpooledDataSourceFactory {

    public C3P0DataSourceFactory() {
        this.dataSource = new ComboPooledDataSource();
    }
}
```



数据源配置mybatis-config.xml：

```xml
            <dataSource type="com.tcl.mybatislearning.datasource.C3P0DataSourceFactory">
                <property name="driverClass" value="${driver}"/>
                <property name="jdbcUrl" value="${url}"/>
                <property name="user" value="${username}"/>
                <property name="password" value="${username}"/>
            </dataSource>
```

**踩坑点**：属性名称变成了：driverClass，jdbcUrl以及user

##### 2.使用HikariCP数据源实现：

1. 依赖添加

   ```xml
       <dependency>
         <groupId>com.zaxxer</groupId>
         <artifactId>HikariCP</artifactId>
         <version>3.4.5</version>
       </dependency>
   ```

   

2. 使用方法一

   工厂类：

   ```java
   public class HikariDataSourceFactory extends UnpooledDataSourceFactory {
       public HikariDataSourceFactory() {
           this.dataSource = new HikariDataSource();
       }
   }
   ```

   数据源配置：

   ```xml
               <dataSource type="com.tcl.mybatislearning.datasource.HikariDataSourceFactory">
                   <property name="jdbcUrl" value="${url}"/>
                   <property name="username" value="${username}"/>
                   <property name="password" value="${username}"/>
               </dataSource>
   ```

   **踩坑点**：属性名又有变化。

3. 使用方法二：

   编写外部配置文件`resources/hikariCP.properties`，里面还包含一些MySQL的优化参数。[参考链接](https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration)

   ```properties
   jdbcUrl=jdbc:mysql://localhost:3306/mybatis_learning?serverTimezone=Asia/Shanghai
   username=root
   password=root
   dataSource.cachePrepStmts=true
   dataSource.prepStmtCacheSize=250
   dataSource.prepStmtCacheSqlLimit=2048
   dataSource.useServerPrepStmts=true
   dataSource.useLocalSessionState=true
   dataSource.rewriteBatchedStatements=true
   dataSource.cacheResultSetMetadata=true
   dataSource.cacheServerConfiguration=true
   dataSource.elideSetAutoCommits=true
   dataSource.maintainTimeStats=false
   ```

   工厂类：

   ```java
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
           // 使用MyBatis提供的工具类读取类路径下的properties文件
           InputStream inputStream = Resources.getResourceAsStream("hikariCP.properties");
           Properties props = new Properties();
           props.load(inputStream);
           HikariConfig hikariConfig = new HikariConfig(props);
           this.dataSource = new HikariDataSource(hikariConfig);
       }
   }
   ```

   数据源配置：

   ```xml
               <dataSource type="com.tcl.mybatislearning.datasource.HikariDataSourceFactory">
               </dataSource>
   ```

   **采坑点**：`<dataSource>`标签中不要重复配置。

​      

##### 3.使用Druid数据源实现

1. 编写属性文件`resources/druid.properties`

   ```properties
   druid.url=jdbc:mysql://localhost:3306/mybatis_learning?serverTimezone=Asia/Shanghai
   druid.username=root
   druid.password=root
   ```

   

2. 工厂类

   ```java
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
   ```

   

3. 数据源配置

   ```xml
               <dataSource type="com.tcl.mybatislearning.datasource.DruidDataSourceFactory">
               </dataSource>
   ```



### 3.8 mappers映射器

mappers标签定义包含 SQL 映射语句的XML映射文件或者Mapper接口的位置。可以使用类路径相对位置或者绝对路径位置。

```xml
<!-- 使用相对于类路径的资源引用 -->
<mappers>
  <mapper resource="org/mybatis/builder/AuthorMapper.xml"/>
  <mapper resource="org/mybatis/builder/BlogMapper.xml"/>
  <mapper resource="org/mybatis/builder/PostMapper.xml"/>
</mappers>
```

```xml
<!-- 使用完全限定资源定位符（URL） -->
<mappers>
  <mapper url="file:///var/mappers/AuthorMapper.xml"/>
  <mapper url="file:///var/mappers/BlogMapper.xml"/>
  <mapper url="file:///var/mappers/PostMapper.xml"/>
</mappers>
```

```xml
<!-- 使用映射器接口实现类的完全限定类名 -->
<mappers>
  <mapper class="org.mybatis.builder.AuthorMapper"/>
  <mapper class="org.mybatis.builder.BlogMapper"/>
  <mapper class="org.mybatis.builder.PostMapper"/>
</mappers>
```

```xml
<!-- 将包内的映射器接口实现全部注册为映射器 -->
<mappers>
  <package name="org.mybatis.builder"/>
</mappers>
```



## 4. SQL映射文件

SQL 映射文件将Mapper接口的方法和SQL语句一一对应，它只有很少的几个顶级元素 ：

- `cache` – 该命名空间的缓存配置。
- `cache-ref` – 引用其它命名空间的缓存配置。
- `resultMap` – 描述如何从数据库结果集中加载对象，是最复杂也是最强大的元素。
- `sql` – 可被其它语句引用的可重用语句块。
- `insert` – 映射插入语句。
- `update` – 映射更新语句。
- `delete` – 映射删除语句。
- `select` – 映射查询语句。

一个空白的SQL映射文件如下：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="">

</mapper>
```





### 4.1 select元素

MyBatis使用select元素来执行查询操作，示例：

```xml
<select id="selectPerson" parameterType="int" resultType="hashmap">
  SELECT * FROM PERSON WHERE ID = #{id}
</select>
```

解析：

- id：Mapper接口的方法名
- parameterType：参数类型，可省略
- resultType：返回值类型，hashmap是`java.util.HashMap`的类型别名
- `#{id}`：预处理语句参数，“id”默认是方法参数名称，可以使用`@Param`注解指定

select元素的所有属性如下表：

| 属性            | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| `id`            | 在命名空间中唯一的标识符，可以被用来引用这条语句。           |
| `parameterType` | 将会传入这条语句的参数的类全限定名或别名。这个属性是可选的，因为 MyBatis 可以通过类型处理器（TypeHandler）推断出具体传入语句的参数，默认值为未设置（unset）。 |
| `resultType`    | 期望从这条语句中返回结果的类全限定名或别名。 **注意，如果返回的是集合，那应该设置为集合包含的类型，而不是集合本身的类型**。 resultType 和 resultMap 之间只能同时使用一个。 |
| `resultMap`     | 对外部 resultMap 的命名引用。结果映射是 MyBatis 最强大的特性，如果你对其理解透彻，许多复杂的映射问题都能迎刃而解。 **resultType 和 resultMap 之间只能同时使用一个**。 |
| `flushCache`    | 将其设置为 true 后，只要语句被调用，都会导致本地缓存和二级缓存被清空，默认值：false。 |
| `useCache`      | 将其设置为 true 后，将会导致本条语句的结果被二级缓存缓存起来，默认值：对 select 元素为 true。 |
| `timeout`       | 这个设置是在抛出异常之前，驱动程序等待数据库返回请求结果的秒数。默认值为未设置（unset）（依赖数据库驱动）。 |
| `fetchSize`     | 这是一个给驱动的建议值，尝试让驱动程序每次批量返回的结果行数等于这个设置值。 默认值为未设置（unset）（依赖驱动）。 |
| `statementType` | 可选 STATEMENT，PREPARED 或 CALLABLE。这会让 MyBatis 分别使用 Statement，PreparedStatement 或 CallableStatement，默认值：PREPARED。 |
| `resultSetType` | FORWARD_ONLY，SCROLL_SENSITIVE, SCROLL_INSENSITIVE 或 DEFAULT（等价于 unset） 中的一个，默认值为 unset （依赖数据库驱动）。 |
| `databaseId`    | 如果配置了数据库厂商标识（databaseIdProvider），MyBatis 会加载所有不带 databaseId 或匹配当前 databaseId 的语句；如果带和不带的语句都有，则不带的会被忽略。 |
| `resultOrdered` | 这个设置仅针对嵌套结果 select 语句：如果为 true，将会假设包含了嵌套结果集或是分组，当返回一个主结果行时，就不会产生对前面结果集的引用。 这就使得在获取嵌套结果集的时候不至于内存不够用。默认值：`false`。 |
| `resultSets`    | 这个设置仅适用于多结果集的情况。它将列出语句执行后返回的结果集并赋予每个结果集一个名称，多个名称之间以逗号分隔。 |

### 4.2 insert/update/delete元素

增删改的元素非常接近。示例：

```xml
<insert id="insertAuthor">
  insert into Author (id,username,password,email,bio)
  values (#{id},#{username},#{password},#{email},#{bio})
</insert>

<update id="updateAuthor">
  update Author set
    username = #{username},
    password = #{password},
    email = #{email},
    bio = #{bio}
  where id = #{id}
</update>

<delete id="deleteAuthor">
  delete from Author where id = #{id}
</delete>
```

所有元素属性表：

| 属性               | 描述                                                         |
| ------------------ | ------------------------------------------------------------ |
| `id`               | 在命名空间中唯一的标识符，可以被用来引用这条语句。           |
| `parameterType`    | 将会传入这条语句的参数的类全限定名或别名。这个属性是可选的，因为 MyBatis 可以通过类型处理器（TypeHandler）推断出具体传入语句的参数，默认值为未设置（unset）。 |
| `flushCache`       | 将其设置为 true 后，只要语句被调用，都会导致本地缓存和二级缓存被清空，默认值：（对 insert、update 和 delete 语句）true。 |
| `timeout`          | 这个设置是在抛出异常之前，驱动程序等待数据库返回请求结果的秒数。默认值为未设置（unset）（依赖数据库驱动）。 |
| `statementType`    | 可选 STATEMENT，PREPARED 或 CALLABLE。这会让 MyBatis 分别使用 Statement，PreparedStatement 或 CallableStatement，默认值：PREPARED。 |
| `useGeneratedKeys` | （仅适用于 insert 和 update）这会令 MyBatis 使用 JDBC 的 getGeneratedKeys 方法来取出由数据库内部生成的主键（比如：像 MySQL 和 SQL Server 这样的关系型数据库管理系统的自动递增字段），默认值：false。 |
| `keyProperty`      | （仅适用于 insert 和 update）指定能够唯一识别对象的属性，MyBatis 会使用 getGeneratedKeys 的返回值或 insert 语句的 selectKey 子元素设置它的值，默认值：未设置（`unset`）。如果生成列不止一个，可以用逗号分隔多个属性名称。 |
| `keyColumn`        | （仅适用于 insert 和 update）设置生成键值在表中的列名，在某些数据库（像 PostgreSQL）中，当主键列不是表中的第一列的时候，是必须设置的。如果生成列不止一个，可以用逗号分隔多个属性名称。 |
| `databaseId`       | 如果配置了数据库厂商标识（databaseIdProvider），MyBatis 会加载所有不带 databaseId 或匹配当前 databaseId 的语句；如果带和不带的语句都有，则不带的会被忽略。 |

**在插入时，返回并设置自动生成的主键**：只需设置 `useGeneratedKeys="true"`，然后再把 `keyProperty` 设置为目标属性即可：

```xml
<insert id="insertAuthor" useGeneratedKeys="true"
    keyProperty="id">
  insert into Author (username,password,email,bio)
  values (#{username},#{password},#{email},#{bio})
</insert>
```



### 4.3 sql元素

sql元素可以用来定义可重用的 SQL 代码片段，以便在其它语句中使用。参数可以在include标签中定义。示例：

```xml
    <sql id="blogColumns">
        ${alias}.id, ${alias}.title, ${alias}.content, ${alias}.author, ${alias}.create_time, ${alias}.update_time
    </sql>
    
```

引用：

```xml
    <select id="selectAll" resultType="com.tcl.mybatislearning.entity.Blog">
        select
            <include refid="blogColumns">
                <property name="alias" value="b"/>
            </include>
        from blog as b;
    </select>
```



### 4.4 参数

**简单参数**

```xml
    <select id="selectById" resultMap="blogMap">
        select *
        from Blog
        where id = #{id};
    </select>
```

因为原始类型如int或者简单类型如String没有其他属性，因此会使用值来作为参数，也不需要显示指定参数类型。

**复杂参数**

```xml
    <insert id="insertBlog"
            parameterType="com.tcl.mybatislearning.entity.Blog"
            useGeneratedKeys="true"
            keyProperty="id">
        insert into blog (title, content, author)
        VALUES (#{title}, #{content}, #{author});
    </insert>
```

如果是一个复杂类型的参数，那么它的属性会被作为预处理语句的参数。

**指定参数类型**

参数也可以指定一个特殊的数据类型：

```xml
    <select id="selectById" resultMap="blogMap">
        select *
        from Blog
        where id = #{id, javaType=Long, jdbcType=bigint};
    </select>
```

除非该对象是一个 `HashMap`。这个时候，你需要显式指定 `javaType` 来确保正确的类型处理器（`TypeHandler`）被使用。一般总是可以根据参数对象的类型来确定javaType。

**字符串替换**

默认情况下，使用 `#{}` 参数语法时，MyBatis 会创建 `PreparedStatement` 参数占位符，并通过占位符安全地设置参数（就像使用 ? 一样）。 这样做更安全，更迅速，通常也是首选做法，不过有时你就是想直接在 SQL 语句中直接插入一个不转义的字符串。 例如`Order By`语句，这时可以使用`${}`语法，但是这种语法有SQL注入的危险。



### 4.5 结果映射

#### 4.5.1 简单映射

结果映射`resultMap`是MyBatis将`ResultSets`转换为POJO的核心。示例：

```xml
    <resultMap id="blogMap" type="com.tcl.mybatislearning.entity.Blog">
        <id property="id" column="id"/>
        <result property="title" column="title"/>
        <result property="content" column="content"/>
        <result property="author" column="author" typeHandler="com.tcl.mybatislearning.handler.AuthorTypeHandler"/>
        <result property="createTime" column="create_time"/>
        <result property="updateTime" column="update_time"/>
    </resultMap>

```

定义好之后便可引用：

```xml
    <select id="selectById" resultMap="blogMap">
        select *
        from Blog
        where id = #{id};
    </select>
```

`resultMap`属性解析：

| 属性          | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| `id`          | 当前命名空间中的一个唯一标识，用于标识一个结果映射。         |
| `type`        | 类的完全限定名, 或者一个类型别名（关于内置的类型别名，可以参考上面的表格）。 |
| `autoMapping` | 如果设置这个属性，MyBatis 将会为本结果映射开启或者关闭自动映射。 这个属性会覆盖全局的属性 autoMappingBehavior。默认值：未设置（unset）。 |



#### 4.5.2 高级映射

实际上，简单映射可以不用显示设置`resultMap`。

`resultMap` 元素有很多子元素和一个值得深入探讨的结构。 下面是`resultMap` 元素的概念视图。

- constructor - 用于在实例化类时，注入结果到构造方法中 
  - idArg - ID 参数；标记出作为 ID 的结果可以帮助提高整体性能 
  - arg - 将被注入到构造方法的一个普通结果 
- id – 一个 ID 结果；标记出作为 ID 的结果可以帮助提高整体性能 
- result – 注入到字段或 JavaBean 属性的普通结果 
- association – 一个复杂类型的关联；许多结果将包装成这种类型 
  - 嵌套结果映射 – 关联可以是 resultMap 元素，或是对其它结果映射的引用 
- collection – 一个复杂类型的集合 
  - 嵌套结果映射 – 集合可以是 resultMap 元素，或是对其它结果映射的引用 
- discriminator – 使用结果值来决定使用哪个 resultMap 
  - case – 基于某些值的结果映射 
    - 嵌套结果映射 – case 也是一个结果映射，因此具有相同的结构和元素；或者引用其它的结果映射

 示例：

```xml
<!-- 非常复杂的结果映射 -->
<resultMap id="detailedBlogResultMap" type="Blog">
  <constructor>
    <idArg column="blog_id" javaType="int"/>
  </constructor>
  <result property="title" column="blog_title"/>
  <association property="author" javaType="Author">
    <id property="id" column="author_id"/>
    <result property="username" column="author_username"/>
    <result property="password" column="author_password"/>
    <result property="email" column="author_email"/>
    <result property="bio" column="author_bio"/>
    <result property="favouriteSection" column="author_favourite_section"/>
  </association>
  <collection property="posts" ofType="Post">
    <id property="id" column="post_id"/>
    <result property="subject" column="post_subject"/>
    <association property="author" javaType="Author"/>
    <collection property="comments" ofType="Comment">
      <id property="id" column="comment_id"/>
    </collection>
    <collection property="tags" ofType="Tag" >
      <id property="id" column="tag_id"/>
    </collection>
    <discriminator javaType="int" column="draft">
      <case value="1" resultType="DraftPost"/>
    </discriminator>
  </collection>
</resultMap>
```

复杂SQL语句：

```xml
<!-- 非常复杂的语句 -->
<select id="selectBlogDetails" resultMap="detailedBlogResultMap">
  select
       B.id as blog_id,
       B.title as blog_title,
       B.author_id as blog_author_id,
       A.id as author_id,
       A.username as author_username,
       A.password as author_password,
       A.email as author_email,
       A.bio as author_bio,
       A.favourite_section as author_favourite_section,
       P.id as post_id,
       P.blog_id as post_blog_id,
       P.author_id as post_author_id,
       P.created_on as post_created_on,
       P.section as post_section,
       P.subject as post_subject,
       P.draft as draft,
       P.body as post_body,
       C.id as comment_id,
       C.post_id as comment_post_id,
       C.name as comment_name,
       C.comment as comment_text,
       T.id as tag_id,
       T.name as tag_name
  from Blog B
       left outer join Author A on B.author_id = A.id
       left outer join Post P on B.id = P.blog_id
       left outer join Comment C on P.id = C.post_id
       left outer join Post_Tag PT on PT.post_id = P.id
       left outer join Tag T on PT.tag_id = T.id
  where B.id = #{id}
</select>
```



#### 4.5.3 resultMap子元素

##### 1.id & result

- 相同点：*id* 和 *result* 元素都将一个列的值映射到一个简单数据类型（String, int, double, Date 等）的属性或字段 。
- 不同点：*id* 元素对应的属性会被标记为对象的标识符，在比较对象实例时使用。 这样可以提高整体的性能，尤其是进行缓存和嵌套结果映射（也就是连接映射）的时候。 

属性列表：

| 属性          | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| `property`    | 映射到列结果的字段或属性。如果 JavaBean 有这个名字的属性（property），会先使用该属性。否则 MyBatis 将会寻找给定名称的字段（field）。 无论是哪一种情形，你都可以使用常见的点式分隔形式进行复杂属性导航。 比如，你可以这样映射一些简单的东西：“username”，或者映射到一些复杂的东西上：“address.street.number”。 |
| `column`      | 数据库中的列名，或者是列的别名。一般情况下，这和传递给 `resultSet.getString(columnName)` 方法的参数一样。 |
| `javaType`    | 一个 Java 类的全限定名，或一个类型别名（关于内置的类型别名，可以参考上面的表格）。 如果你映射到一个 JavaBean，MyBatis 通常可以推断类型。然而，如果你映射到的是 HashMap，那么你应该明确地指定 javaType 来保证行为与期望的相一致。 |
| `jdbcType`    | JDBC 类型，所支持的 JDBC 类型参见这个表格之后的“支持的 JDBC 类型”。 只需要在可能执行插入、更新和删除的且允许空值的列上指定 JDBC 类型。这是 JDBC 的要求而非 MyBatis 的要求。如果你直接面向 JDBC 编程，你需要对可以为空值的列指定这个类型。 |
| `typeHandler` | 我们在前面讨论过默认的类型处理器。使用这个属性，你可以覆盖默认的类型处理器。 这个属性值是一个类型处理器实现类的全限定名，或者是类型别名。 |

##### 2.constructor

除了直接设置JavaBean属性值之外，MyBatis还支持构造方法注入的方式设置属性的值，以此初始化类实例。这种方法通常适用于不可变类。

```xml
<constructor>
   <idArg column="id" javaType="int"/>
   <arg column="username" javaType="String"/>
   <arg column="age" javaType="_int"/>
</constructor>
```

属性列表：

| 属性          | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| `column`      | 数据库中的列名，或者是列的别名。一般情况下，这和传递给 `resultSet.getString(columnName)` 方法的参数一样。 |
| `javaType`    | 一个 Java 类的完全限定名，或一个类型别名（关于内置的类型别名，可以参考上面的表格）。 如果你映射到一个 JavaBean，MyBatis 通常可以推断类型。然而，如果你映射到的是 HashMap，那么你应该明确地指定 javaType 来保证行为与期望的相一致。 |
| `jdbcType`    | JDBC 类型，所支持的 JDBC 类型参见这个表格之前的“支持的 JDBC 类型”。 只需要在可能执行插入、更新和删除的且允许空值的列上指定 JDBC 类型。这是 JDBC 的要求而非 MyBatis 的要求。如果你直接面向 JDBC 编程，你需要对可能存在空值的列指定这个类型。 |
| `typeHandler` | 我们在前面讨论过默认的类型处理器。使用这个属性，你可以覆盖默认的类型处理器。 这个属性值是一个类型处理器实现类的完全限定名，或者是类型别名。 |
| `select`      | 用于加载复杂类型属性的映射语句的 ID，它会从 column 属性中指定的列检索数据，作为参数传递给此 select 语句。具体请参考关联元素。 |
| `resultMap`   | 结果映射的 ID，可以将嵌套的结果集映射到一个合适的对象树中。 它可以作为使用额外 select 语句的替代方案。它可以将多表连接操作的结果映射成一个单一的 `ResultSet`。这样的 `ResultSet` 将会将包含重复或部分数据重复的结果集。为了将结果集正确地映射到嵌套的对象树中，MyBatis 允许你 “串联”结果映射，以便解决嵌套结果集的问题。想了解更多内容，请参考下面的关联元素。 |
| `name`        | 构造方法形参的名字。从 3.4.3 版本开始，通过指定具体的参数名，你可以以任意顺序写入 arg 元素。参看上面的解释。 |



##### 3.association 

关联（association）元素用来处理复杂类型的字段。

```xml
<association property="author" column="blog_author_id" javaType="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
</association>
```

MyBatis提供两种加载关联字段的方式：

- 嵌套 Select 查询：通过执行另外一个 SQL 映射语句来加载期望的复杂类型。
- 嵌套结果映射：使用嵌套的结果映射来处理连接结果的重复子集。

属性列表：

**基本属性**

| 属性          | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| `property`    | 映射到列结果的字段或属性。如果用来匹配的 JavaBean 存在给定名字的属性，那么它将会被使用。否则 MyBatis 将会寻找给定名称的字段。 无论是哪一种情形，你都可以使用通常的点式分隔形式进行复杂属性导航。 比如，你可以这样映射一些简单的东西：“username”，或者映射到一些复杂的东西上：“address.street.number”。 |
| `javaType`    | 一个 Java 类的完全限定名，或一个类型别名（关于内置的类型别名，可以参考上面的表格）。 如果你映射到一个 JavaBean，MyBatis 通常可以推断类型。然而，如果你映射到的是 HashMap，那么你应该明确地指定 javaType 来保证行为与期望的相一致。 |
| `jdbcType`    | JDBC 类型，所支持的 JDBC 类型参见这个表格之前的“支持的 JDBC 类型”。 只需要在可能执行插入、更新和删除的且允许空值的列上指定 JDBC 类型。这是 JDBC 的要求而非 MyBatis 的要求。如果你直接面向 JDBC 编程，你需要对可能存在空值的列指定这个类型。 |
| `typeHandler` | 我们在前面讨论过默认的类型处理器。使用这个属性，你可以覆盖默认的类型处理器。 这个属性值是一个类型处理器实现类的完全限定名，或者是类型别名。 |

**有关嵌套查询Select的属性**

| 属性        | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| `column`    | 数据库中的列名，或者是列的别名。一般情况下，这和传递给 `resultSet.getString(columnName)` 方法的参数一样。 注意：在使用复合主键的时候，你可以使用 `column="{prop1=col1,prop2=col2}"` 这样的语法来指定多个传递给嵌套 Select 查询语句的列名。这会使得 `prop1` 和 `prop2` 作为参数对象，被设置为对应嵌套 Select 语句的参数。 |
| `select`    | 用于加载复杂类型属性的映射语句的 ID，它会从 column 属性指定的列中检索数据，作为参数传递给目标 select 语句。 具体请参考下面的例子。注意：在使用复合主键的时候，你可以使用 `column="{prop1=col1,prop2=col2}"` 这样的语法来指定多个传递给嵌套 Select 查询语句的列名。这会使得 `prop1` 和 `prop2` 作为参数对象，被设置为对应嵌套 Select 语句的参数。 |
| `fetchType` | 可选的。有效值为 `lazy` 和 `eager`。 指定属性后，将在映射中忽略全局配置参数 `lazyLoadingEnabled`，使用属性的值。 |

示例：

```xml
<resultMap id="blogResult" type="Blog">
  <association property="author" column="author_id" javaType="Author" select="selectAuthor"/>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  SELECT * FROM BLOG WHERE ID = #{id}
</select>

<select id="selectAuthor" resultType="Author">
  SELECT * FROM AUTHOR WHERE ID = #{id}
</select>
```

这种方式虽然很简单，但在大型数据集或大型数据表上表现不佳。导致“N+1 查询问题” 。 

**有关嵌套结果映射的属性**

| 属性            | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| `resultMap`     | 结果映射的 ID，可以将此关联的嵌套结果集映射到一个合适的对象树中。 它可以作为使用额外 select 语句的替代方案。它可以将多表连接操作的结果映射成一个单一的 `ResultSet`。这样的 `ResultSet` 有部分数据是重复的。 为了将结果集正确地映射到嵌套的对象树中, MyBatis 允许你“串联”结果映射，以便解决嵌套结果集的问题。使用嵌套结果映射的一个例子在表格以后。 |
| `columnPrefix`  | 当连接多个表时，你可能会不得不使用列别名来避免在 `ResultSet` 中产生重复的列名。指定 columnPrefix 列名前缀允许你将带有这些前缀的列映射到一个外部的结果映射中。 详细说明请参考后面的例子。 |
| `notNullColumn` | 默认情况下，在至少一个被映射到属性的列不为空时，子对象才会被创建。 你可以在这个属性上指定非空的列来改变默认行为，指定后，Mybatis 将只在这些列非空时才创建一个子对象。可以使用逗号分隔来指定多个列。默认值：未设置（unset）。 |
| `autoMapping`   | 如果设置这个属性，MyBatis 将会为本结果映射开启或者关闭自动映射。 这个属性会覆盖全局的属性 autoMappingBehavior。注意，本属性对外部的结果映射无效，所以不能搭配 `select` 或 `resultMap` 元素使用。默认值：未设置（unset）。 |

嵌套关联的简单例子：

```xml
<select id="selectBlog" resultMap="blogResult">
  select
    B.id            as blog_id,
    B.title         as blog_title,
    B.author_id     as blog_author_id,
    A.id            as author_id,
    A.username      as author_username,
    A.password      as author_password,
    A.email         as author_email,
    A.bio           as author_bio
  from Blog B left outer join Author A on B.author_id = A.id
  where B.id = #{id}
</select>

<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author" column="blog_author_id" javaType="Author" resultMap="authorResult"/>
</resultMap>

<resultMap id="authorResult" type="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
  <result property="password" column="author_password"/>
  <result property="email" column="author_email"/>
  <result property="bio" column="author_bio"/>
</resultMap>
```

如果`authorResult`不需要重用，可以直接嵌套：

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author" javaType="Author">
    <id property="id" column="author_id"/>
    <result property="username" column="author_username"/>
    <result property="password" column="author_password"/>
    <result property="email" column="author_email"/>
    <result property="bio" column="author_bio"/>
  </association>
</resultMap>
```

##### 4.collection 

集合元素的属性与关联元素的相似。其中也可以通过嵌套Select和嵌套关联进行映射。

```xml
<collection property="posts" ofType="domain.blog.Post">
  <id property="id" column="post_id"/>
  <result property="subject" column="post_subject"/>
  <result property="body" column="post_body"/>
</collection>
```





### 4.6 缓存

MyBatis 内置了两级缓存：

- 一级缓存：会话缓存，仅仅对一个SqlSession实例中的数据进行缓存。默认开启。

- 二级缓存：全局缓存，对SqlSessionFactory实例中的数据进行缓存。开启方式：在SQL映射文件中添加一行

  ```xml
  <cache/>
  ```

  该配置只对所在的SQL映射文件中起作用。所起的效果如下：

  - 映射文件中的所有 select 语句的结果将会被缓存。
  - 映射文件中的所有 insert、update 和 delete 语句会刷新缓存。
  - 缓存会使用最近最少使用算法（LRU, Least Recently Used）算法来清除不需要的缓存。
  - 缓存不会定时进行刷新（也就是说，没有刷新间隔）。
  - 缓存会保存列表或对象（无论查询方法返回哪种）的 1024 个引用。
  - 缓存会被视为读/写缓存，这意味着获取到的对象并不是共享的，可以安全地被调用者修改，而不干扰其他调用者或线程所做的潜在修改。

  缓存算法，刷新时间，缓存个数和读写类型可以通过`<cache>`元素的属性进行修改：

  - eviction（清除策略）：默认是LRU，可选项如下
    - `LRU` – 最近最少使用：移除最长时间不被使用的对象。
    - `FIFO` – 先进先出：按对象进入缓存的顺序来移除它们。
    - `SOFT` – 软引用：基于垃圾回收器状态和软引用规则移除对象。
    - `WEAK` – 弱引用：更积极地基于垃圾收集器状态和弱引用规则移除对象。

  - flushInterval（自动刷新间隔）：任意正整数，单位是毫秒。 默认不自动刷新。
  - size（引用数目）：任意正整数，默认值是 1024。
  - readOnly（只读）： true 或 false。只读的缓存会给所有调用者返回缓存对象的相同实例，线程不安全。 而可读写的缓存会（通过序列化）返回缓存对象的拷贝， 速度上会慢一些，但是更安全。因此默认值是 false。

  示例配置：

  ```xml
  <cache
    eviction="FIFO"
    flushInterval="60000"
    size="512"
    readOnly="true"/>
  ```

  缓存的配置和缓存实例会被绑定到 SQL 映射文件的命名空间中。 但每条语句可以自定义与缓存交互的方式，或将它们完全排除于缓存之外，这可以通过在每条语句上使用两个简单属性来达成。 默认情况下，语句会这样来配置： 

  ```xml
  <select ... flushCache="false" useCache="true"/>
  <insert ... flushCache="true"/>
  <update ... flushCache="true"/>
  <delete ... flushCache="true"/>
  ```

  

  **最佳实践**

  关于MyBatis缓存的更多细节和存在的问题参考 [美团技术博客](https://tech.meituan.com/2018/01/19/mybatis-cache.html)。最终结论是关闭MyBatis缓存功能。

  ```xml
  <!--mybatis-config.xml-->
  	<settings>
          <setting name="cacheEnabled" value="false"/>
      </settings>
  ```



## 5. 动态SQL

MyBatis的动态sql可以根据不同条件拼接 SQL 语句。主要包括以下几种元素：

- if
- choose (when, otherwise)
- trim (where, set)
- foreach

### 5.1 if

```xml
    <update id="updateBlog">
        update blog
        set
            <if test="title!=null">
                title=#{title},
            </if>
            <if test="content!= null">
                content=#{content},
            </if>
            <if test="author!=null">
                author = #{author}
            </if>
        where id = #{id};
    </update>
```

上述语句当方法参数title，content，author不为null时，才更新相应的列。

### 5.2 choose、when、otherwise

当我们不想使用所有的条件，而只是想从多个条件中选择一个使用时，可以使用choose等元素。类似于Java中的switch语句。

```xml
    <select id="selectBlogLike" resultType="com.tcl.mybatislearning.entity.Blog">
        select * from blog
        where
            <choose>
                <when test="title != null">
                    title like #{title}
                </when>
                <when test="author!=null and author.name != null">
                    author like #{author.name}
                </when>
                <otherwise>
                    id > 1
                </otherwise>
            </choose>
    </select>

```

### 5.3 trim、where、set

#### 1.问题

看看以下例子

```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG
  WHERE
  <if test="state != null">
    state = #{state}
  </if>
  <if test="title != null">
    AND title like #{title}
  </if>
  <if test="author != null and author.name != null">
    AND author_name like #{author.name}
  </if>
</select>
```

当部分条件不满足时，会出现一些问题：

- 全部条件都不满足，SQL会变成这样子：

  ```xml
  SELECT * FROM BLOG
  WHERE
  ```

- 只有第二个条件满足时，SQL会变成这样子：

  ```xml
  SELECT * FROM BLOG
  WHERE
  AND title like ‘someTitle’
  ```

以上两种情况都会出现SQL语法错误，导致查询失败。

#### 2.解决办法

针对以上问题，MyBatis提供了两种解决思路：

- 使用`<where>`元素

  ```xml
  <select id="findActiveBlogLike"
       resultType="Blog">
    SELECT * FROM BLOG
    <where>
      <if test="state != null">
           state = #{state}
      </if>
      <if test="title != null">
          AND title like #{title}
      </if>
      <if test="author != null and author.name != null">
          AND author_name like #{author.name}
      </if>
    </where>
  </select>
  ```

  *where* 元素只会在子元素返回任何内容的情况下才插入 “WHERE” 子句。而且，若子句的开头为 “AND” 或 “OR”，*where* 元素也会将它们去除。 

- 使用`<trim>`元素

  ```xml
  <select id="findActiveBlogLike"
       resultType="Blog">
    	SELECT * FROM BLOG
      <trim prefix="WHERE" prefixOverrides="AND |OR ">
          <if test="state != null">
               state = #{state}
          </if>
          <if test="title != null">
              AND title like #{title}
          </if>
          <if test="author != null and author.name != null">
              AND author_name like #{author.name}
          </if>
      </trim>
  </select>
  ```

  上述例子会插入 *prefix* 属性中指定的内容（WHERE），并移除WHERE后面第一个 *prefixOverrides* 属性中包含的内容（以空格+|作为分隔符）。

动态更新的问题也类似，提供了`<set>`元素：

```xml
<update id="updateAuthorIfNecessary">
  update Author
    <set>
      <if test="username != null">username=#{username},</if>
      <if test="password != null">password=#{password},</if>
      <if test="email != null">email=#{email},</if>
      <if test="bio != null">bio=#{bio}</if>
    </set>
  where id=#{id}
</update>
```

或者`<trim>`元素：

```xml
<update id="updateAuthorIfNecessary">
  update Author
    <trim prefix="SET" suffixOverrides=",">
      <if test="username != null">username=#{username},</if>
      <if test="password != null">password=#{password},</if>
      <if test="email != null">email=#{email},</if>
      <if test="bio != null">bio=#{bio}</if>
    </trim>
  where id=#{id}
</update>
```

移除set最后多余的逗号。



### 5.4 foreach

MyBatis提供`<foreach>`元素对集合进行遍历（尤其是在构建 IN 条件语句或者批量插入时的时候非常有用）。

foreach的属性包含以下几个：

- collection：集合对象的方法参数名，可用`@Param`注解指定

- index，item：分两种情况
  - 可迭代对象（List，Set，数组）：index是当前迭代的索引，item是当前迭代的元素。
  - Map，Map.Entry集合：index是键，item是值。
- open：起始符
- separator：分隔符
- close：结束符

批量查询示例：

```xml
List<Blog> selectByIds(@Param("ids") List<Long> ids);
    
    <select id="selectByIds" resultType="com.tcl.mybatislearning.entity.Blog">
        select * from blog
        where id in
        <foreach collection="ids" index="index" item="id" open="(" close=")" separator=",">
            #{id}
        </foreach>
    </select>  
```

批量插入示例：

```xml
    void insertBatch(@Param("blogs") List<Blog> blogs);
        
    <insert id="insertBatch" useGeneratedKeys="true" keyProperty="id">
        insert into blog (title, content, author)
        values
        <foreach collection="blogs" item="blog" separator=",">
            (#{blog.title}, #{blog.content}, #{blog.author})
        </foreach>
    </insert>
```

