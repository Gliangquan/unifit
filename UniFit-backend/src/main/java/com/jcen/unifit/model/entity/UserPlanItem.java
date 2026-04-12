package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_plan_item")
public class UserPlanItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userPlanId;

    private Integer weekNo;

    private Integer dayNo;

    private Long exerciseId;

    private Integer setsCount;

    private Integer repsCount;

    private Integer durationMinutes;

    private String intensityNote;

    private Integer completed;

    private Date completeTime;

    private Date createTime;

    @TableLogic
    private Integer isDelete;
}
