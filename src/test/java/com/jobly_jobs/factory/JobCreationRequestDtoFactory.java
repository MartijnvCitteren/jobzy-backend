//package app.jobzy.factory;
//
//import app.jobzy.domain.dto.request.JobInfoRequestDto;
//import app.jobzy.domain.enums.Language;
//import app.jobzy.domain.enums.WritingStyle;
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
