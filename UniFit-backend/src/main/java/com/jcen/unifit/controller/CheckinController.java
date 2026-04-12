package com.jcen.unifit.controller;

import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.model.dto.CheckinRequest;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ClassChallengeVO;
import com.jcen.unifit.model.vo.CheckinRankVO;
import com.jcen.unifit.service.BadgeService;
import com.jcen.unifit.service.CheckinService;
import com.jcen.unifit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkin")
public class CheckinController {

    @Resource
    private CheckinService checkinService;

    @Resource
    private BadgeService badgeService;

    @Resource
    private UserService userService;

    @PostMapping("/do")
    public BaseResponse<Boolean> checkin(@RequestBody(required = false) CheckinRequest request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = checkinService.checkin(loginUser, request);
        badgeService.evaluateAndGrant(loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/streak")
    public BaseResponse<Integer> streak(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(checkinService.getStreakDays(loginUser));
    }

    @GetMapping("/ranking")
    public BaseResponse<List<CheckinRankVO>> ranking(@RequestParam(defaultValue = "7") int days,
                                                     @RequestParam(defaultValue = "20") int topN) {
        return ResultUtils.success(checkinService.getRanking(days, topN));
    }

    @GetMapping("/challenge/class")
    public BaseResponse<List<ClassChallengeVO>> classChallenge(@RequestParam(defaultValue = "7") int days,
                                                               @RequestParam(defaultValue = "20") int topN) {
        return ResultUtils.success(checkinService.getClassChallengeRanking(days, topN));
    }

    @GetMapping("/challenge/my-class")
    public BaseResponse<Map<String, Object>> myClassChallenge(@RequestParam(defaultValue = "7") int days,
                                                              HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(checkinService.getMyClassChallenge(loginUser, days));
    }
}
