package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.Token;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.mapper.TokenMapper;
import com.java.cuiyikai.androidbackend.mapper.UserMapper;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Date;
import java.util.Calendar;


@Service
public class TokenServices {
    @Autowired
    TokenMapper mapper;

    @Autowired
    UserMapper userMapper;

    /**
     * Get the latest token with the given username.
     * @param username the related username.
     * @return Newest {@link Token} for the given username.
     */
    public Token queryLatestTokenByUsername(String username) {
        return mapper.queryLatestTokenByUsername(username);
    }

    /**
     * Delete all tokens of the given user.
     * @param username the related username.
     * @return the number of tokens deleted from the database.
     */
    public int deleteTokenByUsername(String username) {
        return mapper.deleteTokenByUsername(username);
    }

    /**
     * Insert a new token for the given user
     * @param username related username
     * @return the new token String.
     */
    @Nullable
    public String insertNewToken(String username) {
        User user = userMapper.queryUserByUsername(username);
        if(user == null)
            return null;
        Calendar calendar = Calendar.getInstance();
        String message = user.getUsername() + "&" + user.getPassword() + "&" + calendar.toString();
        MessageDigest messageDigest;
        String token;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(message.getBytes(StandardCharsets.UTF_8));
            token = Hex.encodeHexString(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        calendar.add(Calendar.DAY_OF_WEEK, 1);
        Date expireDate = new Date(calendar.getTimeInMillis());
        int result = mapper.insertNewToken(user.getId(), token, expireDate);
        if(result > 0)
            return token;
        return null;
    }

    /**
     * Check whether the token is valid.
     * @param token {@link Token} to be checked.
     * @return the validity of the given token.
     */
    public boolean isTokenValid(@Nullable Token token) {
        if(token == null)
            return false;
        Calendar calendar = Calendar.getInstance();
        long curDate = calendar.getTimeInMillis();
        long expireTime = token.getExpireTime().getTime();
        return curDate < expireTime;
    }

    /**
     * Check whether the token is valid.
     * @param token token String to be checked.
     * @return the validity of the given token.
     */
    public boolean isTokenValid(String token) {
        return mapper.queryValidTokenByToken(token) != null;
    }

    /**
     * Get the user of the given token.
     * @param token the related token.
     * @return The {@link User} if exists, otherwise {@code null}.
     */
    public User queryUserByToken(String token) {return mapper.queryUserByToken(token);}
}
