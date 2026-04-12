package com.jcen.unifit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.model.dto.user.UserLoginRequest;
import com.jcen.unifit.model.dto.user.UserQueryRequest;
import com.jcen.unifit.model.dto.user.UserRegisterRequest;
import com.jcen.unifit.model.dto.user.UserUpdateMyRequest;
import com.jcen.unifit.model.dto.user.WeChatLoginRequest;
import com.jcen.unifit.model.dto.user.BalanceRechargeRequest;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.LoginUserVO;
import com.jcen.unifit.model.vo.UserVO;
import com.jcen.unifit.service.FileStorageService;
import com.jcen.unifit.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private FileStorageService fileStorageService;

    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = userService.userRegister(request.getUserAccount(), request.getUserPassword(), request.getCheckPassword(), request.getUserPhone(), request.getUserName());
        return ResultUtils.success(userId);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest request, HttpServletRequest httpServletRequest) {
        if (request == null || StringUtils.isBlank(request.getLoginType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO;
        switch (request.getLoginType()) {
            case "account":
                loginUserVO = userService.userLoginByAccount(request.getUserAccount(), request.getUserPassword(), httpServletRequest);
                break;
            case "phone":
                loginUserVO = userService.userLoginByPhone(request.getUserPhone(), request.getUserPassword(), httpServletRequest);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的登录类型");
        }
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/login/wechat")
    public BaseResponse<LoginUserVO> loginByWechat(@RequestBody WeChatLoginRequest request, HttpServletRequest httpServletRequest) {
        if (request == null || StringUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "code不能为空");
        }
        return ResultUtils.success(userService.userLoginByWeChat(request.getCode(), request.getNickName(), request.getAvatarUrl(), httpServletRequest));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMy(@RequestBody UserUpdateMyRequest request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        User update = new User();
        BeanUtils.copyProperties(request, update);
        update.setId(loginUser.getId());
        return ResultUtils.success(userService.updateById(update));
    }

    /**
     * 登录用户上传头像（存入 avatar/ 目录，返回可访问 URL）
     */
    @PostMapping("/avatar/upload")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile file,
                                             HttpServletRequest httpServletRequest) {
        userService.getLoginUser(httpServletRequest); // 鉴权，未登录抛异常
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过 5MB");
        }
        String url = fileStorageService.upload(file, "avatar");
        return ResultUtils.success(url);
    }

    @PostMapping("/balance/recharge")
    public BaseResponse<LoginUserVO> recharge(@RequestBody(required = false) BalanceRechargeRequest request,
                                              HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        BigDecimal amount = request == null ? null : request.getAmount();
        return ResultUtils.success(userService.rechargeBalance(loginUser, amount));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUsers(@RequestBody UserQueryRequest request) {
        return ResultUtils.success(userService.listUsers(request));
    }

    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<java.util.List<UserVO>> getUserList(@RequestParam(required = false) String userRole) {
        return ResultUtils.success(userService.getUserList(userRole));
    }

    @PostMapping("/teacher/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> createTeacher(@RequestBody UserRegisterRequest request) {
        if (request == null || StringUtils.isBlank(request.getUserAccount()) || StringUtils.isBlank(request.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号和密码不能为空");
        }
        long userId = userService.userRegister(request.getUserAccount(), request.getUserPassword(), request.getUserPassword(), request.getUserPhone(), request.getUserName());
        User user = userService.getById(userId);
        user.setUserRole("teacher");
        userService.updateById(user);
        return ResultUtils.success(userId);
    }
}
