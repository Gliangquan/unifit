package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.HealthProfileMapper;
import com.jcen.unifit.mapper.StudentProfileMapper;
import com.jcen.unifit.mapper.TestScoreMapper;
import com.jcen.unifit.mapper.TestItemMapper;
import com.jcen.unifit.mapper.TestStandardMapper;
import com.jcen.unifit.model.dto.TestScoreAddRequest;
import com.jcen.unifit.model.entity.HealthProfile;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.model.entity.TestScore;
import com.jcen.unifit.model.entity.TestStandard;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.TestScoreVO;
import com.jcen.unifit.service.TestService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestServiceImpl implements TestService {

    @Resource
    private TestScoreMapper testScoreMapper;

    @Resource
    private TestStandardMapper testStandardMapper;

    @Resource
    private HealthProfileMapper healthProfileMapper;

    @Resource
    private TestItemMapper testItemMapper;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Override
    public TestScoreVO addScore(User loginUser, TestScoreAddRequest request) {
        if (request == null || StringUtils.isBlank(request.getItemCode()) || request.getScoreValue() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "成绩参数不完整");
        }
        ensureStudentVerified(loginUser);

        TestStandard matched = matchStandard(loginUser.getId(), request.getItemCode(), request.getScoreValue());

        TestScore score = new TestScore();
        score.setUserId(loginUser.getId());
        score.setItemCode(request.getItemCode());
        score.setScoreValue(request.getScoreValue());
        score.setLevel(matched == null ? "unknown" : matched.getLevel());
        score.setStandardPoint(matched == null ? 0 : matched.getStandardPoint());
        score.setRecordedDate(new Date());
        score.setCreateTime(new Date());
        testScoreMapper.insert(score);

        TestScoreVO vo = new TestScoreVO();
        vo.setItemCode(score.getItemCode());
        vo.setScoreValue(score.getScoreValue());
        vo.setLevel(score.getLevel());
        vo.setStandardPoint(score.getStandardPoint());
        vo.setRecordedDate(score.getRecordedDate());
        return vo;
    }

    @Override
    public List<TestScoreVO> listMyScores(User loginUser, String itemCode) {
        QueryWrapper<TestScore> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId());
        if (itemCode != null && !itemCode.isBlank()) {
            qw.eq("item_code", itemCode);
        }
        qw.orderByDesc("recorded_date");
        List<TestScore> scores = testScoreMapper.selectList(qw);
        List<TestScoreVO> result = new ArrayList<>();
        for (TestScore score : scores) {
            TestScoreVO vo = new TestScoreVO();
            vo.setItemCode(score.getItemCode());
            vo.setScoreValue(score.getScoreValue());
            vo.setLevel(score.getLevel());
            vo.setStandardPoint(score.getStandardPoint());
            vo.setRecordedDate(score.getRecordedDate());
            result.add(vo);
        }
        return result;
    }

    @Override
    public Map<String, Object> getWeaknessAnalysis(User loginUser) {
        QueryWrapper<TestScore> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId()).orderByDesc("recorded_date");
        List<TestScore> scores = testScoreMapper.selectList(qw);

        Map<String, TestScore> latestByItem = new HashMap<>();
        for (TestScore score : scores) {
            latestByItem.putIfAbsent(score.getItemCode(), score);
        }

        List<Map<String, Object>> weakItems = latestByItem.values().stream()
                .filter(item -> item.getStandardPoint() != null && item.getStandardPoint() < 60)
                .map(item -> {
                    Map<String, Object> x = new HashMap<>();
                    x.put("itemCode", item.getItemCode());
                    x.put("level", item.getLevel());
                    x.put("standardPoint", item.getStandardPoint());
                    x.put("suggestion", "建议针对该项目增加专项训练与有氧耐力训练");
                    return x;
                }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("weakItems", weakItems);
        result.put("totalItems", latestByItem.size());
        return result;
    }

    @Override
    public Map<String, Object> getClassCompare(User loginUser) {
        ensureStudentVerified(loginUser);
        StudentProfile myProfile = studentProfileMapper.selectOne(new QueryWrapper<StudentProfile>()
                .eq("user_id", loginUser.getId())
                .eq("verification_status", "approved")
                .last("limit 1"));
        if (myProfile == null || StringUtils.isBlank(myProfile.getClassName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先完善班级信息并通过学生认证");
        }

        List<StudentProfile> classmates = studentProfileMapper.selectList(new QueryWrapper<StudentProfile>()
                .eq("verification_status", "approved")
                .eq("class_name", myProfile.getClassName())
                .orderByAsc("id"));
        if (classmates.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("className", myProfile.getClassName());
            empty.put("classSize", 0);
            empty.put("myRank", 0);
            empty.put("myTotalPoint", 0);
            empty.put("classAverageTotalPoint", 0);
            empty.put("itemComparisons", new ArrayList<>());
            return empty;
        }

        List<Long> classUserIds = classmates.stream().map(StudentProfile::getUserId).collect(Collectors.toList());
        List<TestScore> classScores = testScoreMapper.selectList(new QueryWrapper<TestScore>()
                .in("user_id", classUserIds)
                .orderByDesc("recorded_date", "id"));

        Map<Long, Map<String, TestScore>> latestByUserItem = new HashMap<>();
        for (TestScore score : classScores) {
            latestByUserItem.computeIfAbsent(score.getUserId(), k -> new HashMap<>())
                    .putIfAbsent(score.getItemCode(), score);
        }

        Map<String, String> itemNameMap = testItemMapper.selectList(new QueryWrapper<TestItem>().eq("status", 1))
                .stream().collect(Collectors.toMap(TestItem::getItemCode, TestItem::getItemName, (a, b) -> a));

        Set<String> itemCodes = new HashSet<>();
        Map<Long, Integer> totalPointByUser = new HashMap<>();
        Map<String, List<Integer>> pointByItem = new HashMap<>();

        for (Map.Entry<Long, Map<String, TestScore>> entry : latestByUserItem.entrySet()) {
            int total = 0;
            for (TestScore latest : entry.getValue().values()) {
                int point = latest.getStandardPoint() == null ? 0 : latest.getStandardPoint();
                total += point;
                itemCodes.add(latest.getItemCode());
                pointByItem.computeIfAbsent(latest.getItemCode(), k -> new ArrayList<>()).add(point);
            }
            totalPointByUser.put(entry.getKey(), total);
        }

        Map<String, TestScore> myLatestByItem = latestByUserItem.getOrDefault(loginUser.getId(), new HashMap<>());
        List<Map<String, Object>> itemComparisons = new ArrayList<>();
        List<String> sortedItemCodes = itemCodes.stream().sorted().collect(Collectors.toList());
        for (String itemCode : sortedItemCodes) {
            List<Integer> points = pointByItem.getOrDefault(itemCode, new ArrayList<>());
            double avg = points.isEmpty() ? 0D : points.stream().mapToInt(Integer::intValue).average().orElse(0D);
            TestScore myScore = myLatestByItem.get(itemCode);
            int myPoint = myScore == null || myScore.getStandardPoint() == null ? 0 : myScore.getStandardPoint();

            Map<String, Object> row = new HashMap<>();
            row.put("itemCode", itemCode);
            row.put("itemName", itemNameMap.getOrDefault(itemCode, itemCode));
            row.put("myPoint", myPoint);
            row.put("classAvgPoint", BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            row.put("gap", BigDecimal.valueOf(myPoint - avg).setScale(2, RoundingMode.HALF_UP));
            itemComparisons.add(row);
        }

        List<Map.Entry<Long, Integer>> rankList = totalPointByUser.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toList());
        int myRank = 0;
        for (int i = 0; i < rankList.size(); i++) {
            if (rankList.get(i).getKey().equals(loginUser.getId())) {
                myRank = i + 1;
                break;
            }
        }

        double classAvgTotal = totalPointByUser.isEmpty()
                ? 0D
                : totalPointByUser.values().stream().mapToInt(Integer::intValue).average().orElse(0D);

        Map<String, Object> result = new HashMap<>();
        result.put("className", myProfile.getClassName());
        result.put("classSize", classmates.size());
        result.put("myRank", myRank);
        result.put("myTotalPoint", totalPointByUser.getOrDefault(loginUser.getId(), 0));
        result.put("classAverageTotalPoint", BigDecimal.valueOf(classAvgTotal).setScale(2, RoundingMode.HALF_UP));
        result.put("itemComparisons", itemComparisons);
        return result;
    }

    @Override
    public List<TestItem> listTestItems() {
        QueryWrapper<TestItem> qw = new QueryWrapper<>();
        qw.eq("status", 1).orderByAsc("id");
        return testItemMapper.selectList(qw);
    }

    @Override
    public Map<String, Object> getScoreLevelPreview(User loginUser, String itemCode, BigDecimal scoreValue) {
        if (StringUtils.isBlank(itemCode) || scoreValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "成绩参数不完整");
        }
        ensureStudentVerified(loginUser);
        TestStandard matched = matchStandard(loginUser.getId(), itemCode, scoreValue);
        Map<String, Object> result = new HashMap<>();
        result.put("itemCode", itemCode);
        result.put("scoreValue", scoreValue);
        result.put("level", mapStandardPointToPlanLevel(matched == null ? null : matched.getStandardPoint()));
        result.put("standardPoint", matched == null ? 0 : matched.getStandardPoint());
        result.put("standardLevel", matched == null ? "unknown" : matched.getLevel());
        return result;
    }

    private TestStandard matchStandard(Long userId, String itemCode, BigDecimal scoreValue) {
        HealthProfile profile = healthProfileMapper.selectOne(new QueryWrapper<HealthProfile>().eq("user_id", userId));
        String gender = profile == null || profile.getGender() == null ? "male" : profile.getGender();

        QueryWrapper<TestStandard> qw = new QueryWrapper<>();
        qw.eq("stage", "college")
                .eq("item_code", itemCode)
                .eq("gender", gender)
                .le("min_score", scoreValue)
                .ge("max_score", scoreValue)
                .orderByDesc("standard_point")
                .last("limit 1");
        return testStandardMapper.selectOne(qw);
    }

    private String mapStandardPointToPlanLevel(Integer standardPoint) {
        if (standardPoint == null || standardPoint < 60) {
            return "beginner";
        }
        if (standardPoint < 80) {
            return "intermediate";
        }
        return "advanced";
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
