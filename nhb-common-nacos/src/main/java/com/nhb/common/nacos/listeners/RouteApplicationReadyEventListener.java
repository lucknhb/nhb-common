package com.nhb.common.nacos.listeners;

import com.alibaba.cloud.nacos.utils.StringUtils;
import com.nhb.common.core.utils.FreeMarkerTemplateUtil;
import com.nhb.common.core.utils.ResourceFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/6 9:42
 * @description: 路由模板生成
 */
@Slf4j
@RequiredArgsConstructor
public class RouteApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment environment;

    public static final String APPLICATION_NAME = "spring.application.name";

    public static final String DEFAULT_SERVICE_ID = "application";

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            String serviceId = environment.getProperty(APPLICATION_NAME, DEFAULT_SERVICE_ID);
            //当获取不到数据时 不进行模板生成
            if (StringUtils.isEmpty(serviceId)) {
                return;
            }
            Map<String, Object> map = HashMap.newHashMap(2);
            map.put("serviceId", serviceId);
            String router = getRouter(map);
            log.info("""
                    \n----------首次启动服务请复制以下配置到router.json----------
                    {}
                   """, router);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取模板解析路由配置.
     *
     * @param dataMap map对象
     * @return 路由配置
     */
    private String getRouter(Map<String, Object> dataMap) throws IOException {
        String template = ResourceFileUtil.getResource("templates/routeConfigTemplate.json")
                .getContentAsString(StandardCharsets.UTF_8)
                .trim();
        return FreeMarkerTemplateUtil.getContent(template, dataMap);
    }

}
