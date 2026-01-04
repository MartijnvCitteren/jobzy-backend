package com.jobly_jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class JoblyJobsApplication {

  public static void main(String[] args) {
    SpringApplication.run(JoblyJobsApplication.class, args);
  }

}
