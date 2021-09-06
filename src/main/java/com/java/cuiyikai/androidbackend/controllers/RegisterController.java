package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Token;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import com.java.cuiyikai.androidbackend.services.UserServices;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/api/register")
public class RegisterController {

    private static final String BACKEND_ADDRESS = "http://183.172.183.37:8080/api/register/check?token=";

    @Autowired
    private UserServices userServices;

    @Autowired
    private TokenServices tokenServices;

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @PostMapping("/")
    public void registerNewUser(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        String username = jsonParam.getString("username");
        String password = jsonParam.getString("password");
        String email = jsonParam.getString(NetworkUtilityClass.PARAMETER_EMAIL);
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(username == null || password == null || email == null || username.equals("") || password.equals("") || email.equals(""))
        {
            response.setStatus(400);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "incomplete register request");
            printWriter.print(reply);
            return;
        }
        if(!userServices.checkUsername(username))
        {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "username has been used");
            printWriter.print(reply);
            return;
        }
        userServices.registerNewUser(username, password, email);
        String validateToken = tokenServices.insertNewToken(username);
        HtmlEmail email1 = new HtmlEmail();
        email1.setHostName("smtp.126.com");
        email1.setCharset("utf-8");
        try {
            email1.addTo(email);
            email1.setFrom("cbst987@126.com", "Noreply@android-backend");
            email1.setAuthentication("cbst987@126.com", "MUTRLKLVWOFZSZJG");
            email1.setSubject("验证您的邮箱");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            email1.setMsg("您的邮箱验证地址为："+ BACKEND_ADDRESS + validateToken + "&username=" + username + "。\n" +
                    "请您于" + format.format(new Date()) + "前进行验证。");
            email1.send();
        } catch (Exception e) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "incorrect email address");
            printWriter.print(reply);
            return;
        }
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "register successfully");
        printWriter.print(reply);
    }

    @GetMapping("/check")
    public void checkToken(@RequestParam String token, @RequestParam String username, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(token == null || username == null)
        {
            response.setStatus(400);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "incomplete check request");
            printWriter.print(reply);
            return;
        }
        if(!tokenServices.isTokenValid(token))
        {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        Token token1 = tokenServices.queryLatestTokenByUsername(username);
        if(!token1.getToken().equals(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "token expired or token username mismatch");
            printWriter.print(reply);
            return;
        }
        userServices.updateCheckEmailStatus(username);
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "email checked successfully");
        printWriter.print(reply);
    }

    @GetMapping("/check/username")
    public void checkUsername(@RequestParam String username, HttpServletResponse response) throws IOException {
        logger.info("Checking username {}", username);
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        JSONObject reply = new JSONObject();
        if(userServices.checkUsername(username))
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        else
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
        printWriter.print(reply);
    }
}
