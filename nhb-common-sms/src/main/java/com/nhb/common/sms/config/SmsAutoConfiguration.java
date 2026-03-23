package com.nhb.common.sms.config;

import com.nhb.common.redis.config.RedissonAutoConfiguration;
import com.nhb.common.sms.core.DefaultSmsDao;
import com.nhb.common.sms.handler.SmsExceptionHandler;
import org.dromara.sms4j.api.dao.SmsDao;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 15:25
 * @description:  配置文件请参考 <a href='https://sms4j.com/doc3/'/>
 */
@AutoConfigureAfter(RedissonAutoConfiguration.class)
public class SmsAutoConfiguration {

    @Bean
    @Primary
    public SmsDao smsDao() {
        return new DefaultSmsDao();
    }

    /**
     * 异常处理器
     */
    @Bean
    public SmsExceptionHandler smsExceptionHandler() {
        return new SmsExceptionHandler();
    }
}
