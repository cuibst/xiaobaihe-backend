package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.VisitHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HistoryMapper {

    /**
     * Get user's at most 10 latest search histories from the database with the given username.
     * @param username the username you want to query.
     * @return A {@link List} of {@link SearchHistory} include at most 10 latest search histories.
     */
    @Select("SELECT * FROM search_history WHERE user_id IN (SELECT id FROM username WHERE username = #{username}) ORDER BY time DESC LIMIT 10")
    List<SearchHistory> queryLatestHistoryByUsername(String username);

    /**
     * Get the number of same search histories from the backend.
     * @param userId the user's id of the search history.
     * @param content the search content of the search history.
     * @param subject the subject of the search history.
     * @return A number representing the number of the same histories.
     */
    @Select("SELECT COUNT(1) FROM search_history WHERE user_id = #{userId} and content = #{content} and subject = #{subject}")
    int getMatchHistoryCount(int userId, String content, String subject);

    /**
     * update the search time of the match search history to NOW.
     * @param userId the user's id of the search history.
     * @param content the search content of the search history.
     * @param subject the subject of the search history.
     */
    @Update("UPDATE search_history SET time = NOW() WHERE user_id = #{userId} and content = #{content} and subject = #{subject}")
    void updateOldHistory(int userId, String content, String subject);

    /**
     * delete all the search history of the given user.
     * @param userId the user's id.
     */
    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    void deleteAllUserHistory(int userId);

    /**
     * delete single search history from the database.
     * @param userId the user's id of the search history.
     * @param content the search content of the search history.
     * @param subject the subject of the search history.
     */
    @Delete("DELETE FROM search_history WHERE user_id = #{userId} and content = #{content} and subject = #{subject}")
    void deleteSingleHistory(int userId, String content, String subject);

    /**
     * Add a new search history to the database.
     * @param userId the user's id of the search history.
     * @param content the search content of the search history.
     * @param subject the subject of the search history.
     */
    @Insert("INSERT INTO search_history (user_id, content, subject, time) values (#{userId}, #{content}, #{subject}, NOW())")
    void addHistory(int userId, String content, String subject);

    /**
     * Get user's visit histories sorted by time descend from the database with the given user.
     * @param userId the user's id you want to query.
     * @return A {@link List} of {@link VisitHistory} include all search histories.
     */
    @Select("SELECT * FROM visit_history WHERE user_id = #{userId} ORDER BY time DESC")
    List<VisitHistory> queryVisitHistoryByUserId(int userId);

    /**
     * Get user's at most 5 visit histories sorted by time descend from the database with the given user.
     * @param userId the user's id you want to query.
     * @return A {@link List} of {@link VisitHistory} include at most 5 latest search histories.
     */
    @Select("SELECT * FROM visit_history WHERE user_id = #{userId} ORDER BY time DESC LIMIT 5")
    List<VisitHistory> queryLatestVisitHistoryByUserId(int userId);

    /**
     * Add a new visit history to the database.
     * @param userId the user id of the visit history.
     * @param name the entity's name of the visit history.
     * @param subject the entity's subject of the visit history.
     */
    @Insert("INSERT INTO visit_history (user_id, name, subject, time) values (#{userId}, #{name}, #{subject}, NOW())")
    void addVisitHistory(int userId, String name, String subject);

    /**
     * Get the number of same visit histories from the backend.
     * @param userId the user id of the visit history.
     * @param name the entity's name of the visit history.
     * @param subject the entity's subject of the visit history.
     * @return integer representing the number of match visit histories.
     */
    @Select("SELECT COUNT(1) FROM visit_history WHERE user_id = #{userId} and name = #{name} and subject = #{subject}")
    int getMatchVisitHistoryCount(int userId, String name, String subject);

    /**
     * Update the time of the visit history to NOW.
     * @param userId the user id of the visit history.
     * @param name the entity's name of the visit history.
     * @param subject the entity's subject of the visit history.
     */
    @Update("UPDATE visit_history SET time = NOW() WHERE user_id = #{userId} and name = #{name} and subject = #{subject}")
    void updateVisitHistory(int userId, String name, String subject);

    /**
     * Delete single visit history from the database.
     * @param userId the user id of the visit history.
     * @param name the entity's name of the visit history.
     * @param subject the entity's subject of the visit history.
     */
    @Delete("DELETE FROM visit_history WHERE user_id = #{userId} and name = #{name} and subject = #{subject}")
    void deleteSingleVisitHistory(int userId, String name, String subject);
}
