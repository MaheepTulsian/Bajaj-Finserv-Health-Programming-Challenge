package com.bajaj.service;

import com.bajaj.model.User;
import com.bajaj.model.WebhookResponse.DataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProblemSolverService {

    private static final Logger logger = LoggerFactory.getLogger(ProblemSolverService.class);

    public Object solve(DataWrapper data, String regNo) {
        // Extract last two digits of regNo
        String lastTwoDigits = regNo.substring(regNo.length() - 2);
        int lastTwoDigitsNumeric;
        try {
            lastTwoDigitsNumeric = Integer.parseInt(lastTwoDigits);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse last two digits of regNo: {}", regNo);
            lastTwoDigitsNumeric = 1; // Default to question 1 if parsing fails
        }
        
        logger.info("Reg number last two digits: {}, solving question {}", 
                   lastTwoDigitsNumeric, 
                   lastTwoDigitsNumeric % 2 == 0 ? "2" : "1");
        
        if (lastTwoDigitsNumeric % 2 != 0) {
            // Odd - Question 1: Mutual Followers
            return solveMutualFollowers(data.getUsers());
        } else {
            // Even - Question 2: Nth-Level Followers
            if (data.getN() == null || data.getFindId() == null) {
                logger.warn("Question 2 parameters missing in response. Check response format.");
                // You might want to examine what's actually in the data
                logger.info("Response data content: {}", data);
                // Return a default response or handle as needed
                return new ArrayList<>();
            }
            return solveNthLevelFollowers(data.getUsers(), data.getFindId(), data.getN());
        }
    }
    
    private List<List<Integer>> solveMutualFollowers(List<User> users) {
        List<List<Integer>> result = new ArrayList<>();
        
        // Create a mapping of user IDs to the set of users they follow
        Map<Integer, Set<Integer>> followsMap = new HashMap<>();
        
        for (User user : users) {
            followsMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }
        
        // Find mutual followers
        Set<String> processedPairs = new HashSet<>(); // To avoid duplicates
        
        for (User user : users) {
            int userId = user.getId();
            
            if (user.getFollows() == null) {
                continue;
            }
            
            for (Integer followedId : user.getFollows()) {
                // Generate a unique key for this pair
                String pairKey = Math.min(userId, followedId) + ":" + Math.max(userId, followedId);
                
                // Skip if already processed
                if (processedPairs.contains(pairKey)) {
                    continue;
                }
                
                processedPairs.add(pairKey);
                
                // Check if mutual follow exists
                if (followsMap.containsKey(followedId) && 
                    followsMap.get(followedId).contains(userId)) {
                    
                    // Add the pair with smaller ID first
                    List<Integer> pair = Arrays.asList(
                        Math.min(userId, followedId),
                        Math.max(userId, followedId)
                    );
                    
                    result.add(pair);
                    logger.info("Found mutual follow pair: {}", pair);
                }
            }
        }
        
        return result;
    }
    
    private List<Integer> solveNthLevelFollowers(List<User> users, Integer findId, Integer n) {
        // Add null checks
        if (findId == null || n == null) {
            logger.error("findId or n is null in the response data");
            return new ArrayList<>(); // Return empty list or handle appropriately
        }

        // Create a map for quick lookup
        Map<Integer, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
        
        logger.info("Finding level {} followers for user ID {}", n, findId);
        
        // BFS to find nth level followers
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(findId);
        visited.add(findId);
        
        int level = 0;
        
        while (!queue.isEmpty() && level < n) {
            int size = queue.size();
            logger.debug("Processing level {}, queue size: {}", level, size);
            
            for (int i = 0; i < size; i++) {
                Integer currentId = queue.poll();
                User currentUser = userMap.get(currentId);
                
                if (currentUser != null && currentUser.getFollows() != null) {
                    for (Integer followId : currentUser.getFollows()) {
                        if (!visited.contains(followId)) {
                            visited.add(followId);
                            queue.offer(followId);
                            logger.debug("Added user {} to level {}", followId, level + 1);
                        }
                    }
                }
            }
            level++;
        }
        
        // Extract all IDs at the nth level
        List<Integer> result = new ArrayList<>(queue);
        Collections.sort(result);
        
        logger.info("Found {} followers at level {}: {}", result.size(), n, result);
        return result;
    }
}