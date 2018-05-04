package com.treefinance.delayqueue.common.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.annotation.Order;

/**
 * @author reveewu
 * @date 29/12/2017
 */
@Configuration
@ImportResource(value = "classpath*:spring/dashu-diamond-spring.xml")
@Order(1)
public class DiamondConfiguration {
}
