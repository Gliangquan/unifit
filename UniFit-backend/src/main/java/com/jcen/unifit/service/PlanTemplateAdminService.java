package com.jcen.unifit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.model.dto.PlanTemplateItemUpsertRequest;
import com.jcen.unifit.model.dto.PlanTemplateUpsertRequest;
import com.jcen.unifit.model.entity.Exercise;
import com.jcen.unifit.model.entity.PlanTemplate;
import com.jcen.unifit.model.entity.PlanTemplateItem;
import com.jcen.unifit.model.entity.TestItem;

import java.util.List;

public interface PlanTemplateAdminService {

    Page<PlanTemplate> listTemplates(long current, long pageSize, String keyword,
                                     String testItemCode, String scoreLevel, String fitnessLevel,
                                     String equipmentType, String bmiRange, Integer daysPerWeek, Integer status);

    PlanTemplate upsertTemplate(PlanTemplateUpsertRequest request);

    boolean deleteTemplate(Long id);

    List<PlanTemplateItem> listTemplateItems(Long templateId);

    PlanTemplateItem upsertTemplateItem(PlanTemplateItemUpsertRequest request);

    boolean deleteTemplateItem(Long id);

    List<TestItem> listTestItems();

    List<Exercise> listExercises();
}
