package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("test_standard")
public class TestStandard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stage;

    private String gradeRange;

    private String gender;

    private String itemCode;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    private String level;

    private Integer standardPoint;

    private Date createTime;

    @TableLogic
    private Integer isDelete;
}
