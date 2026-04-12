package com.jcen.unifit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.DeleteRequest;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.model.dto.TestStandardQueryRequest;
import com.jcen.unifit.model.dto.TestStandardUpsertRequest;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.model.entity.TestStandard;
import com.jcen.unifit.service.TestStandardAdminService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/standard")
public class TestStandardAdminController {

    @Resource
    private TestStandardAdminService testStandardAdminService;

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<TestStandard>> list(@RequestBody TestStandardQueryRequest request) {
        return ResultUtils.success(testStandardAdminService.listStandards(request));
    }

    @PostMapping("/upsert")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<TestStandard> upsert(@RequestBody TestStandardUpsertRequest request) {
        return ResultUtils.success(testStandardAdminService.upsertStandard(request));
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest request) {
        return ResultUtils.success(testStandardAdminService.deleteStandard(request.getId()));
    }

    @PostMapping("/import/college-full")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Map<String, Object>> importCollegeFull() {
        int count = testStandardAdminService.importCollegeFullStandard();
        Map<String, Object> result = new HashMap<>();
        result.put("inserted", count);
        return ResultUtils.success(result);
    }

    @GetMapping("/test-items")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<TestItem>> listTestItems() {
        return ResultUtils.success(testStandardAdminService.listTestItems());
    }
}
