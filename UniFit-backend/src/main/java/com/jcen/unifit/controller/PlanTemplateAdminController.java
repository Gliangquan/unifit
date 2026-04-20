package com.jcen.unifit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.DeleteRequest;
import com.jcen.unifit.common.ResultUtils;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.model.dto.PlanTemplateItemUpsertRequest;
import com.jcen.unifit.model.dto.PlanTemplateUpsertRequest;
import com.jcen.unifit.model.entity.Exercise;
import com.jcen.unifit.model.entity.PlanTemplate;
import com.jcen.unifit.model.entity.PlanTemplateItem;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.service.PlanTemplateAdminService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/template")
public class PlanTemplateAdminController {

    @Resource
    private PlanTemplateAdminService planTemplateAdminService;

    @GetMapping("/list")
    @AuthCheck(mustRoles = {UserConstant.ADMIN_ROLE, UserConstant.TEACHER_ROLE})
    public BaseResponse<Page<PlanTemplate>> list(@RequestParam(defaultValue = "1") long current,
                                                 @RequestParam(defaultValue = "10") long pageSize,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String testItemCode,
                                                 @RequestParam(required = false) String scoreLevel,
                                                 @RequestParam(required = false) String fitnessLevel,
                                                 @RequestParam(required = false) String equipmentType,
                                                 @RequestParam(required = false) String bmiRange,
                                                 @RequestParam(required = false) Integer daysPerWeek,
                                                 @RequestParam(required = false) Integer status) {
        return ResultUtils.success(planTemplateAdminService.listTemplates(current, pageSize, keyword,
                testItemCode, scoreLevel, fitnessLevel, equipmentType, bmiRange, daysPerWeek, status));
    }

    @PostMapping("/upsert")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PlanTemplate> upsert(@RequestBody PlanTemplateUpsertRequest request) {
        return ResultUtils.success(planTemplateAdminService.upsertTemplate(request));
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest request) {
        return ResultUtils.success(planTemplateAdminService.deleteTemplate(request.getId()));
    }

    @GetMapping("/items")
    @AuthCheck(mustRoles = {UserConstant.ADMIN_ROLE, UserConstant.TEACHER_ROLE})
    public BaseResponse<List<PlanTemplateItem>> items(@RequestParam Long templateId) {
        return ResultUtils.success(planTemplateAdminService.listTemplateItems(templateId));
    }

    @PostMapping("/item/upsert")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PlanTemplateItem> upsertItem(@RequestBody PlanTemplateItemUpsertRequest request) {
        return ResultUtils.success(planTemplateAdminService.upsertTemplateItem(request));
    }

    @PostMapping("/item/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteItem(@RequestBody DeleteRequest request) {
        return ResultUtils.success(planTemplateAdminService.deleteTemplateItem(request.getId()));
    }

    @GetMapping("/test-items")
    @AuthCheck(mustRoles = {UserConstant.ADMIN_ROLE, UserConstant.TEACHER_ROLE})
    public BaseResponse<List<TestItem>> listTestItems() {
        return ResultUtils.success(planTemplateAdminService.listTestItems());
    }

    @GetMapping("/exercises")
    @AuthCheck(mustRoles = {UserConstant.ADMIN_ROLE, UserConstant.TEACHER_ROLE})
    public BaseResponse<List<Exercise>> listExercises() {
        return ResultUtils.success(planTemplateAdminService.listExercises());
    }
}
