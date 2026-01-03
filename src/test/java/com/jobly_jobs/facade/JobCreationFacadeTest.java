package com.jobly_jobs.facade;

import com.jobly_jobs.domain.dto.request.JobCreationRequestDto;
import com.jobly_jobs.exceptions.JobRequestAlreadyExists;
import com.jobly_jobs.factory.GeneralJobInfoDtoFactory;
import com.jobly_jobs.factory.GeneratedVacancyDtoFactory;
import com.jobly_jobs.factory.JobCreationRequestDtoFactory;
import com.jobly_jobs.prompt.generator.PromptCreator;
import com.jobly_jobs.service.JobRequestService;
import com.jobly_jobs.service.VacancyTextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobCreationFacadeTest {
    @Mock
    private VacancyTextService vacancyTextService;

    @Mock
    private JobRequestService jobRequestService;

    @Mock
    private PromptCreator promptCreator;

    @InjectMocks
    private JobCreationFacade jobCreationFacade;

    @Test
    void givenUniqueRequest_whenGenerateVacancyText_then3ServicesAreCalled() {
        //given
        var generalInfo = GeneralJobInfoDtoFactory.createGeneralInfoDto().build();
        var descriptionInput = JobCreationRequestDtoFactory.createJobDescriptionInputDto()
                .generalInfo(generalInfo)
                .build();
        var generatedVacancyDto = GeneratedVacancyDtoFactory.createGeneratedVacancyDto().build();
        when(jobRequestService.isUniqueJobRequest(any())).thenReturn(true);
        when(promptCreator.createPrompt(any(JobCreationRequestDto.class))).thenReturn("prompt");
        when(vacancyTextService.generatedVacancyText(anyString())).thenReturn(generatedVacancyDto);

        //when
        jobCreationFacade.generateVacancyText(descriptionInput);

        //when & then
        verify(promptCreator, times(1)).createPrompt(any(JobCreationRequestDto.class));
        verify(vacancyTextService, times(1)).generatedVacancyText(anyString());
        verify(jobRequestService, times(1)).createJobRequest((descriptionInput.generalInfo()),
                                                             (generatedVacancyDto));
    }

    @Test
    void givenRequestAlreadyExists_whenGenerateVacancyText_thenThrowException() {
        var generalInfo = GeneralJobInfoDtoFactory.createGeneralInfoDto().build();
        var descriptionInput = JobCreationRequestDtoFactory.createJobDescriptionInputDto()
                .generalInfo(generalInfo)
                .build();
        when(jobRequestService.isUniqueJobRequest(any())).thenReturn(false);

        //when & then
        assertThrows(JobRequestAlreadyExists.class, () -> jobCreationFacade.generateVacancyText(descriptionInput));
    }
}