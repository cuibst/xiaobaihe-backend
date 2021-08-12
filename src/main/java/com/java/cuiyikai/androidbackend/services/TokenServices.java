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

    public Token queryLatestTokenByUsername(String username) {
        return mapper.queryLatestTokenByUsername(username);
    }

    public int deleteTokenByUsername(String username) {
        return mapper.deleteTokenByUsername(username);
    }

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

    public boolean isTokenValid(@Nullable Token token) {
        if(token == null)
            return false;
        Calendar calendar = Calendar.getInstance();
        long curDate = calendar.getTimeInMillis();
        long expireTime = token.getExpireTime().getTime();
        return curDate < expireTime;
    }

    public boolean isTokenValid(String token) {
        return mapper.queryValidTokenByToken(token) != null;
    }
}
