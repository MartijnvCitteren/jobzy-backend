package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import app.jobzy.api.domain.vacancy.valueobject.HoursPerWeek;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCoreRequest;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface HoursPerWeekMapper {

  HoursPerWeek toHoursPerWeek(BigDecimal minHoursPerWeek, BigDecimal maxHoursPerWeek);

  @Mapping(target = "minHours", source = "minHoursPerWeek")
  @Mapping(target = "maxHours", source = "maxHoursPerWeek")
  HoursPerWeek toHoursPerWeek(VacancyCoreRequest vacancyCoreRequest);
}
