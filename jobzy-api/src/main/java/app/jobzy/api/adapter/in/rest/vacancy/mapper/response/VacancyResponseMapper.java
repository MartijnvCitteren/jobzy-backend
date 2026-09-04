package app.jobzy.api.adapter.in.rest.vacancy.mapper.response;

import app.jobzy.api.domain.vacancy.Vacancy;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyResponseMapper {

  @Mapping(target = "id", source = "id.value")
  VacancyResponse toVacancyResponse(Vacancy vacancy);
}
