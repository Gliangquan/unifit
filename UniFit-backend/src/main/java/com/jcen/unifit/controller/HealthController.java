package com.jcen.unifit.controller;

import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.model.dto.HealthProfileUpdateRequest;
import com.jcen.unifit.model.entity.HealthRecord;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.HealthProfileVO;
import com.jcen.unifit.service.HealthService;
import com.jcen.unifit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Resource
    private HealthService healthService;

    @Resource
    private UserService userService;

    @GetMapping("/profile/my")
    public BaseResponse<HealthProfileVO> getMy(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(healthService.getMyHealthProfile(user));
    }

    @PostMapping("/profile/update")
    public BaseResponse<HealthProfileVO> update(@RequestBody HealthProfileUpdateRequest updateRequest, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(healthService.upsertMyHealthProfile(user, updateRequest));
    }

    @GetMapping("/records/my")
    public BaseResponse<List<HealthRecord>> records(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(healthService.listMyRecords(user));
    }
}
