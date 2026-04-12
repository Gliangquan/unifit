package com.jcen.unifit.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ClassVO {

    private Long id;

    private String className;

    private String classCode;

    private String grade;

    private String major;

    private String description;

    private Integer studentCount;

    private Long teacherId;

    private String teacherName;

    private String userAccount;

    private String userEmail;

    private String userPhone;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private List<ClassMemberVO> members;

    private List<ClassTeacherVO> teachers;
}
