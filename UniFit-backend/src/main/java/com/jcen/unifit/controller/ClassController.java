package com.jcen.unifit.controller;

import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.model.vo.ClassVO;
import com.jcen.unifit.service.ClassService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/class")
public class ClassController {

    @Resource
    private ClassService classService;

    /**
     * 获取班级列表
     */
    @GetMapping("/list")
    @AuthCheck(mustRoles = {UserConstant.ADMIN_ROLE, UserConstant.TEACHER_ROLE})
    public BaseResponse<List<ClassVO>> getClassList() {
        List<ClassVO> classes = classService.getClassList();
        return ResultUtils.success(classes);
    }

    /**
     * 获取班级详情
     */
    @GetMapping("/{classId}")
    @AuthCheck(mustRoles = {UserConstant.ADMIN_ROLE, UserConstant.TEACHER_ROLE})
    public BaseResponse<ClassVO> getClassDetail(@PathVariable Long classId) {
        ClassVO classVO = classService.getClassDetail(classId);
        return ResultUtils.success(classVO);
    }

    /**
     * 创建班级
     */
    @PostMapping("/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> createClass(@RequestBody ClassVO classVO) {
        Long classId = classService.createClass(classVO);
        return ResultUtils.success(classId);
    }

    /**
     * 更新班级
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateClass(@RequestBody ClassVO classVO) {
        boolean result = classService.updateClass(classVO);
        return ResultUtils.success(result);
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{classId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteClass(@PathVariable Long classId) {
        boolean result = classService.deleteClass(classId);
        return ResultUtils.success(result);
    }

    /**
     * 添加班级成员
     */
    @PostMapping("/{classId}/member/{userId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addClassMember(@PathVariable Long classId, @PathVariable Long userId) {
        boolean result = classService.addClassMember(classId, userId);
        return ResultUtils.success(result);
    }

    /**
     * 移除班级成员
     */
    @DeleteMapping("/{classId}/member/{userId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> removeClassMember(@PathVariable Long classId, @PathVariable Long userId) {
        boolean result = classService.removeClassMember(classId, userId);
        return ResultUtils.success(result);
    }

    /**
     * 添加班级教师
     */
    @PostMapping("/{classId}/teacher/{teacherId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addClassTeacher(
            @PathVariable Long classId,
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "teacher") String role) {
        boolean result = classService.addClassTeacher(classId, teacherId, role);
        return ResultUtils.success(result);
    }

    /**
     * 移除班级教师
     */
    @DeleteMapping("/{classId}/teacher/{teacherId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> removeClassTeacher(@PathVariable Long classId, @PathVariable Long teacherId) {
        boolean result = classService.removeClassTeacher(classId, teacherId);
        return ResultUtils.success(result);
    }

    /**
     * 获取班级成员列表
     */
    @GetMapping("/{classId}/members")
    @AuthCheck(mustRoles = {UserConstant.ADMIN_ROLE, UserConstant.TEACHER_ROLE})
    public BaseResponse<List<ClassVO>> getClassMembers(@PathVariable Long classId) {
        List<ClassVO> members = classService.getClassMembers(classId);
        return ResultUtils.success(members);
    }

    /**
     * 获取班级教师列表
     */
    @GetMapping("/{classId}/teachers")
    @AuthCheck(mustRoles = {UserConstant.ADMIN_ROLE, UserConstant.TEACHER_ROLE})
    public BaseResponse<List<ClassVO>> getClassTeachers(@PathVariable Long classId) {
        List<ClassVO> teachers = classService.getClassTeachers(classId);
        return ResultUtils.success(teachers);
    }
}
