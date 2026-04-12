package com.jcen.unifit.model.dto.user;

import lombok.Data;

@Data
public class UserUpdateMyRequest {

    private String userName;

    private String userAvatar;

    private String userEmail;
}
