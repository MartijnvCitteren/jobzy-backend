package com.jobly_jobs.service;

import com.jobly_jobs.domain.dto.request.JobInfoRequestDto;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VacancyCreationService {
    public GeneratedVacancyDto createVacancy(JobInfoRequestDto jobInfo, UUID requestId) {
        return null;
    }
}
