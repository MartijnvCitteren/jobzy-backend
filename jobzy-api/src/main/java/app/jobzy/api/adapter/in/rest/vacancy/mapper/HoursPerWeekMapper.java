package app.jobzy.api.adapter.in.rest.vacancy.mapper;

import app.jobzy.api.domain.vacancy.valueobject.HoursPerWeek;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface HoursPerWeekMapper {
  HoursPerWeek toHoursPerWeek(BigDecimal minHoursPerWeek, BigDecimal maxHoursPerWeek);
}
