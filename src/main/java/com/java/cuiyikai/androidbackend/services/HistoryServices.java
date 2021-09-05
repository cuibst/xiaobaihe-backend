package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.User;
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

    public void addHistory(String username, String content, String subject) {
        User user = userMapper.queryUserByUsername(username);
        System.out.printf("Get count: %s %d%n", content, historyMapper.getMatchHistoryCount(user.getId(), content, subject));
        if(historyMapper.getMatchHistoryCount(user.getId(), content, subject) > 0) {
            historyMapper.updateOldHistory(user.getId(), content, subject);
            return;
        }
        System.out.println("add new history");
        historyMapper.addHistory(userMapper.queryUserByUsername(username).getId(), content, subject);
    }

    public void deleteAllUserHistory(int userId) {
        historyMapper.deleteAllUserHistory(userId);
    }

    public void deleteSingleUserHistory(int userId, String content, String subject) {
        historyMapper.deleteSingleHistory(userId, content, subject);
    }

    public List<VisitHistory> getVisitHistoryByUserId(int id) {
        return historyMapper.queryVisitHistoryByUserId(id);
    }

    public List<VisitHistory> getLatestVisitHistoryByUserId(int id) { return historyMapper.queryLatestVisitHistoryByUserId(id); }

    public void addVisitHistory(int userId, String name, String subject) {
        if(historyMapper.getMatchVisitHistoryCount(userId, name, subject) > 0) {
            historyMapper.updateVisitHistory(userId, name, subject);
            return;
        }
        historyMapper.addVisitHistory(userId, name, subject);
    }

    public void deleteSingleVisitHistory(int userId, String name, String subject) {
        historyMapper.deleteSingleVisitHistory(userId, name, subject);
    }
}
