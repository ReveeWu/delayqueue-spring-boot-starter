package com.treefinance.delayqueue.dao.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ReveeWu on 2015/1/9.
 *
 * 添加对父类中被注解的字段的支持
 */
public abstract class SecuredBaseInterceptor {
  private final Logger LOGGER = LoggerFactory.getLogger(SecuredBaseInterceptor.class);

  public Field[] getDeclaredField(Object object) {
    List<Field> fields = new ArrayList<>();
    for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
      try {
        Field[] field = clazz.getDeclaredFields();
        if (null != field && field.length > 0) {
          fields.addAll(Arrays.asList(field));
        }
      } catch (Exception e) {
        LOGGER.error("获取bean字段列表出错", e);
      }
    }

    Field[] field = new Field[fields.size()];
    return fields.toArray(field);
  }

  protected Integer getAlgorithm(String encryptStr) {
    Integer algorithmCode = 1;
    if (encryptStr.contains("$")) {
      String[] datas = encryptStr.split("\\$");
      if (3 != datas.length) {
        LOGGER.error("encryptStr is inValid");
        return -1;
      }
      algorithmCode = Integer.valueOf(datas[1]);
    }
    return algorithmCode;
  }
}
