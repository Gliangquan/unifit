package com.jcen.unifit.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TestScoreVO {

    private String itemCode;

    private BigDecimal scoreValue;

    private String level;

    private Integer standardPoint;

    private Date recordedDate;
}
