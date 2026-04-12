package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class TestStandardQueryRequest extends PageQueryRequest {

    private String stage;

    private String gender;

    private String itemCode;
}
