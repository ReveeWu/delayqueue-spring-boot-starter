package com.treefinance.common.utils;

import com.datatrees.common.security.client.CryptUtil;
import com.datatrees.common.security.client.util.CryptAlgorithm;
import com.treefinance.common.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * @author reveewu
 * @date 09/01/2018
 * 加解密工具
 */
@Slf4j
public abstract class EncryptUtil {
    private static volatile CryptUtil cryptUtil;

    private static void init() {
        if (null == cryptUtil) {
            cryptUtil = ApplicationContextProvider.getBean(CryptUtil.class);
        }
    }

    public static String decrypt(String str, CryptAlgorithm algorithm) {
        try {
            init();
            return cryptUtil.decrypt(str, algorithm);
        } catch (Throwable e) {
            log.error("Decrypt error: {}", e.getMessage());
        }
        return str;
    }

    public static String encrypt(String str, CryptAlgorithm algorithm) {
        try {
            init();
            return cryptUtil.encrypt(str, algorithm);
        } catch (Throwable e) {
            log.error("Encrypt error: {}", e.getMessage());
        }
        return str;
    }
}
