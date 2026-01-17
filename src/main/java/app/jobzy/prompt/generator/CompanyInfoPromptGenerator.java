package app.jobzy.prompt.generator;

import app.jobzy.domain.dto.request.CompanyInfoRequestDto;
import app.jobzy.prompt.dto.CompanyInfoSearchAction;
import app.jobzy.prompt.dto.PromptValues;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CompanyInfoPromptGenerator implements PromptGenerator<CompanyInfoRequestDto> {

  private static final String SCOPE = """
      Focus on a company description. What is there core business? (2 - 4 lines)
      Focus on company goal. What does the company want to achieve? What makes them unique (2 - 4 lines)
      Focus on the company’s positioning as an employer. What makes it a good place to work. (name 3 - 5 reasons)
      Focus on how the company communicates to customers and job applications based on their website and vacancy.
      """;

  @Override
  public PromptValues<CompanyInfoRequestDto> getPrompt(CompanyInfoRequestDto companyInfoRequestDto) {
    return PromptValues.builderForRequest(companyInfoRequestDto)
        .task(getTask(companyInfoRequestDto))
        .scope(SCOPE)
        .limits(DefaultLimits.getDefaultLimits())
        .action(getAction(companyInfoRequestDto))
        .requestObject(companyInfoRequestDto)
        .build();
  }

  private String getTask(CompanyInfoRequestDto companyInfoRequestDto) {
    return String.format("""
        You are an expert business analyst and brand strategist.
        Research the company provided below using publicly available online sources.
        Your most important source for company information is: %s
        If available on this page you find an example vacancy: %s
        Only use the above website addresses if you believe they are valid. Otherwise ignore them!
        Respond in the given format.
        """, companyInfoRequestDto.companyWebsite(), companyInfoRequestDto.exampleVacancyUrl());
  }

  private CompanyInfoSearchAction getAction(CompanyInfoRequestDto companyInfoRequestDto) {
    return CompanyInfoSearchAction.builder()
        .companyName(companyInfoRequestDto.companyName())
        .countryLocated(companyInfoRequestDto.country().toString())
        .urlExampleVacancy(companyInfoRequestDto.exampleVacancyUrl())
        .build();
  }
}
