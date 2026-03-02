package com.nhb.common.signature.aop;

import cn.hutool.core.collection.CollUtil;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.MapUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 16:37
 * @description
 */
@Slf4j
@Aspect
public class ApiSignatureAop {
    /**
     * 随机字符
     */
    public static final String NONCE = "nonce";

    /**
     * 签名（MD5）
     */
    public static final String SIGN = "sign";

    /**
     * 时间戳
     */
    public static final String TIMESTAMP = "timestamp";

    /**
     * 应用标识
     */
    public static final String APP_KEY = "appKey";

    /**
     * 应用密钥
     */
    public static final String APP_SECRET = "appSecret";

    /**
     * 获取请求参数
     * @param request  request
     * @return         参数键值对
     * @throws IOException 抛出异常
     */
    private static Map<String, String> getParameterMap(HttpServletRequest request) throws IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (CollUtil.isNotEmpty(parameterMap)) {
            return MapUtil.getParameterMap(parameterMap).toSingleValueMap();
        }
        byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
        return ArrayUtils.isEmpty(requestBody) ? Collections.emptyMap()
                : JacksonUtil.toMap(requestBody, String.class, String.class);
    }

    @Around("@annotation(com.nhb.common.signature.annotation.ApiSign)")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Assert.notNull(requestAttributes, "RequestAttributes not be null");
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String nonce = request.getHeader(NONCE);
        String timestamp = request.getHeader(TIMESTAMP);
        String sign = request.getHeader(SIGN);
        String appKey = request.getHeader(APP_KEY);
        String appSecret = request.getHeader(APP_SECRET);
        Map<String, String> parameterMap = getParameterMap(request);
        //TODO 处理参数
        return point.proceed();
    }

}
