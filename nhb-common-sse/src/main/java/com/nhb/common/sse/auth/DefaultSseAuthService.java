package com.nhb.common.sse.auth;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.nhb.common.core.utils.StringUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/13 9:03
 * @description: 支持请求头认证/参数认证  优先从请求头获取
 */
@Slf4j
public class DefaultSseAuthService implements SseAuthService{
    /**
     * @param channel Netty通道
     * @param request HTTP升级请求
     * @return 用户唯一标识，返回null表示认证失败（应在此方法内关闭连接）
     */
    @Override
    public Long authenticate(Channel channel, FullHttpRequest request) {
        try {
            // 优先自动获取登录信息
            String token = request.headers().get(HttpHeaderNames.AUTHORIZATION);
            if (StringUtil.isNotBlank(token)) {
                String tokenPrefix = SaManager.getConfig().getTokenPrefix();
                token = token.replaceAll(tokenPrefix, "").trim();
                return Long.valueOf(StpUtil.getLoginIdByToken(token).toString());
            } else {
                //从请求参数中获取
                QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
                Map<String, List<String>> params = decoder.parameters();
                //仅获取第一个参数值
                List<String> uids = params.get("token");
                if (CollUtil.isNotEmpty(uids)) {
                    return Long.valueOf(StpUtil.getLoginIdByToken(uids.getFirst()).toString());
                }
            }
        } catch (Exception e) {
            log.error("Not Login User To Connect SSE Server", e);
            return null;
        }
        log.error("Not Login User To Connect SSE Server");
        // 认证失败
        return null;
    }
}
