package com.treefinance.delayqueue.dao.interceptors;

import com.datatrees.common.security.client.util.CryptAlgorithm;
import com.datatrees.commons.annotation.Encrypted;
import com.treefinance.delayqueue.common.utils.EncryptUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Created by Hf.Pan
 * 
 * 数据加解密的拦截器
 */
@Intercepts({@Signature(type = Executor.class, method = "update",
    args = {MappedStatement.class, Object.class})})
public class SecuredUpdateInterceptor extends SecuredBaseInterceptor implements Interceptor {
  private final Logger logger = LoggerFactory.getLogger(SecuredUpdateInterceptor.class);

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Object[] args = invocation.getArgs();

    List<OriginValueHolder> originValueHolderList = doEncrypt(args);
    try {
      return invocation.proceed();
    } finally {
      // 恢复被加密过的字段
      if (!originValueHolderList.isEmpty()) {
        for (OriginValueHolder holder : originValueHolderList) {
          holder.update();
        }
      }
    }
  }

  private List<OriginValueHolder> doEncrypt(Object[] args) throws IllegalAccessException {
    List<OriginValueHolder> originValueHolderList = new ArrayList<>(args.length);

    for (Object arg : args) {
      if (arg == null || arg instanceof MappedStatement || arg.getClass().getName().startsWith("java"))
        continue;

      Field[] fields = getDeclaredField(arg);// arg.getClass().getDeclaredFields();
      for (Field field : fields) {
        Encrypted encrypted = field.getAnnotation(Encrypted.class);
        if (encrypted == null)
          continue;

        field.setAccessible(true);
        Object val = field.get(arg);
        if (val instanceof String && !StringUtils.isEmpty((String) val)) {
          originValueHolderList.add(new OriginValueHolder(arg, field, val));
          try {
            if (encrypted.value().equals(CryptAlgorithm.AES)) {
              field.set(arg, EncryptUtil.encrypt((String) val, CryptAlgorithm.AES));
            } else if (encrypted.value().equals(CryptAlgorithm.RSA)) {
              field.set(arg, EncryptUtil.encrypt((String) val, CryptAlgorithm.RSA));
            }
          } catch (Exception e) {
            logger.warn("unexpected result type for SecuredUpdateInterceptor: {}");
          }
        }
      }
    }

    return originValueHolderList;
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {}

  public static final class OriginValueHolder {
    private final Object target;
    private final Field field;
    private final Object value;

    public OriginValueHolder(Object target, Field field, Object value) {
      this.target = target;
      this.field = field;
      this.value = value;
    }

    public void update() throws IllegalAccessException {
      field.set(target, value);
    }
  }
}
