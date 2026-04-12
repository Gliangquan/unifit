package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class PlanTemplateItemUpsertRequest {

    private Long id;

    private Long templateId;

    private Integer weekNo;

    private Integer dayNo;

    private Long exerciseId;

    private Integer setsCount;

    private Integer repsCount;

    private Integer durationMinutes;

    private String intensityNote;

    private Integer sortNo;
}
