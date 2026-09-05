package app.jobzy.api.domain;

import app.jobzy.api.shared.Constants;
import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class BaseObject {
  private final LocalDateTime createdAt;
  private LocalDateTime lastModifiedAt;
  private String modifiedBy;

  protected BaseObject() {
    var time = LocalDateTime.now(ZoneId.of(Constants.AMS_TIME_ZONE_ID));
    this.createdAt = time;
    this.lastModifiedAt = time;
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
