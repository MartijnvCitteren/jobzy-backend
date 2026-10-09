package app.jobzy.api.domain;

import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;

public abstract class BaseObject {
  private final LocalDateTime createdAt;
  private LocalDateTime lastModifiedAt;
  private @Nullable String modifiedBy;

  protected BaseObject(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    this.lastModifiedAt = createdAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getLastModifiedAt() {
    return lastModifiedAt;
  }

  public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
    this.lastModifiedAt = lastModifiedAt;
  }

  public @Nullable String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(@Nullable String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }
}
