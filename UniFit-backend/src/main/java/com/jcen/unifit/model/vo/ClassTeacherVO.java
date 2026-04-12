package com.jcen.unifit.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ClassTeacherVO {

    private Long id;

    private Long classId;

    private Long teacherId;

    private String teacherName;

    private String userAccount;

    private String userPhone;

    private String role;

    private Date joinTime;
}
