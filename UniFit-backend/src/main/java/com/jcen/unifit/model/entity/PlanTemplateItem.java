package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("plan_template_item")
public class PlanTemplateItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private Integer weekNo;

    private Integer dayNo;

    private Long exerciseId;

    private Integer setsCount;

    private Integer repsCount;

    private Integer durationMinutes;

    private String intensityNote;

    private Integer sortNo;

    private Date createTime;

    @TableLogic
    private Integer isDelete;
}
