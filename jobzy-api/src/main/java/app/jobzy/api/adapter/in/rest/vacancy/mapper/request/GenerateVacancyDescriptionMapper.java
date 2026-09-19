package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import app.jobzy.api.vacancy.adapter.in.web.contract.GenerateVacancyDescriptionRequest;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface GenerateVacancyDescriptionMapper {

  GenerateVacancyDescriptionCommand toCommand(UUID id, GenerateVacancyDescriptionRequest request);
}
