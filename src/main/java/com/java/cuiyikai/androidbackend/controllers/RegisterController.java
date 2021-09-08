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

/**
 * {@link Controller} for register-related apis.
 * Mapped to url {@code "/api/register"}
 */
@Controller
@RequestMapping("/api/register")
public class RegisterController {

    private static final String BACKEND_ADDRESS = "http://183.172.183.37:8080/api/register/check?token=";

    @Autowired
    private UserServices userServices;

    @Autowired
    private TokenServices tokenServices;

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    /**
     * <p>Receive user's register information and register if possible.</p>
     * <p>If success, backend will send a validate email to user's email address.</p>
     * <p>Map to url {@code "/"}</p>
     * <p>Use {@link RequestMethod#POST} method</p>
     * <p>Will return a {@link JSONObject} with status and message.</p>
     * @param jsonParam A {@link JSONObject} of register parameters, should contains following keys.
     *                  <p>
     *                  "username" : username of the user.
     *                  "password" : password of the user.
     *                  "email"    : email address of the user.
     *                  both values should not be empty, and username should not be same as any user existing in the database.
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link PostMapping}.
     * @throws IOException see {@link PostMapping}
     */
    @PostMapping("/")
    public void registerNewUser(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {

        //Phase 1: Check request parameters.

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

        //Phase 2: send the validation email.
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

        //Phase 3: register the user.
        userServices.registerNewUser(username, password, email);

        //Phase 4: reply success.

        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "register successfully");
        printWriter.print(reply);
    }

    /**
     * <p>Check the user's validate email, and validate the user.</p>
     * <p>Use {@link RequestMethod#GET} and map to url {@code "/check"}</p>
     * <p>Reply a {@link JSONObject} with status and message.</p>
     * @param token user's validate token, should be same with the one when the user is registered.
     * @param username user's username. This should match with the token.
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
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

    /**
     * <p>Check whether there is a collided username</p>
     * <p>Use {@link RequestMethod#GET} method and map to url {@code "/check/username"}</p>
     * <p>Reply a {@link JSONObject} only contains status</p>
     * @param username the username being checked
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
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
