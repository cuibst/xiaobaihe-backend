package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServices {
    @Autowired
    UserMapper userMapper;

    /**
     * Get a user with matching username.
     * @param username the username you want to query.
     * @return The corresponding {@link User}
     */
    public User selectUserByUsername(String username) {
        return userMapper.queryUserByUsername(username);
    }

    /**
     * Checks whether the username and the password are matched.
     * @param username related username
     * @param password related password
     * @return true when username and password match.
     */
    public boolean checkPassword(String username, String password) {
        User user = selectUserByUsername(username);
        return user != null && user.getPassword().equals(password) && user.isChecked();
    }

    /**
     * Checks whether the username is new.
     * @param username related username
     * @return the validity of the username.
     */
    public boolean checkUsername(String username) {
        return userMapper.queryUserByUsername(username) == null;
    }

    /**
     * Register a new user.
     * @param username related username
     * @param password related password
     * @param email user's email
     */
    public void registerNewUser(String username, String password, String email) {
        userMapper.insertNewUser(username, password, email);
    }

    /**
     * Check the email status of the given user.
     * @param username related username.
     */
    public void updateCheckEmailStatus(String username) {
        userMapper.updateChekcedEmailStatus(username);
    }

    /**
     * Update user's info.
     * @param user the {@link User} contain new info
     * @param changedEmail whether the email is changed
     */
    public void updateUserInfo(User user, boolean changedEmail) {
        userMapper.updateUserInfo(user.getPassword(), user.getEmail(), !changedEmail, user.getId());
    }
}
