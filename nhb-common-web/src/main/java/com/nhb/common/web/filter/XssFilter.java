package com.nhb.common.web.filter;

import cn.hutool.core.util.StrUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.web.properties.XssProperties;
import com.nhb.common.web.wrapper.XssHttpServletRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 16:04
 * @description: 防止XSS攻击的过滤器
 */
public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp)) {
            chain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }
        XssProperties properties = SpringContextUtil.getBean(XssProperties.class);
        String prefix = StrUtil.blankToDefault(request.getHeader("X-Forwarded-Prefix"), "");
        // 从请求头获取gateway转发的服务前缀
        List<String> excludeUrls = properties.getExcludeUrls().stream()
                .filter(x -> StringUtils.startsWith(x, prefix))
                .map(x -> x.replaceFirst(prefix, StringUtils.EMPTY))
                .toList();
        return StringUtil.matches(url, excludeUrls);
    }

    @Override
    public void destroy() {

    }
}
