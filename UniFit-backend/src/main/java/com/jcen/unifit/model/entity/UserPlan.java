package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_plan")
public class UserPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long templateId;

    private String testItemCode;

    private String scoreLevel;

    private String fitnessLevel;

    private String equipmentType;

    private Integer daysPerWeek;

    private String snapshotJson;

    private String status;

    private Date startDate;

    private Date endDate;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
