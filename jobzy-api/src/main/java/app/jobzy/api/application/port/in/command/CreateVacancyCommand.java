package app.jobzy.api.application.port.in.command;

import java.math.BigDecimal;

public record CreateVacancyCommand(
    String jobTitle,
    VacancyCategoryDto category,
    LocationDto location,
    WorkplaceTypeDto workplaceType,
    BigDecimal hoursPerWeek) {}
