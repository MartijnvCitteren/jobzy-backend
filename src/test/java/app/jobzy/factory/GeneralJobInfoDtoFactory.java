package app.jobzy.factory;

import app.jobzy.domain.dto.request.GeneralJobDescriptionInfoDto;
import app.jobzy.domain.enums.FunctionGroup;
import java.math.BigDecimal;

public class GeneralJobInfoDtoFactory {

  public static GeneralJobDescriptionInfoDto.GeneralJobDescriptionInfoDtoBuilder createGeneralInfoDto() {
    return GeneralJobDescriptionInfoDto.builder()
        .jobTitle("Software Engineer")
        .functionGroup(FunctionGroup.valueOf("ENGINEERING"))
        .companyName("Google")
        .minSalary(BigDecimal.valueOf(100000))
        .maxSalary(BigDecimal.valueOf(150000));
  }
}
