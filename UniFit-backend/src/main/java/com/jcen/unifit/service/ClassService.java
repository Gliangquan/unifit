package com.jcen.unifit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.unifit.model.entity.Class;
import com.jcen.unifit.model.vo.ClassVO;

import java.util.List;

public interface ClassService extends IService<Class> {

    /**
     * 获取班级列表
     */
    List<ClassVO> getClassList();

    /**
     * 获取班级详情
     */
    ClassVO getClassDetail(Long classId);

    /**
     * 创建班级
     */
    Long createClass(ClassVO classVO);

    /**
     * 更新班级
     */
    boolean updateClass(ClassVO classVO);

    /**
     * 删除班级
     */
    boolean deleteClass(Long classId);

    /**
     * 添加班级成员
     */
    boolean addClassMember(Long classId, Long userId);

    /**
     * 移除班级成员
     */
    boolean removeClassMember(Long classId, Long userId);

    /**
     * 添加班级教师
     */
    boolean addClassTeacher(Long classId, Long teacherId, String role);

    /**
     * 移除班级教师
     */
    boolean removeClassTeacher(Long classId, Long teacherId);

    /**
     * 获取班级成员列表
     */
    List<ClassVO> getClassMembers(Long classId);

    /**
     * 获取班级教师列表
     */
    List<ClassVO> getClassTeachers(Long classId);
}
