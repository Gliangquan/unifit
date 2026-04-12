package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("badge")
public class Badge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String badgeCode;

    private String badgeName;

    private String iconUrl;

    private String conditionType;

    private Integer conditionValue;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
