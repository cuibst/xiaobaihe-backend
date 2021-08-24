package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.VisitHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HistoryMapper {
    @Select("SELECT * FROM search_history WHERE user_id IN (SELECT id FROM username WHERE username = #{username}) ORDER BY time DESC LIMIT 10")
    List<SearchHistory> queryLatestHistoryByUsername(String username);

    @Select("SELECT * FROM search_history WHERE user_id = #{userId} ORDER BY time DESC LIMIT 10")
    List<SearchHistory> queryLatestHistoryByUserId(int userId);

    @Insert("INSERT INTO search_history (user_id, content, time) values (#{userId}, #{content}, NOW())")
    int addHistory(int userId, String content);

    @Select("SELECT * FROM visit_history WHERE user_id IN (SELECT id FROM username WHERE username = #{username}) ORDER BY time DESC")
    List<VisitHistory> queryVisitHistoryByUsername(String username);

    @Select("SELECT * FROM visit_history WHERE user_id = #{userId} ORDER BY time DESC")
    List<VisitHistory> queryVisitHistoryByUserId(int userId);

    @Select("SELECT * FROM visit_history WHERE user_id = #{userId} ORDER BY time DESC LIMIT 5")
    List<VisitHistory> queryLatestVisitHistoryByUserId(int userId);

    @Insert("INSERT INTO visit_history (user_id, name, subject, time) values (#{userId}, #{name}, #{subject}, NOW())")
    int addVisitHistory(int userId, String name, String subject);
}
