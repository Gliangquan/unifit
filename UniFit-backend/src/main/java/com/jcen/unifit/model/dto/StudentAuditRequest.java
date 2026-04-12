package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class StudentAuditRequest {

    private Long userId;

    private String verificationStatus;

    private String rejectReason;
}
