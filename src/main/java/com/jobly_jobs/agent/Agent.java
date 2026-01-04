package com.jobly_jobs.agent;

public interface Agent<T, R> {
  R execute(T prompt);
}
