package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.History;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HistoryMapper {
    @Select("SELECT * FROM history WHERE user_id IN (SELECT id FROM username WHERE username = #{username}) ORDER BY time DESC LIMIT 10")
    List<History> queryLatestHistoryByUsername(String username);

    @Insert("INSERT INTO history (user_id, content, time) values (#{userId}, #{content}, NOW())")
    int addHistory(int userId, String content);
}
