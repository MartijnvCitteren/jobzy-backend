package com.jobly_jobs.facade;

import com.jobly_jobs.domain.dto.request.JobCreationRequestDto;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.exceptions.JobRequestAlreadyExists;
import com.jobly_jobs.promt.generator.PromptCreator;
import com.jobly_jobs.service.JobRequestService;
import com.jobly_jobs.service.VacancyTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JobCreationFacade {
    private final VacancyTextService vacancyTextService;
    private final JobRequestService jobRequestService;
    private final PromptCreator promptCreator;

    public GeneratedVacancyDto generateVacancyText(JobCreationRequestDto descriptionInputDto) {
        if(!jobRequestService.isUniqueJobRequest(descriptionInputDto.generalInfo())){
            String message  = String.format("Job request for company: %s and vacancy: %s is already created in the last" +
                                            "14 days, please update the existing vacancy",
                                            descriptionInputDto.generalInfo().companyName(), descriptionInputDto.generalInfo().jobTitle());
            throw new JobRequestAlreadyExists(message);
        }
        String prompt = promptCreator.createPrompt(descriptionInputDto);
        GeneratedVacancyDto generatedVacancy = vacancyTextService.generatedVacancyText(prompt);
        jobRequestService.createJobRequest(descriptionInputDto.generalInfo(), generatedVacancy);
        return generatedVacancy;
    }
}
