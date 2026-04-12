package com.jcen.unifit.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HealthProfileUpdateRequest {

    private Integer age;

    private String gender;

    private BigDecimal height;

    private BigDecimal weight;
}
