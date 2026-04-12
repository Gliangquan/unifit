package com.jcen.unifit.model.dto.user;

import lombok.Data;

@Data
public class WeChatLoginRequest {

    private String code;

    private String nickName;

    private String avatarUrl;
}
