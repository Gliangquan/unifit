package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("student_profile")
public class StudentProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String studentId;

    private String realName;

    private String className;

    private String verificationStatus;

    private String rejectReason;

    private Long auditBy;

    private Date auditTime;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
