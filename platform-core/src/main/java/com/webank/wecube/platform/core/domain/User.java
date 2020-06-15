package com.webank.wecube.platform.core.domain;

public class User {
    private String userId;
    private String username;
    private String fullName;
    private String description;

    public User(String userId, String username, String fullName, String description) {
        super();
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.description = description;
    }

    public User() {
        super();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
