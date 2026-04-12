package com.jcen.unifit.controller;

import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.model.entity.Badge;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.entity.UserBadge;
import com.jcen.unifit.service.BadgeService;
import com.jcen.unifit.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/badge")
public class BadgeController {

    @Resource
    private BadgeService badgeService;

    @Resource
    private UserService userService;

    @GetMapping("/my")
    public BaseResponse<Map<String, Object>> myBadges(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Badge> badges = badgeService.listMyBadges(loginUser);
        List<UserBadge> records = badgeService.listMyBadgeRecords(loginUser);
        Map<String, Object> result = new HashMap<>();
        result.put("badges", badges);
        result.put("records", records);
        return ResultUtils.success(result);
    }
}
