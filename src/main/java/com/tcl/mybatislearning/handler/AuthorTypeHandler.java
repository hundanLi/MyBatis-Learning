package com.tcl.mybatislearning.handler;

import com.tcl.mybatislearning.entity.Author;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author li
 * @version 1.0
 * @date 2020/8/11 16:57
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
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
