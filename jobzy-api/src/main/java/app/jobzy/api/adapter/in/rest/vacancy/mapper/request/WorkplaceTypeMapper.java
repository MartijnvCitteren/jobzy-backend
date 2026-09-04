package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import app.jobzy.api.vacancy.adapter.in.web.contract.WorkplaceType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
interface WorkplaceTypeMapper {

  app.jobzy.api.domain.vacancy.valueobject.WorkplaceType toDomainWorkplaceType(
      WorkplaceType workplaceType);
}
