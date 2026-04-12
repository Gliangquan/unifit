package com.jcen.unifit.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ConsultMessageVO {

    private Long id;

    private Long userId;

    private String userName;

    private String studentId;

    private String questionContent;

    private String answerContent;

    private String status;

    private Long replyBy;

    private String replyUserName;

    private Date replyTime;

    private Date createTime;
}
