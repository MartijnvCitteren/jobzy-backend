package app.jobzy.domain.dto.response;

import app.jobzy.domain.entity.VacancyText;
import app.jobzy.domain.enums.FunctionGroup;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record JobCreationResponseDto(
    String jobTitle,
    FunctionGroup functionGroup,
    String companyName,
    BigDecimal minSalary,
    BigDecimal maxSalary,
    VacancyText vacancyText
) {

}
