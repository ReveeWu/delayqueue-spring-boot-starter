package com.treefinance.delayqueue.dao.interceptors;

import com.datatrees.common.security.client.util.CryptAlgorithm;
import com.datatrees.commons.annotation.Encrypted;
import com.treefinance.delayqueue.common.utils.EncryptUtil;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;


/**
 * Created by Huanglizhou
 *
 * 解密字段
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets",
    args = {Statement.class})})
public class SecuredQueryInterceptor extends SecuredBaseInterceptor implements Interceptor {
  private final Logger logger = LoggerFactory.getLogger(SecuredQueryInterceptor.class);

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Object result = invocation.proceed();

    if (result instanceof List) {
      List<?> resultList = (List<?>) result;
      for (Object o : resultList) {
        // 不处理jdk中的类型
        if (null == o || o.getClass().getName().startsWith("java"))
          continue;

        Field[] fields = getDeclaredField(o);// o.getClass().getDeclaredFields();
        for (Field field : fields) {
          Encrypted encrypted = field.getAnnotation(Encrypted.class);
          if (encrypted == null)
            continue;

          field.setAccessible(true);
          Object val = field.get(o);
          if (val instanceof String) {
            int algorithmCode = this.getAlgorithm((String) val);
            if (algorithmCode >= 0) {
              CryptAlgorithm algorithm = CryptAlgorithm.getEnumByCode(algorithmCode);
              field.set(o, EncryptUtil.decrypt((String) val, algorithm));
            }
          }
        }
      }

      return result;
    } else {
      logger.warn("unexpected result type for SecuredQueryInterceptor: {}", result.getClass());
    }

    return result;
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {

  }
}
