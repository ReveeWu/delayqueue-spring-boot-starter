package com.treefinance.common.configuration;

import com.datatrees.common.security.client.CryptUtil;
import com.datatrees.common.security.client.impl.CryptConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 *
 * @author reveewu
 * @date 03/07/2017
 */
@Slf4j
@Configuration
@Order(value = 2)
public class CryptConfiguration {
    @Value("${security.keyServerAddr}")
    private String keyServerAddr;

    @Value("${security.keyServerPort}")
    private Integer keyServerPort;

    @Value("${security.serviceName}")
    private String serviceName;

    @Value("${security.env}")
    private String env;

    @Bean
    public CryptUtil cryptUtil() {
        try {
            CryptConfig cryptConfig = new CryptConfig();
            cryptConfig.setKeyServerAddr(keyServerAddr);
            cryptConfig.setKeyServerPort(keyServerPort);
            cryptConfig.setServiceName(serviceName);
            cryptConfig.setEnv(env);

            CryptUtil cryptUtil = CryptUtil.getInstance(cryptConfig);
            return cryptUtil;
        } catch (Exception e) {
            log.error("init cryptUtil error", e);
            return null;
        }
    }
}
