package com.treefinance.delayqueue.service;

import com.treefinance.delayqueue.dao.domain.test.Test;

import java.util.List;

/**
 * @author reveewu
 * @date 24/02/2018
 */
public interface DemoService {
    /**
     * 列举所有test数据
     * @return
     */
    List<Test> listAllTest();
}
