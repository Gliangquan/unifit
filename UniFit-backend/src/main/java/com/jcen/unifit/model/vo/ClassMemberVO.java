package com.jcen.unifit.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ClassMemberVO {

    private Long id;

    private Long classId;

    private Long userId;

    private String userName;

    private String userAccount;

    private String userPhone;

    private Date joinTime;
}
