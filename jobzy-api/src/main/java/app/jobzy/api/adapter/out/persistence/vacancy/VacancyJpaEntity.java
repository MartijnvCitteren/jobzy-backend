package app.jobzy.api.adapter.out.persistence.vacancy;

import app.jobzy.api.domain.vacancy.valueobject.HoursPerWeek;
import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.VacancyStatus;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class VacancyJpaEntity {
  @Id private UUID id;
  private String jobTitle;
  private VacancyCategory category;
  @Embedded private Location location;
  private WorkplaceType workplaceType;
  @Embedded private HoursPerWeek hoursPerWeek;
  private VacancyStatus status;
}
