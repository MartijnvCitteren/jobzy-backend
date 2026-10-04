package app.jobzy.api.adapter.out.ai.support.prompt;

import app.jobzy.api.adapter.out.ai.PromptBase;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Builds a {@link Prompt} by filling a template, fixed for a single {@link PromptPurpose}, with
 * user-supplied input. Each implementation corresponds to one purpose; callers select the matching
 * implementation (see {@link PromptFactory}).
 */
interface PromptCreator {

  /**
   * Creates a prompt by filling this creator's template with the given input.
   *
   * @param promptInput the user-supplied input to fill the template with
   * @return the resulting prompt
   */
  Prompt create(PromptBase promptInput);
}
