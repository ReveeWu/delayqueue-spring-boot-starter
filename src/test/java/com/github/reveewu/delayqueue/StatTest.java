package com.github.reveewu.delayqueue;

import com.github.reveewu.delayqueue.monitor.StatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * @author reveewu
 * @date 20/06/2018
 */
@Slf4j
@SpringBootTest(classes = DemoApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class StatTest {
    @Autowired
    private StatService statService;

    @Test
    public void testSpread() throws Exception {
        System.out.println(statService.timeSpread());
    }

    @Test
    public void testListMetadata() throws Exception {
        System.out.println(statService.listMetadata(new Date()));
    }

    @Test
    public void testCountZset() throws Exception {
        System.out.println(statService.countZset(new Date()));
    }

    @Test
    public void testCountList() throws Exception {
        System.out.println(statService.countList("LIFECYCLE_TRIGGER"));
    }

    @Test
    public void testCountConsuming() throws Exception {
        System.out.println(statService.countConsuming());
    }
}
