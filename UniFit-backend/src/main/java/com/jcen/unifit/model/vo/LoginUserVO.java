package com.jcen.unifit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class LoginUserVO implements Serializable {

    private Long id;

    private String userAccount;

    private String userName;

    private String userAvatar;

    private String userRole;

    private String userPhone;

    private String userEmail;

    private BigDecimal balance;

    private Integer planUnlocked;

    private Date planUnlockTime;

    private String token;

    private Boolean isNewUser;
}
