package com.tcl.mybatislearning.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author li
 * @version 1.0
 * @date 2020/8/11 11:44
 */
@Data
public class Blog {

    private Long id;

    private String title;

    private String content;

    private Author author;

    private Date createTime;

    private Date updateTime;
}
