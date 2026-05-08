package com.nhb.common.websocket.holder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 16:33
 * @description: WebSocketSession 用于保存当前实例中用户会话
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketSessionHolder {
    private static final Map<Long, WebSocketSession> USER_SESSION = new ConcurrentHashMap<>();

    /**
     * 将WebSocket会话添加到用户会话Map中
     *
     * @param userId     用户ID
     * @param session    要添加的WebSocket会话
     */
    public static void addSession(Long userId, WebSocketSession session) {
        removeSession(userId);
        USER_SESSION.put(userId, session);
    }

    /**
     * 从用户会话Map中移除指定会话键对应的WebSocket会话
     *
     * @param userId 用户ID
     */
    public static void removeSession(Long userId) {
        WebSocketSession session = USER_SESSION.remove(userId);
        if (Objects.isNull(session)) {
            return;
        }
        try {
            session.close(CloseStatus.BAD_DATA);
        } catch (Exception ignored) {
            log.info("WebSocket Session Close Exception:{}",ignored.getMessage(),ignored);
        }
    }

    /**
     * 根据会话键从用户会话Map中获取WebSocket会话
     *
     * @param userId 用户ID
     * @return 与给定会话键对应的WebSocket会话，如果不存在则返回null
     */
    public static WebSocketSession getSession(Long userId) {
        return USER_SESSION.get(userId);
    }

    /**
     * 获取存储在用户会话Map中所有WebSocket会话的会话键集合
     *
     * @return 所有WebSocket会话的会话键集合
     */
    public static Set<Long> getSessionsAll() {
        return USER_SESSION.keySet();
    }

    /**
     * 检查给定的会话键是否存在于用户会话Map中
     *
     * @param userId 要检查的会话键
     * @return 如果存在对应的会话键，则返回true；否则返回false
     */
    public static Boolean existSession(Long userId) {
        return USER_SESSION.containsKey(userId);
    }
}
