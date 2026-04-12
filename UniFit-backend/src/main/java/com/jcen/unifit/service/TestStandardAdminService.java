package com.jcen.unifit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.unifit.model.dto.TestStandardQueryRequest;
import com.jcen.unifit.model.dto.TestStandardUpsertRequest;
import com.jcen.unifit.model.entity.TestItem;
import com.jcen.unifit.model.entity.TestStandard;

import java.util.List;

public interface TestStandardAdminService {

    Page<TestStandard> listStandards(TestStandardQueryRequest request);

    TestStandard upsertStandard(TestStandardUpsertRequest request);

    boolean deleteStandard(Long id);

    int importCollegeFullStandard();

    List<TestItem> listTestItems();
}
