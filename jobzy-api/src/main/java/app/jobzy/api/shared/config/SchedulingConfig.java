package app.jobzy.api.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Scheduled} jobs, such as the purge of unconfirmed registrations. Every instance
 * runs them, so a job must be safe to run on several instances at once.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
