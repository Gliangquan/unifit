package com.jcen.unifit.controller;

import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.model.dto.PlanGenerateRequest;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.PlanVO;
import com.jcen.unifit.service.PlanService;
import com.jcen.unifit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plan")
public class PlanController {

    @Resource
    private PlanService planService;

    @Resource
    private UserService userService;

    @PostMapping("/generate")
    public BaseResponse<PlanVO> generate(@RequestBody PlanGenerateRequest request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(planService.generatePlan(loginUser, request));
    }

    @GetMapping("/current")
    public BaseResponse<PlanVO> current(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(planService.getCurrentPlan(loginUser));
    }

    @GetMapping("/list")
    public BaseResponse<List<PlanVO>> list(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(planService.listPlans(loginUser));
    }

    @PostMapping("/purchase")
    public BaseResponse<Map<String, Object>> purchase(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(planService.purchasePlanAccess(loginUser));
    }

    @PostMapping("/item/done")
    public BaseResponse<Boolean> done(@RequestParam Long planItemId, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(planService.markPlanItemDone(loginUser, planItemId));
    }
}
