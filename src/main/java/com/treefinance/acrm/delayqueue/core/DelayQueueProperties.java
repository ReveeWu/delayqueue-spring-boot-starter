package com.treefinance.acrm.delayqueue.core;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author reveewu
 * @date 07/06/2018
 */
@Data
@ConfigurationProperties(prefix = "spring.delayqueue")
public class DelayQueueProperties {
    /**
     * 分组名
     */
    @NotBlank
    private String groupName;

    /**
     * 消费者线程数
     */
    private Integer consumerThreadCount = 1;
}
