package app.jobzy.api.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async}. The executor is Spring Boot's application task executor, which runs on
 * virtual threads because {@code spring.threads.virtual.enabled} is set. Tests replace it with a
 * synchronous executor, so asynchronous flows are tested without sleeping or polling.
 */
@Configuration
@EnableAsync
public class AsyncConfig {}
