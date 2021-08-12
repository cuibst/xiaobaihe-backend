package com.java.cuiyikai.androidbackend.entity;

/**
 * The entity class for mybatis to save result from table 'username'
 */
public class User {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    @Override
    public String toString() {
        return "User{" +
                "id = " + id +
                ", username = '" + username + "'" +
                ", password = '" + password + "'" +
                "}";
    }
}
