package app.jobzy.api.adapter.in.rest.identity.mapper.request;

import app.jobzy.api.application.port.in.command.ConfirmRegistrationCommand;
import app.jobzy.api.identity.adapter.in.web.contract.RegistrationConfirmationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface RegistrationConfirmationRequestMapper {

  ConfirmRegistrationCommand toCommand(RegistrationConfirmationRequest request);
}
