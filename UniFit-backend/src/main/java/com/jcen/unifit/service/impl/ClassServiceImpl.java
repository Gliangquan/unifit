package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.unifit.mapper.ClassMapper;
import com.jcen.unifit.mapper.ClassMemberMapper;
import com.jcen.unifit.mapper.ClassTeacherMapper;
import com.jcen.unifit.mapper.UserMapper;
import com.jcen.unifit.model.entity.Class;
import com.jcen.unifit.model.entity.ClassMember;
import com.jcen.unifit.model.entity.ClassTeacher;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ClassVO;
import com.jcen.unifit.model.vo.ClassMemberVO;
import com.jcen.unifit.model.vo.ClassTeacherVO;
import com.jcen.unifit.service.ClassService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {

    @Resource
    private ClassMapper classMapper;

    @Resource
    private ClassMemberMapper classMemberMapper;

    @Resource
    private ClassTeacherMapper classTeacherMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public List<ClassVO> getClassList() {
        List<Class> classes = classMapper.selectList(new LambdaQueryWrapper<Class>()
                .eq(Class::getIsDelete, 0)
                .orderByDesc(Class::getCreateTime));
        return classes.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ClassVO getClassDetail(Long classId) {
        Class clazz = classMapper.selectById(classId);
        if (clazz == null || clazz.getIsDelete() == 1) {
            return null;
        }
        return convertToVO(clazz);
    }

    @Override
    @Transactional
    public Long createClass(ClassVO classVO) {
        Class clazz = new Class();
        clazz.setClassName(classVO.getClassName());
        clazz.setClassCode(classVO.getClassCode());
        clazz.setGrade(classVO.getGrade());
        clazz.setMajor(classVO.getMajor());
        clazz.setDescription(classVO.getDescription());
        clazz.setTeacherId(classVO.getTeacherId());
        clazz.setStatus(1);
        clazz.setStudentCount(0);
        classMapper.insert(clazz);
        return clazz.getId();
    }

    @Override
    @Transactional
    public boolean updateClass(ClassVO classVO) {
        Class clazz = new Class();
        clazz.setId(classVO.getId());
        clazz.setClassName(classVO.getClassName());
        clazz.setClassCode(classVO.getClassCode());
        clazz.setGrade(classVO.getGrade());
        clazz.setMajor(classVO.getMajor());
        clazz.setDescription(classVO.getDescription());
        clazz.setTeacherId(classVO.getTeacherId());
        clazz.setStatus(classVO.getStatus());
        return classMapper.updateById(clazz) > 0;
    }

    @Override
    @Transactional
    public boolean deleteClass(Long classId) {
        Class clazz = new Class();
        clazz.setId(classId);
        clazz.setIsDelete(1);
        return classMapper.updateById(clazz) > 0;
    }

    @Override
    @Transactional
    public boolean addClassMember(Long classId, Long userId) {
        ClassMember member = new ClassMember();
        member.setClassId(classId);
        member.setUserId(userId);
        classMemberMapper.insert(member);

        // 更新班级学生数量
        Class clazz = classMapper.selectById(classId);
        clazz.setStudentCount(clazz.getStudentCount() + 1);
        classMapper.updateById(clazz);
        return true;
    }

    @Override
    @Transactional
    public boolean removeClassMember(Long classId, Long userId) {
        classMemberMapper.delete(new LambdaQueryWrapper<ClassMember>()
                .eq(ClassMember::getClassId, classId)
                .eq(ClassMember::getUserId, userId));

        // 更新班级学生数量
        Class clazz = classMapper.selectById(classId);
        if (clazz.getStudentCount() > 0) {
            clazz.setStudentCount(clazz.getStudentCount() - 1);
            classMapper.updateById(clazz);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean addClassTeacher(Long classId, Long teacherId, String role) {
        ClassTeacher teacher = new ClassTeacher();
        teacher.setClassId(classId);
        teacher.setTeacherId(teacherId);
        teacher.setRole(role);
        return classTeacherMapper.insert(teacher) > 0;
    }

    @Override
    @Transactional
    public boolean removeClassTeacher(Long classId, Long teacherId) {
        return classTeacherMapper.delete(new LambdaQueryWrapper<ClassTeacher>()
                .eq(ClassTeacher::getClassId, classId)
                .eq(ClassTeacher::getTeacherId, teacherId)) > 0;
    }

    @Override
    public List<ClassVO> getClassMembers(Long classId) {
        List<ClassMember> members = classMemberMapper.selectList(new LambdaQueryWrapper<ClassMember>()
                .eq(ClassMember::getClassId, classId)
                .eq(ClassMember::getIsDelete, 0));
        return members.stream().map(m -> {
            ClassVO vo = new ClassVO();
            vo.setId(m.getId());
            User user = userMapper.selectById(m.getUserId());
            if (user != null) {
                vo.setTeacherId(user.getId());
                vo.setTeacherName(user.getUserName());
                vo.setUserPhone(user.getUserPhone());
                vo.setUserEmail(user.getUserEmail());
                vo.setUserAccount(user.getUserAccount());
            }
            vo.setCreateTime(m.getJoinTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ClassVO> getClassTeachers(Long classId) {
        List<ClassTeacher> teachers = classTeacherMapper.selectList(new LambdaQueryWrapper<ClassTeacher>()
                .eq(ClassTeacher::getClassId, classId)
                .eq(ClassTeacher::getIsDelete, 0));
        return teachers.stream().map(t -> {
            ClassVO vo = new ClassVO();
            vo.setId(t.getId());
            User user = userMapper.selectById(t.getTeacherId());
            if (user != null) {
                vo.setTeacherId(user.getId());
                vo.setTeacherName(user.getUserName());
                vo.setUserAccount(user.getUserAccount());
                vo.setUserPhone(user.getUserPhone());
                vo.setUserEmail(user.getUserEmail());
            }
            vo.setCreateTime(t.getJoinTime());
            vo.setDescription(t.getRole());
            return vo;
        }).collect(Collectors.toList());
    }

    private ClassVO convertToVO(Class clazz) {
        ClassVO vo = new ClassVO();
        vo.setId(clazz.getId());
        vo.setClassName(clazz.getClassName());
        vo.setClassCode(clazz.getClassCode());
        vo.setGrade(clazz.getGrade());
        vo.setMajor(clazz.getMajor());
        vo.setDescription(clazz.getDescription());
        vo.setStudentCount(clazz.getStudentCount());
        vo.setTeacherId(clazz.getTeacherId());
        vo.setStatus(clazz.getStatus());
        vo.setCreateTime(clazz.getCreateTime());
        vo.setUpdateTime(clazz.getUpdateTime());

        if (clazz.getTeacherId() != null) {
            User teacher = userMapper.selectById(clazz.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getUserName());
            }
        }

        return vo;
    }
}
