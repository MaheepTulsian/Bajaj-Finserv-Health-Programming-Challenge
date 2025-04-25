package com.bajaj.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class RetryUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);
    
    public static <T> T retry(Supplier<T> supplier, int maxAttempts, String operationName) {
        int attempts = 0;
        while (true) {
            try {
                attempts++;
                return supplier.get();
            } catch (Exception e) {
                if (attempts >= maxAttempts) {
                    logger.error("Operation '{}' failed after {} attempts: {}", 
                        operationName, attempts, e.getMessage());
                    throw e;
                }
                
                long backoffMs = calculateBackoff(attempts);
                logger.warn("Attempt {} for operation '{}' failed. Retrying in {} ms. Error: {}", 
                    attempts, operationName, backoffMs, e.getMessage());
                
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }
    
    private static long calculateBackoff(int attempt) {
        return 1000L * attempt; // Simple linear backoff
    }
}