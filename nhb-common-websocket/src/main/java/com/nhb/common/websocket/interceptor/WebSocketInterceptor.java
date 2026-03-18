package com.nhb.common.websocket.interceptor;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.nhb.common.websocket.constant.WebSocketConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 8:58
 * @description: WebSocket握手请求的拦截器
 */
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {
    /**
     * WebSocket握手之前执行的前置处理方法
     *
     * @param request    WebSocket握手请求
     * @param response   WebSocket握手响应
     * @param wsHandler  WebSocket处理程序
     * @param attributes 与WebSocket会话关联的属性
     * @return 如果允许握手继续进行，则返回true；否则返回false
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            //先判断是否登录 未登录 则不允许连接
            if (StpUtil.isLogin()) {
                Long userId = (Long) StpUtil.getLoginId();
                attributes.put(WebSocketConstants.USER_ID, userId);
                return true;
            }
        } catch (NotLoginException e) {
            log.error("WebSocket Auth Fail'{}',System Resources Cannot Accessed", e.getMessage(),e);
        }
        return false;
    }

    /**
     * WebSocket握手成功后执行的后置处理方法
     *
     * @param request   WebSocket握手请求
     * @param response  WebSocket握手响应
     * @param wsHandler WebSocket处理程序
     * @param exception 握手过程中可能出现的异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 在这个方法中可以执行一些握手成功后的后续处理逻辑，比如记录日志或者其他操作
    }
}
