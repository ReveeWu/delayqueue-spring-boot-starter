package com.treefinance.delayqueue.service;

/**
 * @author reveewu
 * @date 05/05/2018
 */
public final class Constant {
    public static final String REDIS_PREFIX= "DELAY_QUEUE_SERVICE_";
    public static final String META_DATA_KEY=REDIS_PREFIX + "META_DATA";
    public static final String ZSET_KEY= REDIS_PREFIX + "ZSET";
    public static final String LIST_KEY_FORMAT = REDIS_PREFIX + "LIST_%s_%s";

    public static String getListKey(String groupName, String topic) {
        return String.format(LIST_KEY_FORMAT, groupName, topic);
    }
}
