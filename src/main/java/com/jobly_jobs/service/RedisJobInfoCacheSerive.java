package com.jobly_jobs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobly_jobs.domain.dto.AiCompanyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisJobInfoCacheSerive {
    private final RedisClient redisClient = new RedisClient.Builder().build();
    private final ObjectMapper objectMapper;

    public void put(UUID uuid, AiCompanyInfo aiCompanyInfo) {
        try {
            SetParams setParams = new SetParams();
            setParams.ex(3600L);
            String json = objectMapper.writeValueAsString(aiCompanyInfo);
            redisClient.set(uuid.toString(),json, setParams);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String companyWebsite, UUID uuid) {
            SetParams setParams = new SetParams();
            setParams.ex(3500L);
            redisClient.set(companyWebsite,uuid.toString(), setParams);

    }

    public Optional<AiCompanyInfo> getCompanyInfo(UUID uuid) {
        try{
            String json = redisClient.get(uuid.toString());
            if(!ObjectUtils.isEmpty(json)){
                return Optional.of(objectMapper.readValue(json, AiCompanyInfo.class));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }
    public Optional<UUID> getUUID(String companyWebsite) {
        String uuidAsString = redisClient.get(companyWebsite);
        if (!ObjectUtils.isEmpty(uuidAsString)) {
            return Optional.of(UUID.fromString(uuidAsString));
        }
        return Optional.empty();
    }

}
