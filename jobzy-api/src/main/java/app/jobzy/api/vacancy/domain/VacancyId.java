package app.jobzy.api.vacancy.domain;

import java.util.UUID;

public record VacancyId(UUID value) {

  public VacancyId {
    if (value == null) {
      throw new IllegalArgumentException("VacancyId requires a value");
    }
  }

  public static VacancyId newId() {
    return new VacancyId(UUID.randomUUID());
  }
}
