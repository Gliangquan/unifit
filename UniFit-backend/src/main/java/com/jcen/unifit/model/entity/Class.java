package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("class")
public class Class {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String className;

    private String classCode;

    private String grade;

    private String major;

    private String description;

    private Integer studentCount;

    private Long teacherId;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
