package com.treefinance;

import com.treefinance.delayqueue.client.DelayQueueService;
import com.treefinance.delayqueue.client.bean.DelayMessage;
import com.treefinance.delayqueue.client.enums.ConsumeStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executors;

/**
 * @author reveewu
 * @date 06/05/2018
 */
@Slf4j
public class DelayQueueServiceTest extends BaseTest {
    @Autowired
    private DelayQueueService delayQueueService;

    private String groupName = "test";
    private String topic = "test";

    @Test
    public void testPush() throws Exception {
        Executors.newSingleThreadExecutor().execute(()->{
            for (int i=0;i<1000;i++) {
                Integer userId = 1000000000+i;
                DelayMessage delayMessage = new DelayMessage(groupName, topic, userId.toString(), userId.toString(), 30000);
                delayQueueService.push(delayMessage);
            }
        });

        Executors.newSingleThreadExecutor().execute(()->{
            for(;;)
                delayQueueService.consume(groupName, topic, (messageExt -> {
                    log.info("{}  {}", messageExt.getId(), messageExt.toString());
                    long x = System.currentTimeMillis()%3;
                    if (x==0)
                        return ConsumeStatus.SUCCESS;
                    if (x==1)
                        return ConsumeStatus.RECONSUME;
                    if (x==2)
                        return ConsumeStatus.FAIL;
                    return ConsumeStatus.SUCCESS;
                }),log);
        });

        Executors.newSingleThreadExecutor().execute(()->{
            for (int i=0;i<1000;i++) {
                Integer userId = 1000000000+i;
                DelayMessage delayMessage = new DelayMessage(groupName, "test2", userId.toString(), userId.toString(), 30000);
                delayQueueService.push(delayMessage);
            }
        });

        Executors.newSingleThreadExecutor().execute(()->{
            for(;;)
                delayQueueService.consume(groupName, "test2", (messageExt -> {
                    log.info("{}  {}", messageExt.getId(), messageExt.toString());
                    long x = System.currentTimeMillis()%3;
                    if (x==0)
                        return ConsumeStatus.SUCCESS;
                    if (x==1)
                        return ConsumeStatus.RECONSUME;
                    if (x==2)
                        return ConsumeStatus.FAIL;
                    return ConsumeStatus.SUCCESS;
                }),log);
        });



        for(;;)
        Thread.sleep(100);
    }

    @Test
    public void testConsume() throws Exception {

    }
}
