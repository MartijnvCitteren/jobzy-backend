package app.jobzy.api.adapter.out.persistence.generation;

import app.jobzy.api.adapter.out.persistence.BaseJpaEntity;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.domain.generation.valueobject.GenerationStatus;
import app.jobzy.api.domain.vacancy.valueobject.Language;
import app.jobzy.api.shared.gdpr.PersonalData;
import app.jobzy.api.shared.gdpr.ProcessData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for {@code VacancyDescriptionGeneration}, flattened into one row.
 *
 * <p>{@code vacancyId} is a plain column, not an association: the generation only references the
 * vacancy and never loads it. The user's answers and the draft are personal data (free text about a
 * team routinely names people, and the draft can repeat it); the job title and language are copies
 * of vacancy fields, which are process data on the vacancy as well. The index on {@code
 * requestedAt} serves the retention clean-up.
 */
@Getter
@Setter
@Entity
@Table(
    name = "vacancy_description_generation",
    indexes = @Index(name = "ix_generation_requested_at", columnList = "requestedAt"))
public class VacancyDescriptionGenerationJpaEntity extends BaseJpaEntity {
  @Id private UUID id;

  @ProcessData
  @Column(nullable = false)
  private UUID vacancyId;

  @ProcessData
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GenerationStatus status;

  @ProcessData
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private GenerationFailureReason failureReason;

  @ProcessData
  @Column(nullable = false, length = 200)
  private String jobTitle;

  @ProcessData
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 2)
  private Language language;

  @PersonalData
  @Column(nullable = false, length = 1000)
  private String mostImportantTasks;

  @PersonalData
  @Column(nullable = false, length = 1000)
  private String team;

  @PersonalData
  @Column(nullable = false, length = 1000)
  private String whyNiceJob;

  @PersonalData
  @Column(length = 1000)
  private String draftSummary;

  @PersonalData
  @Column(length = 5000)
  private String draftJobDescription;

  @PersonalData
  @Column(length = 5000)
  private String draftTasks;

  @ProcessData
  @Column(length = 100)
  private String model;

  @ProcessData
  @Column(length = 100)
  private String promptVersion;

  @ProcessData
  @Column(nullable = false)
  private LocalDateTime requestedAt;

  @ProcessData private LocalDateTime completedAt;
}
