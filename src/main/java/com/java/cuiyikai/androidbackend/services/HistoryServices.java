package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.VisitHistory;
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

    public List<SearchHistory> getLatestHistoryByUsername(String username) {
        return historyMapper.queryLatestHistoryByUsername(username);
    }

    public void addHistory(String username, String content) {
        historyMapper.addHistory(userMapper.queryUserByUsername(username).getId(), content);
    }

    public List<VisitHistory> getVisitHistoryByUserId(int id) {
        return historyMapper.queryVisitHistoryByUserId(id);
    }

    public List<VisitHistory> getLatestVisitHistoryByUserId(int id) { return historyMapper.queryLatestVisitHistoryByUserId(id); }

    public void addVisitHistory(int userId, String name, String subject) {
        historyMapper.addVisitHistory(userId, name, subject);
    }
}
