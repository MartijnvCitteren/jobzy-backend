package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import app.jobzy.api.application.port.in.command.SetManualVacancyDescriptionCommand;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionRequest;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyDescriptionRequestMapper {

  @Mapping(target = "vacancyId", source = "id")
  SetManualVacancyDescriptionCommand toCommand(UUID id, VacancyDescriptionRequest request);
}
