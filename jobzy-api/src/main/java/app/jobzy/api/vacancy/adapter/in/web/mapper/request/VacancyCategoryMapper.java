package app.jobzy.api.vacancy.adapter.in.web.mapper.request;

import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCategory;
import app.jobzy.api.vacancy.application.port.in.dto.VacancyCategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
interface VacancyCategoryMapper {

  VacancyCategoryDto toDto(VacancyCategory category);
}
