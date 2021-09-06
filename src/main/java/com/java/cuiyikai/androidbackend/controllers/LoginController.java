package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Token;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import com.java.cuiyikai.androidbackend.services.UserServices;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping("/api/login")
@CrossOrigin(origins = "*")
public class LoginController {
    @Autowired
    private UserServices userServices;

    @Autowired
    private TokenServices tokenServices;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @PostMapping("/")
    public void login(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        String username = jsonParam.getString("username");
        String password = jsonParam.getString("password");
        if(username == null || password == null)
        {
            response.setStatus(400);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "incomplete login request");
            printWriter.print(reply);
            return;
        }
        boolean result = userServices.checkPassword(username, password);
        JSONObject reply = new JSONObject();
        if (result) {
            logger.info("Login success with username:{}", username);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
            Token token = tokenServices.queryLatestTokenByUsername(username);
            if(!tokenServices.isTokenValid(token)) {
                tokenServices.deleteTokenByUsername(username);
                tokenServices.insertNewToken(username);
                token = tokenServices.queryLatestTokenByUsername(username);
            }
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "log in successfully");
            reply.put(NetworkUtilityClass.PARAMETER_TOKEN, token.getToken());
        } else {
            response.setStatus(406);
            logger.info("Login failed with username:{}", username);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "password or username incorrect or email not checked");
        }
        printWriter.print(reply);
    }

    @GetMapping("/exchangeToken")
    public void exchangeToken(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token, HttpServletResponse response) throws IOException {
        User user = tokenServices.queryUserByToken(token);
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        JSONObject reply = new JSONObject();
        if(user == null) {
            response.setStatus(403);
            logger.info("Login failed with token:{}", token);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "Token not exist");
            printWriter.print(reply);
            return;
        }
        Token latestToken = tokenServices.queryLatestTokenByUsername(user.getUsername());
        if(latestToken == null || !latestToken.getToken().equals(token)) {
            response.setStatus(403);
            logger.info("Login failed with token:{}", token);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "Token not exist");
            printWriter.print(reply);
            return;
        }
        tokenServices.deleteTokenByUsername(user.getUsername());
        token = tokenServices.insertNewToken(user.getUsername());
        logger.info("Token exchange success");
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "log in successfully");
        reply.put(NetworkUtilityClass.PARAMETER_TOKEN, token);
        printWriter.print(reply);
    }

}
