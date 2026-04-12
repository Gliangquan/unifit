package com.jcen.unifit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.model.dto.ExerciseCommentAddRequest;
import com.jcen.unifit.model.dto.ExerciseLikeToggleRequest;
import com.jcen.unifit.model.dto.ExerciseUpsertRequest;
import com.jcen.unifit.model.entity.ExerciseAlternative;
import com.jcen.unifit.model.entity.Exercise;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ExerciseCommentVO;
import com.jcen.unifit.model.vo.ExerciseLikeVO;
import com.jcen.unifit.model.vo.ExerciseLikeRecordVO;

import java.util.List;

public interface ExerciseService {

    Exercise createOrUpdate(User loginUser, ExerciseUpsertRequest request);

    boolean deleteById(Long id);

    Page<Exercise> listExercises(long current, long pageSize, String keyword, String category, String difficulty);

    Exercise getById(Long id);

    List<Exercise> listAlternatives(Long exerciseId);

    List<ExerciseAlternative> listAlternativeRelations(Long exerciseId);

    ExerciseAlternative upsertAlternative(Long exerciseId, Long alternativeExerciseId);

    boolean deleteAlternative(Long id);

    List<ExerciseCommentVO> listComments(Long exerciseId);

    ExerciseCommentVO addComment(User loginUser, ExerciseCommentAddRequest request);

    boolean deleteComment(User loginUser, Long commentId);

    ExerciseLikeVO toggleLike(User loginUser, ExerciseLikeToggleRequest request);

    ExerciseLikeVO getLikeStatus(User loginUser, Long exerciseId);

    Page<ExerciseLikeRecordVO> listMyLikes(User loginUser, long current, long pageSize);

    Page<ExerciseCommentVO> listMyComments(User loginUser, long current, long pageSize);
}
