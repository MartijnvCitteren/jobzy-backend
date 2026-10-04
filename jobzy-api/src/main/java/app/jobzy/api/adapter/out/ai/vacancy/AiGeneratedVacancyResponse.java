package app.jobzy.api.adapter.out.ai.vacancy;

import app.jobzy.api.adapter.out.ai.AiResponseBase;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescriptionSource;

public class AiGeneratedVacancyResponse extends AiResponseBase {
  String summary;
  String jobDescription;
  String tasks;
  String aboutUs;
  VacancyDescriptionSource source = VacancyDescriptionSource.GENERATED;
}
