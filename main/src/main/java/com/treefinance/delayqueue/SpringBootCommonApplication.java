package com.treefinance.delayqueue;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author reveewu
 * @date 23/02/2018
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.treefinance.dao.mapper"})
public class SpringBootCommonApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootCommonApplication.class, args);
    }
}
