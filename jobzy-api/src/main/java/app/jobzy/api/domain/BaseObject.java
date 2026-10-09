package app.jobzy.api.domain;

import java.time.LocalDateTime;

public abstract class BaseObject {
  private final LocalDateTime createdAt;
  private LocalDateTime lastModifiedAt;
  private String modifiedBy;

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

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }
}
