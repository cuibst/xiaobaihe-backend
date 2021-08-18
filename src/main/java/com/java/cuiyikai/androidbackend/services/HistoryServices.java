package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.History;
import com.java.cuiyikai.androidbackend.mapper.HistoryMapper;
import com.java.cuiyikai.androidbackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryServices {

    @Autowired
    HistoryMapper historyMapper;

    @Autowired
    UserMapper userMapper;

    public List<History> getLatestHistoryByUsername(String username) {
        return historyMapper.queryLatestHistoryByUsername(username);
    }

    public void addHistory(String username, String content) {
        historyMapper.addHistory(userMapper.queryUserByUsername(username).getId(), content);
    }
}
