package com.github.hdghg.capcha.domain;

import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * Model class that represents user of the application
 */
public class ApplicationUser {

    @Id
    private String id;
    public final String username;
    public final String password;
    public final List<String> roleList;

    public ApplicationUser(String username, String password, List<String> roleList) {
        this.username = username;
        this.password = password;
        this.roleList = roleList;
    }

    public ApplicationUser setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getRoleList() {
        return roleList;
    }
}
