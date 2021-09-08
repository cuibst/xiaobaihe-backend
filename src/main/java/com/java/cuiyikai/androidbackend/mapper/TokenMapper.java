package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Token;
import com.java.cuiyikai.androidbackend.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.lang.Nullable;

import java.sql.Date;

@Mapper
public interface TokenMapper {
    /**
     * Get the latest token with the given username.
     * @param username the related username.
     * @return Newest {@link Token} for the given username.
     */
    @Select("SELECT * FROM token WHERE user_id IN (SELECT id FROM username WHERE username = #{username}) ORDER BY expireTime DESC LIMIT 1")
    Token queryLatestTokenByUsername(String username);

    /**
     * Delete all tokens of the given user.
     * @param username the related username.
     * @return the number of tokens deleted from the database.
     */
    @Delete("DELETE FROM token WHERE user_id IN (SELECT id FROM username WHERE username = #{username})")
    int deleteTokenByUsername(String username);

    /**
     * Insert a new token into the database.
     * @param userId the related user's id.
     * @param token the new token.
     * @param expireTime the expires time of the token.
     * @return 1 when added, 0 when failed.
     */
    @Insert("INSERT INTO token (user_id, token, expireTime) VALUES (#{userId}, #{token}, #{expireTime})")
    int insertNewToken(int userId, String token, Date expireTime);

    /**
     * Get the token with given string that hasn't expired.
     * @param token the token string.
     * @return The {@link Token} if such token exists. {@code null} when not exists.
     */
    @Nullable
    @Select("SELECT * FROM token WHERE token = #{token} and expireTime > NOW()")
    Token queryValidTokenByToken(String token);

    /**
     * Get the user of the given token.
     * @param token the related token.
     * @return The {@link User} if exists, otherwise {@code null}.
     */
    @Nullable
    @Select("SELECT * from username WHERE id IN (SELECT user_id FROM token WHERE token = #{token} ORDER BY expireTime DESC LIMIT 1)")
    User queryUserByToken(String token);
}
