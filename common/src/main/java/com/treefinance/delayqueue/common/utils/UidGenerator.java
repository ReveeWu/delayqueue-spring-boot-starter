package com.treefinance.delayqueue.common.utils;

import com.alibaba.dubbo.config.annotation.Reference;
import com.treefinance.commonservice.uid.UidService;
import com.treefinance.delayqueue.common.ApplicationContextProvider;
import org.springframework.stereotype.Component;

/**
 * @author reveewu
 * @date 08/12/2017
 */
@Component
public class UidGenerator {
    @Reference
    public UidService uidService;

    public static long getId() {
        return ApplicationContextProvider.getBean(UidGenerator.class).uidService.getId();
    }

    public static long[] getIds(int size) {
        return ApplicationContextProvider.getBean(UidGenerator.class).uidService.getIds(size);
    }
}
