package com.jcen.unifit.controller;

import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.model.vo.UserVO;
import com.jcen.unifit.service.AdminService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/user/status")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateStatus(@RequestParam Long userId, @RequestParam Integer status) {
        return ResultUtils.success(adminService.updateUserStatus(userId, status));
    }

    @GetMapping("/user/{userId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserDetail(@PathVariable Long userId) {
        return ResultUtils.success(adminService.getUserDetail(userId));
    }

    @PutMapping("/user/{userId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@PathVariable Long userId,
                                            @RequestParam(required = false) String userName,
                                            @RequestParam(required = false) String userPhone,
                                            @RequestParam(required = false) String userRole) {
        return ResultUtils.success(adminService.updateUser(userId, userName, userPhone, userRole));
    }

    @DeleteMapping("/user/{userId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@PathVariable Long userId) {
        return ResultUtils.success(adminService.deleteUser(userId));
    }

    @GetMapping("/dashboard")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Map<String, Object>> dashboard() {
        Map<String, Object> result = new HashMap<>();
        result.put("studentCount", adminService.countStudents());
        result.put("checkinLast7Days", adminService.countCheckinsLast7Days());
        result.put("pendingStudentAudit", adminService.countPendingStudentAudits());
        result.put("pendingMessages", adminService.countPendingMessages());
        result.put("activeClasses", adminService.countActiveClasses());
        return ResultUtils.success(result);
    }

    @GetMapping("/dashboard/detail")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Map<String, Object>> dashboardDetail() {
        return ResultUtils.success(adminService.getDashboardDetail());
    }

    @GetMapping("/dashboard/checkin-trend")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<Map<String, Object>>> checkinTrend() {
        return ResultUtils.success(adminService.getCheckinTrend());
    }

    @GetMapping("/dashboard/class-ranking")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<Map<String, Object>>> classRanking() {
        return ResultUtils.success(adminService.getClassRanking());
    }

    @GetMapping("/dashboard/test-distribution")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<Map<String, Object>>> testDistribution() {
        return ResultUtils.success(adminService.getTestItemDistribution());
    }

    @GetMapping("/dashboard/activity-stats")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Map<String, Object>> activityStats() {
        return ResultUtils.success(adminService.getUserActivityStats());
    }

    @GetMapping("/analysis/overview")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Map<String, Object>> analysisOverview() {
        return ResultUtils.success(adminService.getAnalysisOverview());
    }

    @GetMapping("/analysis/class-comparison")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<Map<String, Object>>> classComparison() {
        return ResultUtils.success(adminService.getClassComparisonData());
    }

    @GetMapping("/export/users-scores")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseEntity<byte[]> exportCsv() {
        String csv = adminService.exportUserAndScoreCsv();
        byte[] data = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=unifit_users_scores.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(data);
    }

    @GetMapping("/export/class-challenge")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseEntity<byte[]> exportClassChallenge(@RequestParam(defaultValue = "30") int days) {
        String csv = adminService.exportClassChallengeCsv(days);
        byte[] data = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=unifit_class_challenge.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(data);
    }
}
