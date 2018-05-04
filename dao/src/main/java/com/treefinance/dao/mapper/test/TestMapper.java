package com.treefinance.dao.mapper.test;

import com.treefinance.dao.domain.test.Test;
import com.treefinance.dao.domain.test.TestCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface TestMapper {
    int countByExample(TestCriteria example);

    int deleteByExample(TestCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(Test record);

    int insertSelective(Test record);

    List<Test> selectByExampleWithRowbounds(TestCriteria example, RowBounds rowBounds);

    List<Test> selectByExample(TestCriteria example);

    Test selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Test record, @Param("example") TestCriteria example);

    int updateByExample(@Param("record") Test record, @Param("example") TestCriteria example);

    int updateByPrimaryKeySelective(Test record);

    int updateByPrimaryKey(Test record);
}