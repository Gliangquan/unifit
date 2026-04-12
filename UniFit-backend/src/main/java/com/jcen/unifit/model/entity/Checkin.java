package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("checkin")
public class Checkin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long userPlanId;

    private Date checkinDate;

    private Integer durationMinutes;

    private String note;

    private Date createTime;

    @TableLogic
    private Integer isDelete;
}
