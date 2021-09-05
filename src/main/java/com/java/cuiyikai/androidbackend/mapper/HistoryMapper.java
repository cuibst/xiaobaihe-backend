package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.VisitHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HistoryMapper {
    @Select("SELECT * FROM search_history WHERE user_id IN (SELECT id FROM username WHERE username = #{username}) ORDER BY time DESC LIMIT 10")
    List<SearchHistory> queryLatestHistoryByUsername(String username);

    @Select("SELECT COUNT(1) FROM search_history WHERE user_id = #{userId} and content = #{content} and subject = #{subject}")
    int getMatchHistoryCount(int userId, String content, String subject);

    @Update("UPDATE search_history SET time = NOW() WHERE user_id = #{userId} and content = #{content} and subject = #{subject}")
    void updateOldHistory(int userId, String content, String subject);

    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    void deleteAllUserHistory(int userId);

    @Delete("DELETE FROM search_history WHERE user_id = #{userId} and content = #{content} and subject = #{subject}")
    void deleteSingleHistory(int userId, String content, String subject);

    @Insert("INSERT INTO search_history (user_id, content, subject, time) values (#{userId}, #{content}, #{subject}, NOW())")
    void addHistory(int userId, String content, String subject);

    @Select("SELECT * FROM visit_history WHERE user_id IN (SELECT id FROM username WHERE username = #{username}) ORDER BY time DESC")
    List<VisitHistory> queryVisitHistoryByUsername(String username);

    @Select("SELECT * FROM visit_history WHERE user_id = #{userId} ORDER BY time DESC")
    List<VisitHistory> queryVisitHistoryByUserId(int userId);

    @Select("SELECT * FROM visit_history WHERE user_id = #{userId} ORDER BY time DESC LIMIT 5")
    List<VisitHistory> queryLatestVisitHistoryByUserId(int userId);

    @Insert("INSERT INTO visit_history (user_id, name, subject, time) values (#{userId}, #{name}, #{subject}, NOW())")
    void addVisitHistory(int userId, String name, String subject);

    @Select("SELECT COUNT(1) FROM visit_history WHERE user_id = #{userId} and name = #{name} and subject = #{subject}")
    int getMatchVisitHistoryCount(int userId, String name, String subject);

    @Update("UPDATE visit_history SET time = NOW() WHERE user_id = #{userId} and name = #{name} and subject = #{subject}")
    void updateVisitHistory(int userId, String name, String subject);

    @Delete("DELETE FROM visit_history WHERE user_id = #{userId} and name = #{name} and subject = #{subject}")
    void deleteSingleVisitHistory(int userId, String name, String subject);
}
