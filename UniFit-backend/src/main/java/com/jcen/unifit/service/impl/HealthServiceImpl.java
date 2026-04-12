package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.unifit.mapper.HealthProfileMapper;
import com.jcen.unifit.mapper.HealthRecordMapper;
import com.jcen.unifit.model.dto.HealthProfileUpdateRequest;
import com.jcen.unifit.model.entity.HealthProfile;
import com.jcen.unifit.model.entity.HealthRecord;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.HealthProfileVO;
import com.jcen.unifit.service.HealthService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
public class HealthServiceImpl implements HealthService {

    @Resource
    private HealthProfileMapper healthProfileMapper;

    @Resource
    private HealthRecordMapper healthRecordMapper;

    @Override
    public HealthProfileVO getMyHealthProfile(User loginUser) {
        HealthProfile profile = ensureProfile(loginUser.getId());
        HealthProfileVO vo = new HealthProfileVO();
        BeanUtils.copyProperties(profile, vo);
        return vo;
    }

    @Override
    public HealthProfileVO upsertMyHealthProfile(User loginUser, HealthProfileUpdateRequest request) {
        HealthProfile profile = ensureProfile(loginUser.getId());
        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());
        profile.setUpdateTime(new Date());

        if (request.getHeight() != null && request.getWeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightMeter = request.getHeight().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal bmi = request.getWeight().divide(heightMeter.multiply(heightMeter), 2, RoundingMode.HALF_UP);
            profile.setBmiValue(bmi);
            profile.setBmiStatus(bmiStatus(bmi));

            HealthRecord record = new HealthRecord();
            record.setUserId(loginUser.getId());
            record.setHeight(request.getHeight());
            record.setWeight(request.getWeight());
            record.setBmiValue(bmi);
            record.setRecordDate(new Date());
            record.setCreateTime(new Date());
            healthRecordMapper.insert(record);
        }

        healthProfileMapper.updateById(profile);
        HealthProfileVO vo = new HealthProfileVO();
        BeanUtils.copyProperties(profile, vo);
        return vo;
    }

    @Override
    public List<HealthRecord> listMyRecords(User loginUser) {
        QueryWrapper<HealthRecord> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId()).orderByDesc("record_date");
        return healthRecordMapper.selectList(qw);
    }

    @Override
    public String bmiStatus(BigDecimal bmi) {
        if (bmi == null) {
            return "unknown";
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

    @Override
    public HealthProfile ensureProfile(Long userId) {
        QueryWrapper<HealthProfile> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        HealthProfile profile = healthProfileMapper.selectOne(qw);
        if (profile != null) {
            return profile;
        }
        profile = new HealthProfile();
        profile.setUserId(userId);
        profile.setCreateTime(new Date());
        profile.setUpdateTime(new Date());
        healthProfileMapper.insert(profile);
        return profile;
    }
}
