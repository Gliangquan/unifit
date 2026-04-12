package com.jcen.unifit.service;

import com.jcen.unifit.model.dto.PlanGenerateRequest;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.PlanVO;

import java.util.List;
import java.util.Map;

public interface PlanService {

    PlanVO generatePlan(User loginUser, PlanGenerateRequest request);

    PlanVO getCurrentPlan(User loginUser);

    List<PlanVO> listPlans(User loginUser);

    boolean markPlanItemDone(User loginUser, Long planItemId);

    Map<String, Object> purchasePlanAccess(User loginUser);
}
