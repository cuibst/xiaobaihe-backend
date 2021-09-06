package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import com.java.cuiyikai.androidbackend.services.UserServices;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/api/user")
public class UserController {

    private static final String BACKEND_ADDRESS = "http://183.172.183.37:8080/api/register/check?token=";

    @Autowired
    private TokenServices tokenServices;

    @Autowired
    private UserServices userServices;

    @PostMapping("/changeInfo")
    public void changeUserInfo(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString(NetworkUtilityClass.PARAMETER_TOKEN);
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        User user = tokenServices.queryUserByToken(token);
        if(!jsonParam.containsKey("password") || !jsonParam.containsKey(NetworkUtilityClass.PARAMETER_EMAIL) || !jsonParam.containsKey("oldPassword") || !jsonParam.getString("oldPassword").equals(user.getPassword())) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }
        boolean changedEmail = user.getEmail().equals(jsonParam.getString(NetworkUtilityClass.PARAMETER_EMAIL));
        user.setEmail(jsonParam.getString(NetworkUtilityClass.PARAMETER_EMAIL));
        if(changedEmail) {
            String validateToken = tokenServices.insertNewToken(user.getUsername());
            String email = user.getEmail();
            HtmlEmail email1 = new HtmlEmail();
            email1.setHostName("smtp.126.com");
            email1.setCharset("utf-8");
            try {
                email1.addTo(email);
                email1.setFrom("cbst987@126.com", "Noreply@android-backend");
                email1.setAuthentication("cbst987@126.com", "MUTRLKLVWOFZSZJG");
                email1.setSubject("验证您的邮箱");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                email1.setMsg("您的邮箱验证地址为："+ BACKEND_ADDRESS + validateToken + "&username=" + user.getUsername() + "。\n" +
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
        }
        user.setPassword(jsonParam.getString("password"));
        user.setEmail(jsonParam.getString(NetworkUtilityClass.PARAMETER_EMAIL));
        userServices.updateUserInfo(user, changedEmail);
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }
}
