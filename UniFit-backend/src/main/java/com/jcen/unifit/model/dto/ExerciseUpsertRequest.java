package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class ExerciseUpsertRequest {

    private Long id;

    private String name;

    private String category;

    private String difficulty;

    private String equipmentRequired;

    private String description;

    private String coverImageUrl;

    private String contentMd;

    private String demoVideoUrl;

    private String demoImageUrls;

    private Integer status;
}
