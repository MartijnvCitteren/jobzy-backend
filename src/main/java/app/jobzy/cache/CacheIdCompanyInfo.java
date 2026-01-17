package app.jobzy.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CacheIdCompanyInfo {

  private static final int MAX_SIZE = 60_000;
  private static final int EXPIRE_TIME_MINUTES = 59;
  private final Cache<String, UUID> cache = Caffeine.newBuilder()
      .maximumSize(MAX_SIZE)
      .expireAfterWrite(Duration.ofMinutes(EXPIRE_TIME_MINUTES))
      .build();

  public void putCompanyWebsite(String website, UUID companyInfoId) {
    cache.put(website, companyInfoId);
  }

  public Optional<UUID> getUuid(String website) {
    UUID uuid = cache.getIfPresent(website);
    if (uuid == null) {
      return Optional.empty();
    }
    return Optional.of(uuid);
  }
}
