package app.jobzy.api.adapter.out.ai.support.mistral;

import app.jobzy.api.adapter.out.ai.AiResponseBase;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

@RequiredArgsConstructor
public class MistralAi {
  ChatModel mistral;

  public ChatResponse generateText(Prompt prompt, Class<? extends AiResponseBase> format) {
    return null;
  }
}
