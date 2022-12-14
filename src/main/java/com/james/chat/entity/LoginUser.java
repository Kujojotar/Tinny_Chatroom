package com.james.chat.entity;

import java.io.Serializable;

public class LoginUser implements Serializable {
    private String username;
    private String password;

    private static final long serialVersionUID = 63463762L;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
