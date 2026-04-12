package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.unifit.mapper.CheckinMapper;
import com.jcen.unifit.mapper.ConsultMessageMapper;
import com.jcen.unifit.mapper.StudentProfileMapper;
import com.jcen.unifit.mapper.TestScoreMapper;
import com.jcen.unifit.mapper.UserMapper;
import com.jcen.unifit.model.entity.Checkin;
import com.jcen.unifit.model.entity.ConsultMessage;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.TestScore;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ClassChallengeVO;
import com.jcen.unifit.service.AdminService;
import com.jcen.unifit.service.CheckinService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TestScoreMapper testScoreMapper;

    @Resource
    private CheckinMapper checkinMapper;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Resource
    private ConsultMessageMapper consultMessageMapper;

    @Resource
    private CheckinService checkinService;

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
        
        // 活跃用户（最近7天有打卡）
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
        
        // 平均打卡时长
        long totalDuration = recentCheckins.stream()
                .mapToLong(c -> c.getDurationMinutes() != null ? c.getDurationMinutes() : 0)
                .sum();
        long avgDuration = recentCheckins.size() > 0 ? totalDuration / recentCheckins.size() : 0;
        stats.put("avgDuration", avgDuration);
        
        return stats;
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
