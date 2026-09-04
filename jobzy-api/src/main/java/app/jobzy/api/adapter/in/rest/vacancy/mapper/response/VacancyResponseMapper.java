package app.jobzy.api.adapter.in.rest.vacancy.mapper.response;

import app.jobzy.api.adapter.in.rest.vacancy.mapper.request.HoursPerWeekMapper;
import app.jobzy.api.domain.vacancy.Vacancy;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {HoursPerWeekMapper.class},
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VacancyResponseMapper {

  @Mapping(target = "minHoursPerWeek", source = "hoursPerWeek.minHours")
  @Mapping(target = "maxHoursPerWeek", source = "hoursPerWeek.maxHours")
  VacancyResponse toVacancyResponse(Vacancy vacancy);

  default OffsetDateTime map(LocalDateTime localDateTime) {
    return localDateTime == null ? null : localDateTime.atOffset(java.time.ZoneOffset.UTC);
  }
}
