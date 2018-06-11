package com.treefinance.acrm.delayqueue.core;

/**
 * @author reveewu
 * @date 05/05/2018
 */
public final class Constant {
    private static final String REDIS_PREFIX= "%s_DELAY_QUEUE_SERVICE_";

    private static final String META_DATA_KEY=REDIS_PREFIX + "META_DATA";
    private static final String ZSET_KEY= REDIS_PREFIX + "ZSET";
    private static final String LIST_KEY_FORMAT = REDIS_PREFIX + "LIST_%s";

    private static final String MOVE_BRICK_LOCK = REDIS_PREFIX + "MOVE_BRICK_LOCK";

    public static String getMetaDataKey(String groupName) {
        return String.format(META_DATA_KEY, groupName);
    }

    public static String getZsetKey(String groupName) {
        return String.format(ZSET_KEY, groupName);
    }

    public static String getListKey(String groupName, String topic) {
        return String.format(LIST_KEY_FORMAT, groupName, topic);
    }

    public static String getMoveBrickLockName(String groupName) {
        return String.format(MOVE_BRICK_LOCK, groupName);
    }
}
