package com.jcen.unifit.service;

import com.jcen.unifit.model.dto.HealthProfileUpdateRequest;
import com.jcen.unifit.model.entity.HealthProfile;
import com.jcen.unifit.model.entity.HealthRecord;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.HealthProfileVO;

import java.util.List;

public interface HealthService {

    HealthProfileVO getMyHealthProfile(User loginUser);

    HealthProfileVO upsertMyHealthProfile(User loginUser, HealthProfileUpdateRequest request);

    List<HealthRecord> listMyRecords(User loginUser);

    String bmiStatus(java.math.BigDecimal bmi);

    HealthProfile ensureProfile(Long userId);
}
