package com.jcen.unifit.model.vo;

import lombok.Data;

@Data
public class CheckinRankVO {

    private Long userId;

    private String userName;

    private String userAvatar;

    private Long checkinCount;

    private Integer totalDuration;
}
