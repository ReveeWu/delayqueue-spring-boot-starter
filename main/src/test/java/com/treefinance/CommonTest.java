package com.treefinance;

import com.datatrees.common.security.client.util.CryptAlgorithm;
import com.treefinance.delayqueue.common.utils.EncryptUtil;
import com.treefinance.delayqueue.common.utils.UidGenerator;
import com.treefinance.delayqueue.dao.mapper.test.TestMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author reveewu
 * @date 24/02/2018
 */
public class CommonTest extends BaseTest {
    @Autowired
    private TestMapper testMapper;

    @Test
    public void mybatisTest() {
        Assert.assertNotNull(testMapper.selectByExample(null));
    }

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
