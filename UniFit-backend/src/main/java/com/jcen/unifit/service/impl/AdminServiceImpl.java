package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcen.unifit.mapper.CheckinMapper;
import com.jcen.unifit.mapper.ClassMapper;
import com.jcen.unifit.mapper.ClassMemberMapper;
import com.jcen.unifit.mapper.ConsultMessageMapper;
import com.jcen.unifit.mapper.StudentProfileMapper;
import com.jcen.unifit.mapper.TestItemMapper;
import com.jcen.unifit.mapper.TestScoreMapper;
import com.jcen.unifit.mapper.UserMapper;
import com.jcen.unifit.mapper.UserPlanItemMapper;
import com.jcen.unifit.mapper.UserPlanMapper;
import com.jcen.unifit.model.entity.Checkin;
import com.jcen.unifit.model.entity.Class;
import com.jcen.unifit.model.entity.ClassMember;
import com.jcen.unifit.model.entity.ConsultMessage;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.model.entity.TestScore;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.entity.UserPlan;
import com.jcen.unifit.model.entity.UserPlanItem;
import com.jcen.unifit.model.vo.ClassChallengeVO;
import com.jcen.unifit.service.AdminService;
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
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TestScoreMapper testScoreMapper;

    @Resource
    private TestItemMapper testItemMapper;

    @Resource
    private CheckinMapper checkinMapper;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Resource
    private ClassMapper classMapper;

    @Resource
    private ClassMemberMapper classMemberMapper;

    @Resource
    private ConsultMessageMapper consultMessageMapper;

    @Resource
    private CheckinService checkinService;

    @Resource
    private UserPlanMapper userPlanMapper;

    @Resource
    private UserPlanItemMapper userPlanItemMapper;

    @Override
    public String exportUserAndScoreCsv() {
        List<User> users = userMapper.selectList(new QueryWrapper<User>().eq("user_role", "student"));
        List<TestScore> scores = testScoreMapper.selectList(new QueryWrapper<>());
        Map<Long, Long> scoreCountMap = scores.stream().collect(Collectors.groupingBy(TestScore::getUserId, Collectors.counting()));

        StringBuilder sb = new StringBuilder("userId,userAccount,userName,userPhone,scoreCount\n");
        for (User user : users) {
            sb.append(user.getId()).append(",")
                    .append(escape(user.getUserAccount())).append(",")
                    .append(escape(user.getUserName())).append(",")
                    .append(escape(user.getUserPhone())).append(",")
                    .append(scoreCountMap.getOrDefault(user.getId(), 0L))
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public long countStudents() {
        return userMapper.selectCount(new QueryWrapper<User>().eq("user_role", "student"));
    }

    @Override
    public long countCheckinsLast7Days() {
        LocalDate from = LocalDate.now().minusDays(6);
        Date fromDate = Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return checkinMapper.selectCount(new QueryWrapper<Checkin>().ge("checkin_date", fromDate));
    }

    @Override
    public long countPendingStudentAudits() {
        return studentProfileMapper.selectCount(new QueryWrapper<StudentProfile>()
                .eq("verification_status", "pending"));
    }

    @Override
    public long countPendingMessages() {
        return consultMessageMapper.selectCount(new QueryWrapper<ConsultMessage>()
                .eq("status", "pending"));
    }

    @Override
    public long countActiveClasses() {
        List<StudentProfile> profiles = studentProfileMapper.selectList(new QueryWrapper<StudentProfile>()
                .eq("verification_status", "approved")
                .isNotNull("class_name"));
        Set<String> classes = profiles.stream()
                .map(StudentProfile::getClassName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        return classes.size();
    }

    @Override
    public String exportClassChallengeCsv(int days) {
        List<ClassChallengeVO> rows = checkinService.getClassChallengeRanking(days, 200);
        StringBuilder sb = new StringBuilder("rank,className,memberCount,participantCount,checkinCount,totalDuration,avgDurationPerParticipant\n");
        for (int i = 0; i < rows.size(); i++) {
            ClassChallengeVO row = rows.get(i);
            sb.append(i + 1).append(",")
                    .append(escape(row.getClassName())).append(",")
                    .append(row.getMemberCount() == null ? 0 : row.getMemberCount()).append(",")
                    .append(row.getParticipantCount() == null ? 0 : row.getParticipantCount()).append(",")
                    .append(row.getCheckinCount() == null ? 0 : row.getCheckinCount()).append(",")
                    .append(row.getTotalDuration() == null ? 0 : row.getTotalDuration()).append(",")
                    .append(row.getAvgDurationPerParticipant() == null ? 0 : row.getAvgDurationPerParticipant())
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    public com.jcen.unifit.model.vo.UserVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        com.jcen.unifit.model.vo.UserVO vo = new com.jcen.unifit.model.vo.UserVO();
        vo.setId(user.getId());
        vo.setUserAccount(user.getUserAccount());
        vo.setUserName(user.getUserName());
        vo.setUserAvatar(user.getUserAvatar());
        vo.setUserRole(user.getUserRole());
        vo.setUserPhone(user.getUserPhone());
        vo.setUserEmail(user.getUserEmail());
        vo.setStatus(user.getStatus());
        vo.setBalance(user.getBalance());
        vo.setPlanUnlocked(user.getPlanUnlocked());
        return vo;
    }

    @Override
    public boolean updateUser(Long userId, String userName, String userPhone, String userRole) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        if (StringUtils.isNotBlank(userName)) {
            user.setUserName(userName);
        }
        if (StringUtils.isNotBlank(userPhone)) {
            user.setUserPhone(userPhone);
        }
        if (StringUtils.isNotBlank(userRole)) {
            user.setUserRole(userRole);
        }
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean deleteUser(Long userId) {
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    public Map<String, Object> getDashboardDetail() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // 基础统计
        long studentCount = countStudents();
        long checkinLast7Days = countCheckinsLast7Days();
        long pendingAudit = countPendingStudentAudits();
        long pendingMessages = countPendingMessages();
        long activeClasses = countActiveClasses();
        
        result.put("studentCount", studentCount);
        result.put("checkinLast7Days", checkinLast7Days);
        result.put("pendingStudentAudit", pendingAudit);
        result.put("pendingMessages", pendingMessages);
        result.put("activeClasses", activeClasses);
        
        // 计算打卡率
        double checkinRate = studentCount > 0 ? (double) checkinLast7Days / (studentCount * 7) * 100 : 0;
        result.put("checkinRate", String.format("%.1f%%", checkinRate));
        
        // 计算认证率
        long approvedCount = studentProfileMapper.selectCount(new QueryWrapper<StudentProfile>()
                .eq("verification_status", "approved"));
        double verificationRate = studentCount > 0 ? (double) approvedCount / studentCount * 100 : 0;
        result.put("verificationRate", String.format("%.1f%%", verificationRate));
        
        // 获取今日打卡数
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        long todayCheckin = checkinMapper.selectCount(new QueryWrapper<Checkin>()
                .ge("checkin_date", todayStart));
        result.put("todayCheckin", todayCheckin);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getCheckinTrend() {
        List<Map<String, Object>> trend = new java.util.ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Date dayStart = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dayEnd = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = checkinMapper.selectCount(new QueryWrapper<Checkin>()
                    .ge("checkin_date", dayStart)
                    .lt("checkin_date", dayEnd));
            
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            trend.add(item);
        }
        
        return trend;
    }

    @Override
    public List<Map<String, Object>> getClassRanking() {
        List<ClassChallengeVO> rows = checkinService.getClassChallengeRanking(7, 10);
        List<Map<String, Object>> ranking = new java.util.ArrayList<>();
        
        for (int i = 0; i < rows.size(); i++) {
            ClassChallengeVO row = rows.get(i);
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("rank", i + 1);
            item.put("className", row.getClassName());
            item.put("checkinCount", row.getCheckinCount() != null ? row.getCheckinCount() : 0);
            item.put("participantCount", row.getParticipantCount() != null ? row.getParticipantCount() : 0);
            ranking.add(item);
        }
        
        return ranking;
    }

    @Override
    public List<Map<String, Object>> getTestItemDistribution() {
        List<TestScore> scores = testScoreMapper.selectList(new QueryWrapper<>());
        Map<String, Long> distribution = scores.stream()
                .collect(Collectors.groupingBy(TestScore::getItemCode, Collectors.counting()));
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<String, Long> entry : distribution.entrySet()) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("itemCode", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getUserActivityStats() {
        Map<String, Object> stats = new java.util.HashMap<>();

        LocalDate from = LocalDate.now().minusDays(6);
        Date fromDate = Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Checkin> recentCheckins = checkinMapper.selectList(new QueryWrapper<Checkin>()
                .ge("checkin_date", fromDate));
        Set<Long> activeUsers = recentCheckins.stream()
                .map(Checkin::getUserId)
                .collect(Collectors.toSet());

        long totalStudents = countStudents();
        stats.put("activeUsers", activeUsers.size());
        stats.put("totalStudents", totalStudents);
        stats.put("activityRate", totalStudents > 0 ? String.format("%.1f%%", (double) activeUsers.size() / totalStudents * 100) : "0%");

        long totalDuration = recentCheckins.stream()
                .mapToLong(c -> c.getDurationMinutes() != null ? c.getDurationMinutes() : 0)
                .sum();
        long avgDuration = recentCheckins.size() > 0 ? totalDuration / recentCheckins.size() : 0;
        stats.put("avgDuration", avgDuration);

        return stats;
    }

    @Override
    public Map<String, Object> getAnalysisOverview() {
        Map<String, Object> result = new java.util.HashMap<>();
        List<User> students = userMapper.selectList(new QueryWrapper<User>().eq("user_role", "student"));
        List<TestScore> scores = testScoreMapper.selectList(new QueryWrapper<TestScore>());
        Map<Long, Map<String, TestScore>> latestByUserItem = buildLatestScoreMap(scores);

        int totalStudents = students.size();
        int measuredStudents = latestByUserItem.size();
        int totalPoint = latestByUserItem.values().stream()
                .flatMap(map -> map.values().stream())
                .mapToInt(score -> score.getStandardPoint() == null ? 0 : score.getStandardPoint())
                .sum();
        long scoreCount = latestByUserItem.values().stream().mapToLong(Map::size).sum();
        double avgScore = scoreCount > 0 ? (double) totalPoint / scoreCount : 0D;

        long excellentCount = latestByUserItem.values().stream()
                .flatMap(map -> map.values().stream())
                .filter(score -> score.getStandardPoint() != null && score.getStandardPoint() >= 80)
                .count();
        long passCount = latestByUserItem.values().stream()
                .flatMap(map -> map.values().stream())
                .filter(score -> score.getStandardPoint() != null && score.getStandardPoint() >= 60)
                .count();

        result.put("totalStudents", measuredStudents);
        result.put("registeredStudents", totalStudents);
        result.put("avgScore", BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP));
        result.put("excellentRate", scoreCount > 0 ? formatPercent((double) excellentCount / scoreCount * 100) : "0.0%");
        result.put("passRate", scoreCount > 0 ? formatPercent((double) passCount / scoreCount * 100) : "0.0%");
        result.put("testItemStats", buildTestItemStats(latestByUserItem));
        result.put("gradeDistribution", buildGradeDistribution(latestByUserItem));
        result.put("checkinStats", buildCheckinAnalysis());
        result.put("planStats", buildPlanAnalysis());
        result.put("classComparison", getClassComparisonData());
        return result;
    }

    @Override
    public List<Map<String, Object>> getClassComparisonData() {
        List<Class> classes = classMapper.selectList(new LambdaQueryWrapper<Class>()
                .eq(Class::getIsDelete, 0)
                .eq(Class::getStatus, 1)
                .orderByAsc(Class::getClassName));
        if (classes.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        List<TestScore> allScores = testScoreMapper.selectList(new QueryWrapper<TestScore>());
        Map<Long, Map<String, TestScore>> latestByUserItem = buildLatestScoreMap(allScores);
        List<Checkin> allCheckins = checkinMapper.selectList(new QueryWrapper<Checkin>());
        List<Map<String, Object>> rows = new java.util.ArrayList<>();

        for (Class clazz : classes) {
            List<ClassMember> members = classMemberMapper.selectList(new LambdaQueryWrapper<ClassMember>()
                    .eq(ClassMember::getClassId, clazz.getId())
                    .eq(ClassMember::getIsDelete, 0));
            Set<Long> userIds = members.stream().map(ClassMember::getUserId).collect(Collectors.toSet());
            if (userIds.isEmpty()) {
                rows.add(buildEmptyClassComparison(clazz));
                continue;
            }

            int totalScore = 0;
            int scoreCount = 0;
            int excellentCount = 0;
            for (Long userId : userIds) {
                Map<String, TestScore> latestItems = latestByUserItem.getOrDefault(userId, Map.of());
                for (TestScore score : latestItems.values()) {
                    int point = score.getStandardPoint() == null ? 0 : score.getStandardPoint();
                    totalScore += point;
                    scoreCount++;
                    if (point >= 80) {
                        excellentCount++;
                    }
                }
            }

            List<Checkin> classCheckins = allCheckins.stream()
                    .filter(checkin -> userIds.contains(checkin.getUserId()))
                    .collect(Collectors.toList());
            Set<Long> checkinUsers = classCheckins.stream().map(Checkin::getUserId).collect(Collectors.toSet());
            int totalDuration = classCheckins.stream().mapToInt(item -> item.getDurationMinutes() == null ? 0 : item.getDurationMinutes()).sum();

            Map<String, Object> row = new java.util.HashMap<>();
            row.put("classId", clazz.getId());
            row.put("className", clazz.getClassName());
            row.put("studentCount", userIds.size());
            row.put("checkinRate", userIds.isEmpty() ? "0.0%" : formatPercent((double) checkinUsers.size() / userIds.size() * 100));
            row.put("avgScore", scoreCount > 0 ? BigDecimal.valueOf((double) totalScore / scoreCount).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            row.put("avgDuration", userIds.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf((double) totalDuration / userIds.size()).setScale(2, RoundingMode.HALF_UP));
            row.put("excellentRate", scoreCount > 0 ? formatPercent((double) excellentCount / scoreCount * 100) : "0.0%");
            row.put("checkinCount", classCheckins.size());
            row.put("participantCount", checkinUsers.size());
            rows.add(row);
        }

        rows.sort((a, b) -> {
            BigDecimal avgA = new BigDecimal(String.valueOf(a.get("avgScore")));
            BigDecimal avgB = new BigDecimal(String.valueOf(b.get("avgScore")));
            return avgB.compareTo(avgA);
        });
        return rows;
    }

    private Map<Long, Map<String, TestScore>> buildLatestScoreMap(List<TestScore> scores) {
        Map<Long, Map<String, TestScore>> latestByUserItem = new HashMap<>();
        List<TestScore> sorted = scores.stream()
                .sorted(Comparator.comparing(TestScore::getRecordedDate, Comparator.nullsLast(Date::compareTo)).reversed()
                        .thenComparing(TestScore::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .collect(Collectors.toList());
        for (TestScore score : sorted) {
            latestByUserItem.computeIfAbsent(score.getUserId(), key -> new HashMap<>())
                    .putIfAbsent(score.getItemCode(), score);
        }
        return latestByUserItem;
    }

    private List<Map<String, Object>> buildTestItemStats(Map<Long, Map<String, TestScore>> latestByUserItem) {
        Map<String, String> itemNameMap = testItemMapper.selectList(new QueryWrapper<TestItem>().eq("status", 1))
                .stream()
                .collect(Collectors.toMap(TestItem::getItemCode, TestItem::getItemName, (a, b) -> a));
        Map<String, List<Integer>> pointsByItem = new HashMap<>();
        for (Map<String, TestScore> latestItems : latestByUserItem.values()) {
            for (TestScore score : latestItems.values()) {
                pointsByItem.computeIfAbsent(score.getItemCode(), key -> new java.util.ArrayList<>())
                        .add(score.getStandardPoint() == null ? 0 : score.getStandardPoint());
            }
        }

        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : pointsByItem.entrySet()) {
            List<Integer> values = entry.getValue();
            if (values.isEmpty()) {
                continue;
            }
            double avg = values.stream().mapToInt(Integer::intValue).average().orElse(0D);
            int max = values.stream().mapToInt(Integer::intValue).max().orElse(0);
            int min = values.stream().mapToInt(Integer::intValue).min().orElse(0);
            long excellent = values.stream().filter(v -> v >= 80).count();
            long pass = values.stream().filter(v -> v >= 60).count();

            Map<String, Object> item = new java.util.HashMap<>();
            item.put("itemCode", entry.getKey());
            item.put("itemName", itemNameMap.getOrDefault(entry.getKey(), entry.getKey()));
            item.put("participantCount", values.size());
            item.put("avgScore", BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            item.put("maxScore", max);
            item.put("minScore", min);
            item.put("excellentRate", formatPercent((double) excellent / values.size() * 100));
            item.put("passRate", formatPercent((double) pass / values.size() * 100));
            rows.add(item);
        }
        rows.sort(Comparator.comparing(item -> String.valueOf(item.get("itemCode"))));
        return rows;
    }

    private List<Map<String, Object>> buildGradeDistribution(Map<Long, Map<String, TestScore>> latestByUserItem) {
        Map<String, Long> counts = new HashMap<>();
        counts.put("excellent", 0L);
        counts.put("good", 0L);
        counts.put("pass", 0L);
        counts.put("fail", 0L);
        for (Map<String, TestScore> latestItems : latestByUserItem.values()) {
            for (TestScore score : latestItems.values()) {
                int point = score.getStandardPoint() == null ? 0 : score.getStandardPoint();
                if (point >= 80) {
                    counts.put("excellent", counts.get("excellent") + 1);
                } else if (point >= 70) {
                    counts.put("good", counts.get("good") + 1);
                } else if (point >= 60) {
                    counts.put("pass", counts.get("pass") + 1);
                } else {
                    counts.put("fail", counts.get("fail") + 1);
                }
            }
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        result.add(buildDistributionItem("excellent", "优秀", counts.get("excellent")));
        result.add(buildDistributionItem("good", "良好", counts.get("good")));
        result.add(buildDistributionItem("pass", "及格", counts.get("pass")));
        result.add(buildDistributionItem("fail", "不及格", counts.get("fail")));
        return result;
    }

    private Map<String, Object> buildCheckinAnalysis() {
        Map<String, Object> result = new java.util.HashMap<>();
        List<Checkin> rows = checkinMapper.selectList(new QueryWrapper<Checkin>().orderByDesc("checkin_date"));
        Set<Long> activeUsers = rows.stream().map(Checkin::getUserId).collect(Collectors.toSet());
        long totalDuration = rows.stream().mapToLong(item -> item.getDurationMinutes() == null ? 0 : item.getDurationMinutes()).sum();
        result.put("totalCheckins", rows.size());
        result.put("activeUsers", activeUsers.size());
        result.put("avgDuration", rows.isEmpty() ? 0 : BigDecimal.valueOf((double) totalDuration / rows.size()).setScale(2, RoundingMode.HALF_UP));
        result.put("checkinRate", countStudents() > 0 ? formatPercent((double) activeUsers.size() / countStudents() * 100) : "0.0%");

        Map<LocalDate, List<Checkin>> grouped = rows.stream().collect(Collectors.groupingBy(item -> item.getCheckinDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
        List<Map<String, Object>> details = grouped.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<Checkin>>comparingByKey().reversed())
                .limit(30)
                .map(entry -> {
                    List<Checkin> dayRows = entry.getValue();
                    IntSummaryStatistics summary = dayRows.stream().mapToInt(item -> item.getDurationMinutes() == null ? 0 : item.getDurationMinutes()).summaryStatistics();
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("date", entry.getKey().toString());
                    item.put("checkinCount", dayRows.size());
                    item.put("activeUserCount", dayRows.stream().map(Checkin::getUserId).collect(Collectors.toSet()).size());
                    item.put("avgDuration", summary.getCount() == 0 ? 0 : BigDecimal.valueOf(summary.getAverage()).setScale(2, RoundingMode.HALF_UP));
                    item.put("maxDuration", summary.getCount() == 0 ? 0 : summary.getMax());
                    item.put("minDuration", summary.getCount() == 0 ? 0 : summary.getMin());
                    return item;
                })
                .collect(Collectors.toList());
        result.put("details", details);

        int[] buckets = new int[7];
        for (Checkin item : rows) {
            int hour = item.getCheckinDate().toInstant().atZone(ZoneId.systemDefault()).getHour();
            if (hour < 8) buckets[0]++;
            else if (hour < 10) buckets[1]++;
            else if (hour < 12) buckets[2]++;
            else if (hour < 16) buckets[3]++;
            else if (hour < 18) buckets[4]++;
            else if (hour < 20) buckets[5]++;
            else buckets[6]++;
        }
        List<Map<String, Object>> timeDistribution = new java.util.ArrayList<>();
        String[] labels = {"6-8点", "8-10点", "10-12点", "12-16点", "16-18点", "18-20点", "20-24点"};
        for (int i = 0; i < labels.length; i++) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("label", labels[i]);
            item.put("count", buckets[i]);
            timeDistribution.add(item);
        }
        result.put("timeDistribution", timeDistribution);
        return result;
    }

    private Map<String, Object> buildPlanAnalysis() {
        Map<String, Object> result = new java.util.HashMap<>();
        List<UserPlan> plans = userPlanMapper.selectList(new QueryWrapper<UserPlan>().orderByDesc("create_time"));
        List<UserPlanItem> planItems = userPlanItemMapper.selectList(new QueryWrapper<UserPlanItem>());
        Map<Long, List<UserPlanItem>> itemMap = planItems.stream().collect(Collectors.groupingBy(UserPlanItem::getUserPlanId));
        Map<Long, User> userMap = userMapper.selectList(new QueryWrapper<User>()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        int completedPlans = 0;
        List<Map<String, Object>> details = new java.util.ArrayList<>();
        Map<String, Integer> difficultyMap = new HashMap<>();
        difficultyMap.put("低", 0);
        difficultyMap.put("中等", 0);
        difficultyMap.put("高", 0);

        for (UserPlan plan : plans) {
            List<UserPlanItem> items = itemMap.getOrDefault(plan.getId(), java.util.Collections.emptyList());
            long completedCount = items.stream().filter(item -> item.getCompleted() != null && item.getCompleted() == 1).count();
            int progress = items.isEmpty() ? 0 : (int) Math.round((double) completedCount / items.size() * 100);
            if (progress >= 100 || "completed".equals(plan.getStatus())) {
                completedPlans++;
            }
            String difficulty = resolvePlanDifficulty(plan.getFitnessLevel());
            difficultyMap.put(difficulty, difficultyMap.getOrDefault(difficulty, 0) + 1);

            Map<String, Object> row = new java.util.HashMap<>();
            row.put("planId", plan.getId());
            row.put("studentName", userMap.containsKey(plan.getUserId()) ? userMap.get(plan.getUserId()).getUserName() : "用户#" + plan.getUserId());
            row.put("targetItem", plan.getTestItemCode());
            row.put("difficulty", difficulty);
            row.put("progress", progress);
            row.put("startDate", plan.getStartDate());
            row.put("endDate", plan.getEndDate());
            details.add(row);
        }

        double avgProgress = details.stream().mapToInt(item -> (Integer) item.get("progress")).average().orElse(0D);
        result.put("totalPlans", plans.size());
        result.put("completionRate", plans.isEmpty() ? "0.0%" : formatPercent((double) completedPlans / plans.size() * 100));
        result.put("avgProgress", plans.isEmpty() ? "0.0%" : formatPercent(avgProgress));
        result.put("incompletePlans", Math.max(plans.size() - completedPlans, 0));
        result.put("details", details.stream().sorted((a, b) -> Integer.compare((Integer) b.get("progress"), (Integer) a.get("progress"))).limit(30).collect(Collectors.toList()));
        result.put("difficultyDistribution", difficultyMap.entrySet().stream().map(entry -> {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            return item;
        }).collect(Collectors.toList()));
        return result;
    }

    private String resolvePlanDifficulty(String fitnessLevel) {
        if ("advanced".equalsIgnoreCase(fitnessLevel)) {
            return "高";
        }
        if ("basic".equalsIgnoreCase(fitnessLevel)) {
            return "中等";
        }
        return "低";
    }

    private Map<String, Object> buildDistributionItem(String key, String name, Long value) {
        Map<String, Object> item = new java.util.HashMap<>();
        item.put("key", key);
        item.put("name", name);
        item.put("value", value == null ? 0 : value);
        return item;
    }

    private Map<String, Object> buildEmptyClassComparison(Class clazz) {
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("classId", clazz.getId());
        row.put("className", clazz.getClassName());
        row.put("studentCount", 0);
        row.put("checkinRate", "0.0%");
        row.put("avgScore", BigDecimal.ZERO);
        row.put("avgDuration", BigDecimal.ZERO);
        row.put("excellentRate", "0.0%");
        row.put("checkinCount", 0);
        row.put("participantCount", 0);
        return row;
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
