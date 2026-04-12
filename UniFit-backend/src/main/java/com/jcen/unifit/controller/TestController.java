package com.jcen.unifit.controller;

import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.model.dto.TestScoreAddRequest;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.TestScoreVO;
import com.jcen.unifit.service.TestService;
import com.jcen.unifit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private TestService testService;

    @Resource
    private UserService userService;

    @PostMapping("/score/add")
    public BaseResponse<TestScoreVO> addScore(@RequestBody TestScoreAddRequest request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(testService.addScore(loginUser, request));
    }

    @GetMapping("/score/history")
    public BaseResponse<List<TestScoreVO>> history(@RequestParam(required = false) String itemCode, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(testService.listMyScores(loginUser, itemCode));
    }

    @GetMapping("/analysis/weakness")
    public BaseResponse<Map<String, Object>> weakness(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(testService.getWeaknessAnalysis(loginUser));
    }

    @GetMapping("/analysis/class/compare")
    public BaseResponse<Map<String, Object>> classCompare(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(testService.getClassCompare(loginUser));
    }

    @GetMapping("/items")
    public BaseResponse<List<TestItem>> items() {
        return ResultUtils.success(testService.listTestItems());
    }
}
