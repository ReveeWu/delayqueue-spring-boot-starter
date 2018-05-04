package com.treefinance.api;

import com.treefinance.api.vo.Answer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author reveewu
 * @date 22/01/2018
 */
@ControllerAdvice
@Slf4j
public class AdviceController extends AbstractController {
    /**
     * 全局异常捕捉处理
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Answer<?> errorHandler(Exception ex) {
        log.error("controller error!", ex);
        return super.renderError("接口异常: "+ex.getMessage());
    }
}
