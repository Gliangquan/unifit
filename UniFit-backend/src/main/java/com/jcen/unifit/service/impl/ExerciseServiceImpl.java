package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.ExerciseAlternativeMapper;
import com.jcen.unifit.mapper.ExerciseCommentMapper;
import com.jcen.unifit.mapper.ExerciseLikeMapper;
import com.jcen.unifit.mapper.ExerciseMapper;
import com.jcen.unifit.mapper.UserMapper;
import com.jcen.unifit.model.dto.ExerciseCommentAddRequest;
import com.jcen.unifit.model.dto.ExerciseLikeToggleRequest;
import com.jcen.unifit.model.dto.ExerciseUpsertRequest;
import com.jcen.unifit.model.entity.Exercise;
import com.jcen.unifit.model.entity.ExerciseAlternative;
import com.jcen.unifit.model.entity.ExerciseComment;
import com.jcen.unifit.model.entity.ExerciseLike;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ExerciseCommentVO;
import com.jcen.unifit.model.vo.ExerciseLikeVO;
import com.jcen.unifit.model.vo.ExerciseLikeRecordVO;
import com.jcen.unifit.service.ExerciseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    @Resource
    private ExerciseMapper exerciseMapper;

    @Resource
    private ExerciseAlternativeMapper exerciseAlternativeMapper;

    @Resource
    private ExerciseCommentMapper exerciseCommentMapper;

    @Resource
    private ExerciseLikeMapper exerciseLikeMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Exercise createOrUpdate(User loginUser, ExerciseUpsertRequest request) {
        if (request == null || StringUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能为空");
        }

        String title = request.getName().trim();
        if (title.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题长度不能超过128");
        }

        Exercise exercise;
        if (request.getId() == null) {
            exercise = new Exercise();
            exercise.setCreateTime(new Date());
            exercise.setLikeCount(0);
            exercise.setCommentCount(0);
            exercise.setPublishUserId(loginUser.getId());
            exercise.setPublishTime(new Date());
        } else {
            exercise = exerciseMapper.selectById(request.getId());
            if (exercise == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动作不存在");
            }
        }

        BeanUtils.copyProperties(request, exercise);
        exercise.setName(title);
        if (exercise.getPublishUserId() == null) {
            exercise.setPublishUserId(loginUser.getId());
        }
        if (exercise.getPublishTime() == null) {
            exercise.setPublishTime(new Date());
        }
        if (exercise.getLikeCount() == null) {
            exercise.setLikeCount(0);
        }
        if (exercise.getCommentCount() == null) {
            exercise.setCommentCount(0);
        }
        exercise.setUpdateTime(new Date());
        if (exercise.getStatus() == null) {
            exercise.setStatus(1);
        }

        if (exercise.getId() == null) {
            exerciseMapper.insert(exercise);
        } else {
            exerciseMapper.updateById(exercise);
        }
        fillPublishUserName(Collections.singletonList(exercise));
        return exercise;
    }

    @Override
    public boolean deleteById(Long id) {
        return exerciseMapper.deleteById(id) > 0;
    }

    @Override
    public Page<Exercise> listExercises(long current, long pageSize, String keyword, String category, String difficulty) {
        QueryWrapper<Exercise> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            qw.and(w -> w.like("name", keyword).or().like("description", keyword));
        }
        if (StringUtils.isNotBlank(category)) {
            qw.eq("category", category);
        }
        if (StringUtils.isNotBlank(difficulty)) {
            qw.eq("difficulty", difficulty);
        }
        qw.eq("status", 1).orderByAsc("id");
        Page<Exercise> page = exerciseMapper.selectPage(new Page<>(current, pageSize), qw);
        fillPublishUserName(page.getRecords());
        return page;
    }

    @Override
    public Exercise getById(Long id) {
        Exercise exercise = exerciseMapper.selectById(id);
        if (exercise != null) {
            fillPublishUserName(Collections.singletonList(exercise));
        }
        return exercise;
    }

    @Override
    public List<Exercise> listAlternatives(Long exerciseId) {
        QueryWrapper<ExerciseAlternative> qw = new QueryWrapper<>();
        qw.eq("exercise_id", exerciseId);
        List<ExerciseAlternative> relations = exerciseAlternativeMapper.selectList(qw);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = relations.stream().map(ExerciseAlternative::getAlternativeExerciseId).collect(Collectors.toList());
        return exerciseMapper.selectBatchIds(ids);
    }

    @Override
    public List<ExerciseAlternative> listAlternativeRelations(Long exerciseId) {
        if (exerciseId == null || exerciseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "动作ID不合法");
        }
        return exerciseAlternativeMapper.selectList(new QueryWrapper<ExerciseAlternative>()
                .eq("exercise_id", exerciseId)
                .orderByDesc("id"));
    }

    @Override
    public ExerciseAlternative upsertAlternative(Long exerciseId, Long alternativeExerciseId) {
        if (exerciseId == null || exerciseId <= 0 || alternativeExerciseId == null || alternativeExerciseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "替代动作参数不完整");
        }
        if (exerciseId.equals(alternativeExerciseId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "动作不能替代自己");
        }
        if (exerciseMapper.selectById(exerciseId) == null || exerciseMapper.selectById(alternativeExerciseId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动作不存在");
        }

        QueryWrapper<ExerciseAlternative> qw = new QueryWrapper<>();
        qw.eq("exercise_id", exerciseId).eq("alternative_exercise_id", alternativeExerciseId).last("limit 1");
        ExerciseAlternative exists = exerciseAlternativeMapper.selectOne(qw);
        if (exists != null) {
            return exists;
        }

        ExerciseAlternative row = new ExerciseAlternative();
        row.setExerciseId(exerciseId);
        row.setAlternativeExerciseId(alternativeExerciseId);
        row.setCreateTime(new Date());
        exerciseAlternativeMapper.insert(row);
        return row;
    }

    @Override
    public boolean deleteAlternative(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系ID不合法");
        }
        return exerciseAlternativeMapper.deleteById(id) > 0;
    }

    @Override
    public List<ExerciseCommentVO> listComments(Long exerciseId) {
        if (exerciseId == null || exerciseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "动作ID不合法");
        }
        QueryWrapper<ExerciseComment> qw = new QueryWrapper<>();
        qw.eq("exercise_id", exerciseId).orderByDesc("id");
        List<ExerciseComment> comments = exerciseCommentMapper.selectList(qw);
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Exercise> exerciseMap = new HashMap<>();
        Exercise exercise = exerciseMapper.selectById(exerciseId);
        if (exercise != null) {
            exerciseMap.put(exerciseId, exercise);
        }
        Set<Long> userIds = comments.stream().map(ExerciseComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<ExerciseCommentVO> result = new ArrayList<>();
        for (ExerciseComment comment : comments) {
            User user = userMap.get(comment.getUserId());
            ExerciseCommentVO vo = new ExerciseCommentVO();
            vo.setId(comment.getId());
            vo.setExerciseId(comment.getExerciseId());
            fillExerciseInfo(vo, exerciseMap.get(comment.getExerciseId()));
            vo.setUserId(comment.getUserId());
            vo.setUserName(user == null ? "用户" : user.getUserName());
            vo.setUserAvatar(user == null ? null : user.getUserAvatar());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount() == null ? 0 : comment.getLikeCount());
            vo.setCreateTime(comment.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    public ExerciseCommentVO addComment(User loginUser, ExerciseCommentAddRequest request) {
        if (request == null || request.getExerciseId() == null || StringUtils.isBlank(request.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论参数不完整");
        }
        Exercise exercise = exerciseMapper.selectById(request.getExerciseId());
        if (exercise == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动作不存在");
        }
        String content = request.getContent().trim();
        if (content.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论不能超过500字");
        }

        ExerciseComment row = new ExerciseComment();
        row.setExerciseId(request.getExerciseId());
        row.setUserId(loginUser.getId());
        row.setContent(content);
        row.setLikeCount(0);
        row.setCreateTime(new Date());
        row.setUpdateTime(new Date());
        exerciseCommentMapper.insert(row);

        increaseCounter(request.getExerciseId(), "comment_count");

        ExerciseCommentVO vo = new ExerciseCommentVO();
        vo.setId(row.getId());
        vo.setExerciseId(row.getExerciseId());
        vo.setUserId(row.getUserId());
        vo.setUserName(loginUser.getUserName());
        vo.setUserAvatar(loginUser.getUserAvatar());
        vo.setContent(row.getContent());
        vo.setLikeCount(0);
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }

    @Override
    public boolean deleteComment(User loginUser, Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论ID不合法");
        }
        ExerciseComment row = exerciseCommentMapper.selectById(commentId);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        if (!isAdmin && !row.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅评论作者或管理员可删除");
        }
        boolean ok = exerciseCommentMapper.deleteById(commentId) > 0;
        if (ok) {
            decreaseCounter(row.getExerciseId(), "comment_count");
        }
        return ok;
    }

    @Override
    public ExerciseLikeVO toggleLike(User loginUser, ExerciseLikeToggleRequest request) {
        if (request == null || request.getExerciseId() == null || request.getExerciseId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点赞参数不完整");
        }
        Exercise exercise = exerciseMapper.selectById(request.getExerciseId());
        if (exercise == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动作不存在");
        }

        QueryWrapper<ExerciseLike> likeQw = new QueryWrapper<>();
        likeQw.eq("exercise_id", request.getExerciseId()).eq("user_id", loginUser.getId());
        ExerciseLike exists = exerciseLikeMapper.selectOne(likeQw);
        boolean liked;
        if (exists != null) {
            exerciseLikeMapper.deleteById(exists.getId());
            decreaseCounter(request.getExerciseId(), "like_count");
            liked = false;
        } else {
            ExerciseLike row = new ExerciseLike();
            row.setExerciseId(request.getExerciseId());
            row.setUserId(loginUser.getId());
            row.setCreateTime(new Date());
            exerciseLikeMapper.insert(row);
            increaseCounter(request.getExerciseId(), "like_count");
            liked = true;
        }
        Exercise latest = exerciseMapper.selectById(request.getExerciseId());
        int likeCount = latest == null || latest.getLikeCount() == null ? 0 : latest.getLikeCount();
        return new ExerciseLikeVO(liked, likeCount);
    }

    @Override
    public ExerciseLikeVO getLikeStatus(User loginUser, Long exerciseId) {
        if (exerciseId == null || exerciseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "动作ID不合法");
        }
        Exercise exercise = exerciseMapper.selectById(exerciseId);
        if (exercise == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动作不存在");
        }
        QueryWrapper<ExerciseLike> likeQw = new QueryWrapper<>();
        likeQw.eq("exercise_id", exerciseId).eq("user_id", loginUser.getId());
        boolean liked = exerciseLikeMapper.selectCount(likeQw) > 0;
        int likeCount = exercise.getLikeCount() == null ? 0 : exercise.getLikeCount();
        return new ExerciseLikeVO(liked, likeCount);
    }

    @Override
    public Page<ExerciseLikeRecordVO> listMyLikes(User loginUser, long current, long pageSize) {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.max(1, Math.min(pageSize, 100));
        Page<ExerciseLike> likePage = exerciseLikeMapper.selectPage(new Page<>(safeCurrent, safeSize),
                new QueryWrapper<ExerciseLike>().eq("user_id", loginUser.getId()).orderByDesc("id"));

        List<ExerciseLike> likeRecords = likePage.getRecords();
        Set<Long> exerciseIds = likeRecords.stream().map(ExerciseLike::getExerciseId).collect(Collectors.toSet());
        Map<Long, Exercise> exerciseMap = exerciseIds.isEmpty() ? Map.of() : exerciseMapper.selectBatchIds(exerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity(), (a, b) -> a));

        List<ExerciseLikeRecordVO> records = new ArrayList<>();
        for (ExerciseLike like : likeRecords) {
            Exercise exercise = exerciseMap.get(like.getExerciseId());
            if (exercise == null) {
                continue;
            }
            ExerciseLikeRecordVO vo = new ExerciseLikeRecordVO();
            vo.setExerciseId(exercise.getId());
            vo.setExerciseName(exercise.getName());
            vo.setCoverImageUrl(exercise.getCoverImageUrl());
            vo.setCategory(exercise.getCategory());
            vo.setDifficulty(exercise.getDifficulty());
            vo.setEquipmentRequired(exercise.getEquipmentRequired());
            vo.setDescription(exercise.getDescription());
            vo.setLikeCount(exercise.getLikeCount() == null ? 0 : exercise.getLikeCount());
            vo.setCommentCount(exercise.getCommentCount() == null ? 0 : exercise.getCommentCount());
            vo.setLikedAt(like.getCreateTime());
            records.add(vo);
        }

        Page<ExerciseLikeRecordVO> result = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public Page<ExerciseCommentVO> listMyComments(User loginUser, long current, long pageSize) {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.max(1, Math.min(pageSize, 100));
        Page<ExerciseComment> commentPage = exerciseCommentMapper.selectPage(new Page<>(safeCurrent, safeSize),
                new QueryWrapper<ExerciseComment>().eq("user_id", loginUser.getId()).orderByDesc("id"));

        List<ExerciseComment> comments = commentPage.getRecords();
        Set<Long> exerciseIds = comments.stream().map(ExerciseComment::getExerciseId).collect(Collectors.toSet());
        Map<Long, Exercise> exerciseMap = exerciseIds.isEmpty() ? Map.of() : exerciseMapper.selectBatchIds(exerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity(), (a, b) -> a));

        List<ExerciseCommentVO> records = new ArrayList<>();
        for (ExerciseComment comment : comments) {
            ExerciseCommentVO vo = new ExerciseCommentVO();
            vo.setId(comment.getId());
            vo.setExerciseId(comment.getExerciseId());
            fillExerciseInfo(vo, exerciseMap.get(comment.getExerciseId()));
            vo.setUserId(comment.getUserId());
            vo.setUserName(loginUser.getUserName());
            vo.setUserAvatar(loginUser.getUserAvatar());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount() == null ? 0 : comment.getLikeCount());
            vo.setCreateTime(comment.getCreateTime());
            records.add(vo);
        }

        Page<ExerciseCommentVO> result = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        result.setRecords(records);
        return result;
    }

    private void fillExerciseInfo(ExerciseCommentVO vo, Exercise exercise) {
        if (vo == null || exercise == null) {
            return;
        }
        vo.setExerciseName(exercise.getName());
        vo.setExerciseCoverImageUrl(exercise.getCoverImageUrl());
    }

    private void fillPublishUserName(List<Exercise> exercises) {
        if (exercises == null || exercises.isEmpty()) {
            return;
        }
        Set<Long> publishUserIds = exercises.stream()
                .map(Exercise::getPublishUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (publishUserIds.isEmpty()) {
            return;
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(publishUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        for (Exercise exercise : exercises) {
            if (exercise.getPublishUserId() == null) {
                continue;
            }
            User publisher = userMap.get(exercise.getPublishUserId());
            if (publisher != null) {
                exercise.setPublishUserName(publisher.getUserName());
            }
        }
    }

    private void increaseCounter(Long exerciseId, String field) {
        UpdateWrapper<Exercise> uw = new UpdateWrapper<>();
        uw.eq("id", exerciseId).setSql(field + " = IFNULL(" + field + ", 0) + 1");
        exerciseMapper.update(null, uw);
    }

    private void decreaseCounter(Long exerciseId, String field) {
        UpdateWrapper<Exercise> uw = new UpdateWrapper<>();
        uw.eq("id", exerciseId).setSql(field + " = GREATEST(IFNULL(" + field + ", 0) - 1, 0)");
        exerciseMapper.update(null, uw);
    }
}
