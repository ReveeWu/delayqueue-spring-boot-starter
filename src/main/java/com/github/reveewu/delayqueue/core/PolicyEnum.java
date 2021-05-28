package com.github.reveewu.delayqueue.core;

import java.io.Serializable;

/**
 * @Description: push策略
 * @Author: ouyangtao
 * @Date: 2018/8/27
 */
public enum PolicyEnum implements Serializable {
    /**
     * 忽略
     */
    IGNORE,
    /**
     * 覆盖
     */
    COVER,
    /**
     * 新增
     */
    ADD,
}
