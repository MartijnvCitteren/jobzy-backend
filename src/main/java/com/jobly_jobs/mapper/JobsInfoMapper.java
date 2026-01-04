package com.jobly_jobs.mapper;

import com.jobly_jobs.domain.dto.JobInfo;
import com.jobly_jobs.domain.dto.request.JobInfoRequestDto;
import java.util.UUID;
import org.mapstruct.Mapper;

@Mapper
public interface JobsInfoMapper {
  JobInfo toJobInfo(JobInfoRequestDto dto, UUID companyInfoToken);

}
