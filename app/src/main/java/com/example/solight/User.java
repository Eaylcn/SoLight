package com.example.solight;

public class User {
    private String id;
    private String authorityLevel;

    // Constructor, getters and setters
    public User(String id, String authorityLevel) {
        this.id = id;
        this.authorityLevel = authorityLevel;
    }

    public String getId() {
        return id;
    }

    public String getAuthorityLevel() {
        return authorityLevel;
    }
}
