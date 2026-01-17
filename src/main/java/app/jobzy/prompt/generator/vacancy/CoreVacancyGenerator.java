package app.jobzy.prompt.generator.vacancy;

import app.jobzy.cache.CacheCompanyInfoService;
import app.jobzy.domain.dto.JobInfo;
import app.jobzy.domain.dto.agent.CompanyInfoAiResponse;
import app.jobzy.exceptions.VacancySessionExpired;
import app.jobzy.prompt.dto.PromptValues;
import app.jobzy.prompt.generator.DefaultLimits;
import app.jobzy.prompt.generator.PromptGenerator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class CoreVacancyGenerator implements PromptGenerator<JobInfo> {
  private final CacheCompanyInfoService cacheCompanyInfoService;

  private static final String SCOPE = """
      Focus on writing about dayToDayDescription (3 - 6 lines)
      Focus on writing about jobDescription (5 - 15 lines)
      Focus on writing about uniqueSellingPoints (3 - 6 lines)
      Focus on writing about requirements(4 - 7 bulletpoints)
      Focus on the company. Explain the goal of the company and why it's nice to join them achieving their mission (5-10 lines)
      """;

  @Override
  public PromptValues<JobInfo> getPrompt(JobInfo jobInfo) {

    Optional<CompanyInfoAiResponse> cachedCompanyInfo = cacheCompanyInfoService.getCompanyInfo(jobInfo.companyInfoToken());
    if (cachedCompanyInfo.isEmpty()) {
      throw new VacancySessionExpired("No cached company info found for token " + jobInfo.companyInfoToken());
    }

    return PromptValues.builderForRequest(jobInfo)
        .task(getTask(jobInfo, cachedCompanyInfo.get()))
        .scope(SCOPE)
        .limits(DefaultLimits.getDefaultLimits())
        .requestObject(jobInfo)
        .build();
  }

  private String getTask(JobInfo jobInfo, CompanyInfoAiResponse companyInfo) {
    String language = jobInfo.writingStyle().language().toString();
    String writingStyle = jobInfo.writingStyle().writingStyle().getDescription();
    String jobTitle = jobInfo.seniorityLevel().toString() + " " + jobInfo.jobTitle();
    String tasks = jobInfo.tasks();
    String skills = jobInfo.skills();


    return String.format("""
        Act like an experienced recruiter and copywriter.
        You write everything this langue: %s
        Your writing style is: %s
        You write specific parts of the vacancy.
        You write about "dayToDayDescription", here you explain what a day looks like as a %s
        You write about "jobDescription", here you explain the purpose of your role and the biggest challenges as %s
        You write about "jobUniqueSellingPoints" here you explain what makes this job unique
        You write about "requirements", here you explain what skills are needed to be successfull in a role.You write this
        in a bullet point list. In this list you prioritize education and work experience.
        Use this information for the day-to-day, job description and uniqueSellingPoints: %s
        Use this information for requirements  that are needed in this job: %s
        Write everything in the context of this company information: %s
        Respond in the given format.
        """, language, writingStyle, jobTitle, jobTitle, tasks, skills, companyInfo.toString());
  }
}
