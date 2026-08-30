package app.jobzy.api.vacancy.adapter.in.web.mapper.request;

import app.jobzy.api.vacancy.adapter.in.web.contract.WorkplaceType;
import app.jobzy.api.vacancy.application.port.in.dto.WorkplaceTypeDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
interface WorkplaceTypeMapper {

  WorkplaceTypeDto toDto(WorkplaceType workplaceType);
}
