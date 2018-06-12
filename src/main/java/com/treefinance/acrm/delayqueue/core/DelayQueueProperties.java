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
    @NotBlank
    private String groupName;
}
