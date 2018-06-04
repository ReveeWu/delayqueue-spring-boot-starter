package com.treefinance.delayqueue;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author reveewu
 * @date 23/02/2018
 */
@SpringBootApplication
@EnableDubboConfiguration
public class SpringBootCommonApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootCommonApplication.class, args);
    }
}
