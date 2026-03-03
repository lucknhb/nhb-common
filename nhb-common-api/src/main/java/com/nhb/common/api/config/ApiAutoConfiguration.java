package com.nhb.common.api.config;

import com.nhb.common.api.handler.OpenApiHandler;
import com.nhb.common.api.properties.ApiProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.SecurityService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/1 23:00
 * @description
 */
@RequiredArgsConstructor
@AutoConfigureBefore(SpringDocConfigProperties.class)
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnBooleanProperty(prefix = ApiProperties.PREFIX, name = "api-docs.enabled", havingValue = true, matchIfMissing = true)
public class ApiAutoConfiguration {
    private final ServerProperties serverProperties;

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI openApi(ApiProperties properties) {
        OpenAPI openApi = new OpenAPI();
        // 文档基本信息
        ApiProperties.InfoProperties infoProperties = properties.getInfo();
        Info info = convertInfo(infoProperties);
        openApi.info(info);
        // 扩展文档信息
        openApi.externalDocs(properties.getExternalDocs());
        openApi.tags(properties.getTags());
        openApi.paths(properties.getPaths());
        if (properties.getComponents() != null) {
            openApi.components(properties.getComponents());
            Set<String> keySet = properties.getComponents().getSecuritySchemes().keySet();
            List<SecurityRequirement> list = new ArrayList<>();
            SecurityRequirement securityRequirement = new SecurityRequirement();
            keySet.forEach(securityRequirement::addList);
            list.add(securityRequirement);
            openApi.security(list);
        }
        return openApi;
    }

    private Info convertInfo(ApiProperties.InfoProperties infoProperties) {
        Info info = new Info();
        info.setTitle(infoProperties.getTitle());
        info.setDescription(infoProperties.getDescription());
        info.setContact(infoProperties.getContact());
        info.setLicense(infoProperties.getLicense());
        info.setVersion(infoProperties.getVersion());
        return info;
    }

    /**
     * 自定义 openApi 处理器
     */
    @Bean
    public OpenAPIService openApiBuilder(Optional<OpenAPI> openApi,
                                         SecurityService securityParser,
                                         SpringDocConfigProperties springDocConfigProperties,
                                         PropertyResolverUtils propertyResolverUtils,
                                         Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomisers,
                                         Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomisers,
                                         Optional<JavadocProvider> javadocProvider) {
        return new OpenApiHandler(openApi, securityParser, springDocConfigProperties, propertyResolverUtils, openApiBuilderCustomisers, serverBaseUrlCustomisers, javadocProvider);
    }

    /**
     * 对已经生成好的 OpenApi 进行自定义操作
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer(ApiProperties properties) {
        String finalContextPath;
        //判断是否是单体应用 既无微服务架构时的api获取路径
        if (Boolean.TRUE.equals(properties.getSingleFlag())){
            String contextPath = serverProperties.getServlet().getContextPath();
            if (StringUtils.isBlank(contextPath) || "/".equals(contextPath)) {
                finalContextPath = "";
            } else {
                finalContextPath = contextPath;
            }
        }else {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            // 从请求头获取gateway转发的服务前缀
            finalContextPath = StringUtils.defaultIfBlank(request.getHeader("X-Forwarded-Prefix"), "");
        }
        return openApi -> {
            Paths oldPaths = openApi.getPaths();
            if (oldPaths instanceof PlusPaths) {
                return;
            }
            PlusPaths newPaths = new PlusPaths();
            oldPaths.forEach((k, v) -> newPaths.addPathItem(finalContextPath + k, v));
            openApi.setPaths(newPaths);
        };
    }

    /**
     * 单独使用一个类便于判断 解决springdoc路径拼接重复问题
     */
    static class PlusPaths extends Paths {
        public PlusPaths() {
            super();
        }
    }
}
