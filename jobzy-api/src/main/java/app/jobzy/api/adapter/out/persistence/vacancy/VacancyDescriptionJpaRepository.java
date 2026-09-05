package app.jobzy.api.adapter.out.persistence.vacancy;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Low-level repository for {@code VacancyDescriptionJpaEntity}. Queries by unique {@code vacancyId}
 * (not primary key) to support upsert-by-vacancy logic in {@code VacancyRepositoryAdapter}.
 */
@Repository
public interface VacancyDescriptionJpaRepository
    extends JpaRepository<VacancyDescriptionJpaEntity, UUID> {
  /**
   * Fetch the description entity for a given vacancy, if present.
   *
   * @param vacancyId the vacancy id
   * @return the description entity, or empty if never set
   */
  Optional<VacancyDescriptionJpaEntity> findByVacancyId(UUID vacancyId);
}
