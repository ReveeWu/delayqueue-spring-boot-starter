package com.github.reveewu.delayqueue.core;

/**
 * @author reveewu
 * @date 05/05/2018
 */
public final class Constant {
    /**
     * groupName_延时队列前缀
     */
    private static final String REDIS_PREFIX= "%s_DELAY_QUEUE_SERVICE_";

    /**
     * 元数据
     */
    private static final String META_DATA_KEY=REDIS_PREFIX + "META_DATA";
    /**
     * 未到期zset
     */
    private static final String ZSET_KEY= REDIS_PREFIX + "ZSET";
    /**
     * 到期待消费list
     */
    private static final String LIST_KEY_FORMAT = REDIS_PREFIX + "LIST_%s";
    /**
     * 消费中zset
     */
    private static final String CONSUMING_KEY_FORMAT = REDIS_PREFIX + "CONSUMING";

    /**
     * 搬砖锁 从未到期 -> 到期待消费
     */
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

    public static String getConsumingKey(String groupName) {
        return String.format(CONSUMING_KEY_FORMAT, groupName);
    }

    public static String getMoveBrickLockName(String groupName) {
        return String.format(MOVE_BRICK_LOCK, groupName);
    }
}
