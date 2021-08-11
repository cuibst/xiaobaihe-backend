package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.services.UserServices;
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

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void login(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        boolean result = userServices.checkPassword(jsonParam.getString("username"), jsonParam.getString("password"));
        JSONObject reply = new JSONObject();
        if (result) {
            logger.info("Login success with username:{}", jsonParam.getString("username"));
            reply.put("status", "ok");
        } else {
            logger.info("Login failed with username:{}", jsonParam.getString("username"));
            reply.put("status", "fail");
        }
        printWriter.print(reply);
    }
}
