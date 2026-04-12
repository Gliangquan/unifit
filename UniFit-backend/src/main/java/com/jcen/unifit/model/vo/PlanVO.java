package com.jcen.unifit.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class PlanVO {

    private Long planId;

    private String testItemCode;

    private String scoreLevel;

    private String fitnessLevel;

    private String equipmentType;

    private Integer daysPerWeek;

    private String status;

    private Date startDate;

    private Date endDate;

    private Map<String, Object> snapshot;

    private List<PlanItemVO> items;
}
