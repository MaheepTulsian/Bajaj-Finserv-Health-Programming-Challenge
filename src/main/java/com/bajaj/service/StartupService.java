package com.bajaj.service;

import com.bajaj.model.SolutionResponse;
import com.bajaj.model.WebhookRequest;
import com.bajaj.model.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StartupService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private ProblemSolverService problemSolverService;
    
    @Value("${api.generate-webhook-url}")
    private String generateWebhookUrl;
    
    @Value("${user.name}")
    private String userName;
    
    @Value("${user.regNo}")
    private String regNo;
    
    @Value("${user.email}")
    private String email;
    
    @Override
    public void run(ApplicationArguments args) {
        try {
            // Create request object
            WebhookRequest request = new WebhookRequest();
            request.setName(userName);
            request.setRegNo(regNo);
            request.setEmail(email);
            
            logger.info("Making request to generate webhook with user: {}, regNo: {}", userName, regNo);
            
            // Make POST request to generate webhook
            WebhookResponse response = restTemplate.postForObject(
                generateWebhookUrl, 
                request, 
                WebhookResponse.class
            );

            logger.info("Response received: {}", response);
            
            if (response == null) {
                logger.error("Received null response from webhook generation endpoint");
                return;
            }
            
            logger.info("Successfully received webhook: {}", response.getWebhook());
            
            // Solve the problem
            Object solution = problemSolverService.solve(response.getData(), regNo);
            
            // Create solution response
            SolutionResponse solutionResponse = new SolutionResponse();
            solutionResponse.setRegNo(regNo);
            solutionResponse.setOutcome(solution);
            
            logger.info("Prepared solution: {}", solutionResponse.getOutcome());
            
            // Send solution to webhook
            webhookService.sendSolution(
                response.getWebhook(), 
                response.getAccessToken(), 
                solutionResponse
            );
        } catch (Exception e) {
            logger.error("Error during startup processing", e);
        }
    }
}