package com.treefinance.acrm.delayqueue;

import com.treefinance.acrm.delayqueue.client.DelayQueueClient;
import com.treefinance.acrm.delayqueue.core.ConsumeStatus;
import com.treefinance.acrm.delayqueue.monitor.StatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author reveewu
 * @date 20/06/2018
 */
@Slf4j
@SpringBootTest(classes = DemoApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClientTest {
    @Autowired
    private DelayQueueClient delayQueueClient;
    @Autowired
    private StatService statService;

    @Test
    public void test() throws Exception {
//        Executors.newSingleThreadExecutor().execute(()->{
//            while (true) {
//                DelayMessage delayMessage = new DelayMessage("test", UUID.randomUUID().toString(), "test", 5000);
//                try {
//                    delayQueueClient.push(delayMessage);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        delayQueueClient.registerTopicListener("test",(messageExt)->{
            System.out.println(Thread.currentThread().getName() + messageExt.toString());
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            return ConsumeStatus.SUCCESS;
        });

        while (true) {
            Thread.sleep(100);
        }
    }

    @Test
    public void testStat() throws Exception {
        System.out.println( statService.timeSpread());
    }

}
