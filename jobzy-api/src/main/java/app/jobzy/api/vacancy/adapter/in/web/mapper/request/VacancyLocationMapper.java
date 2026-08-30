package app.jobzy.api.vacancy.adapter.in.web.mapper.request;

import app.jobzy.api.vacancy.adapter.in.web.contract.Location;
import app.jobzy.api.vacancy.application.port.in.dto.LocationDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
interface VacancyLocationMapper {
  LocationDto toDto(Location location);
}
