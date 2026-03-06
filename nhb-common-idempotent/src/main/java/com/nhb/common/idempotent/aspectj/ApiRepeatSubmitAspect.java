package com.nhb.common.idempotent.aspectj;

import cn.dev33.satoken.SaManager;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.nhb.common.core.constant.GlobalConstants;
import com.nhb.common.core.domain.ResultMessage;
import com.nhb.common.core.exception.ServiceException;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.I18MessageUtil;
import com.nhb.common.core.utils.ServletUtil;
import com.nhb.common.idempotent.annotation.ApiRepeatSubmit;
import com.nhb.common.redis.utils.RedissonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 13:54
 * @description: 防止重复提交
 */
@Slf4j
@Aspect
public class ApiRepeatSubmitAspect {
    private static final ThreadLocal<String> KEY_CACHE = new ThreadLocal<>();

    @Before("@annotation(apiRepeatSubmit)")
    public void doBefore(JoinPoint point, ApiRepeatSubmit apiRepeatSubmit) {
        // 如果注解不为0 则使用注解数值
        long interval = apiRepeatSubmit.timeUnit().toMillis(apiRepeatSubmit.interval());
        if (interval < 1000) {
            throw new ServiceException("重复提交间隔时间不能小于'1'秒");
        }
        HttpServletRequest request = ServletUtil.getRequest();
        String nowParams = argsArrayToString(point.getArgs());
        // 请求地址（作为存放cache的key值）
        String url = Objects.requireNonNull(request).getRequestURI();
        // 唯一值（没有消息头则使用请求地址）
        String submitKey = StringUtils.trimToEmpty(request.getHeader(SaManager.getConfig().getTokenName()));
        submitKey = SecureUtil.md5(submitKey + ":" + nowParams);
        // 唯一标识（指定key + url + 消息头）
        String cacheRepeatKey = GlobalConstants.REPEAT_SUBMIT_KEY + url + submitKey;
        if (RedissonUtil.setObjectIfAbsent(cacheRepeatKey, "", Duration.of(interval, apiRepeatSubmit.timeUnit().toChronoUnit()))) {
            KEY_CACHE.set(cacheRepeatKey);
        } else {
            String message = apiRepeatSubmit.message();
            if (StringUtils.startsWith(message, "{") && StringUtils.endsWith(message, "}")) {
                message = I18MessageUtil.message(StringUtils.substring(message, 1, message.length() - 1));
            }
            throw new ServiceException(message);
        }
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(apiRepeatSubmit)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, ApiRepeatSubmit apiRepeatSubmit, Object result) {
        if (result instanceof ResultMessage<?> r) {
            try {
                // 成功则不删除redis数据 保证在有效时间内无法重复提交
                if (r.getCode() == ResultMessage.SUCCESS) {
                    return;
                }
                RedissonUtil.deleteObject(KEY_CACHE.get());
            } finally {
                KEY_CACHE.remove();
            }
        }
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param exception 异常
     */
    @AfterThrowing(value = "@annotation(apiRepeatSubmit)", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, ApiRepeatSubmit apiRepeatSubmit, Exception exception) {
        RedissonUtil.deleteObject(KEY_CACHE.get());
        KEY_CACHE.remove();
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringJoiner params = new StringJoiner(" ");
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }
        for (Object o : paramsArray) {
            if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                params.add(JacksonUtil.toJsonString(o));
            }
        }
        return params.toString();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile
                || o instanceof HttpServletRequest
                || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
