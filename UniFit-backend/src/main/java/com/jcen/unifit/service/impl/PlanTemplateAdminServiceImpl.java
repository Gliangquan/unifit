package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.ExerciseMapper;
import com.jcen.unifit.mapper.PlanTemplateItemMapper;
import com.jcen.unifit.mapper.PlanTemplateMapper;
import com.jcen.unifit.mapper.TestItemMapper;
import com.jcen.unifit.model.dto.PlanTemplateItemUpsertRequest;
import com.jcen.unifit.model.dto.PlanTemplateUpsertRequest;
import com.jcen.unifit.model.entity.Exercise;
import com.jcen.unifit.model.entity.PlanTemplate;
import com.jcen.unifit.model.entity.PlanTemplateItem;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.service.PlanTemplateAdminService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class PlanTemplateAdminServiceImpl implements PlanTemplateAdminService {

    @Resource
    private PlanTemplateMapper planTemplateMapper;

    @Resource
    private PlanTemplateItemMapper planTemplateItemMapper;

    @Resource
    private TestItemMapper testItemMapper;

    @Resource
    private ExerciseMapper exerciseMapper;

    @Override
    public Page<PlanTemplate> listTemplates(long current, long pageSize, String keyword,
                                            String testItemCode, String scoreLevel, String fitnessLevel,
                                            String equipmentType, String bmiRange, Integer daysPerWeek, Integer status) {
        QueryWrapper<PlanTemplate> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            qw.and(x -> x.like("template_code", keyword).or().like("template_name", keyword));
        }
        if (StringUtils.isNotBlank(testItemCode)) {
            qw.eq("test_item_code", testItemCode);
        }
        if (StringUtils.isNotBlank(scoreLevel)) {
            qw.eq("score_level", scoreLevel);
        }
        if (StringUtils.isNotBlank(fitnessLevel)) {
            qw.eq("fitness_level", fitnessLevel);
        }
        if (StringUtils.isNotBlank(equipmentType)) {
            qw.eq("equipment_type", equipmentType);
        }
        if (StringUtils.isNotBlank(bmiRange)) {
            qw.eq("bmi_range", bmiRange);
        }
        if (daysPerWeek != null) {
            qw.eq("days_per_week", daysPerWeek);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("id");
        return planTemplateMapper.selectPage(new Page<>(current, pageSize), qw);
    }

    @Override
    public PlanTemplate upsertTemplate(PlanTemplateUpsertRequest request) {
        if (request == null || StringUtils.isAnyBlank(request.getTemplateCode(), request.getTemplateName(), request.getTestItemCode(), request.getScoreLevel(), request.getFitnessLevel(), request.getEquipmentType()) || request.getDaysPerWeek() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模板参数不完整");
        }

        PlanTemplate template;
        if (request.getId() == null) {
            template = new PlanTemplate();
            template.setCreateTime(new Date());
        } else {
            template = planTemplateMapper.selectById(request.getId());
            if (template == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "模板不存在");
            }
        }

        BeanUtils.copyProperties(request, template);
        template.setUpdateTime(new Date());
        if (template.getStatus() == null) {
            template.setStatus(1);
        }

        if (template.getId() == null) {
            planTemplateMapper.insert(template);
        } else {
            planTemplateMapper.updateById(template);
        }
        return template;
    }

    @Override
    public boolean deleteTemplate(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        planTemplateItemMapper.delete(new QueryWrapper<PlanTemplateItem>().eq("template_id", id));
        return planTemplateMapper.deleteById(id) > 0;
    }

    @Override
    public List<PlanTemplateItem> listTemplateItems(Long templateId) {
        QueryWrapper<PlanTemplateItem> qw = new QueryWrapper<>();
        qw.eq("template_id", templateId).orderByAsc("week_no", "day_no", "sort_no", "id");
        return planTemplateItemMapper.selectList(qw);
    }

    @Override
    public PlanTemplateItem upsertTemplateItem(PlanTemplateItemUpsertRequest request) {
        if (request == null || request.getTemplateId() == null || request.getWeekNo() == null || request.getDayNo() == null || request.getExerciseId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模板动作参数不完整");
        }

        PlanTemplateItem item;
        if (request.getId() == null) {
            item = new PlanTemplateItem();
            item.setCreateTime(new Date());
        } else {
            item = planTemplateItemMapper.selectById(request.getId());
            if (item == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "模板动作不存在");
            }
        }

        BeanUtils.copyProperties(request, item);
        if (item.getSortNo() == null) {
            item.setSortNo(1);
        }

        if (item.getId() == null) {
            planTemplateItemMapper.insert(item);
        } else {
            planTemplateItemMapper.updateById(item);
        }
        return item;
    }

    @Override
    public boolean deleteTemplateItem(Long id) {
        return planTemplateItemMapper.deleteById(id) > 0;
    }

    @Override
    public List<TestItem> listTestItems() {
        return testItemMapper.selectList(new QueryWrapper<TestItem>().eq("status", 1).orderByAsc("id"));
    }

    @Override
    public List<Exercise> listExercises() {
        return exerciseMapper.selectList(new QueryWrapper<Exercise>().eq("status", 1).orderByAsc("id"));
    }
}
