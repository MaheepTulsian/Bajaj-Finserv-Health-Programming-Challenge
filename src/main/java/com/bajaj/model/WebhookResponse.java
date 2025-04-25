package com.bajaj.model;

import java.util.List;

public class WebhookResponse {
    private String webhook;
    private String accessToken;
    private DataWrapper data;

    // Getters and Setters
    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public DataWrapper getData() {
        return data;
    }

    public void setData(DataWrapper data) {
        this.data = data;
    }

    public static class DataWrapper {
        private List<User> users;
        // For Question 2
        private Integer n;
        private Integer findId;

        // Getters and Setters
        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }

        public Integer getN() {
            return n;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        public Integer getFindId() {
            return findId;
        }

        public void setFindId(Integer findId) {
            this.findId = findId;
        }
    }
}