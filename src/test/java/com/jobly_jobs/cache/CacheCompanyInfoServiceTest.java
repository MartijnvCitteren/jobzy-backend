package com.jobly_jobs.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jobly_jobs.domain.dto.AiCompanyInfo;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CacheCompanyInfoServiceTest {

  private CacheCompanyInfoService cacheCompanyInfoService;
  private UUID companyInfoId;
  private AiCompanyInfo aiCompanyInfo;

  @BeforeEach
  void setUp() {
    cacheCompanyInfoService = new CacheCompanyInfoService();
    companyInfoId = UUID.randomUUID();
    aiCompanyInfo = new AiCompanyInfo(
        "Test Company Description",
        "Test Company Goal",
        "Test USP for Employees",
        "Professional and friendly"
    );
  }

  @Test
  @DisplayName("Given valid UUID and AiCompanyInfo, when putting into cache, then retrieve successfully")
  void givenValidUuidAndAiCompanyInfo_whenPuttingIntoCache_thenRetrieveSuccessfully() {
    // When
    cacheCompanyInfoService.putCompanyInfo(companyInfoId, aiCompanyInfo);
    Optional<AiCompanyInfo> result = cacheCompanyInfoService.getCompanyInfo(companyInfoId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(aiCompanyInfo, result.get());
  }

  @Test
  @DisplayName("Given UUID not in cache, when getting company info, then return empty Optional")
  void givenUuidNotInCache_whenGettingCompanyInfo_thenReturnEmptyOptional() {
    // When
    Optional<AiCompanyInfo> result = cacheCompanyInfoService.getCompanyInfo(UUID.randomUUID());

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Given multiple UUIDs in cache, when getting company info, then return correct info for each")
  void givenMultipleUuidsInCache_whenGettingCompanyInfo_thenReturnCorrectInfoForEach() {
    // Given
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    AiCompanyInfo info1 = new AiCompanyInfo(
        "Company 1 Description",
        "Company 1 Goal",
        "Company 1 USP",
        "Formal"
    );
    AiCompanyInfo info2 = new AiCompanyInfo(
        "Company 2 Description",
        "Company 2 Goal",
        "Company 2 USP",
        "Casual"
    );

    // When
    cacheCompanyInfoService.putCompanyInfo(uuid1, info1);
    cacheCompanyInfoService.putCompanyInfo(uuid2, info2);

    // Then
    Optional<AiCompanyInfo> result1 = cacheCompanyInfoService.getCompanyInfo(uuid1);
    Optional<AiCompanyInfo> result2 = cacheCompanyInfoService.getCompanyInfo(uuid2);

    assertTrue(result1.isPresent());
    assertTrue(result2.isPresent());
    assertEquals(info1, result1.get());
    assertEquals(info2, result2.get());
  }

  @Test
  @DisplayName("Given existing UUID in cache, when updating with new company info, then return new info")
  void givenExistingUuidInCache_whenUpdatingWithNewCompanyInfo_thenReturnNewInfo() {
    // Given
    AiCompanyInfo oldInfo = new AiCompanyInfo(
        "Old Description",
        "Old Goal",
        "Old USP",
        "Old Tone"
    );
    AiCompanyInfo newInfo = new AiCompanyInfo(
        "New Description",
        "New Goal",
        "New USP",
        "New Tone"
    );
    cacheCompanyInfoService.putCompanyInfo(companyInfoId, oldInfo);

    // When
    cacheCompanyInfoService.putCompanyInfo(companyInfoId, newInfo);
    Optional<AiCompanyInfo> result = cacheCompanyInfoService.getCompanyInfo(companyInfoId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(newInfo, result.get());
  }

  @Test
  @DisplayName("Given company info with all fields populated, when caching and retrieving, then all fields match")
  void givenCompanyInfoWithAllFieldsPopulated_whenCachingAndRetrieving_thenAllFieldsMatch() {
    // Given
    AiCompanyInfo fullInfo = new AiCompanyInfo(
        "Full company description with detailed information",
        "Achieve market leadership in technology sector",
        "Excellent work-life balance and competitive compensation",
        "Innovative, dynamic, and supportive"
    );

    // When
    cacheCompanyInfoService.putCompanyInfo(companyInfoId, fullInfo);
    Optional<AiCompanyInfo> result = cacheCompanyInfoService.getCompanyInfo(companyInfoId);

    // Then
    assertTrue(result.isPresent());
    AiCompanyInfo retrievedInfo = result.get();
    assertEquals(fullInfo.companyDescription(), retrievedInfo.companyDescription());
    assertEquals(fullInfo.companyGoal(), retrievedInfo.companyGoal());
    assertEquals(fullInfo.uspForEmployees(), retrievedInfo.uspForEmployees());
    assertEquals(fullInfo.toneOfVoice(), retrievedInfo.toneOfVoice());
  }
}

