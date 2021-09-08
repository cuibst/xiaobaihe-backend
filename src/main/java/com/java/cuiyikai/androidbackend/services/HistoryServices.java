package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.entity.VisitHistory;
import com.java.cuiyikai.androidbackend.mapper.HistoryMapper;
import com.java.cuiyikai.androidbackend.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryServices {

    private static final Logger logger = LoggerFactory.getLogger(HistoryServices.class);

    @Autowired
    private HistoryMapper historyMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * get user's latest search history
     * @param username related username.
     * @return the {@link List} of latest at most 15 {@link SearchHistory} of the given user.
     */
    public List<SearchHistory> getLatestHistoryByUsername(String username) {
        return historyMapper.queryLatestHistoryByUsername(username);
    }

    /**
     * Add a new search history to the database
     * @param username the related username
     * @param content the content of the search history
     * @param subject the subject of the search history
     */
    public void addHistory(String username, String content, String subject) {
        User user = userMapper.queryUserByUsername(username);
        logger.info("Get count: {} {}%n", content, historyMapper.getMatchHistoryCount(user.getId(), content, subject));
        if(historyMapper.getMatchHistoryCount(user.getId(), content, subject) > 0) {
            historyMapper.updateOldHistory(user.getId(), content, subject);
            return;
        }
        logger.info("add new history");
        historyMapper.addHistory(userMapper.queryUserByUsername(username).getId(), content, subject);
    }

    /**
     * Delete all the user's search history
     * @param userId the related user's id.
     */
    public void deleteAllUserHistory(int userId) {
        historyMapper.deleteAllUserHistory(userId);
    }

    /**
     * Delete single search history from the database.
     * @param userId the related user's id.
     * @param content the content of the search history
     * @param subject the subject of the search history
     */
    public void deleteSingleUserHistory(int userId, String content, String subject) {
        historyMapper.deleteSingleHistory(userId, content, subject);
    }

    /**
     * Get user's visit histories sorted by time descend from the database with the given user.
     * @param id the user's id you want to query.
     * @return A {@link List} of {@link VisitHistory} include all search histories.
     */
    public List<VisitHistory> getVisitHistoryByUserId(int id) {
        return historyMapper.queryVisitHistoryByUserId(id);
    }

    /**
     * Get user's at most 5 visit histories sorted by time descend from the database with the given user.
     * @param id the user's id you want to query.
     * @return A {@link List} of {@link VisitHistory} include at most 5 latest search histories.
     */
    public List<VisitHistory> getLatestVisitHistoryByUserId(int id) { return historyMapper.queryLatestVisitHistoryByUserId(id); }

    /**
     * add a new visit history when the history doesn't exist, otherwise update the time.
     * @param userId related user's id
     * @param name entity's name of the visit history
     * @param subject entity's subject of the visit history
     */
    public void addVisitHistory(int userId, String name, String subject) {
        if(historyMapper.getMatchVisitHistoryCount(userId, name, subject) > 0) {
            historyMapper.updateVisitHistory(userId, name, subject);
            return;
        }
        historyMapper.addVisitHistory(userId, name, subject);
    }

    /**
     * Delete single visit history from the database.
     * @param userId the user id of the visit history.
     * @param name the entity's name of the visit history.
     * @param subject the entity's subject of the visit history.
     */
    public void deleteSingleVisitHistory(int userId, String name, String subject) {
        historyMapper.deleteSingleVisitHistory(userId, name, subject);
    }
}
