package com.nhb.common.signature.core;

import com.nhb.common.signature.utils.RequestBodyHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;

import java.io.IOException;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/14 15:18
 * @description:
 */
public class ApiSignFilter implements Filter, Ordered {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            try {
                // 用包装类替换原始 request
                ApiSignHttpServletRequestWrapper wrappedRequest = new ApiSignHttpServletRequestWrapper(httpServletRequest);
                RequestBodyHolder.setBody(wrappedRequest.getBody());
                chain.doFilter(wrappedRequest, response);
            }finally {
                RequestBodyHolder.clear();
            }
        } else {
            chain.doFilter(request, response);
        }

    }

    /**
     * 该顺序需要在 {@link com.nhb.common.encrypt.filter.EncryptFilter} 之前处理 获取未解密前的数据
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
