package app.jobzy.api.vacancy.adapter.in.web.mapper.request;

import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCoreRequest;
import app.jobzy.api.vacancy.application.port.in.dto.VacancyCoreRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {VacancyCategoryMapper.class, VacancyLocationMapper.class, WorkplaceTypeMapper.class},
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyCoreRequestMapper {

  VacancyCoreRequestDto toDto(VacancyCoreRequest vacancyCoreRequest);
}
