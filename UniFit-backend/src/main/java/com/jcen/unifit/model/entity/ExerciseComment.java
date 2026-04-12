package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("exercise_comment")
public class ExerciseComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long exerciseId;

    private Long userId;

    private String content;

    private Integer likeCount;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
