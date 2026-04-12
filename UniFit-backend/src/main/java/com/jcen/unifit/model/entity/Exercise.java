package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("exercise")
public class Exercise {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private String difficulty;

    private String equipmentRequired;

    private String description;

    private String coverImageUrl;

    private String contentMd;

    private String demoVideoUrl;

    private String demoImageUrls;

    private Long publishUserId;

    @TableField(exist = false)
    private String publishUserName;

    private Date publishTime;

    private Integer likeCount;

    private Integer commentCount;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
