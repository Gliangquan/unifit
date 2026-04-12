package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.TestItemMapper;
import com.jcen.unifit.mapper.TestStandardMapper;
import com.jcen.unifit.model.dto.TestStandardQueryRequest;
import com.jcen.unifit.model.dto.TestStandardUpsertRequest;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.model.entity.TestStandard;
import com.jcen.unifit.service.TestStandardAdminService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class TestStandardAdminServiceImpl implements TestStandardAdminService {

    @Resource
    private TestStandardMapper testStandardMapper;

    @Resource
    private TestItemMapper testItemMapper;

    @Override
    public Page<TestStandard> listStandards(TestStandardQueryRequest request) {
        long current = request.getCurrent() == null ? 1 : request.getCurrent();
        long pageSize = request.getPageSize() == null ? 20 : request.getPageSize();

        QueryWrapper<TestStandard> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(request.getStage())) {
            qw.eq("stage", request.getStage());
        }
        if (StringUtils.isNotBlank(request.getGender())) {
            qw.eq("gender", request.getGender());
        }
        if (StringUtils.isNotBlank(request.getItemCode())) {
            qw.eq("item_code", request.getItemCode());
        }
        qw.orderByAsc("stage", "gender", "item_code", "standard_point");
        return testStandardMapper.selectPage(new Page<>(current, pageSize), qw);
    }

    @Override
    public TestStandard upsertStandard(TestStandardUpsertRequest request) {
        if (request == null || StringUtils.isAnyBlank(request.getStage(), request.getGradeRange(), request.getGender(), request.getItemCode(), request.getLevel()) || request.getMinScore() == null || request.getMaxScore() == null || request.getStandardPoint() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标准参数不完整");
        }

        TestStandard standard;
        if (request.getId() == null) {
            standard = new TestStandard();
            standard.setCreateTime(new Date());
        } else {
            standard = testStandardMapper.selectById(request.getId());
            if (standard == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "标准不存在");
            }
        }

        BeanUtils.copyProperties(request, standard);
        if (standard.getId() == null) {
            testStandardMapper.insert(standard);
        } else {
            testStandardMapper.updateById(standard);
        }
        return standard;
    }

    @Override
    public boolean deleteStandard(Long id) {
        return testStandardMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importCollegeFullStandard() {
        ensureCollegeTestItems();

        // 采用物理删除，避免多次导入后逻辑删除数据持续堆积
        testStandardMapper.deleteByStagePhysical("college");

        List<TestStandard> data = new ArrayList<>();

        // 男生
        data.addAll(buildHigher("male", "vital_capacity", Arrays.asList(
                range(5040, 99999, "excellent", 100),
                range(4920, 5039.99, "good", 90),
                range(4800, 4919.99, "good", 80),
                range(4550, 4799.99, "pass", 70),
                range(4300, 4549.99, "pass", 60),
                range(4180, 4299.99, "pass", 50),
                range(4060, 4179.99, "fail", 40),
                range(3940, 4059.99, "fail", 30),
                range(3820, 3939.99, "fail", 20),
                range(0, 3819.99, "fail", 10)
        )));
        data.addAll(buildLower("male", "run_50", Arrays.asList(
                range(0, 6.69, "excellent", 100),
                range(6.70, 6.79, "good", 90),
                range(6.80, 6.89, "good", 80),
                range(6.90, 6.99, "pass", 70),
                range(7.00, 7.09, "pass", 60),
                range(7.10, 7.19, "pass", 50),
                range(7.20, 7.29, "fail", 40),
                range(7.30, 7.39, "fail", 30),
                range(7.40, 7.49, "fail", 20),
                range(7.50, 99, "fail", 10)
        )));
        data.addAll(buildHigher("male", "sit_reach", Arrays.asList(
                range(24.9, 99, "excellent", 100),
                range(22.7, 24.89, "good", 90),
                range(20.6, 22.69, "good", 80),
                range(18.5, 20.59, "pass", 70),
                range(16.1, 18.49, "pass", 60),
                range(14.2, 16.09, "pass", 50),
                range(12.3, 14.19, "fail", 40),
                range(10.4, 12.29, "fail", 30),
                range(8.5, 10.39, "fail", 20),
                range(-99, 8.49, "fail", 10)
        )));
        data.addAll(buildHigher("male", "long_jump", Arrays.asList(
                range(273, 999, "excellent", 100),
                range(268, 272.99, "good", 90),
                range(263, 267.99, "good", 80),
                range(256, 262.99, "pass", 70),
                range(248, 255.99, "pass", 60),
                range(244, 247.99, "pass", 50),
                range(240, 243.99, "fail", 40),
                range(236, 239.99, "fail", 30),
                range(232, 235.99, "fail", 20),
                range(0, 231.99, "fail", 10)
        )));
        data.addAll(buildHigher("male", "pull_up", Arrays.asList(
                range(19, 200, "excellent", 100),
                range(17, 18.99, "good", 90),
                range(15, 16.99, "good", 80),
                range(13, 14.99, "pass", 70),
                range(11, 12.99, "pass", 60),
                range(10, 10.99, "pass", 50),
                range(9, 9.99, "fail", 40),
                range(8, 8.99, "fail", 30),
                range(7, 7.99, "fail", 20),
                range(0, 6.99, "fail", 10)
        )));
        data.addAll(buildLower("male", "run_1000", Arrays.asList(
                range(0, 197, "excellent", 100),
                range(198, 206.99, "good", 90),
                range(207, 215.99, "good", 80),
                range(216, 224.99, "pass", 70),
                range(225, 232.99, "pass", 60),
                range(233, 240.99, "pass", 50),
                range(241, 248.99, "fail", 40),
                range(249, 256.99, "fail", 30),
                range(257, 264.99, "fail", 20),
                range(265, 999, "fail", 10)
        )));

        // 女生
        data.addAll(buildHigher("female", "vital_capacity", Arrays.asList(
                range(3400, 99999, "excellent", 100),
                range(3350, 3399.99, "good", 90),
                range(3300, 3349.99, "good", 80),
                range(3150, 3299.99, "pass", 70),
                range(3000, 3149.99, "pass", 60),
                range(2900, 2999.99, "pass", 50),
                range(2800, 2899.99, "fail", 40),
                range(2700, 2799.99, "fail", 30),
                range(2600, 2699.99, "fail", 20),
                range(0, 2599.99, "fail", 10)
        )));
        data.addAll(buildLower("female", "run_50", Arrays.asList(
                range(0, 7.49, "excellent", 100),
                range(7.50, 7.59, "good", 90),
                range(7.60, 7.69, "good", 80),
                range(7.70, 7.79, "pass", 70),
                range(7.80, 7.89, "pass", 60),
                range(7.90, 7.99, "pass", 50),
                range(8.00, 8.09, "fail", 40),
                range(8.10, 8.19, "fail", 30),
                range(8.20, 8.29, "fail", 20),
                range(8.30, 99, "fail", 10)
        )));
        data.addAll(buildHigher("female", "sit_reach", Arrays.asList(
                range(25.8, 99, "excellent", 100),
                range(24.0, 25.79, "good", 90),
                range(22.2, 23.99, "good", 80),
                range(20.6, 22.19, "pass", 70),
                range(19.0, 20.59, "pass", 60),
                range(17.2, 18.99, "pass", 50),
                range(15.4, 17.19, "fail", 40),
                range(13.6, 15.39, "fail", 30),
                range(11.8, 13.59, "fail", 20),
                range(-99, 11.79, "fail", 10)
        )));
        data.addAll(buildHigher("female", "long_jump", Arrays.asList(
                range(207, 999, "excellent", 100),
                range(201, 206.99, "good", 90),
                range(195, 200.99, "good", 80),
                range(188, 194.99, "pass", 70),
                range(181, 187.99, "pass", 60),
                range(178, 180.99, "pass", 50),
                range(175, 177.99, "fail", 40),
                range(172, 174.99, "fail", 30),
                range(169, 171.99, "fail", 20),
                range(0, 168.99, "fail", 10)
        )));
        data.addAll(buildHigher("female", "sit_up", Arrays.asList(
                range(56, 200, "excellent", 100),
                range(53, 55.99, "good", 90),
                range(50, 52.99, "good", 80),
                range(46, 49.99, "pass", 70),
                range(42, 45.99, "pass", 60),
                range(40, 41.99, "pass", 50),
                range(38, 39.99, "fail", 40),
                range(36, 37.99, "fail", 30),
                range(34, 35.99, "fail", 20),
                range(0, 33.99, "fail", 10)
        )));
        data.addAll(buildLower("female", "run_800", Arrays.asList(
                range(0, 198, "excellent", 100),
                range(199, 207.99, "good", 90),
                range(208, 216.99, "good", 80),
                range(217, 225.99, "pass", 70),
                range(226, 234.99, "pass", 60),
                range(235, 242.99, "pass", 50),
                range(243, 250.99, "fail", 40),
                range(251, 258.99, "fail", 30),
                range(259, 266.99, "fail", 20),
                range(267, 999, "fail", 10)
        )));

        for (TestStandard standard : data) {
            testStandardMapper.insert(standard);
        }
        return data.size();
    }

    @Override
    public List<TestItem> listTestItems() {
        return testItemMapper.selectList(new QueryWrapper<TestItem>().eq("status", 1).orderByAsc("id"));
    }

    private void ensureCollegeTestItems() {
        ensureTestItem("pull_up", "引体向上", "count", "higher");
        ensureTestItem("sit_up", "仰卧起坐", "count", "higher");
        ensureTestItem("run_1000", "1000米跑", "second", "lower");
        ensureTestItem("run_800", "800米跑", "second", "lower");
        ensureTestItem("long_jump", "立定跳远", "cm", "higher");
        ensureTestItem("vital_capacity", "肺活量", "ml", "higher");
        ensureTestItem("run_50", "50米跑", "second", "lower");
        ensureTestItem("sit_reach", "坐位体前屈", "cm", "higher");
    }

    private void ensureTestItem(String code, String name, String unit, String direction) {
        TestItem item = testItemMapper.selectOne(new QueryWrapper<TestItem>().eq("item_code", code));
        if (item != null) {
            return;
        }
        item = new TestItem();
        item.setItemCode(code);
        item.setItemName(name);
        item.setScoreUnit(unit);
        item.setScoreDirection(direction);
        item.setStatus(1);
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        testItemMapper.insert(item);
    }

    private List<TestStandard> buildHigher(String gender, String itemCode, List<RangeSpec> ranges) {
        return buildCommon(gender, itemCode, ranges);
    }

    private List<TestStandard> buildLower(String gender, String itemCode, List<RangeSpec> ranges) {
        return buildCommon(gender, itemCode, ranges);
    }

    private List<TestStandard> buildCommon(String gender, String itemCode, List<RangeSpec> ranges) {
        List<TestStandard> list = new ArrayList<>();
        for (RangeSpec spec : ranges) {
            TestStandard standard = new TestStandard();
            standard.setStage("college");
            standard.setGradeRange("all");
            standard.setGender(gender);
            standard.setItemCode(itemCode);
            standard.setMinScore(spec.min);
            standard.setMaxScore(spec.max);
            standard.setLevel(spec.level);
            standard.setStandardPoint(spec.point);
            standard.setCreateTime(new Date());
            list.add(standard);
        }
        return list;
    }

    private RangeSpec range(double min, double max, String level, int point) {
        return new RangeSpec(BigDecimal.valueOf(min), BigDecimal.valueOf(max), level, point);
    }

    private static class RangeSpec {
        private final BigDecimal min;
        private final BigDecimal max;
        private final String level;
        private final Integer point;

        private RangeSpec(BigDecimal min, BigDecimal max, String level, Integer point) {
            this.min = min;
            this.max = max;
            this.level = level;
            this.point = point;
        }
    }
}
