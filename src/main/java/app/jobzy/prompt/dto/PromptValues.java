package app.jobzy.prompt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptValues<T> {
  private String task;
  private String scope;
  private Limits limits;
  private Action action;
  private T requestObject;

  public static <T> PromptValuesBuilder<T> builderForRequest(T request) {
    return PromptValues.<T>builder().requestObject(request);
  }
}
