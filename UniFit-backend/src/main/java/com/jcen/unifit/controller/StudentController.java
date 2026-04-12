package com.jcen.unifit.controller;

import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.model.dto.StudentAuditRequest;
import com.jcen.unifit.model.dto.StudentVerifySubmitRequest;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.service.StudentService;
import com.jcen.unifit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Resource
    private StudentService studentService;

    @Resource
    private UserService userService;

    @PostMapping("/verify/submit")
    public BaseResponse<StudentProfile> submit(@RequestBody StudentVerifySubmitRequest request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(studentService.submitVerification(loginUser, request));
    }

    @GetMapping("/profile/my")
    public BaseResponse<StudentProfile> myProfile(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(studentService.getMyProfile(loginUser));
    }

    @GetMapping("/verify/pending")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<StudentProfile>> listPending() {
        return ResultUtils.success(studentService.listPendingProfiles());
    }

    @PostMapping("/verify/audit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> audit(@RequestBody StudentAuditRequest request, HttpServletRequest httpServletRequest) {
        User admin = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(studentService.audit(request, admin));
    }
}
