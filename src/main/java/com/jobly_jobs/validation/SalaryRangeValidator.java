package com.jobly_jobs.validation;

import com.jobly_jobs.domain.dto.request.BenefitsRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import org.springframework.util.ObjectUtils;

public class SalaryRangeValidator implements ConstraintValidator<SalaryRange, BenefitsRequestDto> {

  @Override
  public boolean isValid(BenefitsRequestDto benefitsRequestDto, ConstraintValidatorContext constraintValidatorContext) {
    if (ObjectUtils.isEmpty(benefitsRequestDto.maxSalary()) && !ObjectUtils.isEmpty(benefitsRequestDto.minSalary())) {
      return true;
    }
    BigDecimal min = ObjectUtils.isEmpty(benefitsRequestDto.minSalary()) ? BigDecimal.ZERO : benefitsRequestDto.minSalary();
    BigDecimal max = ObjectUtils.isEmpty(benefitsRequestDto.maxSalary()) ? BigDecimal.ZERO : benefitsRequestDto.maxSalary();
    return max.compareTo(min) >= 0;
  }
}
