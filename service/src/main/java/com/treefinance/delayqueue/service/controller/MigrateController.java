package com.treefinance.delayqueue.service.controller;

import com.treefinance.delayqueue.service.MigrateService;
import com.treefinance.delayqueue.service.controller.vo.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author reveewu
 * @date 31/05/2018
 */
@RestController
@RequestMapping("/migrate")
public class MigrateController extends AbstractController {
    @Autowired
    private MigrateService migrateService;

    @RequestMapping("/do")
    public Answer<?> doMigrate(@RequestParam("key") String key, @RequestParam("groupName") String groupName, @RequestParam("topic") String topic) {
        migrateService.migrate(key, groupName, topic);
        return super.renderOK();
    }
}
