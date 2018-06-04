package com.treefinance.delayqueue.service.controller.vo;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by reveewu on 28/06/2017.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Answer<T> {
    private Integer code;
    private String msg;
    private T result;

    public Answer() {
        code = 0;
    }

    @SuppressWarnings("rawtypes")
    public static Answer<?> newBuilder() {
        return new Answer();
    }

    public T getResult() {
        return result;
    }

    public Answer<T> setResult(T result) {
        this.result = result;
        return this;
    }

    public Integer getCode() {
        return code;
    }

    public Answer<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Answer<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Answer<T> setCodeAndMsg(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }
}
