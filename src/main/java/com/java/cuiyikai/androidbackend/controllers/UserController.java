package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import com.java.cuiyikai.androidbackend.services.UserServices;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link Controller} for user-info-related apis.
 * Mapped to url {@code "/api/user"}
 */
@Controller
@RequestMapping("/api/user")
public class UserController {

    private static final String BACKEND_ADDRESS = "http://183.172.183.37:8080/api/register/check?token=";

    @Autowired
    private TokenServices tokenServices;

    @Autowired
    private UserServices userServices;

    /**
     * <p>Change user's info in the database</p>
     * <p>Will resend validate email if the email is changed.</p>
     * <p>Use {@link org.springframework.web.bind.annotation.RequestMethod#GET} method, and map to url {@code "/changeInfo"}.</p>
     * <p>Reply a {@link JSONObject} contains status and message.</p>
     * @param jsonParam A {@link JSONObject} contains following keys:
     *                  <p>
     *                  "token" : user's request token.
     *                  "oldPassword" : user's original password.
     *                  "password" : user's new password, if password not changed, please fill it with old password.
     *                  "email" user's new email. Please fill original one if not changed.
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
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
                email1.setSubject("??????????????????");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                email1.setMsg("??????????????????????????????"+ BACKEND_ADDRESS + validateToken + "&username=" + user.getUsername() + "???\n" +
                        "?????????" + format.format(new Date()) + "??????????????????");
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
