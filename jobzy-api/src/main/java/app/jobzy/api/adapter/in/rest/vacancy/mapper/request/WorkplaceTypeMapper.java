package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import app.jobzy.api.vacancy.adapter.in.web.contract.WorkplaceType;
import app.jobzy.api.application.port.in.dto.WorkplaceTypeDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
interface WorkplaceTypeMapper {

  WorkplaceTypeDto toDto(WorkplaceType workplaceType);
}
