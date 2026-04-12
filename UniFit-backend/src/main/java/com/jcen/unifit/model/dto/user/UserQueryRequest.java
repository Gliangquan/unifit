package com.jcen.unifit.model.dto.user;

import com.jcen.unifit.model.dto.PageQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageQueryRequest {

    private String userAccount;

    private String userName;

    private String userPhone;

    private String userRole;
}
