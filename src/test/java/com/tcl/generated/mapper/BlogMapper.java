package com.tcl.generated.mapper;

import com.tcl.generated.model.Blog;
import java.util.List;

public interface BlogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table blog
     *
     * @mbg.generated Fri Aug 14 10:11:18 CST 2020
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table blog
     *
     * @mbg.generated Fri Aug 14 10:11:18 CST 2020
     */
    int insert(Blog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table blog
     *
     * @mbg.generated Fri Aug 14 10:11:18 CST 2020
     */
    Blog selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table blog
     *
     * @mbg.generated Fri Aug 14 10:11:18 CST 2020
     */
    List<Blog> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table blog
     *
     * @mbg.generated Fri Aug 14 10:11:18 CST 2020
     */
    int updateByPrimaryKey(Blog record);
}