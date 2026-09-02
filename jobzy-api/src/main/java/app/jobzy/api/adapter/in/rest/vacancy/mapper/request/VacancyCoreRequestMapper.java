package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import app.jobzy.api.application.port.in.command.CreateVacancyCommand;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCoreRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {VacancyCategoryMapper.class, VacancyLocationMapper.class, WorkplaceTypeMapper.class},
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyCoreRequestMapper {

  CreateVacancyCommand toDto(VacancyCoreRequest vacancyCoreRequest);
}
