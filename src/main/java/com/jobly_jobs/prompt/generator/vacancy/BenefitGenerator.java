package com.jobly_jobs.prompt.generator.vacancy;

import com.jobly_jobs.domain.dto.JobInfo;
import com.jobly_jobs.prompt.dto.PromptValues;
import com.jobly_jobs.prompt.generator.DefaultLimits;
import com.jobly_jobs.prompt.generator.PromptGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class BenefitGenerator implements PromptGenerator<JobInfo>{

  private static final String SCOPE = """
      Focus on writing the job offer (4 -7 bulletpoints)
      Focus on about who the can contact (1 - 3 lines of text)
      """;

  @Override public PromptValues<JobInfo> getPrompt(JobInfo jobInfo) {
    return PromptValues.builderForRequest(jobInfo)
        .task(getTask(jobInfo))
        .scope(SCOPE)
        .limits(DefaultLimits.getDefaultLimits())
        .requestObject(jobInfo)
        .build();
  }

  private String getTask(JobInfo jobInfo) {
    String language = jobInfo.writingStyle().language().toString();
    String jobTitle = jobInfo.seniorityLevel().toString() + " " + jobInfo.jobTitle();
    String minimumSalary = jobInfo.benefits().minSalary().toString();
    String maximumSalary = jobInfo.benefits().maxSalary().toString();
    String payMentPeriod = jobInfo.benefits().salaryPeriod().toString();
    String otherPerks = jobInfo.benefits().extraPerks();
    String contact = jobInfo.contactInfo().toString();


    return String.format("""   
        Act like an experienced recruiter and copywriter.
        You write everything this langue: %s
        You write specific parts of the vacancy for a %s
        You write about the offer. What can a company offer you in terms of working environment and salary.
        Therefor you use the minimum salary: %s , maximum salary: %s , payment period: %s , other perks: %s
        You also write about who they can contact at the company.
        Therefor your use the contact information: %s
        """, language, jobTitle, minimumSalary, maximumSalary, payMentPeriod, otherPerks, contact);

  }
}
