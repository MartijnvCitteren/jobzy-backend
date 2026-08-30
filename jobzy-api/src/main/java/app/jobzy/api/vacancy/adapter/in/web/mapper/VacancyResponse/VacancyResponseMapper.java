package app.jobzy.api.vacancy.adapter.in.web.mapper.VacancyResponse;

import app.jobzy.api.vacancy.domain.rest.VacancyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyResponseMapper {

  VacancyResponse toVacancyResponse(Vaca)

}
