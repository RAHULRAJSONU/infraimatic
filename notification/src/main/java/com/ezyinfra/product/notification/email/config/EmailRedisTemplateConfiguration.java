package com.ezyinfra.product.notification.email.config;

import com.ezyinfra.product.notification.email.model.EmailSchedulingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;

import static com.ezyinfra.product.notification.email.service.impl.ConditionalExpression.PERSISTENCE_IS_ENABLED_WITH_REDIS;

@Configuration
@ConditionalOnExpression(PERSISTENCE_IS_ENABLED_WITH_REDIS)
public class EmailRedisTemplateConfiguration {

    private final RedisConnectionFactory redisConnectionFactory;

    @Autowired
    public EmailRedisTemplateConfiguration(final RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Bean
    @Qualifier("orderingTemplate")
    public StringRedisTemplate createOrderingTemplate() throws IOException {
        StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory);
        template.setEnableTransactionSupport(true);
        return template;
    }

    @Bean
    @Qualifier("valueTemplate")
    public RedisTemplate<String, EmailSchedulingData> createValueTemplate() throws IOException {
        RedisTemplate<String, EmailSchedulingData> template = new RedisTemplate<>();
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jdkSerializationRedisSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.setConnectionFactory(redisConnectionFactory);
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

}
