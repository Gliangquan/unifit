package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class StudentVerifySubmitRequest {

    private String studentId;

    private String realName;

    private String className;
}
