package com.bajaj.service;

import com.bajaj.model.SolutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${retry.max-attempts:4}")
    private int maxAttempts;
    
    public void sendSolution(String webhookUrl, String accessToken, SolutionResponse solution) {
        int attempts = 0;
        boolean success = false;
        
        while (!success && attempts < maxAttempts) {
            attempts++;
            try {
                logger.info("Attempt {} to send solution to webhook: {}", attempts, webhookUrl);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", accessToken);
                
                HttpEntity<SolutionResponse> entity = new HttpEntity<>(solution, headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Successfully sent solution to webhook after {} attempts", attempts);
                    success = true;
                } else {
                    logger.warn("Webhook returned non-success status code: {}", response.getStatusCodeValue());
                    backoffSleep(attempts);
                }
            } catch (Exception e) {
                logger.error("Attempt {} failed with error: {}", attempts, e.getMessage());
                backoffSleep(attempts);
            }
        }
        
        if (!success) {
            logger.error("Failed to send solution to webhook after {} attempts", maxAttempts);
        }
    }
    
    private void backoffSleep(int attempt) {
        try {
            long sleepTime = 1000L * attempt; // Simple exponential backoff
            logger.info("Backing off for {} ms before retry", sleepTime);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted during backoff");
        }
    }
}