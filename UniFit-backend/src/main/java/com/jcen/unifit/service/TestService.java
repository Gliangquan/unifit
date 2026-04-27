package com.jcen.unifit.service;

import com.jcen.unifit.model.dto.TestScoreAddRequest;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.TestScoreVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TestService {

    TestScoreVO addScore(User loginUser, TestScoreAddRequest request);

    List<TestScoreVO> listMyScores(User loginUser, String itemCode);

    Map<String, Object> getWeaknessAnalysis(User loginUser);

    Map<String, Object> getClassCompare(User loginUser);

    List<TestItem> listTestItems(User loginUser);

    Map<String, Object> getScoreRulePreview(User loginUser, String itemCode);

    Map<String, Object> getScoreLevelPreview(User loginUser, String itemCode, BigDecimal scoreValue);
}
