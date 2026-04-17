package com.jcen.unifit.service;

import com.jcen.unifit.model.entity.Badge;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.entity.UserBadge;

import java.util.List;
import java.util.Map;

public interface BadgeService {

    void evaluateAndGrant(User loginUser);

    List<Badge> listMyBadges(User loginUser);

    List<UserBadge> listMyBadgeRecords(User loginUser);

    List<Map<String, Object>> listBadgeWall(User loginUser);
}
