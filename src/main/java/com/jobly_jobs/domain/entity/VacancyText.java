package com.jobly_jobs.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@Table(name = "vacancy_text")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VacancyText extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Setter(value = AccessLevel.NONE)
  private Long id;

  @Lob
  @Column(columnDefinition = "text")
  private String summary;

  @Lob
  @Column(columnDefinition = "text")
  private String companyDescription;

  @Lob
  @Column(columnDefinition = "text")
  private String teamDescription;

  @Lob
  @Column(columnDefinition = "text")
  private String dayToDayDescription;

  @Lob
  @Column(columnDefinition = "text")
  private String jobDescription;

  @Lob
  @Column(columnDefinition = "text")
  private String jobUniqueSellingPoints;

  @Lob
  @Column(columnDefinition = "text")
  private String requirements;

  @Lob
  @Column(columnDefinition = "text")
  private String offer;

  @Lob
  @Column(columnDefinition = "text")
  private String contactInformation;

  @OneToOne(mappedBy = "vacancyText")
  private JobCreationRequest jobCreationRequest;
}
