package app.jobzy.api.vacancy.application.port.in.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.jobzy.api.application.port.in.dto.VacancyCategoryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VacancyCategoryDtoTest {

  @ParameterizedTest
  @EnumSource(VacancyCategoryDto.class)
  @DisplayName(
      "given known category number, when fromCategoryNumber then returns matching category")
  void givenKnownCategoryNumberWhenFromCategoryNumberThenReturnsMatchingCategory(
      VacancyCategoryDto category) {
    VacancyCategoryDto result = VacancyCategoryDto.fromCategoryNumber(category.getCategoryNumber());

    assertEquals(category, result);
  }

  @Test
  @DisplayName("given unknown category number, when fromCategoryNumber then throws")
  void givenUnknownCategoryNumberWhenFromCategoryNumberThenThrows() {
    assertThrows(IllegalArgumentException.class, () -> VacancyCategoryDto.fromCategoryNumber(-1));
  }
}
