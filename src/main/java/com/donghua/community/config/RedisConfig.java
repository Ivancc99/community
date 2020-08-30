package com.donghua.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate (RedisConnectionFactory factory) {
        // 数据库的连接是由连接工厂创建的，所以要将工厂注入给ｔｅｍｐｌａｔｅ
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        //　设置ｋｅｙ的序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // 设置ｖａｌｕｅ的序列化方式
        redisTemplate.setValueSerializer(RedisSerializer.json());
        // 设置ｈａｓｈ的ｋｅｙ的序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        //　设置ｈａｓｈ的ｖａｌｕｅ的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
