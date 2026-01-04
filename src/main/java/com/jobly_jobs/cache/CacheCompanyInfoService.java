package com.jobly_jobs.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jobly_jobs.domain.dto.agent.CompanyInfoAiResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CacheCompanyInfoService {

  private static final int MAX_SIZE = 60_000;
  private static final int EXPIRE_TIME_MINUTES = 60;
  private final Cache<UUID, CompanyInfoAiResponse> cache = Caffeine.newBuilder()
      .maximumSize(MAX_SIZE)
      .expireAfterWrite(Duration.ofMinutes(EXPIRE_TIME_MINUTES))
      .build();

  public void putCompanyInfo(UUID uuid, CompanyInfoAiResponse companyInfoAiResponse) {
    cache.put(uuid, companyInfoAiResponse);
  }

  public Optional<CompanyInfoAiResponse> getCompanyInfo(UUID uuid) {
    CompanyInfoAiResponse info = cache.getIfPresent(uuid);
    if (info == null) {
      return Optional.empty();
    }
    return Optional.of(info);
  }
}
