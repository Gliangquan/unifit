package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("class_member")
public class ClassMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;

    private Long userId;

    private Date joinTime;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
