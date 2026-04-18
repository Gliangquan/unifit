package com.jcen.unifit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.mapper.ExerciseAlternativeMapper;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.ExerciseMapper;
import com.jcen.unifit.mapper.HealthProfileMapper;
import com.jcen.unifit.mapper.PlanTemplateItemMapper;
import com.jcen.unifit.mapper.PlanTemplateMapper;
import com.jcen.unifit.mapper.StudentProfileMapper;
import com.jcen.unifit.mapper.TestItemMapper;
import com.jcen.unifit.mapper.TestStandardMapper;
import com.jcen.unifit.mapper.UserMapper;
import com.jcen.unifit.mapper.UserPlanItemMapper;
import com.jcen.unifit.mapper.UserPlanMapper;
import com.jcen.unifit.model.dto.PlanGenerateRequest;
import com.jcen.unifit.model.entity.ExerciseAlternative;
import com.jcen.unifit.model.entity.Exercise;
import com.jcen.unifit.model.entity.HealthProfile;
import com.jcen.unifit.model.entity.PlanTemplate;
import com.jcen.unifit.model.entity.PlanTemplateItem;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.model.entity.TestStandard;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.entity.UserPlan;
import com.jcen.unifit.model.entity.UserPlanItem;
import com.jcen.unifit.model.vo.PlanItemVO;
import com.jcen.unifit.model.vo.PlanVO;
import com.jcen.unifit.service.PlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlanServiceImpl implements PlanService {

    private static final BigDecimal PLAN_UNLOCK_PRICE = BigDecimal.valueOf(19.9).setScale(2, RoundingMode.HALF_UP);

    @Resource
    private PlanTemplateMapper planTemplateMapper;

    @Resource
    private PlanTemplateItemMapper planTemplateItemMapper;

    @Resource
    private UserPlanMapper userPlanMapper;

    @Resource
    private UserPlanItemMapper userPlanItemMapper;

    @Resource
    private ExerciseMapper exerciseMapper;

    @Resource
    private ExerciseAlternativeMapper exerciseAlternativeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Resource
    private TestStandardMapper testStandardMapper;

    @Resource
    private TestItemMapper testItemMapper;

    @Resource
    private HealthProfileMapper healthProfileMapper;

    @Override
    public PlanVO generatePlan(User loginUser, PlanGenerateRequest request) {
        if (request == null || StringUtils.isBlank(request.getTestItemCode())
                || request.getCurrentScore() == null || request.getDaysPerWeek() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "计划参数不完整");
        }
        if (request.getDaysPerWeek() < 1 || request.getDaysPerWeek() > 7) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "每周训练天数必须在1-7之间");
        }

        User latest = userMapper.selectById(loginUser.getId());
        if (latest == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        ensureStudentVerified(latest);
        if (!Integer.valueOf(1).equals(latest.getPlanUnlocked())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请先购买方案后再生成训练计划");
        }

        String scoreLevel = calculateScoreLevel(latest.getId(), request);
        String bmiRange = resolveBmiRange(latest.getId(), request.getBmiValue());
        String normalizedFitnessLevel = normalizeFitnessLevel(request.getFitnessLevel());
        String normalizedEquipmentType = normalizeEquipmentType(request.getEquipmentType());
        PlanTemplate template = pickTemplate(request, scoreLevel, bmiRange);
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未匹配到训练模板");
        }

        // 只保留一个进行中的计划
        Date now = new Date();
        UpdateWrapper<UserPlan> archiveUw = new UpdateWrapper<>();
        archiveUw.eq("user_id", loginUser.getId())
                .eq("status", "active")
                .set("status", "archived")
                .set("update_time", now);
        userPlanMapper.update(null, archiveUw);

        UserPlan userPlan = new UserPlan();
        userPlan.setUserId(loginUser.getId());
        userPlan.setTemplateId(template.getId());
        userPlan.setTestItemCode(request.getTestItemCode());
        userPlan.setScoreLevel(scoreLevel);
        userPlan.setFitnessLevel(StringUtils.defaultIfBlank(normalizedFitnessLevel, request.getFitnessLevel()));
        userPlan.setEquipmentType(StringUtils.defaultIfBlank(normalizedEquipmentType, request.getEquipmentType()));
        userPlan.setDaysPerWeek(request.getDaysPerWeek());
        userPlan.setStatus("active");

        LocalDate startLocal = LocalDate.now();
        LocalDate endLocal = startLocal.plusWeeks(4).minusDays(1);
        userPlan.setStartDate(Date.from(startLocal.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        userPlan.setEndDate(Date.from(endLocal.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        userPlan.setSnapshotJson(JSONUtil.toJsonStr(request));
        userPlan.setCreateTime(now);
        userPlan.setUpdateTime(now);
        userPlanMapper.insert(userPlan);

        QueryWrapper<PlanTemplateItem> itemQw = new QueryWrapper<>();
        itemQw.eq("template_id", template.getId()).orderByAsc("week_no", "day_no", "sort_no");
        List<PlanTemplateItem> templateItems = planTemplateItemMapper.selectList(itemQw);
        List<UserPlanItem> personalizedItems = buildPersonalizedPlanItems(templateItems, request, bmiRange, scoreLevel);
        for (UserPlanItem userPlanItem : personalizedItems) {
            userPlanItem.setUserPlanId(userPlan.getId());
            userPlanItem.setCompleted(0);
            userPlanItem.setCreateTime(new Date());
            userPlanItemMapper.insert(userPlanItem);
        }

        return assemblePlanVO(userPlan);
    }

    @Override
    public PlanVO getCurrentPlan(User loginUser) {
        ensureStudentVerified(loginUser);
        QueryWrapper<UserPlan> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId()).eq("status", "active").orderByDesc("id").last("limit 1");
        UserPlan plan = userPlanMapper.selectOne(qw);
        return plan == null ? null : assemblePlanVO(plan);
    }

    @Override
    public List<PlanVO> listPlans(User loginUser) {
        ensureStudentVerified(loginUser);
        QueryWrapper<UserPlan> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId()).orderByDesc("id");
        List<UserPlan> plans = userPlanMapper.selectList(qw);
        List<PlanVO> result = new ArrayList<>();
        for (UserPlan plan : plans) {
            result.add(assemblePlanVO(plan));
        }
        return result;
    }

    @Override
    public boolean markPlanItemDone(User loginUser, Long planItemId) {
        ensureStudentVerified(loginUser);
        UserPlanItem item = userPlanItemMapper.selectById(planItemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "计划动作不存在");
        }
        UserPlan plan = userPlanMapper.selectById(item.getUserPlanId());
        if (plan == null || !plan.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
        }
        if (Integer.valueOf(1).equals(item.getCompleted())) {
            return true;
        }
        item.setCompleted(1);
        item.setCompleteTime(new Date());
        boolean updated = userPlanItemMapper.updateById(item) > 0;
        if (!updated) {
            return false;
        }
        long remaining = userPlanItemMapper.selectCount(new QueryWrapper<UserPlanItem>()
                .eq("user_plan_id", plan.getId())
                .eq("completed", 0));
        if (remaining == 0 && "active".equals(plan.getStatus())) {
            plan.setStatus("completed");
            plan.setUpdateTime(new Date());
            userPlanMapper.updateById(plan);
        }
        return true;
    }

    @Override
    public Map<String, Object> purchasePlanAccess(User loginUser) {
        User latest = userMapper.selectById(loginUser.getId());
        if (latest == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        ensureStudentVerified(latest);

        BigDecimal currentBalance = latest.getBalance() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : latest.getBalance().setScale(2, RoundingMode.HALF_UP);

        if (Integer.valueOf(1).equals(latest.getPlanUnlocked())) {
            return buildPurchaseResult(currentBalance, BigDecimal.ZERO, true);
        }

        if (currentBalance.compareTo(PLAN_UNLOCK_PRICE) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "余额不足，请先模拟充值");
        }

        BigDecimal afterPay = currentBalance.subtract(PLAN_UNLOCK_PRICE).setScale(2, RoundingMode.HALF_UP);
        latest.setBalance(afterPay);
        latest.setPlanUnlocked(1);
        latest.setPlanUnlockTime(new Date());
        int updated = userMapper.updateById(latest);
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "购买失败");
        }
        return buildPurchaseResult(afterPay, PLAN_UNLOCK_PRICE, false);
    }

    private Map<String, Object> buildPurchaseResult(BigDecimal balance, BigDecimal cost, boolean alreadyUnlocked) {
        Map<String, Object> result = new HashMap<>();
        result.put("planUnlocked", 1);
        result.put("balance", balance);
        result.put("cost", cost);
        result.put("alreadyUnlocked", alreadyUnlocked);
        return result;
    }

    private PlanTemplate pickTemplate(PlanGenerateRequest request, String scoreLevel, String bmiRange) {
        QueryWrapper<PlanTemplate> qw = new QueryWrapper<>();
        qw.eq("status", 1)
                .eq("test_item_code", request.getTestItemCode())
                .eq("score_level", scoreLevel)
                .eq("days_per_week", request.getDaysPerWeek())
                .orderByDesc("id");
        List<PlanTemplate> candidates = planTemplateMapper.selectList(qw);
        if (candidates.isEmpty()) {
            return null;
        }

        String fit = normalizeFitnessLevel(request.getFitnessLevel());
        String equip = normalizeEquipmentType(request.getEquipmentType());
        String bmi = normalize(StringUtils.defaultIfBlank(bmiRange, "all"));

        PlanTemplate best = null;
        int bestScore = Integer.MIN_VALUE;
        for (PlanTemplate candidate : candidates) {
            int score = 0;
            String candidateFit = normalizeFitnessLevel(candidate.getFitnessLevel());
            String candidateEquip = normalizeEquipmentType(candidate.getEquipmentType());
            String candidateBmi = normalize(StringUtils.defaultIfBlank(candidate.getBmiRange(), "all"));

            if (StringUtils.isNotBlank(fit)) {
                score += fit.equals(candidateFit) ? 30 : -20;
            }
            if (StringUtils.isNotBlank(equip)) {
                score += equip.equals(candidateEquip) ? 30 : -20;
            }
            if (bmi.equals(candidateBmi)) {
                score += 25;
            } else if ("all".equals(candidateBmi)) {
                score += 10;
            } else {
                score -= 5;
            }

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private List<UserPlanItem> buildPersonalizedPlanItems(List<PlanTemplateItem> templateItems,
                                                          PlanGenerateRequest request,
                                                          String bmiRange,
                                                          String scoreLevel) {
        if (templateItems == null || templateItems.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> baseExerciseIds = templateItems.stream().map(PlanTemplateItem::getExerciseId).collect(Collectors.toSet());
        Map<Long, Exercise> exerciseMap = baseExerciseIds.isEmpty()
                ? new HashMap<>()
                : exerciseMapper.selectBatchIds(baseExerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity(), (a, b) -> a, HashMap::new));
        Map<Long, List<Exercise>> alternativeMap = loadAlternativeExerciseMap(baseExerciseIds, exerciseMap);
        List<Exercise> activeExercises = exerciseMapper.selectList(new QueryWrapper<Exercise>()
                .eq("status", 1)
                .orderByAsc("id"));

        boolean highBmi = "overweight".equals(bmiRange) || "obese".equals(bmiRange);
        boolean lowBmi = "underweight".equals(bmiRange);
        String normalizedFitnessLevel = normalizeFitnessLevel(request == null ? null : request.getFitnessLevel());
        String normalizedEquipmentType = normalizeEquipmentType(request == null ? null : request.getEquipmentType());
        String testItemCode = request == null ? null : request.getTestItemCode();
        int targetDifficultyRank = targetDifficultyRank(normalizedFitnessLevel, scoreLevel);
        int daysPerWeek = request == null || request.getDaysPerWeek() == null ? 0 : request.getDaysPerWeek();

        List<UserPlanItem> result = new ArrayList<>();
        for (PlanTemplateItem item : templateItems) {
            Long selectedExerciseId = item.getExerciseId();
            Exercise exercise = exerciseMap.get(selectedExerciseId);
            if (exercise == null) {
                selectedExerciseId = chooseExerciseForStage(item, alternativeMap, exerciseMap, normalizedFitnessLevel);
                exercise = exerciseMap.get(selectedExerciseId);
            }

            int sets = scaleMetric(item.getSetsCount(), 1D + 0.12D * Math.max(0, Math.min(3, safeWeek(item.getWeekNo()) - 1)), 1, 50);
            int reps = scaleMetric(item.getRepsCount(), 1D + 0.12D * Math.max(0, Math.min(3, safeWeek(item.getWeekNo()) - 1)), 1, 300);
            int duration = scaleMetric(item.getDurationMinutes(), 1D + 0.10D * Math.max(0, Math.min(3, safeWeek(item.getWeekNo()) - 1)), 5, 180);
            String note = appendNote(StringUtils.trimToEmpty(item.getIntensityNote()),
                    buildPersonalizationNote(testItemCode, normalizedEquipmentType, safeDay(item.getDayNo()), daysPerWeek));

            if (exercise != null) {
                if (highBmi) {
                    if (isAerobicExercise(exercise)) {
                        duration = scaleMetric(duration, 1.20D, 8, 240);
                    } else {
                        note = appendNote(note, "建议追加10-15分钟中低强度有氧");
                    }
                }
                if (lowBmi) {
                    if (isStrengthExercise(exercise)) {
                        sets = scaleMetric(sets, 1.15D, 1, 60);
                        reps = scaleMetric(reps, 1.15D, 1, 350);
                    } else if (isAerobicExercise(exercise)) {
                        duration = scaleMetric(duration, 0.90D, 5, 180);
                    }
                }
            }

            UserPlanItem planItem = new UserPlanItem();
            planItem.setWeekNo(item.getWeekNo());
            planItem.setDayNo(item.getDayNo());
            planItem.setExerciseId(selectedExerciseId);
            planItem.setSetsCount(sets <= 0 ? item.getSetsCount() : sets);
            planItem.setRepsCount(reps <= 0 ? item.getRepsCount() : reps);
            planItem.setDurationMinutes(duration <= 0 ? item.getDurationMinutes() : duration);
            planItem.setIntensityNote(note);
            result.add(planItem);
        }
        return result;
    }

    private Exercise pickPersonalizedExercise(PlanTemplateItem item,
                                              Exercise current,
                                              List<Exercise> alternatives,
                                              List<Exercise> activeExercises,
                                              String testItemCode,
                                              String equipmentType,
                                              int targetDifficultyRank,
                                              int daysPerWeek) {
        Map<Long, Exercise> candidates = new java.util.LinkedHashMap<>();
        if (current != null && current.getId() != null) {
            candidates.put(current.getId(), current);
        }
        for (Exercise alternative : alternatives) {
            if (alternative != null && alternative.getId() != null) {
                candidates.putIfAbsent(alternative.getId(), alternative);
            }
        }
        for (Exercise exercise : activeExercises) {
            if (exercise != null && exercise.getId() != null) {
                candidates.putIfAbsent(exercise.getId(), exercise);
            }
        }
        if (candidates.isEmpty()) {
            return current;
        }

        int dayNo = safeDay(item.getDayNo());
        Exercise best = current;
        int bestScore = current == null ? Integer.MIN_VALUE
                : computeExerciseMatchScore(current, testItemCode, equipmentType, targetDifficultyRank, dayNo, daysPerWeek) + 6;
        for (Exercise candidate : candidates.values()) {
            int score = computeExerciseMatchScore(candidate, testItemCode, equipmentType, targetDifficultyRank, dayNo, daysPerWeek);
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private int computeExerciseMatchScore(Exercise exercise,
                                          String testItemCode,
                                          String equipmentType,
                                          int targetDifficultyRank,
                                          int dayNo,
                                          int daysPerWeek) {
        if (exercise == null) {
            return Integer.MIN_VALUE;
        }
        String source = normalize(StringUtils.defaultString(exercise.getCategory()) + " "
                + StringUtils.defaultString(exercise.getName()) + " "
                + StringUtils.defaultString(exercise.getDescription()) + " "
                + StringUtils.defaultString(exercise.getEquipmentRequired()));
        int score = 0;
        score += 18 - Math.min(18, Math.abs(difficultyRank(exercise.getDifficulty()) - targetDifficultyRank) * 8);
        score += countKeywordMatches(source, goalKeywords(testItemCode)) * 12;

        if (isRunningGoal(testItemCode) && (source.contains("跑") || source.contains("有氧") || source.contains("跳绳"))) {
            score += 10;
        }
        if (isStrengthGoal(testItemCode) && (source.contains("hiit") || source.contains("腰腹") || source.contains("核心")
                || source.contains("力量") || source.contains("上肢") || source.contains("下肢"))) {
            score += 10;
        }
        if (isFlexibilityGoal(testItemCode) && (source.contains("瑜伽") || source.contains("八段锦")
                || source.contains("恢复") || source.contains("拉伸"))) {
            score += 14;
        }

        if (daysPerWeek > 0 && dayNo == daysPerWeek) {
            if (source.contains("恢复") || source.contains("瑜伽") || source.contains("八段锦")
                    || source.contains("拉伸") || source.contains("慢跑")) {
                score += 12;
            }
            if (source.contains("hiit") || source.contains("暴汗")) {
                score -= 4;
            }
        } else if (source.contains("恢复") || source.contains("拉伸")) {
            score -= 2;
        }

        String normalizedEquipmentType = normalizeEquipmentType(equipmentType);
        if ("track".equals(normalizedEquipmentType)) {
            if (source.contains("跑") || source.contains("跳绳") || source.contains("有氧")) {
                score += 8;
            }
            if (source.contains("瑜伽") || source.contains("八段锦")) {
                score -= 3;
            }
        }
        if ("gym".equals(normalizedEquipmentType)
                && (source.contains("力量") || source.contains("hiit") || source.contains("上肢") || source.contains("下肢"))) {
            score += 4;
        }
        if ("bodyweight".equals(normalizedEquipmentType)
                && (source.contains("器械") || source.contains("弹力带") || source.contains("跑道"))) {
            score -= 6;
        }

        int dayVariant = Math.abs((exercise.getId().intValue() % 5) - ((Math.max(dayNo, 1) - 1) % 5));
        score -= dayVariant;
        return score;
    }

    private String buildPersonalizationNote(String testItemCode, String equipmentType, int dayNo, int daysPerWeek) {
        String targetText = "专项提升";
        if (isRunningGoal(testItemCode)) {
            targetText = "耐力与节奏";
        } else if (isStrengthGoal(testItemCode)) {
            targetText = "力量与核心";
        } else if (isFlexibilityGoal(testItemCode)) {
            targetText = "柔韧与恢复";
        }
        String equipmentText = "bodyweight".equals(equipmentType) ? "徒手/宿舍可完成" :
                ("track".equals(equipmentType) ? "跑道环境优先" : ("gym".equals(equipmentType) ? "有器械条件可加练" : "按当前条件执行"));
        String dayText = daysPerWeek > 0 && dayNo == daysPerWeek ? "本日建议以恢复衔接为主" : "本日建议以专项刺激为主";
        return "个性化方向：" + targetText + "；" + equipmentText + "；" + dayText;
    }

    private List<String> goalKeywords(String testItemCode) {
        if (StringUtils.isBlank(testItemCode)) {
            return List.of();
        }
        switch (testItemCode) {
            case "pull_up":
                return List.of("上肢", "背", "引体", "俯卧撑", "核心", "腰腹", "hiit");
            case "sit_up":
                return List.of("核心", "腹", "腰腹", "hiit", "瑜伽");
            case "long_jump":
                return List.of("下肢", "跳", "爆发", "跳绳", "跑", "hiit");
            case "run_1000":
            case "run_800":
            case "run_50":
                return List.of("跑", "有氧", "跳绳", "耐力", "hiit");
            case "vital_capacity":
                return List.of("有氧", "跑", "跳绳", "呼吸", "恢复", "瑜伽", "八段锦");
            case "sit_reach":
                return List.of("瑜伽", "八段锦", "拉伸", "恢复", "柔韧");
            default:
                return List.of();
        }
    }

    private int countKeywordMatches(String source, List<String> keywords) {
        if (StringUtils.isBlank(source) || keywords == null || keywords.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String keyword : keywords) {
            if (StringUtils.isNotBlank(keyword) && source.contains(normalize(keyword))) {
                count++;
            }
        }
        return count;
    }

    private boolean isRunningGoal(String testItemCode) {
        return "run_1000".equals(testItemCode) || "run_800".equals(testItemCode) || "run_50".equals(testItemCode);
    }

    private boolean isStrengthGoal(String testItemCode) {
        return "pull_up".equals(testItemCode) || "sit_up".equals(testItemCode) || "long_jump".equals(testItemCode);
    }

    private boolean isFlexibilityGoal(String testItemCode) {
        return "sit_reach".equals(testItemCode);
    }

    private int targetDifficultyRank(String fitnessLevel, String scoreLevel) {
        int fitnessRank = difficultyRank(fitnessLevel);
        int scoreRank = difficultyRank(scoreLevel);
        return Math.max(1, Math.min(3, Math.max(fitnessRank, scoreRank)));
    }

    private Map<Long, List<Exercise>> loadAlternativeExerciseMap(Set<Long> exerciseIds, Map<Long, Exercise> exerciseMap) {
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return Map.of();
        }
        List<ExerciseAlternative> relations = exerciseAlternativeMapper.selectList(new QueryWrapper<ExerciseAlternative>()
                .in("exercise_id", exerciseIds));
        if (relations.isEmpty()) {
            return Map.of();
        }

        Set<Long> alternativeIds = relations.stream().map(ExerciseAlternative::getAlternativeExerciseId).collect(Collectors.toSet());
        if (!alternativeIds.isEmpty()) {
            List<Exercise> alternatives = exerciseMapper.selectBatchIds(alternativeIds);
            for (Exercise alternative : alternatives) {
                exerciseMap.putIfAbsent(alternative.getId(), alternative);
            }
        }

        Map<Long, List<Exercise>> grouped = new HashMap<>();
        for (ExerciseAlternative relation : relations) {
            Exercise alternative = exerciseMap.get(relation.getAlternativeExerciseId());
            if (alternative == null) {
                continue;
            }
            grouped.computeIfAbsent(relation.getExerciseId(), k -> new ArrayList<>()).add(alternative);
        }
        return grouped;
    }

    private Long chooseExerciseForStage(PlanTemplateItem item, Map<Long, List<Exercise>> alternativeMap,
                                        Map<Long, Exercise> exerciseMap, String fitnessLevel) {
        Long exerciseId = item.getExerciseId();
        Exercise current = exerciseMap.get(exerciseId);
        List<Exercise> alternatives = alternativeMap.getOrDefault(exerciseId, List.of());
        if (current == null || alternatives.isEmpty()) {
            return exerciseId;
        }

        int currentRank = difficultyRank(current.getDifficulty());
        String normalizedLevel = normalizeFitnessLevel(fitnessLevel);
        boolean preferEasier = safeWeek(item.getWeekNo()) <= 1 || "beginner".equals(normalizedLevel);
        boolean preferHarder = safeWeek(item.getWeekNo()) >= 4 || "advanced".equals(normalizedLevel);

        if (preferEasier) {
            Exercise easier = alternatives.stream()
                    .filter(alt -> difficultyRank(alt.getDifficulty()) < currentRank)
                    .max((a, b) -> Integer.compare(difficultyRank(a.getDifficulty()), difficultyRank(b.getDifficulty())))
                    .orElse(null);
            if (easier != null) {
                return easier.getId();
            }
        }

        if (preferHarder) {
            Exercise harder = alternatives.stream()
                    .filter(alt -> difficultyRank(alt.getDifficulty()) > currentRank)
                    .min((a, b) -> Integer.compare(difficultyRank(a.getDifficulty()), difficultyRank(b.getDifficulty())))
                    .orElse(null);
            if (harder != null) {
                return harder.getId();
            }
        }
        return exerciseId;
    }

    private int safeWeek(Integer weekNo) {
        return weekNo == null || weekNo <= 0 ? 1 : weekNo;
    }

    private int safeDay(Integer dayNo) {
        return dayNo == null || dayNo <= 0 ? 1 : dayNo;
    }

    private int difficultyRank(String difficulty) {
        String value = StringUtils.lowerCase(StringUtils.trimToEmpty(difficulty));
        if (value.contains("newbie") || value.contains("beginner") || value.contains("初级") || value.contains("零基础")) {
            return 1;
        }
        if (value.contains("basic") || value.contains("intermediate") || value.contains("中级") || value.contains("进阶")) {
            return 2;
        }
        if (value.contains("advanced") || value.contains("高阶") || value.contains("强化")) {
            return 3;
        }
        return 2;
    }

    private boolean isAerobicExercise(Exercise exercise) {
        String source = StringUtils.lowerCase(StringUtils.defaultString(exercise.getCategory()) + " " + StringUtils.defaultString(exercise.getName()));
        return source.contains("有氧") || source.contains("run") || source.contains("跑")
                || source.contains("cardio") || source.contains("耐力") || source.contains("跳绳")
                || source.contains("hiit");
    }

    private boolean isStrengthExercise(Exercise exercise) {
        String source = StringUtils.lowerCase(StringUtils.defaultString(exercise.getCategory()) + " " + StringUtils.defaultString(exercise.getName()));
        return source.contains("力量") || source.contains("上肢") || source.contains("下肢")
                || source.contains("核心") || source.contains("增肌") || source.contains("strength")
                || source.contains("引体") || source.contains("深蹲") || source.contains("俯卧撑");
    }

    private int scaleMetric(Integer value, double factor, int minWhenPositive, int maxCap) {
        if (value == null || value <= 0) {
            return 0;
        }
        int scaled = (int) Math.round(value * factor);
        if (scaled < minWhenPositive) {
            scaled = minWhenPositive;
        }
        return Math.min(scaled, maxCap);
    }

    private String appendNote(String note, String extra) {
        if (StringUtils.isBlank(extra)) {
            return StringUtils.trimToEmpty(note);
        }
        if (StringUtils.isBlank(note)) {
            return extra;
        }
        if (note.contains(extra)) {
            return note;
        }
        return note + "；" + extra;
    }

    private String calculateScoreLevel(Long userId, PlanGenerateRequest request) {
        TestStandard matchedStandard = matchStandardByProfile(userId, request.getTestItemCode(), request.getCurrentScore());
        if (matchedStandard != null && matchedStandard.getStandardPoint() != null) {
            return mapStandardPointToLevel(matchedStandard.getStandardPoint());
        }

        TestItem testItem = testItemMapper.selectOne(new QueryWrapper<TestItem>()
                .eq("item_code", request.getTestItemCode())
                .last("limit 1"));
        String scoreDirection = testItem == null ? "higher" : testItem.getScoreDirection();
        return fallbackLevelByDirection(request.getCurrentScore(), scoreDirection);
    }

    private String mapStandardPointToLevel(Integer standardPoint) {
        if (standardPoint < 60) {
            return "beginner";
        }
        if (standardPoint < 80) {
            return "intermediate";
        }
        return "advanced";
    }

    private String fallbackLevelByDirection(BigDecimal score, String direction) {
        if (score == null) {
            return "beginner";
        }
        if ("lower".equalsIgnoreCase(direction)) {
            if (score.compareTo(BigDecimal.valueOf(220)) <= 0) {
                return "advanced";
            }
            if (score.compareTo(BigDecimal.valueOf(280)) <= 0) {
                return "intermediate";
            }
            return "beginner";
        }
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "advanced";
        }
        if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "intermediate";
        }
        return "beginner";
    }

    private TestStandard matchStandardByProfile(Long userId, String itemCode, BigDecimal scoreValue) {
        if (StringUtils.isBlank(itemCode) || scoreValue == null) {
            return null;
        }
        String gender = "male";
        HealthProfile profile = healthProfileMapper.selectOne(new QueryWrapper<HealthProfile>()
                .eq("user_id", userId)
                .last("limit 1"));
        if (profile != null && StringUtils.isNotBlank(profile.getGender())) {
            gender = profile.getGender();
        }
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

    private String resolveBmiRange(Long userId, BigDecimal requestBmi) {
        BigDecimal bmi = requestBmi;
        if (bmi == null) {
            HealthProfile profile = healthProfileMapper.selectOne(new QueryWrapper<HealthProfile>()
                    .eq("user_id", userId)
                    .last("limit 1"));
            if (profile != null) {
                bmi = profile.getBmiValue();
            }
        }
        if (bmi == null) {
            return "all";
        }
        if (bmi.compareTo(BigDecimal.valueOf(18.5)) < 0) {
            return "underweight";
        }
        if (bmi.compareTo(BigDecimal.valueOf(24)) < 0) {
            return "normal";
        }
        if (bmi.compareTo(BigDecimal.valueOf(28)) < 0) {
            return "overweight";
        }
        return "obese";
    }

    private String normalize(String value) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(value));
    }

    private String normalizeFitnessLevel(String value) {
        String normalized = normalize(value);
        if (StringUtils.isBlank(normalized)) {
            return normalized;
        }
        if (normalized.contains("newbie") || normalized.contains("beginner") || normalized.contains("初级")) {
            return "beginner";
        }
        if (normalized.contains("basic") || normalized.contains("intermediate") || normalized.contains("中级") || normalized.contains("进阶")) {
            return "intermediate";
        }
        if (normalized.contains("advanced") || normalized.contains("高级") || normalized.contains("强化")) {
            return "advanced";
        }
        return normalized;
    }

    private String normalizeEquipmentType(String value) {
        String normalized = normalize(value);
        if (StringUtils.isBlank(normalized)) {
            return normalized;
        }
        if (normalized.contains("bodyweight") || normalized.contains("无器械") || normalized.contains("自重")) {
            return "bodyweight";
        }
        if (normalized.contains("track") || normalized.contains("跑道")) {
            return "track";
        }
        if (normalized.contains("gym") || normalized.contains("健身房") || normalized.contains("器械")) {
            return "gym";
        }
        return normalized;
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

    private PlanVO assemblePlanVO(UserPlan plan) {
        PlanVO vo = new PlanVO();
        vo.setPlanId(plan.getId());
        vo.setTestItemCode(plan.getTestItemCode());
        vo.setScoreLevel(plan.getScoreLevel());
        vo.setFitnessLevel(plan.getFitnessLevel());
        vo.setEquipmentType(plan.getEquipmentType());
        vo.setDaysPerWeek(plan.getDaysPerWeek());
        vo.setStatus(plan.getStatus());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setSnapshot(parseSnapshot(plan.getSnapshotJson()));

        QueryWrapper<UserPlanItem> itemQw = new QueryWrapper<>();
        itemQw.eq("user_plan_id", plan.getId()).orderByAsc("week_no", "day_no", "id");
        List<UserPlanItem> items = userPlanItemMapper.selectList(itemQw);

        Set<Long> exerciseIds = items.stream().map(UserPlanItem::getExerciseId).collect(Collectors.toSet());
        Map<Long, Exercise> exerciseMap = exerciseIds.isEmpty() ? Map.of() : exerciseMapper.selectBatchIds(exerciseIds)
                .stream().collect(Collectors.toMap(Exercise::getId, Function.identity()));

        List<PlanItemVO> itemVos = new ArrayList<>();
        for (UserPlanItem item : items) {
            PlanItemVO itemVO = new PlanItemVO();
            BeanUtils.copyProperties(item, itemVO);
            Exercise exercise = exerciseMap.get(item.getExerciseId());
            itemVO.setExerciseName(exercise == null ? "" : exercise.getName());
            itemVos.add(itemVO);
        }
        vo.setItems(itemVos);
        return vo;
    }

    private Map<String, Object> parseSnapshot(String snapshotJson) {
        if (StringUtils.isBlank(snapshotJson)) {
            return Map.of();
        }
        try {
            return JSONUtil.toBean(snapshotJson, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
