package com.treefinance.delayqueue.dao.domain.test;

import java.io.Serializable;

public class Test implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private Long a;

    /**
     * 商户编号
     */
    private Long b;

    /**
     * 商户编号
     */
    private Long c;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getA() {
        return a;
    }

    public void setA(Long a) {
        this.a = a;
    }

    public Long getB() {
        return b;
    }

    public void setB(Long b) {
        this.b = b;
    }

    public Long getC() {
        return c;
    }

    public void setC(Long c) {
        this.c = c;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", a=").append(a);
        sb.append(", b=").append(b);
        sb.append(", c=").append(c);
        sb.append("]");
        return sb.toString();
    }
}