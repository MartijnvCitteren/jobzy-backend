package app.jobzy.prompt.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Limits {

  private String mustDo;
  private String mustAvoid;
  private String missingInfo;
  private String factuality;
}
