package com.jcen.unifit.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TestScoreAddRequest {

    private String itemCode;

    private BigDecimal scoreValue;
}
