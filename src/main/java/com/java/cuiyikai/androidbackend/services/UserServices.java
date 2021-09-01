package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServices {
    @Autowired
    UserMapper userMapper;

    public User selectUserByUsername(String username) {
        return userMapper.queryUserByUsername(username);
    }

    public boolean checkPassword(String username, String password) {
        User user = selectUserByUsername(username);
        return user != null && user.getPassword().equals(password) && user.isChecked();
    }

    public boolean checkUsername(String username) {
        return userMapper.queryUserByUsername(username) == null;
    }

    public void registerNewUser(String username, String password, String email) {
        userMapper.insertNewUser(username, password, email);
    }

    public void updateCheckEmailStatus(String username) {
        userMapper.updateChekcedEmailStatus(username);
    }

    public void updateUserInfo(User user, boolean changedEmail) {
        userMapper.updateUserInfo(user.getPassword(), user.getEmail(), !changedEmail, user.getId());
    }
}
