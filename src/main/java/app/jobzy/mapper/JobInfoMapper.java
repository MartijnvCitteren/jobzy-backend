package app.jobzy.mapper;

import app.jobzy.domain.dto.JobInfo;
import app.jobzy.domain.dto.request.JobInfoRequestDto;
import java.util.UUID;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobInfoMapper {

  JobInfo toJobInfo(JobInfoRequestDto dto, UUID companyInfoToken);

}
