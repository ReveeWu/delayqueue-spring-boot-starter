package com.treefinance.delayqueue.dao.interceptors;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author reveewu
 * @date 29/12/2017
 */
@Configuration
public class InterceptorConfiguration {
    @Bean
    public Interceptor securedQueryInterceptor() {
        return new SecuredQueryInterceptor();
    }

    @Bean
    public Interceptor securedUpdateInterceptor() {
        return new SecuredUpdateInterceptor();
    }

    @Bean
    public Interceptor offsetLimitInterceptor() {
        return new OffsetLimitInterceptor();
    }
}
