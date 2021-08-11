package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.History;
import com.java.cuiyikai.androidbackend.mapper.HistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryServices {

    @Autowired
    HistoryMapper historyMapper;

    public List<History> getLatestHistoryByUsername(String username) {
        return historyMapper.queryLatestHistoryByUsername(username);
    }
}
