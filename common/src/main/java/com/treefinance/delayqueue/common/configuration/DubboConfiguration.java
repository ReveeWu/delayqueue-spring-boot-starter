package com.treefinance.delayqueue.common.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author reveewu
 * @date 29/12/2017
 */
@Configuration
@ImportResource(value = {"classpath*:spring/dubbo.xml"})
public class DubboConfiguration {
}
