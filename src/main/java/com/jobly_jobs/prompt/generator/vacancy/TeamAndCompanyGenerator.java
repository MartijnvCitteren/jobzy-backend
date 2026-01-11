package com.jobly_jobs.prompt.generator.vacancy;

import com.jobly_jobs.cache.CacheCompanyInfoService;
import com.jobly_jobs.domain.dto.JobInfo;
import com.jobly_jobs.domain.dto.agent.CompanyInfoAiResponse;
import com.jobly_jobs.exceptions.VacancySessionExpired;
import com.jobly_jobs.prompt.dto.PromptValues;
import com.jobly_jobs.prompt.generator.DefaultLimits;
import com.jobly_jobs.prompt.generator.PromptGenerator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class TeamAndCompanyGenerator implements PromptGenerator<JobInfo> {

  private static final String SCOPE = """
      Focus on writing about the the team and why this team is fun and good to work with (3 - 6 lines)
      Focus on the company. Explain the goal of the company and why it's nice to join them achieving their mission (5-10 lines)
      """;
  private final CacheCompanyInfoService cacheCompanyInfoService;

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

  private String getTask(JobInfo jobInfo, CompanyInfoAiResponse cachedCompanyInfo) {
    String language = jobInfo.writingStyle().language().toString();
    String writingStyle = jobInfo.writingStyle().writingStyle().getDescription();
    String teamDescription = jobInfo.teamDescription();
    String companyInfo = cachedCompanyInfo.toString();

    return String.format("""
        Act like an experienced recruiter and copywriter.
        You write everything this langue: %s
        Your writing style is: %s
        You write a small part of the vacancy. Only about the team and company (culture)
        Use this information about the team: %s
        Use this information about the company: %s
        Respond in the given format.
        """, language, writingStyle, teamDescription, companyInfo);
  }
}
