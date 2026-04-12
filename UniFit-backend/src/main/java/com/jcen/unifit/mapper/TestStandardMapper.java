package com.jcen.unifit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcen.unifit.model.entity.TestStandard;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface TestStandardMapper extends BaseMapper<TestStandard> {

    @Delete("DELETE FROM test_standard WHERE stage = #{stage}")
    int deleteByStagePhysical(@Param("stage") String stage);
}
