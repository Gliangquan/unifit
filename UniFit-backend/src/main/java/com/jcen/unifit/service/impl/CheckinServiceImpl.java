package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.CheckinMapper;
import com.jcen.unifit.mapper.StudentProfileMapper;
import com.jcen.unifit.mapper.UserMapper;
import com.jcen.unifit.model.dto.CheckinRequest;
import com.jcen.unifit.model.entity.Checkin;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ClassChallengeVO;
import com.jcen.unifit.model.vo.CheckinRankVO;
import com.jcen.unifit.service.CheckinService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CheckinServiceImpl implements CheckinService {

    @Resource
    private CheckinMapper checkinMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Override
    public boolean checkin(User loginUser, CheckinRequest request) {
        ensureStudentVerified(loginUser);
        if (request != null && request.getDurationMinutes() != null && request.getDurationMinutes() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "打卡时长必须大于0");
        }

        LocalDate today = LocalDate.now();
        Date start = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        QueryWrapper<Checkin> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId()).ge("checkin_date", start).lt("checkin_date", end);
        if (checkinMapper.selectCount(qw) > 0) {
            return true;
        }

        Checkin checkin = new Checkin();
        checkin.setUserId(loginUser.getId());
        checkin.setUserPlanId(request == null ? null : request.getUserPlanId());
        checkin.setDurationMinutes(request == null || request.getDurationMinutes() == null ? 60 : request.getDurationMinutes());
        checkin.setNote(request == null ? null : request.getNote());
        checkin.setCheckinDate(new Date());
        checkin.setCreateTime(new Date());
        return checkinMapper.insert(checkin) > 0;
    }

    @Override
    public int getStreakDays(User loginUser) {
        QueryWrapper<Checkin> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId()).orderByDesc("checkin_date");
        List<Checkin> list = checkinMapper.selectList(qw);
        Set<LocalDate> dates = list.stream()
                .map(x -> x.getCheckinDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate cursor = LocalDate.now();
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    @Override
    public List<CheckinRankVO> getRanking(int limitDays, int topN) {
        LocalDate from = LocalDate.now().minusDays(Math.max(limitDays, 1) - 1L);
        Date fromDate = Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant());

        QueryWrapper<Checkin> qw = new QueryWrapper<>();
        qw.select("user_id as userId", "count(*) as checkinCount", "sum(duration_minutes) as totalDuration")
                .ge("checkin_date", fromDate)
                .groupBy("user_id")
                .orderByDesc("checkinCount", "totalDuration")
                .last("limit " + Math.max(topN, 10));

        List<Map<String, Object>> rows = checkinMapper.selectMaps(qw);
        List<Long> userIds = rows.stream().map(x -> Long.parseLong(String.valueOf(x.get("userId")))).collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<CheckinRankVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long userId = Long.parseLong(String.valueOf(row.get("userId")));
            User user = userMap.get(userId);
            CheckinRankVO vo = new CheckinRankVO();
            vo.setUserId(userId);
            vo.setUserName(user == null ? "" : user.getUserName());
            vo.setUserAvatar(user == null ? "" : user.getUserAvatar());
            vo.setCheckinCount(Long.parseLong(String.valueOf(row.get("checkinCount"))));
            vo.setTotalDuration(Integer.parseInt(String.valueOf(row.get("totalDuration"))));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ClassChallengeVO> getClassChallengeRanking(int limitDays, int topN) {
        List<StudentProfile> profiles = studentProfileMapper.selectList(new QueryWrapper<StudentProfile>()
                .eq("verification_status", "approved")
                .isNotNull("class_name"));
        if (profiles.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, String> userClassMap = profiles.stream()
                .filter(p -> StringUtils.isNotBlank(p.getClassName()))
                .collect(Collectors.toMap(StudentProfile::getUserId, StudentProfile::getClassName, (a, b) -> a));
        if (userClassMap.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Integer> memberCountMap = new HashMap<>();
        for (StudentProfile profile : profiles) {
            String className = StringUtils.trimToNull(profile.getClassName());
            if (className == null) {
                continue;
            }
            memberCountMap.put(className, memberCountMap.getOrDefault(className, 0) + 1);
        }

        LocalDate from = LocalDate.now().minusDays(Math.max(limitDays, 1) - 1L);
        Date fromDate = Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Checkin> checkins = checkinMapper.selectList(new QueryWrapper<Checkin>()
                .ge("checkin_date", fromDate)
                .orderByDesc("checkin_date"));
        if (checkins.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> classCheckinCount = new HashMap<>();
        Map<String, Integer> classDuration = new HashMap<>();
        Map<String, Set<Long>> classParticipants = new HashMap<>();
        for (Checkin checkin : checkins) {
            String className = userClassMap.get(checkin.getUserId());
            if (StringUtils.isBlank(className)) {
                continue;
            }
            classCheckinCount.put(className, classCheckinCount.getOrDefault(className, 0L) + 1L);
            int duration = checkin.getDurationMinutes() == null ? 0 : checkin.getDurationMinutes();
            classDuration.put(className, classDuration.getOrDefault(className, 0) + duration);
            classParticipants.computeIfAbsent(className, k -> new java.util.HashSet<>()).add(checkin.getUserId());
        }

        List<ClassChallengeVO> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : classCheckinCount.entrySet()) {
            String className = entry.getKey();
            int participantCount = classParticipants.getOrDefault(className, Set.of()).size();
            int totalDuration = classDuration.getOrDefault(className, 0);

            ClassChallengeVO vo = new ClassChallengeVO();
            vo.setClassName(className);
            vo.setMemberCount(memberCountMap.getOrDefault(className, 0));
            vo.setParticipantCount(participantCount);
            vo.setCheckinCount(entry.getValue());
            vo.setTotalDuration(totalDuration);
            BigDecimal avg = participantCount <= 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(totalDuration)
                    .divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);
            vo.setAvgDurationPerParticipant(avg);
            result.add(vo);
        }

        result.sort(Comparator.comparing(ClassChallengeVO::getCheckinCount, Comparator.reverseOrder())
                .thenComparing(ClassChallengeVO::getTotalDuration, Comparator.reverseOrder())
                .thenComparing(ClassChallengeVO::getParticipantCount, Comparator.reverseOrder())
                .thenComparing(ClassChallengeVO::getClassName));
        int limit = Math.max(topN, 1);
        if (result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    @Override
    public Map<String, Object> getMyClassChallenge(User loginUser, int limitDays) {
        ensureStudentVerified(loginUser);
        StudentProfile myProfile = studentProfileMapper.selectOne(new QueryWrapper<StudentProfile>()
                .eq("user_id", loginUser.getId())
                .eq("verification_status", "approved")
                .last("limit 1"));
        if (myProfile == null || StringUtils.isBlank(myProfile.getClassName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先完善班级信息");
        }

        List<ClassChallengeVO> ranking = getClassChallengeRanking(limitDays, 1000);
        int rank = 0;
        ClassChallengeVO myClass = null;
        for (int i = 0; i < ranking.size(); i++) {
            if (myProfile.getClassName().equals(ranking.get(i).getClassName())) {
                rank = i + 1;
                myClass = ranking.get(i);
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("className", myProfile.getClassName());
        result.put("rank", rank);
        result.put("stats", myClass);
        result.put("rankingSize", ranking.size());
        return result;
    }

    private void ensureStudentVerified(User loginUser) {
        if (!UserConstant.STUDENT_ROLE.equals(loginUser.getUserRole())) {
            return;
        }
        StudentProfile profile = studentProfileMapper.selectOne(new QueryWrapper<StudentProfile>()
                .eq("user_id", loginUser.getId())
                .last("limit 1"));
        if (profile == null || !"approved".equals(profile.getVerificationStatus())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请先完成学生认证并通过审核");
        }
    }
}
