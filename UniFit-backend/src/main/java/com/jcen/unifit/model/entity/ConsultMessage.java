package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("consult_message")
public class ConsultMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String questionContent;

    private String answerContent;

    private String status;

    private Long replyBy;

    private Date replyTime;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
