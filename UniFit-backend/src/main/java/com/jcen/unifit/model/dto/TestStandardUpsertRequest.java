package com.jcen.unifit.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TestStandardUpsertRequest {

    private Long id;

    private String stage;

    private String gradeRange;

    private String gender;

    private String itemCode;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    private String level;

    private Integer standardPoint;
}
