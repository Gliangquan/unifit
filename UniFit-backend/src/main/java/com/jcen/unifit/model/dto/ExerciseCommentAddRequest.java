package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class ExerciseCommentAddRequest {

    private Long exerciseId;

    private String content;
}
