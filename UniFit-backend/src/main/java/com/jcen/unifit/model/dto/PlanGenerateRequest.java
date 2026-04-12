package com.jcen.unifit.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanGenerateRequest {

    private String testItemCode;

    private BigDecimal currentScore;

    private String fitnessLevel;

    private String equipmentType;

    private Integer daysPerWeek;

    private BigDecimal bmiValue;
}
