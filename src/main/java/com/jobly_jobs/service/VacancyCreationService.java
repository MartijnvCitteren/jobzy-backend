package com.jobly_jobs.service;

import com.jobly_jobs.agent.vacancy.BenefitAgent;
import com.jobly_jobs.agent.vacancy.CoreVacancyAgent;
import com.jobly_jobs.agent.vacancy.TeamAndCompanyAgent;
import com.jobly_jobs.domain.dto.JobInfo;
import com.jobly_jobs.domain.dto.agent.BenefitsVacancyAiResponse;
import com.jobly_jobs.domain.dto.agent.CompanyAndTeamAiResponse;
import com.jobly_jobs.domain.dto.agent.CoreVacancyAiResponse;
import com.jobly_jobs.domain.dto.request.JobInfoRequestDto;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.mapper.JobInfoMapper;
import com.jobly_jobs.mapper.VacancyMapper;
import com.jobly_jobs.prompt.dto.PromptValues;
import com.jobly_jobs.prompt.generator.vacancy.BenefitGenerator;
import com.jobly_jobs.prompt.generator.vacancy.CoreVacancyGenerator;
import com.jobly_jobs.prompt.generator.vacancy.TeamAndCompanyGenerator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class VacancyCreationService {

  private final JobInfoMapper jobInfoMapper;
  private final TeamAndCompanyGenerator teamAndCompanyGenerator;
  private final CoreVacancyGenerator coreVacancyGenerator;
  private final BenefitGenerator benefitGenerator;
  private final TeamAndCompanyAgent teamAndCompanyAgent;
  private final CoreVacancyAgent coreVacancyAgent;
  private final BenefitAgent benefitAgent;
  private final VacancyMapper vacancyMapper;

  public GeneratedVacancyDto createVacancy(JobInfoRequestDto jobInfoDto, UUID companyToken) {
    JobInfo jobInfo = jobInfoMapper.toJobInfo(jobInfoDto, companyToken);
    PromptValues<JobInfo> teamAndCompanyPrompt = teamAndCompanyGenerator.getPrompt(jobInfo);
    PromptValues<JobInfo> corePrompt = coreVacancyGenerator.getPrompt(jobInfo);
    PromptValues<JobInfo> benefitsPrompt = benefitGenerator.getPrompt(jobInfo);

    CompletableFuture<CompanyAndTeamAiResponse> teamAndCompanyFuture = CompletableFuture.supplyAsync(() ->
        teamAndCompanyAgent.execute(teamAndCompanyPrompt)
    );

    CompletableFuture<CoreVacancyAiResponse> coreFuture = CompletableFuture.supplyAsync(() ->
        coreVacancyAgent.execute(corePrompt)
    );

    CompletableFuture<BenefitsVacancyAiResponse> benefitFuture = CompletableFuture.supplyAsync(() ->
        benefitAgent.execute(benefitsPrompt));

    CompletableFuture.allOf(teamAndCompanyFuture, coreFuture, benefitFuture).join();

    CompanyAndTeamAiResponse teamAndCompanyResponse = teamAndCompanyFuture.join();
    log.info("teamAndCompanyResponse={}", teamAndCompanyResponse);
    CoreVacancyAiResponse coreResponse = coreFuture.join();
    log.info("coreResponse={}", coreResponse);
    BenefitsVacancyAiResponse benefitResponse = benefitFuture.join();
    log.info("benefitResponse={}", benefitResponse);


    return vacancyMapper.toVacancyDto(teamAndCompanyResponse, coreResponse,  benefitResponse);
  }
}
