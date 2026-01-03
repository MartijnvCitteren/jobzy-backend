package com.jobly_jobs.service;

import com.jobly_jobs.domain.dto.request.GeneralJobDescriptionInfoDto;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.domain.dto.response.JobCreationResponseDto;
import com.jobly_jobs.domain.entity.JobCreationRequest;
import com.jobly_jobs.domain.mapper.JobCreationMapper;
import com.jobly_jobs.domain.mapper.VacancyTextMapper;
import com.jobly_jobs.repository.JobCreationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Log4j2
@RequiredArgsConstructor
public class JobRequestService {
    private final JobCreationRepository jobCreationRepository;

    @Transactional
    public void createJobRequest(GeneralJobDescriptionInfoDto jobInfo, GeneratedVacancyDto vacancyDto) {
        try {
            JobCreationRequest jobCreationRequest = JobCreationMapper.toNewJobCreationRequest(jobInfo);
            jobCreationRequest.setVacancyText(VacancyTextMapper.toVacancyText(vacancyDto));
            jobCreationRepository.save(jobCreationRequest);

        } catch (DataAccessException e) {
            log.error("Error while saving job creation request for job: {} and company {}", jobInfo.jobTitle(),
                      jobInfo.companyName());
            throw e;
        }
    }

    @Transactional
    public JobCreationResponseDto getJobRequest(long id) {
        try {
            JobCreationRequest jobCreationRequest = jobCreationRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Job creation request with id: " + id + " not " + "found"));
            return JobCreationMapper.toJobCreationResponseDto(jobCreationRequest);
        } catch (DataAccessException e) {
            log.error("Error while fetching job creation request with id: {}", id);
            throw e;
        }
    }

    public boolean isUniqueJobRequest(GeneralJobDescriptionInfoDto jobInfo) {
        return jobCreationRepository.findByJobTitleAndFunctionGroupAndCompanyNameAndCreationDateAfter(
                        jobInfo.jobTitle(), jobInfo.functionGroup(), jobInfo.companyName(),
                        LocalDateTime.now().minusWeeks(2))
                .isEmpty();

    }
}
