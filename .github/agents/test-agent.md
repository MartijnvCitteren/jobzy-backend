# Testing Agent

## Purpose
Write comprehensive tests for Jobzy backend using JUnit, Mockito, and RestAssured.

## Unit Tests (Mockito + JUnit)

### format rules
- Use @Displayname to explain what the test does
- Use Gherkin formatting (given, when, then)
- Use Parameterized test when repetitive tests need to be done

### format example
```java
@Test
  @DisplayName("given valid company info request with valid URLs, when getting token, then return token successfully")
  void givenValidCompanyInfoRequest_whenGettingToken_thenReturnTokenSuccessfully() {
    // Given
    when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(true);
    when(urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl())).thenReturn(true);
    when(cacheIdCompanyInfo.getUuid(companyInfoRequestDto.companyWebsite())).thenReturn(Optional.empty());
    when(promptGenerator.getPrompt(companyInfoRequestDto)).thenReturn(promptValues);
    when(agent.execute(promptValues)).thenReturn(companyInfoAiResponse);

    // When
    companyInfoRetrievalService.getCompanyInfoResponseToken(companyInfoRequestDto);

    // Then
    verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
    verify(urlValidation).isValid(companyInfoRequestDto.exampleVacancyUrl());
    verify(promptGenerator).getPrompt(companyInfoRequestDto);
    verify(agent).execute(promptValues);
    verify(cacheCompanyInfoService).putCompanyInfo(any(UUID.class), any(CompanyInfoAiResponse.class));
    verify(cacheIdCompanyInfo).putCompanyWebsite(any(String.class), any(UUID.class));
  }
```
### What to Test
✅ Happy paths
✅ Error cases
✅ Edge cases (null, empty, boundaries)
✅ Business rule validation

### What NOT to Mock
- DTO's, entities and POJOs
- Value objects
- Testing all null value possibilities when mapping objects.

## Integration Test (RestAssured + WireMock)
TODO

