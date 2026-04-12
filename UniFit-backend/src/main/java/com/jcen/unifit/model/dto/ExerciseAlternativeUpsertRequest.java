package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class ExerciseAlternativeUpsertRequest {

    private Long exerciseId;

    private Long alternativeExerciseId;
}
