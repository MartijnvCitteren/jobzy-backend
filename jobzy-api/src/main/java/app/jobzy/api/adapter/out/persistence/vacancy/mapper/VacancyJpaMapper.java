package app.jobzy.api.adapter.out.persistence.vacancy.mapper;

import app.jobzy.api.adapter.out.persistence.vacancy.VacancyDescriptionJpaEntity;
import app.jobzy.api.adapter.out.persistence.vacancy.VacancyJpaEntity;
import app.jobzy.api.domain.vacancy.Vacancy;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {VacancyDescriptionJpaMapper.class},
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyJpaMapper {
  VacancyJpaEntity toJpaEntity(Vacancy vacancy);

  @Mapping(target = "id", source = "vacancyEntity.id")
  @Mapping(target = "description", source = "descriptionEntity")
  Vacancy toDomain(
      VacancyJpaEntity vacancyEntity, @Nullable VacancyDescriptionJpaEntity descriptionEntity);
}
