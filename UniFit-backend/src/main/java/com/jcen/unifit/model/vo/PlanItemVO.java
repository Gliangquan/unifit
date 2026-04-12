package com.jcen.unifit.model.vo;

import lombok.Data;

@Data
public class PlanItemVO {

    private Integer weekNo;

    private Integer dayNo;

    private Long exerciseId;

    private String exerciseName;

    private Integer setsCount;

    private Integer repsCount;

    private Integer durationMinutes;

    private String intensityNote;

    private Integer completed;
}
