package com.nhb.common.encrypt.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.encrypt.annotation.ApiEncrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/8 16:50
 * @description: 扫描项目中使用了@ApiEncrypt注解的方法 仅允许 POST/PUT方式使用
 */
@Slf4j
public class ApiEncryptCheckListener implements ApplicationListener<ApplicationReadyEvent> {
    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //获取该项目中所有的controller
        RequestMappingHandlerMapping handlerMapping = SpringContextUtil.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        Set<String> urls = new HashSet<>();
        List<String> methods = List.of(RequestMethod.POST.name(), RequestMethod.PUT.name());
        handlerMethods.forEach((mapping, handlerMethod) -> {
            ApiEncrypt apiEncrypt = handlerMethod.getMethodAnnotation(ApiEncrypt.class);
            Set<RequestMethod> requestMethods = mapping.getMethodsCondition().getMethods();
            List<String> methodValues = requestMethods.stream().map(RequestMethod::name).collect(Collectors.toList());
            methodValues.removeAll(methods);
            //请求的参数仅PUT/POST允许加密
            if (apiEncrypt != null && apiEncrypt.request() && !methodValues.isEmpty()) {
                urls.add(handlerMethod.toString());
            }
        });
        Assert.isTrue(CollUtil.isEmpty(urls), StrUtil.format("请求参数加密仅支持{}方式,以下资源存在该问题[{}]", String.join("/", methods), String.join("/", urls)));


    }
}
