package com.jcen.unifit.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ExerciseLikeRecordVO {

    private Long exerciseId;

    private String exerciseName;

    private String coverImageUrl;

    private String category;

    private String difficulty;

    private String equipmentRequired;

    private String description;

    private Integer likeCount;

    private Integer commentCount;

    private Date likedAt;
}
