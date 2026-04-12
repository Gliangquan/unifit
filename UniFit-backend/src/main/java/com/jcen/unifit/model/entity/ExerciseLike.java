package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("exercise_like")
public class ExerciseLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long exerciseId;

    private Long userId;

    private Date createTime;
}
