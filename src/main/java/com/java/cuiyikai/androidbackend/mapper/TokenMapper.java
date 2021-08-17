package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Token;
import com.java.cuiyikai.androidbackend.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;

@Mapper
public interface TokenMapper {
    @Select("SELECT * FROM token WHERE user_id IN (SELECT id FROM username WHERE username = #{username}) ORDER BY expireTime DESC LIMIT 1")
    Token queryLatestTokenByUsername(String username);

    @Delete("DELETE FROM token WHERE user_id IN (SELECT id FROM username WHERE username = #{username})")
    int deleteTokenByUsername(String username);

    @Insert("INSERT INTO token (user_id, token, expireTime) VALUES (#{userId}, #{token}, #{expireTime})")
    int insertNewToken(int userId, String token, Date expireTime);

    @Select("SELECT * FROM token WHERE token = #{token} and expireTime > NOW()")
    Token queryValidTokenByToken(String token);

    @Select("SELECT * from username WHERE id IN (SELECT user_id FROM token WHERE token = #{token} ORDER BY expireTime DESC LIMIT 1)")
    User queryUserByToken(String token);
}
