package com.jcen.unifit.service;

import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.UserVO;
import java.util.Map;
import java.util.List;

public interface AdminService {

    String exportUserAndScoreCsv();

    boolean updateUserStatus(Long userId, Integer status);

    long countStudents();

    long countCheckinsLast7Days();

    long countPendingStudentAudits();

    long countPendingMessages();

    long countActiveClasses();

    String exportClassChallengeCsv(int days);

    /**
     * 获取用户详情
     */
    UserVO getUserDetail(Long userId);

    /**
     * 编辑用户信息
     */
    boolean updateUser(Long userId, String userName, String userPhone, String userRole);

    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);

    /**
     * 获取仪表板详细数据
     */
    Map<String, Object> getDashboardDetail();

    /**
     * 获取打卡趋势数据（最近7天）
     */
    List<Map<String, Object>> getCheckinTrend();

    /**
     * 获取班级排行榜
     */
    List<Map<String, Object>> getClassRanking();

    /**
     * 获取体测项目分布
     */
    List<Map<String, Object>> getTestItemDistribution();

    /**
     * 获取用户活跃度统计
     */
    Map<String, Object> getUserActivityStats();

    /**
     * 获取数据分析总览
     */
    Map<String, Object> getAnalysisOverview();

    /**
     * 获取班级对比分析数据
     */
    List<Map<String, Object>> getClassComparisonData();
}
