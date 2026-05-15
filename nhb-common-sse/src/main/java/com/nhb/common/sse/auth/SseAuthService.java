package com.nhb.common.sse.auth;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/12 17:03
 * @description: SSE连接认证
 */
public interface SseAuthService {
    /**
     * @param channel Netty通道
     * @param request HTTP升级请求
     * @return 用户唯一标识，返回null表示认证失败（应在此方法内关闭连接）
     */
    Long authenticate(Channel channel, FullHttpRequest request);

}
