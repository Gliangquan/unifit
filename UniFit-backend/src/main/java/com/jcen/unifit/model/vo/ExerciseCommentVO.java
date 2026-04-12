package com.jcen.unifit.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ExerciseCommentVO {

    private Long id;

    private Long exerciseId;

    private String exerciseName;

    private String exerciseCoverImageUrl;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String content;

    private Integer likeCount;

    private Date createTime;
}
