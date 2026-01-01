package com.jobly_jobs.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;

@Configuration
public class RedisConfig {

    @Bean
    public RedisClient redisClient() {
        return RedisClient.builder().build();
    }
}
