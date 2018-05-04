package com.treefinance.api;

import com.treefinance.api.vo.Answer;
import com.treefinance.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author reveewu
 * @date 24/02/2018
 */
@RestController
@RequestMapping(value = "/demo")
public class DemoController extends AbstractController {
    @Autowired
    private DemoService demoService;

    @RequestMapping(value = "/")
    public Answer<?> index() {
        return renderAnswer(demoService.listAllTest());
    }
}
