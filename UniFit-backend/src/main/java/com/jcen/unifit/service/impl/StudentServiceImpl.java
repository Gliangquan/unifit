package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.StudentProfileMapper;
import com.jcen.unifit.model.dto.StudentAuditRequest;
import com.jcen.unifit.model.dto.StudentVerifySubmitRequest;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.User;
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

    @Override
    public StudentProfile submitVerification(User loginUser, StudentVerifySubmitRequest request) {
        if (request == null || StringUtils.isAnyBlank(request.getStudentId(), request.getRealName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学号和真实姓名不能为空");
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
        profile.setClassName(StringUtils.trimToNull(request.getClassName()));
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
        return studentProfileMapper.selectOne(qw);
    }

    @Override
    public List<StudentProfile> listPendingProfiles() {
        QueryWrapper<StudentProfile> qw = new QueryWrapper<>();
        qw.eq("verification_status", "pending").orderByAsc("create_time");
        return studentProfileMapper.selectList(qw);
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
        return studentProfileMapper.updateById(profile) > 0;
    }
}
