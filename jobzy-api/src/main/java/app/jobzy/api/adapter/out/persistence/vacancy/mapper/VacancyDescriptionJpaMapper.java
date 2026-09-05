package app.jobzy.api.adapter.out.persistence.vacancy.mapper;

import app.jobzy.api.adapter.out.persistence.vacancy.VacancyDescriptionJpaEntity;
import app.jobzy.api.domain.UuidV7Generator;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import java.time.LocalDateTime;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Maps between {@code VacancyDescription} domain VO and {@code VacancyDescriptionJpaEntity}. */
@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyDescriptionJpaMapper {

  /**
   * Map a JPA entity to domain value object.
   *
   * @param entity the persisted entity
   * @return the domain VO
   */
  VacancyDescription toDomain(VacancyDescriptionJpaEntity entity);

  /**
   * Map domain VO to JPA entity, performing upsert logic: reuse the existing entity's id and
   * createdAt if it exists (update case), or generate fresh ones if null (insert case). This
   * ensures multiple writes for the same vacancy update the same row, not accumulate duplicates.
   *
   * @param vacancyId the vacancy id to associate
   * @param description the domain VO
   * @param existing the existing entity if any, or null for insert
   * @return the entity ready to persist
   */
  default VacancyDescriptionJpaEntity toJpaEntity(
      UUID vacancyId,
      VacancyDescription description,
      @Nullable VacancyDescriptionJpaEntity existing) {
    var entity = existing != null ? existing : new VacancyDescriptionJpaEntity();
    if (existing == null) {
      entity.setId(UuidV7Generator.getUUID());
      entity.setCreatedAt(LocalDateTime.now());
    }
    entity.setVacancyId(vacancyId);
    entity.setSummary(description.summary());
    entity.setJobDescription(description.jobDescription());
    entity.setTasks(description.tasks());
    entity.setWhatWeOffer(description.whatWeOffer());
    entity.setAboutUs(description.aboutUs());
    entity.setSource(description.source());
    entity.setUpdatedAt(LocalDateTime.now());
    return entity;
  }
}
