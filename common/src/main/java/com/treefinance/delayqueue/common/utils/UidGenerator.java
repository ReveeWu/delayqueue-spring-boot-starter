package com.treefinance.delayqueue.common.utils;

import com.treefinance.delayqueue.common.ApplicationContextProvider;
import com.treefinance.commonservice.uid.UidService;

/**
 * @author reveewu
 * @date 08/12/2017
 */
public class UidGenerator {
    public static volatile UidService uidService = null;

    public static long getId() {
        if (null == uidService) {
            uidService = ApplicationContextProvider.getBean(UidService.class);
        }
        return uidService.getId();
    }

    public static long[] getIds(int size) {
        if (null == uidService) {
            uidService = ApplicationContextProvider.getBean(UidService.class);
        }
        return uidService.getIds(size);
    }
}
