//package com.jobly_jobs.factory;
//
//import com.jobly_jobs.domain.dto.request.JobInfoRequestDto;
//import com.jobly_jobs.domain.enums.Language;
//import com.jobly_jobs.domain.enums.WritingStyle;
//
//public class JobCreationRequestDtoFactory {
//    public static JobInfoRequestDto.JobCreationRequestDtoBuilder createJobDescriptionInputDto() {
//        return JobInfoRequestDto.builder()
//                .jobSummary("This is a job summary")
//                .tasks("These are the tasks")
//                .skills("These are the skills")
//                .teamDescription("This is the team description")
//                .writingStyle(WritingStyle.CASUAL)
//                .language(Language.ENGLISH)
//                .generalInfo(null);
//
//    }
//}
