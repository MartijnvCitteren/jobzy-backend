package com.jobly_jobs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jobly_jobs.domain.dto.request.GeneralJobDescriptionInfoDto;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.domain.dto.response.JobCreationResponseDto;
import com.jobly_jobs.domain.entity.JobCreationRequest;
import com.jobly_jobs.factory.GeneralJobInfoDtoFactory;
import com.jobly_jobs.factory.GeneratedVacancyDtoFactory;
import com.jobly_jobs.factory.JobCreationRequestFactory;
import com.jobly_jobs.repository.JobCreationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;


@ExtendWith(MockitoExtension.class)
class JobRequestServiceTest {

  @Mock
  private JobCreationRepository jobCreationRepository;

  @InjectMocks
  private JobRequestService jobRequestService;

  @Captor
  private ArgumentCaptor<JobCreationRequest> jobCreationRequestCaptor;

  private GeneratedVacancyDto vacancyDto;

  @BeforeEach
  void setUp() {
    vacancyDto = GeneratedVacancyDtoFactory.createGeneratedVacancyDto().build();
  }


  @Test
  void givenCorrectInfo_whenTheJobRequestIsCreated_thenTheIsSavedJobRequestWithCorrectValues() {
    //Given
    var jobInfo = GeneralJobInfoDtoFactory.createGeneralInfoDto().build();

    //When
    jobRequestService.createJobRequest(jobInfo, vacancyDto);

    //Then
    verify(jobCreationRepository).save(jobCreationRequestCaptor.capture());
    JobCreationRequest jobCreationRequest = jobCreationRequestCaptor.getValue();
    assertEquals(jobInfo.jobTitle(), jobCreationRequest.getJobTitle());
    assertEquals(jobInfo.companyName(), jobCreationRequest.getCompanyName());
    assertEquals(jobInfo.functionGroup(), jobCreationRequest.getFunctionGroup());
    assertEquals(jobInfo.minSalary(), jobCreationRequest.getMinSalary());
    assertEquals(jobInfo.maxSalary(), jobCreationRequest.getMaxSalary());
  }

  @Test
  void givenADataAccessException_whenTheJobRequestIsCreated_thenThrowDataAccessException() {
    // Given
    DataAccessException dataAccessException = mock(DataAccessException.class);
    GeneralJobDescriptionInfoDto jobInfo = GeneralJobInfoDtoFactory.createGeneralInfoDto().build();

    // When & then
    when(jobCreationRepository.save(any())).thenThrow(dataAccessException);
    assertThrows(DataAccessException.class, () -> jobRequestService.createJobRequest(jobInfo, vacancyDto));
  }

  @Test
  void givenNoSimilairRequestInDB_whenJobIsUnique_thenReturnTrue() {
    //given
    var jobInfo = GeneralJobInfoDtoFactory.createGeneralInfoDto().build();
    when(jobCreationRepository.findByJobTitleAndFunctionGroupAndCompanyNameAndCreationDateAfter(any(), any(), any(),
        any())).thenReturn(
        Optional.empty());
    //when & then
    assertTrue(jobRequestService.isUniqueJobRequest(jobInfo));
  }

  @Test
  void givenSimilairRequestInDB_whenJobIsUnique_thenReturnFalse() {
    //given
    var jobInfo = GeneralJobInfoDtoFactory.createGeneralInfoDto().build();
    when(jobCreationRepository.findByJobTitleAndFunctionGroupAndCompanyNameAndCreationDateAfter(any(), any(), any(),
        any())).thenReturn(
        Optional.of(mock(JobCreationRequest.class)));
    //when & then
    assertFalse(jobRequestService.isUniqueJobRequest(jobInfo));
  }

  @Test
  void givenRequestExist_whenGetJobRequest_thenReturnJobRequestDto() {
    //given
    var creationRequest = JobCreationRequestFactory.createJobCreationRequest().build();
    when(jobCreationRepository.findById(any())).thenReturn(Optional.of(creationRequest));

    //when
    JobCreationResponseDto result = jobRequestService.getJobRequest(1L);

    //then
    assertEquals(result.companyName(), creationRequest.getCompanyName());
    assertEquals(result.functionGroup(), creationRequest.getFunctionGroup());
    assertEquals(result.jobTitle(), creationRequest.getJobTitle());
    assertEquals(result.minSalary(), creationRequest.getMinSalary());
    assertEquals(result.maxSalary(), creationRequest.getMaxSalary());
  }

  @Test
  void givenRequestDoesNotExist_whenGetJobRequest_thenThrowEntityNotFoundException() {
    //given
    when(jobCreationRepository.findById(any())).thenReturn(Optional.empty());

    //when & then
    assertThrows(EntityNotFoundException.class, () -> jobRequestService.getJobRequest(1L));
  }

  @Test
  void givenDatabaseDown_whenGetJobRequest_thenThrowDataAccessException() {
    //given
    DataAccessException dataAccessException = mock(DataAccessException.class);
    when(jobCreationRepository.findById(any())).thenThrow(dataAccessException);

    //when & them
    assertThrows(DataAccessException.class, () -> jobRequestService.getJobRequest(1L));
  }

}