package app.jobzy.service;

import app.jobzy.agent.vacancy.BenefitAgent;
import app.jobzy.agent.vacancy.CoreVacancyAgent;
import app.jobzy.agent.vacancy.TeamAndCompanyAgent;
import app.jobzy.domain.dto.JobInfo;
import app.jobzy.domain.dto.agent.BenefitsVacancyAiResponse;
import app.jobzy.domain.dto.agent.CompanyAndTeamAiResponse;
import app.jobzy.domain.dto.agent.CoreVacancyAiResponse;
import app.jobzy.domain.dto.request.JobInfoRequestDto;
import app.jobzy.domain.dto.response.GeneratedVacancyDto;
import app.jobzy.mapper.JobInfoMapper;
import app.jobzy.mapper.VacancyMapper;
import app.jobzy.prompt.dto.PromptValues;
import app.jobzy.prompt.generator.vacancy.BenefitGenerator;
import app.jobzy.prompt.generator.vacancy.CoreVacancyGenerator;
import app.jobzy.prompt.generator.vacancy.TeamAndCompanyGenerator;
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
