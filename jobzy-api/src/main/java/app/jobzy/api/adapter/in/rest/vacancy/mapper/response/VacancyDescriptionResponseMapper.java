package app.jobzy.api.adapter.in.rest.vacancy.mapper.response;

import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyDescriptionResponseMapper {

  VacancyDescriptionResponse toResponse(VacancyDescription description);
}
