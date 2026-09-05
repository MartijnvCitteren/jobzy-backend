package app.jobzy.api.adapter.out.persistence.vacancy;

import app.jobzy.api.adapter.out.persistence.vacancy.mapper.VacancyDescriptionJpaMapper;
import app.jobzy.api.adapter.out.persistence.vacancy.mapper.VacancyJpaMapper;
import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.domain.vacancy.Vacancy;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class VacancyRepositoryAdapter implements VacancyRepository {
  private final VacancyJpaMapper jpaMapper;
  private final VacancyDescriptionJpaMapper descriptionJpaMapper;
  private final VacancyJpaRepository jpaRepository;
  private final VacancyDescriptionJpaRepository descriptionJpaRepository;

  @Override
  public void save(Vacancy vacancy) {
    var jpaVacancy = jpaMapper.toJpaEntity(vacancy);
    jpaRepository.save(jpaVacancy);

    if (vacancy.getDescription() != null) {
      var existingDescription =
          descriptionJpaRepository.findByVacancyId(vacancy.getId()).orElse(null);
      var descriptionEntity =
          descriptionJpaMapper.toJpaEntity(
              vacancy.getId(), vacancy.getDescription(), existingDescription);
      descriptionJpaRepository.save(descriptionEntity);
    }

    log.info("Vacancy is saved");
  }

  @Override
  public Optional<Vacancy> findById(UUID id) {
    return jpaRepository
        .findById(id)
        .map(
            vacancyEntity -> {
              var descriptionEntity = descriptionJpaRepository.findByVacancyId(id).orElse(null);
              return jpaMapper.toDomain(vacancyEntity, descriptionEntity);
            });
  }
}
