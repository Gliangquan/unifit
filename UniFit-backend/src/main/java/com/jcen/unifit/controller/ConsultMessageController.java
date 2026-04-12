package com.jcen.unifit.controller;

import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.model.dto.ConsultMessageReplyRequest;
import com.jcen.unifit.model.dto.ConsultMessageSubmitRequest;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ConsultMessageVO;
import com.jcen.unifit.service.ConsultMessageService;
import com.jcen.unifit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/message")
public class ConsultMessageController {

    @Resource
    private ConsultMessageService consultMessageService;

    @Resource
    private UserService userService;

    @PostMapping("/submit")
    public BaseResponse<ConsultMessageVO> submit(@RequestBody ConsultMessageSubmitRequest request,
                                                 HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(consultMessageService.submit(loginUser, request == null ? null : request.getQuestionContent()));
    }

    @GetMapping("/my")
    public BaseResponse<List<ConsultMessageVO>> my(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(consultMessageService.listMy(loginUser));
    }

    @GetMapping("/pending")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<ConsultMessageVO>> pending() {
        return ResultUtils.success(consultMessageService.listPending());
    }

    @PostMapping("/reply")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ConsultMessageVO> reply(@RequestBody ConsultMessageReplyRequest request,
                                                HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(consultMessageService.reply(loginUser,
                request == null ? null : request.getId(),
                request == null ? null : request.getAnswerContent()));
    }
}
