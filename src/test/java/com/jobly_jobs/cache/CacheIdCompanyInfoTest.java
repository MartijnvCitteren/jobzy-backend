package com.jobly_jobs.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheIdCompanyInfoTest {

    private CacheIdCompanyInfo cacheIdCompanyInfo;
    private UUID companyInfoId;
    private String companyWebsite;

    @BeforeEach
    void setUp() {
        cacheIdCompanyInfo = new CacheIdCompanyInfo();
        companyInfoId = UUID.randomUUID();
        companyWebsite = "https://example.com";
    }

    @Test
    @DisplayName("Given valid website and UUID, when putting into cache, then retrieve successfully")
    void givenValidWebsiteAndUuid_whenPuttingIntoCache_thenRetrieveSuccessfully() {
        // When
        cacheIdCompanyInfo.putCompanyWebsite(companyWebsite, companyInfoId);
        Optional<UUID> result = cacheIdCompanyInfo.getUuid(companyWebsite);

        // Then
        assertTrue(result.isPresent());
        assertEquals(companyInfoId, result.get());
    }

    @Test
    @DisplayName("Given website not in cache, when getting UUID, then return empty Optional")
    void givenWebsiteNotInCache_whenGettingUuid_thenReturnEmptyOptional() {
        // When
        Optional<UUID> result = cacheIdCompanyInfo.getUuid("https://notincache.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given multiple websites in cache, when getting UUID, then return correct UUID for each")
    void givenMultipleWebsitesInCache_whenGettingUuid_thenReturnCorrectUuidForEach() {
        // Given
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        String website1 = "https://example1.com";
        String website2 = "https://example2.com";

        // When
        cacheIdCompanyInfo.putCompanyWebsite(website1, uuid1);
        cacheIdCompanyInfo.putCompanyWebsite(website2, uuid2);

        // Then
        Optional<UUID> result1 = cacheIdCompanyInfo.getUuid(website1);
        Optional<UUID> result2 = cacheIdCompanyInfo.getUuid(website2);

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(uuid1, result1.get());
        assertEquals(uuid2, result2.get());
    }

    @Test
    @DisplayName("Given existing website in cache, when updating with new UUID, then return new UUID")
    void givenExistingWebsiteInCache_whenUpdatingWithNewUuid_thenReturnNewUuid() {
        // Given
        UUID oldUuid = UUID.randomUUID();
        UUID newUuid = UUID.randomUUID();
        cacheIdCompanyInfo.putCompanyWebsite(companyWebsite, oldUuid);

        // When
        cacheIdCompanyInfo.putCompanyWebsite(companyWebsite, newUuid);
        Optional<UUID> result = cacheIdCompanyInfo.getUuid(companyWebsite);

        // Then
        assertTrue(result.isPresent());
        assertEquals(newUuid, result.get());
    }
}

