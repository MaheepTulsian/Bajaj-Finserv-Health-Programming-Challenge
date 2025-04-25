package com.bajaj.model;

import java.util.List;

public class User {
    private Integer id;
    private String name;
    private List<Integer> follows;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getFollows() {
        return follows;
    }

    public void setFollows(List<Integer> follows) {
        this.follows = follows;
    }
}