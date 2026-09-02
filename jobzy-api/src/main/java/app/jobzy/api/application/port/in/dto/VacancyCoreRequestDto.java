package app.jobzy.api.application.port.in.dto;

import java.math.BigDecimal;

public record VacancyCoreRequestDto(
    String jobTitle,
    VacancyCategoryDto category,
    LocationDto location,
    WorkplaceTypeDto workplaceType,
    BigDecimal hoursPerWeek) {}
