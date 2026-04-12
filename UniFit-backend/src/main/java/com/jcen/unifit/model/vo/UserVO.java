package com.jcen.unifit.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserVO {

    private Long id;

    private String userAccount;

    private String userName;

    private String userAvatar;

    private String userRole;

    private String userPhone;

    private String userEmail;

    private Integer status;

    private BigDecimal balance;

    private Integer planUnlocked;
}
