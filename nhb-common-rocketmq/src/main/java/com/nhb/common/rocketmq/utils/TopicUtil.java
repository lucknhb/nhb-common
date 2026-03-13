package com.nhb.common.rocketmq.utils;

import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.rocketmq.properties.RocketMQConfigProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 16:44
 * @description: topic工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TopicUtil {

    /**
     * 如果开启了使用根据项目的环境来区分 则为topic加上后缀
     * @param topic  原始topic
     * @return       处理后的topic
     */
    public static String topicSuffix(String topic) {
        RocketMQConfigProperties properties = SpringContextUtil.getBean(RocketMQConfigProperties.class);
        Boolean flag = properties.getProfileEnabled();
        if (flag) {
            Environment environment = SpringContextUtil.getBean(Environment.class);
            String profile = environment.getProperty("spring.profiles.active");
            return topic + "_"+ profile;
        }
        return topic;
    }
}
