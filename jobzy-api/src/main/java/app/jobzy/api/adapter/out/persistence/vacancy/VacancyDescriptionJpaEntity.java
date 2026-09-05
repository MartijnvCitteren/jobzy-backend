package app.jobzy.api.adapter.out.persistence.vacancy;

import app.jobzy.api.domain.vacancy.valueobject.VacancyDescriptionSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for vacancy descriptions. 1:1 relationship with {@code Vacancy} via unique {@code
 * vacancyId} FK, but no JPA {@code @OneToOne} annotation to avoid lazy-loading proxy surprises.
 * Managed by {@code VacancyRepositoryAdapter} as part of the aggregate load/save.
 */
@Getter
@Setter
@Entity
public class VacancyDescriptionJpaEntity {
  @Id private UUID id;

  @Column(unique = true)
  private UUID vacancyId;

  private String summary;
  private String jobDescription;
  private String tasks;
  private String whatWeOffer;
  private String aboutUs;
  private VacancyDescriptionSource source;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
