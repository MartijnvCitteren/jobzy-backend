package app.jobzy.domain.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class GenericEntity {

  @Setter(value = AccessLevel.NONE)
  LocalDateTime creationDate;
  LocalDateTime updateDate;

  @PrePersist
  protected void onCreate() {
    creationDate = LocalDateTime.now();
    updateDate = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updateDate = LocalDateTime.now();
  }
}
