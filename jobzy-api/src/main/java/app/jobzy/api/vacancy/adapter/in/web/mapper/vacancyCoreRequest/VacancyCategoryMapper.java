package app.jobzy.api.vacancy.adapter.in.web.mapper.vacancyCoreRequest;

import app.jobzy.api.vacancy.application.port.in.dto.VacancyCategoryDto;
import app.jobzy.api.vacancy.domain.rest.VacancyCategory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
interface VacancyCategoryMapper {

  VacancyCategoryDto toDto(VacancyCategory category);
}
