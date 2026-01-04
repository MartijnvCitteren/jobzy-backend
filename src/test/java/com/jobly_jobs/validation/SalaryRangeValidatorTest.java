package com.jobly_jobs.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jobly_jobs.domain.dto.request.BenefitsRequestDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SalaryRangeValidatorTest {

  SalaryRangeValidator salaryRangeValidator = new SalaryRangeValidator();


  @Test
  @DisplayName("min salary is less then max salary, then is valid")
  void minSalaryIsLessThenMaxSalary() {
    var dto = getBenefitsRequestDto("100", "200");
    assertTrue(salaryRangeValidator.isValid(dto, null));
  }

  @Test
  @DisplayName("min salary is more then max salary, then is not valid")
  void minSalaryIsMoreThenMaxSalary() {
    var dto = getBenefitsRequestDto("200", "100");
    assertFalse(salaryRangeValidator.isValid(dto, null));
  }

  @Test
  @DisplayName("min salary is equal to max salary, then is valid")
  void minSalaryIsEqualToMaxSalary() {
    var dto = getBenefitsRequestDto("100", "100");
    assertTrue(salaryRangeValidator.isValid(dto, null));
  }

  @Test
  @DisplayName("min salary is null, then is valid")
  void minSalaryIsNull() {
    var dto = getBenefitsRequestDto(null, "100");
    assertTrue(salaryRangeValidator.isValid(dto, null));
  }

  @Test
  @DisplayName("max salary is null, then is valid")
  void maxSalaryIsNull() {
    var dto = getBenefitsRequestDto("100", null);
    assertTrue(salaryRangeValidator.isValid(dto, null));
  }

  @Test
  @DisplayName("Both salary values are null, then valid")
  void bothSalaryValuesAreNull() {
    var dto = getBenefitsRequestDto(null, null);
    assertTrue(salaryRangeValidator.isValid(dto, null));
  }

  private BenefitsRequestDto getBenefitsRequestDto(String minSalary, String maxSalary) {
    if (minSalary == null && maxSalary == null) {
      return BenefitsRequestDto.builder().build();
    } else if (minSalary == null) {
      return BenefitsRequestDto.builder().maxSalary(new BigDecimal(maxSalary)).build();
    } else if (maxSalary == null) {
      return BenefitsRequestDto.builder().minSalary(new BigDecimal(minSalary)).build();
    }

    return BenefitsRequestDto.builder().minSalary(new BigDecimal(minSalary)).maxSalary(new BigDecimal(maxSalary)).build();
  }

}