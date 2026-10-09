package app.jobzy.api.adapter.out.persistence;

import app.jobzy.api.shared.gdpr.PersonalData;
import app.jobzy.api.shared.gdpr.ProcessData;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Shared audit-column base for all JPA entities: {@code createdAt}, {@code lastModifiedAt} and
 * {@code modifiedBy}. {@code createdAt} and {@code lastModifiedAt} are populated by Spring Data JPA
 * auditing (see {@code JpaAuditingConfig}), not by adapter code. {@code modifiedBy} is currently
 * unmanaged, since no auditor-aware provider exists yet in this codebase.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseJpaEntity {
  @ProcessData @CreatedDate private LocalDateTime createdAt;
  @ProcessData @LastModifiedDate private LocalDateTime lastModifiedAt;
  @PersonalData private String modifiedBy;
}
