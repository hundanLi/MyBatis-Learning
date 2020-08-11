package com.tcl.mybatislearning.mapper;

import com.tcl.mybatislearning.entity.Blog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author li
 * @version 1.0
 * @date 2020/8/11 11:43
 */
@Mapper
public interface BlogMapper {

    Blog selectById(Long id);

    void insertBlog(Blog blog);

    void updateBlog(Blog blog);

    void deleteBlogById(Long id);

    List<Blog> selectAll();

    Integer count();

}
