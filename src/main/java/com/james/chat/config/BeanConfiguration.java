package com.james.chat.config;

import com.james.chat.redis.RedisService;
import com.james.chat.util.ApplicationChannelTracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    final RedisService redisService;

    public BeanConfiguration(RedisService redisService) {
        this.redisService = redisService;
    }

    @Bean
    public ApplicationChannelTracer applicationChannelTracer() {
        ApplicationChannelTracer tracer = new ApplicationChannelTracer();
        tracer.setRedisService(redisService);
        return tracer;
    }
}
