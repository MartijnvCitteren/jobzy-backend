package com.jobly_jobs.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobly_jobs.domain.dto.AiCompanyInfo;
import com.jobly_jobs.exceptions.SystemException;
import com.jobly_jobs.factory.AiCompanyInfoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.params.SetParams;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CacheCompanyServiceTest {

    @Mock
    private RedisClient redisClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CacheCompanyService cacheCompanyService;

    private AiCompanyInfo aiCompanyInfo;
    private UUID uuid;
    private String companyWebsite;
    private String jsonString;

    @BeforeEach
    void setUp() {
        aiCompanyInfo = AiCompanyInfoFactory.createAiCompanyInfo();
        uuid = UUID.randomUUID();
        companyWebsite = "https://example.com";
        jsonString = "{\"companyDescription\":\"test\",\"companyGoal\":\"goal\",\"uspForEmployees\":\"usp\"," +
                "\"toneOfVoice\":\"tone\"}";
    }

    @Test
    @DisplayName("Given valid UUID and AiCompanyInfo, when putting into cache, then store successfully")
    void givenValidUuidAndAiCompanyInfo_whenPuttingIntoCache_thenStoreSuccessfully() throws JsonProcessingException {
        // Given
        when(objectMapper.writeValueAsString(aiCompanyInfo)).thenReturn(jsonString);

        // When
        cacheCompanyService.put(uuid, aiCompanyInfo);

        // Then
        verify(objectMapper).writeValueAsString(aiCompanyInfo);
        verify(redisClient).set(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Given JSON processing exception during serialization, when putting into cache, then throw " +
            "SystemException")
    void givenJsonProcessingException_whenPuttingIntoCache_thenThrowSystemException() throws JsonProcessingException {
        // Given
        String errorMessage = "Serialization error";
        when(objectMapper.writeValueAsString(aiCompanyInfo)).thenThrow(new JsonProcessingException(errorMessage) {
        });

        // When & Then
        assertThrows(SystemException.class, () -> cacheCompanyService.put(uuid, aiCompanyInfo));
        verify(objectMapper).writeValueAsString(aiCompanyInfo);
    }

    @Test
    @DisplayName("Given valid company website and UUID, when putting into cache, then store successfully")
    void givenValidCompanyWebsiteAndUuid_whenPuttingIntoCache_thenStoreSuccessfully() {
        cacheCompanyService.put(companyWebsite, uuid);

        // Then
        verify(redisClient).set(eq(companyWebsite), eq(uuid.toString()), any(SetParams.class));
    }

    @Test
    @DisplayName("Given valid UUID in cache, when getting company info, then return AiCompanyInfo")
    void givenValidUuidInCache_whenGettingCompanyInfo_thenReturnAiCompanyInfo() throws JsonProcessingException {
        // Given
        when(redisClient.get(uuid.toString())).thenReturn(jsonString);
        when(objectMapper.readValue(jsonString, AiCompanyInfo.class)).thenReturn(aiCompanyInfo);

        // When
        Optional<AiCompanyInfo> result = cacheCompanyService.getCompanyInfo(uuid);

        // Then
        assertTrue(result.isPresent());
        assertEquals(aiCompanyInfo, result.get());
        verify(redisClient).get(uuid.toString());
        verify(objectMapper).readValue(jsonString, AiCompanyInfo.class);
    }

    @Test
    @DisplayName("Given UUID not in cache, when getting company info, then return empty Optional")
    void givenUuidNotInCache_whenGettingCompanyInfo_thenReturnEmptyOptional() {
        // Given
        when(redisClient.get(uuid.toString())).thenReturn(null);

        // When
        Optional<AiCompanyInfo> result = cacheCompanyService.getCompanyInfo(uuid);

        // Then
        assertTrue(result.isEmpty());
        verify(redisClient).get(uuid.toString());
    }

    @Test
    @DisplayName("Given empty string from cache, when getting company info, then return empty Optional")
    void givenEmptyStringFromCache_whenGettingCompanyInfo_thenReturnEmptyOptional() {
        // Given
        when(redisClient.get(uuid.toString())).thenReturn("");

        // When
        Optional<AiCompanyInfo> result = cacheCompanyService.getCompanyInfo(uuid);

        // Then
        assertTrue(result.isEmpty());
        verify(redisClient).get(uuid.toString());
    }

    @Test
    @DisplayName("Given JSON processing exception during deserialization, when getting company info, then throw " +
            "SystemException")
    void givenJsonProcessingException_whenGettingCompanyInfo_thenThrowSystemException() throws JsonProcessingException {
        // Given
        String errorMessage = "Deserialization error";
        when(redisClient.get(uuid.toString())).thenReturn(jsonString);
        when(objectMapper.readValue(jsonString, AiCompanyInfo.class)).thenThrow(
                new JsonProcessingException(errorMessage) {
                });

        // When & Then
        SystemException exception = assertThrows(SystemException.class, () -> cacheCompanyService.getCompanyInfo(uuid));

        verify(redisClient).get(uuid.toString());
        verify(objectMapper).readValue(jsonString, AiCompanyInfo.class);
    }

    @Test
    @DisplayName("Given valid company website in cache, when getting UUID, then return UUID")
    void givenValidCompanyWebsiteInCache_whenGettingUuid_thenReturnUuid() {
        // Given
        when(redisClient.get(companyWebsite)).thenReturn(uuid.toString());

        // When
        Optional<UUID> result = cacheCompanyService.getUUID(companyWebsite);

        // Then
        assertTrue(result.isPresent());
        assertEquals(uuid, result.get());
        verify(redisClient).get(companyWebsite);
    }

    @Test
    @DisplayName("Given company website not in cache, when getting UUID, then return empty Optional")
    void givenCompanyWebsiteNotInCache_whenGettingUuid_thenReturnEmptyOptional() {
        // Given
        when(redisClient.get(companyWebsite)).thenReturn(null);

        // When
        Optional<UUID> result = cacheCompanyService.getUUID(companyWebsite);

        // Then
        assertTrue(result.isEmpty());
        verify(redisClient).get(companyWebsite);
    }

    @Test
    @DisplayName("Given empty string from cache, when getting UUID, then return empty Optional")
    void givenEmptyStringFromCache_whenGettingUuid_thenReturnEmptyOptional() {
        // Given
        when(redisClient.get(companyWebsite)).thenReturn("");

        // When
        Optional<UUID> result = cacheCompanyService.getUUID(companyWebsite);

        // Then
        assertTrue(result.isEmpty());
        verify(redisClient).get(companyWebsite);
    }
}