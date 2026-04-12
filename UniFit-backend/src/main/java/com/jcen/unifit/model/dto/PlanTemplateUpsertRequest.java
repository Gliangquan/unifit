package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class PlanTemplateUpsertRequest {

    private Long id;

    private String templateCode;

    private String templateName;

    private String testItemCode;

    private String scoreLevel;

    private String fitnessLevel;

    private String equipmentType;

    private String bmiRange;

    private Integer daysPerWeek;

    private String description;

    private Integer status;
}
