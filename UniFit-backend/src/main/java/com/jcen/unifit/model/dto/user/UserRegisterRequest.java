package com.jcen.unifit.model.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String userPhone;

    private String userName;
}
