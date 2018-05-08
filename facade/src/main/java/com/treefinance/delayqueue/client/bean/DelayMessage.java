package com.treefinance.delayqueue.client.bean;

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
     * 项目组名称，区分不同项目
     */
    private String groupName;
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

    public DelayMessage(String groupName, String topic, String dataKey, String body, long delay) {
        this.groupName = groupName;
        this.topic = topic;
        this.dataKey = dataKey;
        this.body = body;
        this.delay = delay;
    }

    public DelayMessage(String groupName, String topic, String dataKey, String body, Date expireTime) {
        this.groupName = groupName;
        this.topic = topic;
        this.dataKey = dataKey;
        this.body = body;
        this.delay = expireTime.getTime() - System.currentTimeMillis();
    }
}
