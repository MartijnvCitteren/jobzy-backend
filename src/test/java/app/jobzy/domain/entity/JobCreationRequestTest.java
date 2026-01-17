package app.jobzy.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.jobzy.factory.JobCreationRequestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobCreationRequestTest {


  @Test
  void givenExistingJobCreationRequest_whenGetAnyValue_thenValueIsReturned() {
    //given
    JobCreationRequest jobCreationRequest = JobCreationRequestFactory.createJobCreationRequest().build();

    //when
    String jobTitle = jobCreationRequest.getJobTitle();

    //then
    assertEquals("Software Engineer", jobTitle);
    assertNotNull(jobCreationRequest.getFunctionGroup());
    assertNotNull(jobCreationRequest.getCompanyName());
    assertNotNull(jobCreationRequest.getMinSalary());
    assertNotNull(jobCreationRequest.getMaxSalary());
  }

}