package app.jobzy.repository;

import app.jobzy.domain.entity.VacancyText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacancyTextRepository extends JpaRepository<VacancyText, Long> {

}
