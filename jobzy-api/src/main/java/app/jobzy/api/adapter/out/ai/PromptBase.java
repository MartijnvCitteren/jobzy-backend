package app.jobzy.api.adapter.out.ai;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PromptBase {
  private Map<String, String> promptValues;

  protected PromptBase(Map<String, String> values) {
    this.promptValues = values;
  }
}
