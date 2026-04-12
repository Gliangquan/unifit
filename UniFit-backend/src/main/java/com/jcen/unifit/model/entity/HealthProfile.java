package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("health_profile")
public class HealthProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer age;

    private String gender;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal bmiValue;

    private String bmiStatus;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
