package com.treefinance.delayqueue.service;

/**
 * @author reveewu
 * @date 31/05/2018
 */
public interface MigrateService {
    /**
     * 迁移
     * @param key
     * @param groupName
     * @param topic
     */
    void migrate(String key, String groupName, String topic);
}
