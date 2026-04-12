package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.unifit.mapper.BadgeMapper;
import com.jcen.unifit.mapper.CheckinMapper;
import com.jcen.unifit.mapper.UserBadgeMapper;
import com.jcen.unifit.model.entity.Badge;
import com.jcen.unifit.model.entity.Checkin;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.entity.UserBadge;
import com.jcen.unifit.service.BadgeService;
import com.jcen.unifit.service.CheckinService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BadgeServiceImpl implements BadgeService {

    @Resource
    private BadgeMapper badgeMapper;

    @Resource
    private UserBadgeMapper userBadgeMapper;

    @Resource
    private CheckinMapper checkinMapper;

    @Resource
    private CheckinService checkinService;

    @Override
    public void evaluateAndGrant(User loginUser) {
        List<Badge> badges = badgeMapper.selectList(new QueryWrapper<Badge>().eq("status", 1));
        List<UserBadge> myBadges = userBadgeMapper.selectList(new QueryWrapper<UserBadge>().eq("user_id", loginUser.getId()));
        Set<Long> ownedBadgeIds = myBadges.stream().map(UserBadge::getBadgeId).collect(Collectors.toSet());

        long checkinCount = checkinMapper.selectCount(new QueryWrapper<Checkin>().eq("user_id", loginUser.getId()));
        int streakDays = checkinService.getStreakDays(loginUser);

        for (Badge badge : badges) {
            if (ownedBadgeIds.contains(badge.getId())) {
                continue;
            }

            boolean achieved = false;
            if ("checkin_count".equals(badge.getConditionType())) {
                achieved = checkinCount >= badge.getConditionValue();
            } else if ("streak_days".equals(badge.getConditionType())) {
                achieved = streakDays >= badge.getConditionValue();
            }

            if (achieved) {
                UserBadge userBadge = new UserBadge();
                userBadge.setUserId(loginUser.getId());
                userBadge.setBadgeId(badge.getId());
                userBadge.setAchievedDate(new Date());
                userBadge.setCreateTime(new Date());
                userBadgeMapper.insert(userBadge);
            }
        }
    }

    @Override
    public List<Badge> listMyBadges(User loginUser) {
        List<UserBadge> records = listMyBadgeRecords(loginUser);
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> badgeIds = records.stream().map(UserBadge::getBadgeId).collect(Collectors.toList());
        return badgeMapper.selectBatchIds(badgeIds);
    }

    @Override
    public List<UserBadge> listMyBadgeRecords(User loginUser) {
        return userBadgeMapper.selectList(new QueryWrapper<UserBadge>().eq("user_id", loginUser.getId()).orderByDesc("achieved_date"));
    }
}
