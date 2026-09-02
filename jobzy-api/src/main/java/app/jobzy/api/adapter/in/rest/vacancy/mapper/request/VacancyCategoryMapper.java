package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import app.jobzy.api.application.port.in.dto.VacancyCategoryDto;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCategory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
interface VacancyCategoryMapper {

  VacancyCategoryDto toDto(VacancyCategory category);
}
