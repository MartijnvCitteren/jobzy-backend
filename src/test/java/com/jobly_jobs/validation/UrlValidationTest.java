package com.jobly_jobs.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UrlValidationTest {

    @InjectMocks
    private UrlValidation urlValidation;

    @Test
    @DisplayName("given existing domain full https url, then return true")
    void testFullHttpsUrlValidation() {
        var url = "https://www.google.com";
        assertTrue(urlValidation.isValid(url));
    }

    @Test
    @DisplayName("given existing www.address.extension, then return true")
    void testWwwUrlValidation() {
        var url = "www.google.com";
        assertTrue(urlValidation.isValid(url));
    }

    @Test
    @DisplayName("given strange valid url, then return true")
    void testStrangeUrlValidation() {
        var url = "https://about.google/";
        assertTrue(urlValidation.isValid(url));
    }

    @Test
    @DisplayName("given domain does not exists, then return false")
    void testDomainDoesNotExists() {
        var url = "www.johndoewantstests.com";
        assertFalse(urlValidation.isValid(url));
    }

}