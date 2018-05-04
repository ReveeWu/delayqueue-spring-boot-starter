package com.treefinance.delayqueue.api;


import com.treefinance.delayqueue.api.vo.Answer;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;

public abstract class AbstractController {

    public HashMap<String, String> parseRequestParameter(HttpServletRequest request) {
        HashMap<String, String> ret = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            ret.put(name, request.getParameter(name));
        }

        return ret;
    }

    public Answer<?> renderError(String msg) {
        Answer<Object> answer = new Answer<>();
        answer.setCode(1);
        answer.setMsg(msg);
        return answer;
    }

    public Answer<?> renderAnswer(Object result) {
        Answer<Object> answer = new Answer<>();
        answer.setCode(0);
        answer.setMsg("操作完成");
        answer.setResult(result);
        return answer;
    }

    public Answer<?> renderDefaultAnswer() {
        Answer<Object> answer = new Answer<>();
        answer.setCode(0);
        answer.setMsg("操作完成");
        return answer;
    }

    public Answer<?> renderOK() {
        Answer<Object> answer = new Answer<>();
        answer.setCode(0);
        answer.setMsg("操作完成");
        answer.setResult("OK");
        return answer;
    }
}
