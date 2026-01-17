package app.jobzy.domain.dto.request;

import app.jobzy.domain.enums.SalaryPeriod;
import app.jobzy.validation.SalaryRange;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@SalaryRange
public record BenefitsRequestDto(
    @NotNull
    SalaryPeriod salaryPeriod,
    @DecimalMin(value = "0.0")
    @Digits(integer = 6, fraction = 2, message = "please enter a number between 0.00 and 999999.99")
    BigDecimal minSalary,
    @Digits(integer = 6, fraction = 2, message = "please enter a number between 0.00 and 999999.99")
    BigDecimal maxSalary,
    @Size(min = 10, max = 100, message = "Please describe your company perks in 10 - 200 characters.")
    String extraPerks
) {

}
