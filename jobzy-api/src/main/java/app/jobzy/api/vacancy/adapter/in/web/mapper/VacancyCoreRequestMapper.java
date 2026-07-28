package app.jobzy.api.vacancy.adapter.in.web.mapper;

import app.jobzy.api.vacancy.application.port.in.dto.LocationDto;
import app.jobzy.api.vacancy.application.port.in.dto.VacancyCategoryDto;
import app.jobzy.api.vacancy.application.port.in.dto.VacancyCoreRequestDto;
import app.jobzy.api.vacancy.application.port.in.dto.WorkplaceTypeDto;
import app.jobzy.api.vacancy.domain.rest.Location;
import app.jobzy.api.vacancy.domain.rest.VacancyCategory;
import app.jobzy.api.vacancy.domain.rest.VacancyCoreRequest;
import app.jobzy.api.vacancy.domain.rest.WorkplaceType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VacancyCoreRequestMapper {

  VacancyCoreRequestDto toDto(VacancyCoreRequest vacancyCoreRequest);

  LocationDto toDto(Location location);

  VacancyCategoryDto toDto(VacancyCategory category);

  WorkplaceTypeDto toDto(WorkplaceType workplaceType);
}
