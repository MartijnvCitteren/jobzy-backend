package com.jobly_jobs.domain.entity;


import com.jobly_jobs.domain.enums.FunctionGroup;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter()
@Table(name = "job_creation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class JobCreationRequest extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Setter(value = AccessLevel.NONE)
  private long id;
  private String jobTitle;
  private FunctionGroup functionGroup;
  private String companyName;
  private BigDecimal minSalary;
  private BigDecimal maxSalary;

  @OneToOne(cascade = CascadeType.ALL)
  private VacancyText vacancyText;

  public JobCreationRequest(String jobTitle, FunctionGroup functionGroup, String companyName, BigDecimal minSalary,
      BigDecimal maxSalary, LocalDateTime creationDate, LocalDateTime updateTime) {
    this.jobTitle = jobTitle;
    this.functionGroup = functionGroup;
    this.companyName = companyName;
    this.minSalary = minSalary;
    this.maxSalary = maxSalary;
    this.creationDate = creationDate;
    this.updateDate = updateTime;
  }

}

