package app.jobzy.api.domain.vacancy.valueobject;

import app.jobzy.api.domain.UuidV7Generator;
import java.util.UUID;

public record VacancyId(UUID value) {

  public VacancyId {
    if (value == null) {
      throw new IllegalArgumentException("VacancyId requires a value");
    }
  }

  public static VacancyId newId() {
    return new VacancyId(UuidV7Generator.getUUID());
  }
}
