package com.treefinance.service.impl;

import com.treefinance.dao.domain.test.Test;
import com.treefinance.dao.mapper.test.TestMapper;
import com.treefinance.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author reveewu
 * @date 24/02/2018
 */
@Slf4j
@Service
public class DemoServiceImpl implements DemoService {
    @Autowired
    private TestMapper testMapper;

    @Override
    public List<Test> listAllTest() {
        return testMapper.selectByExample(null);
    }
}
