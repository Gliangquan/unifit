package com.jcen.unifit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.DeleteRequest;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.model.dto.ExerciseAlternativeUpsertRequest;
import com.jcen.unifit.model.dto.ExerciseCommentAddRequest;
import com.jcen.unifit.model.dto.ExerciseLikeToggleRequest;
import com.jcen.unifit.model.dto.ExerciseUpsertRequest;
import com.jcen.unifit.model.entity.Exercise;
import com.jcen.unifit.model.entity.ExerciseAlternative;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ExerciseCommentVO;
import com.jcen.unifit.model.vo.ExerciseLikeVO;
import com.jcen.unifit.model.vo.ExerciseLikeRecordVO;
import com.jcen.unifit.service.ExerciseService;
import com.jcen.unifit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/exercise")
public class ExerciseController {

    @Resource
    private ExerciseService exerciseService;

    @Resource
    private UserService userService;

    @GetMapping("/list")
    public BaseResponse<Page<Exercise>> list(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long pageSize,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) String difficulty) {
        return ResultUtils.success(exerciseService.listExercises(current, pageSize, keyword, category, difficulty));
    }

    @GetMapping("/get")
    public BaseResponse<Exercise> getById(@RequestParam Long id) {
        return ResultUtils.success(exerciseService.getById(id));
    }

    @GetMapping("/alternatives")
    public BaseResponse<List<Exercise>> alternatives(@RequestParam Long id) {
        return ResultUtils.success(exerciseService.listAlternatives(id));
    }

    @GetMapping("/alternative/relations")
    public BaseResponse<List<ExerciseAlternative>> alternativeRelations(@RequestParam Long exerciseId) {
        return ResultUtils.success(exerciseService.listAlternativeRelations(exerciseId));
    }

    @PostMapping("/alternative/upsert")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ExerciseAlternative> upsertAlternative(@RequestBody ExerciseAlternativeUpsertRequest request) {
        return ResultUtils.success(exerciseService.upsertAlternative(
                request == null ? null : request.getExerciseId(),
                request == null ? null : request.getAlternativeExerciseId()));
    }

    @PostMapping("/alternative/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAlternative(@RequestBody DeleteRequest request) {
        return ResultUtils.success(exerciseService.deleteAlternative(request == null ? null : request.getId()));
    }

    @PostMapping("/upsert")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Exercise> upsert(@RequestBody ExerciseUpsertRequest request, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(exerciseService.createOrUpdate(loginUser, request));
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest request) {
        return ResultUtils.success(exerciseService.deleteById(request.getId()));
    }

    @GetMapping("/comment/list")
    public BaseResponse<List<ExerciseCommentVO>> listComments(@RequestParam Long exerciseId) {
        return ResultUtils.success(exerciseService.listComments(exerciseId));
    }

    @GetMapping("/comment/my")
    public BaseResponse<Page<ExerciseCommentVO>> myComments(@RequestParam(defaultValue = "1") long current,
                                                            @RequestParam(defaultValue = "20") long pageSize,
                                                            HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(exerciseService.listMyComments(loginUser, current, pageSize));
    }

    @PostMapping("/comment/add")
    public BaseResponse<ExerciseCommentVO> addComment(@RequestBody ExerciseCommentAddRequest request,
                                                      HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(exerciseService.addComment(loginUser, request));
    }

    @PostMapping("/comment/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest request,
                                               HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(exerciseService.deleteComment(loginUser, request == null ? null : request.getId()));
    }

    @PostMapping("/like/toggle")
    public BaseResponse<ExerciseLikeVO> toggleLike(@RequestBody ExerciseLikeToggleRequest request,
                                                   HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(exerciseService.toggleLike(loginUser, request));
    }

    @GetMapping("/like/status")
    public BaseResponse<ExerciseLikeVO> likeStatus(@RequestParam Long exerciseId,
                                                   HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(exerciseService.getLikeStatus(loginUser, exerciseId));
    }

    @GetMapping("/like/my")
    public BaseResponse<Page<ExerciseLikeRecordVO>> myLikes(@RequestParam(defaultValue = "1") long current,
                                                            @RequestParam(defaultValue = "20") long pageSize,
                                                            HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(exerciseService.listMyLikes(loginUser, current, pageSize));
    }
}
