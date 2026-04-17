package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcen.unifit.mapper.ClassMapper;
import com.jcen.unifit.mapper.ClassMemberMapper;
import com.jcen.unifit.mapper.StudentProfileMapper;
import com.jcen.unifit.model.dto.StudentAuditRequest;
import com.jcen.unifit.model.dto.StudentVerifySubmitRequest;
import com.jcen.unifit.model.entity.Class;
import com.jcen.unifit.model.entity.ClassMember;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ClassVO;
import com.jcen.unifit.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Resource
    private ClassMapper classMapper;

    @Resource
    private ClassMemberMapper classMemberMapper;

    @Override
    public StudentProfile submitVerification(User loginUser, StudentVerifySubmitRequest request) {
        if (request == null || StringUtils.isAnyBlank(request.getStudentId(), request.getRealName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学号和真实姓名不能为空");
        }
        if (request.getClassId() == null || request.getClassId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要加入的班级");
        }

        Class clazz = classMapper.selectById(request.getClassId());
        if (clazz == null || clazz.getIsDelete() != null && clazz.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "班级不存在");
        }

        QueryWrapper<StudentProfile> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId());
        StudentProfile profile = studentProfileMapper.selectOne(qw);
        Date now = new Date();
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUserId(loginUser.getId());
            profile.setCreateTime(now);
        }

        profile.setStudentId(request.getStudentId());
        profile.setRealName(request.getRealName());
        profile.setClassId(clazz.getId());
        profile.setClassName(clazz.getClassName());
        profile.setVerificationStatus("pending");
        profile.setRejectReason(null);
        profile.setAuditBy(null);
        profile.setAuditTime(null);
        profile.setUpdateTime(now);

        if (profile.getId() == null) {
            studentProfileMapper.insert(profile);
        } else {
            studentProfileMapper.updateById(profile);
        }
        return profile;
    }

    @Override
    public StudentProfile getMyProfile(User loginUser) {
        QueryWrapper<StudentProfile> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId());
        StudentProfile profile = studentProfileMapper.selectOne(qw);
        fillClassInfo(profile);
        return profile;
    }

    @Override
    public List<ClassVO> listAvailableClasses() {
        return classMapper.selectList(new LambdaQueryWrapper<Class>()
                        .eq(Class::getIsDelete, 0)
                        .eq(Class::getStatus, 1)
                        .orderByAsc(Class::getClassName))
                .stream()
                .map(clazz -> {
                    ClassVO vo = new ClassVO();
                    vo.setId(clazz.getId());
                    vo.setClassName(clazz.getClassName());
                    vo.setClassCode(clazz.getClassCode());
                    vo.setGrade(clazz.getGrade());
                    vo.setMajor(clazz.getMajor());
                    vo.setDescription(clazz.getDescription());
                    vo.setStudentCount(clazz.getStudentCount());
                    vo.setTeacherId(clazz.getTeacherId());
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<StudentProfile> listPendingProfiles() {
        QueryWrapper<StudentProfile> qw = new QueryWrapper<>();
        qw.eq("verification_status", "pending").orderByAsc("create_time");
        List<StudentProfile> profiles = studentProfileMapper.selectList(qw);
        profiles.forEach(this::fillClassInfo);
        return profiles;
    }

    @Override
    public List<StudentProfile> listAuditHistoryProfiles() {
        QueryWrapper<StudentProfile> qw = new QueryWrapper<>();
        qw.in("verification_status", "approved", "rejected")
                .orderByDesc("audit_time", "update_time", "id");
        List<StudentProfile> profiles = studentProfileMapper.selectList(qw);
        profiles.forEach(this::fillClassInfo);
        return profiles;
    }

    @Override
    public boolean audit(StudentAuditRequest request, User admin) {
        if (request == null || request.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!"approved".equals(request.getVerificationStatus()) && !"rejected".equals(request.getVerificationStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "审核状态不合法");
        }

        QueryWrapper<StudentProfile> qw = new QueryWrapper<>();
        qw.eq("user_id", request.getUserId());
        StudentProfile profile = studentProfileMapper.selectOne(qw);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学生认证记录不存在");
        }

        profile.setVerificationStatus(request.getVerificationStatus());
        profile.setRejectReason(request.getRejectReason());
        profile.setAuditBy(admin.getId());
        profile.setAuditTime(new Date());
        profile.setUpdateTime(new Date());
        boolean updated = studentProfileMapper.updateById(profile) > 0;
        if (!updated) {
            return false;
        }

        if ("approved".equals(request.getVerificationStatus())) {
            syncApprovedMember(profile);
        }
        return true;
    }

    private void fillClassInfo(StudentProfile profile) {
        if (profile == null) {
            return;
        }
        if (profile.getClassId() != null && profile.getClassId() > 0) {
            Class clazz = classMapper.selectById(profile.getClassId());
            if (clazz != null) {
                profile.setClassName(clazz.getClassName());
                return;
            }
        }
        if (StringUtils.isNotBlank(profile.getClassName())) {
            Class clazz = classMapper.selectOne(new LambdaQueryWrapper<Class>()
                    .eq(Class::getClassName, profile.getClassName())
                    .eq(Class::getIsDelete, 0)
                    .last("limit 1"));
            if (clazz != null) {
                profile.setClassId(clazz.getId());
                profile.setClassName(clazz.getClassName());
            }
        }
    }

    private void syncApprovedMember(StudentProfile profile) {
        fillClassInfo(profile);
        if (profile.getClassId() == null || profile.getClassId() <= 0) {
            return;
        }
        ClassMember exists = classMemberMapper.selectOne(new LambdaQueryWrapper<ClassMember>()
                .eq(ClassMember::getClassId, profile.getClassId())
                .eq(ClassMember::getUserId, profile.getUserId())
                .eq(ClassMember::getIsDelete, 0)
                .last("limit 1"));
        if (exists != null) {
            return;
        }
        ClassMember member = new ClassMember();
        member.setClassId(profile.getClassId());
        member.setUserId(profile.getUserId());
        member.setJoinTime(new Date());
        member.setCreateTime(new Date());
        member.setUpdateTime(new Date());
        classMemberMapper.insert(member);

        Class clazz = classMapper.selectById(profile.getClassId());
        if (clazz != null) {
            Long count = classMemberMapper.selectCount(new LambdaQueryWrapper<ClassMember>()
                    .eq(ClassMember::getClassId, profile.getClassId())
                    .eq(ClassMember::getIsDelete, 0));
            clazz.setStudentCount(count == null ? 0 : count.intValue());
            classMapper.updateById(clazz);
        }
    }
}
