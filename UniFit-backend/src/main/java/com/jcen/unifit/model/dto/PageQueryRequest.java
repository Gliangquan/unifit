package com.jcen.unifit.model.dto;

import lombok.Data;

@Data
public class PageQueryRequest {

    private Long current = 1L;

    private Long pageSize = 10L;

    private String sortField;

    private String sortOrder = "desc";
}
