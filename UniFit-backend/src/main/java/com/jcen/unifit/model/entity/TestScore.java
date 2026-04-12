package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("test_score")
public class TestScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String itemCode;

    private BigDecimal scoreValue;

    private String level;

    private Integer standardPoint;

    private Date recordedDate;

    private Date createTime;

    @TableLogic
    private Integer isDelete;
}
