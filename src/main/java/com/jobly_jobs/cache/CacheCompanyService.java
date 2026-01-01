package com.jobly_jobs.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobly_jobs.domain.dto.AiCompanyInfo;
import com.jobly_jobs.exceptions.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class CacheCompanyService {
    private final RedisClient redisClient;
    private final ObjectMapper objectMapper;
    private static final long EXPIRE_COMPANY_INFO_SECONDS = 3600;


    public void put(UUID uuid, AiCompanyInfo aiCompanyInfo) {
        try {
            SetParams setParams = setExpirationTimeInSeconds(EXPIRE_COMPANY_INFO_SECONDS);
            String json = objectMapper.writeValueAsString(aiCompanyInfo);
            redisClient.set(uuid.toString(),json, setParams);
        } catch (JsonProcessingException e) {
            log.error("Error serializing AiCompanyInfo", e);
            throw new SystemException(e.getMessage());
        }
    }

    public void put(String companyWebsite, UUID uuid) {
            SetParams setParams = setExpirationTimeInSeconds(EXPIRE_COMPANY_INFO_SECONDS-30);
            redisClient.set(companyWebsite,uuid.toString(), setParams);
    }

    public Optional<AiCompanyInfo> getCompanyInfo(UUID uuid) {
        try{
            String json = redisClient.get(uuid.toString());
            if(ObjectUtils.isEmpty(json)){
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, AiCompanyInfo.class));

        } catch (JsonProcessingException e) {
            log.error("Error deserializing AiCompanyInfo", e);
            throw new SystemException(e.getMessage());
        }
    }

    public Optional<UUID> getUUID(String companyWebsite) {
        String uuidAsString = redisClient.get(companyWebsite);
        if (ObjectUtils.isEmpty(uuidAsString)) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(uuidAsString));
    }

    private SetParams setExpirationTimeInSeconds(long seconds) {
        SetParams setParams = new SetParams();
        setParams.ex(seconds);
        return setParams;
    }
}
