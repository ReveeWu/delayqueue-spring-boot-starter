package com.treefinance.delayqueue.client.bean;

import com.treefinance.delayqueue.client.enums.Status;
import lombok.Data;

/**
 * @author reveewu
 * @date 04/05/2018
 * <p>
 * 延时消息体
 */
@Data
public class DelayMessageExt extends DelayMessage {
    /**
     * id = groupName + topic + dataKey
     * 表示业务唯一键
     */
    private String id;
    /**
     * 执行时间
     */
    private long executeTime;
    /**
     * 创建时间
     */
    private long createTime = System.currentTimeMillis();
    /**
     * 状态 待入列-待出列-待消费-已消费-待重新消费-消费失败
     */
    private String status = Status.WAIT_PUSH.name();
    /**
     * 消费失败次数
     */
    private int consumeErrorCnt = 0;
}
