package com.nhb.common.core.config;

import com.nhb.common.core.manager.ManualTransactionManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/26 11:20
 * @description:
 */
@AutoConfiguration
@ConditionalOnBean({ PlatformTransactionManager.class })
public class TransactionTemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TransactionOperations.class)
    public ManualTransactionManager manualTransactionManager(PlatformTransactionManager transactionManager) {
        return new ManualTransactionManager(transactionManager);
    }
}
