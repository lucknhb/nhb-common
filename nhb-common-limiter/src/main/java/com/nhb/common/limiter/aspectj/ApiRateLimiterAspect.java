package com.nhb.common.limiter.aspectj;

import com.nhb.common.core.constant.GlobalConstants;
import com.nhb.common.core.exception.ServiceException;
import com.nhb.common.core.utils.I18MessageUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.limiter.annotation.ApiRateLimiter;
import com.nhb.common.redis.utils.RedissonUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 10:32
 * @description: 限流处理
 */
@Slf4j
@Aspect
public class ApiRateLimiterAspect {
    /**
     * 定义spel表达式解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();
    /**
     * 定义spel解析模版
     */
    private final ParserContext parserContext = new TemplateParserContext();
    /**
     * 方法参数解析器
     */
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();


    @Before("@annotation(apiRateLimiter)")
    public void doBefore(JoinPoint point, ApiRateLimiter apiRateLimiter) {
        int time = apiRateLimiter.time();
        int count = apiRateLimiter.count();
        int timeout = apiRateLimiter.timeOut();
        try {
            String combineKey = getCombineKey(apiRateLimiter, point);
            long number = RedissonUtil.rateLimiter(combineKey, apiRateLimiter.rateType(), count, time, timeout);
            if (number == -1) {
                String message = apiRateLimiter.message();
                if (StringUtils.startsWith(message, "{") && StringUtils.endsWith(message, "}")) {
                    message = I18MessageUtil.message(StringUtils.substring(message, 1, message.length() - 1));
                }
                throw new ServiceException(message);
            }
            log.info("Limiter number => {}, remaining number => {}, Cache key => '{}'", count, number, combineKey);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            } else {
                throw new RuntimeException("限流异常，请稍候再试", e);
            }
        }
    }

    private String getCombineKey(ApiRateLimiter apiRateLimiter, JoinPoint point) {
        String key = apiRateLimiter.key();
        // 判断 key 不为空 和 不是表达式
        if (StringUtils.isNotBlank(key) && StringUtils.containsAny(key, "#")) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method targetMethod = signature.getMethod();
            Object[] args = point.getArgs();
            MethodBasedEvaluationContext context =
                    new MethodBasedEvaluationContext(null, targetMethod, args, pnd);
            context.setBeanResolver(new BeanFactoryResolver(SpringContextUtil.getBeanFactory()));
            Expression expression;
            if (StringUtils.startsWith(key, parserContext.getExpressionPrefix())
                    && StringUtils.endsWith(key, parserContext.getExpressionSuffix())) {
                expression = parser.parseExpression(key, parserContext);
            } else {
                expression = parser.parseExpression(key);
            }
            key = expression.getValue(context, String.class);
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Assert.notNull(requestAttributes, "RequestAttributes Not Be Null");
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        return GlobalConstants.RATE_LIMIT_KEY + Objects.requireNonNull(httpServletRequest).getRequestURI() + ":" +
                apiRateLimiter.limitType().resolve(httpServletRequest) + ":" +
                key;
    }
}
