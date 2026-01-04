package com.jobly_jobs.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Prompt {

  @Getter(value = AccessLevel.NONE)
  private String genericContext;
  private String companyDescription;
  private String teamDescription;
  private String dayToDayDescription;
  private String jobDescription;
  private String jobUniqueSellingPoints;
  private String requirements;
  private String offer;
  private String contactInformation;
  private String summary;
}
