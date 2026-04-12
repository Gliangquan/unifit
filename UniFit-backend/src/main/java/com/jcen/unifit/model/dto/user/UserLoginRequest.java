package com.jcen.unifit.model.dto.user;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String loginType;

    private String userAccount;

    private String userPhone;

    private String userPassword;
}
