package com.jcen.unifit.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HealthProfileVO {

    private Integer age;

    private String gender;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal bmiValue;

    private String bmiStatus;
}
