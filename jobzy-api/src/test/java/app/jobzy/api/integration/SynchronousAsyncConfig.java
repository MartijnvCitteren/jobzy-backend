package app.jobzy.api.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

/**
 * Replaces Spring Boot's application task executor with one that runs {@code @Async} work on the
 * calling thread. A started description generation is then completed before the {@code POST}
 * returns, so tests can assert on the outcome without sleeping or polling. Spring Boot backs off
 * from creating its own executor when this bean exists.
 */
@TestConfiguration(proxyBeanMethods = false)
public class SynchronousAsyncConfig {

  @Bean(name = {"applicationTaskExecutor", "taskExecutor"})
  SyncTaskExecutor applicationTaskExecutor() {
    return new SyncTaskExecutor();
  }
}
