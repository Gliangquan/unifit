package com.jcen.unifit.service;

import com.jcen.unifit.model.dto.CheckinRequest;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ClassChallengeVO;
import com.jcen.unifit.model.vo.CheckinRankVO;

import java.util.List;
import java.util.Map;

public interface CheckinService {

    Map<String, Object> checkin(User loginUser, CheckinRequest request);

    int getStreakDays(User loginUser);

    List<Map<String, Object>> getCalendarCheckins(User loginUser, int limitDays);

    List<CheckinRankVO> getRanking(int limitDays, int topN);

    List<ClassChallengeVO> getClassChallengeRanking(int limitDays, int topN);

    Map<String, Object> getMyClassChallenge(User loginUser, int limitDays);
}
