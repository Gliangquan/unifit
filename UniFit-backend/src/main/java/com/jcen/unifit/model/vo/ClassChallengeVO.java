package com.jcen.unifit.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClassChallengeVO {

    private String className;

    private Integer memberCount;

    private Integer participantCount;

    private Long checkinCount;

    private Integer totalDuration;

    private BigDecimal avgDurationPerParticipant;
}
