package com.treefinance.delayqueue.common;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author reveewu
 * @date 23/01/2018
 */
public interface ApplicationReadyListener extends ApplicationListener {
    @Override
    default void onApplicationEvent(ApplicationEvent event) {
        // 监听ApplicationReadyEvent
        if (event instanceof ApplicationReadyEvent && ((ApplicationReadyEvent) event).getApplicationContext().getParent() == null) {
            onApplicationReady();
        }
    }

    /**
     * 应用启动成功时调用
     */
    void onApplicationReady();
}
