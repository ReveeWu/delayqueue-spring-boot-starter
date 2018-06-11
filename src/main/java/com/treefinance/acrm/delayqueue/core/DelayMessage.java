package com.treefinance.acrm.delayqueue.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author reveewu
 * @date 04/05/2018
 * <p>
 * 延时消息体
 */
@Data
public class DelayMessage implements Serializable {
    /**
     * 主题
     */
    private String topic;
    /**
     * id 作为元数据map的key
     */
    private String dataKey;
    /**
     * 消息体
     */
    private String body;
    /**
     * 延时毫秒数
     */
    private long delay;

    public DelayMessage() {}

    public DelayMessage(String topic, String dataKey, String body, long delay) {
        this.topic = topic;
        this.dataKey = dataKey;
        this.body = body;
        this.delay = delay;
    }

    public DelayMessage(String topic, String dataKey, String body, Date expireTime) {
        this.topic = topic;
        this.dataKey = dataKey;
        this.body = body;
        this.delay = expireTime.getTime() - System.currentTimeMillis();
    }
}
