package com.treefinance;

import com.alibaba.dubbo.config.annotation.Reference;
import com.datatrees.common.security.client.util.CryptAlgorithm;
import com.treefinance.commonservice.uid.UidService;
import com.treefinance.delayqueue.common.utils.EncryptUtil;
import com.treefinance.delayqueue.common.utils.UidGenerator;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author reveewu
 * @date 24/02/2018
 */
public class CommonTest extends BaseTest {
    @Reference
    private UidService uidService;

    @Test
    public void uidTest() {
        Assert.assertNotNull(UidGenerator.getId());
    }

    @Test
    public void encryptTest() {
        String str = "test";
        String strEncrypt = EncryptUtil.encrypt(str, CryptAlgorithm.AES);
        Assert.assertNotEquals(str, strEncrypt);

        String strDecrypt = EncryptUtil.decrypt(strEncrypt, CryptAlgorithm.AES);
        Assert.assertEquals(str, strDecrypt);
    }
}
