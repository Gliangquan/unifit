package com.jcen.unifit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userAccount;

    private String userPassword;

    private String userName;

    private String userAvatar;

    private String userRole;

    private String userPhone;

    private String userEmail;

    private String unionId;

    private String mpOpenId;

    private Integer status;

    private BigDecimal balance;

    private Integer planUnlocked;

    private Date planUnlockTime;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}
