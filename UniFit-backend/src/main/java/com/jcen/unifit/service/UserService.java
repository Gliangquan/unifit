package com.jcen.unifit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.unifit.model.dto.user.UserQueryRequest;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.LoginUserVO;
import com.jcen.unifit.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword, String userPhone, String userName);

    LoginUserVO userLoginByAccount(String userAccount, String userPassword, HttpServletRequest request);

    LoginUserVO userLoginByPhone(String userPhone, String userPassword, HttpServletRequest request);

    LoginUserVO userLoginByWeChat(String code, String nickName, String avatarUrl, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    UserVO getUserVO(User user);

    Page<UserVO> listUsers(UserQueryRequest queryRequest);

    java.util.List<UserVO> getUserList(String userRole);

    LoginUserVO rechargeBalance(User loginUser, BigDecimal amount);
}
