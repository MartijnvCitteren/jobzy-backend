package app.jobzy.api.application.port.out;

import app.jobzy.api.domain.vacancy.Vacancy;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public interface VacancyRepository {
  void save(Vacancy vacancy);

  Optional<Vacancy> findById(UUID id);
}
