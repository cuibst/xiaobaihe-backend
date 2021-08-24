package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.entity.VisitHistory;
import com.java.cuiyikai.androidbackend.services.HistoryServices;

import com.java.cuiyikai.androidbackend.services.TokenServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller class for api related to visit histories.
 */
@Controller
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    HistoryServices historyServices;

    @Autowired
    TokenServices tokenServices;

    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

    /**
     * Reply the last 10 search history of the user with the given name in json. <br>
     * Response a json as follows. <br>
     * {"data" : [(at most 10 related search history)], "status" : "ok" }
     * @param token The username you want to check, given in GET method.
     * @param response The response in json
     * @throws IOException when {@link java.io.PrintWriter} encounters some internal errors.
     */
    @GetMapping("/getHistory")
    public void getUserHistory(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        List<SearchHistory> searchHistoryList = historyServices.getLatestHistoryByUsername(username);
        ArrayList<String> result = new ArrayList<>();
        for (SearchHistory searchHistory : searchHistoryList) {
            result.add(searchHistory.getContent());
        }
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        reply.put("data", result);
        printWriter.print(reply);
        logger.info("Search history for user:{} replied.", username);
    }

    @GetMapping("/addHistory")
    public void addHistory(@RequestParam("token") String token, @RequestParam("content") String content, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        historyServices.addHistory(username, content);
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }

    @GetMapping("/getVisitHistory")
    public void getVisitHistory(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        User user = tokenServices.queryUserByToken(token);
        List<VisitHistory> visitHistoryList = historyServices.getVisitHistoryByUserId(user.getId());
        ArrayList<JSONObject> result = new ArrayList<>();
        for (VisitHistory visitHistory : visitHistoryList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", visitHistory.getName());
            jsonObject.put("subject", visitHistory.getSubject());
            result.add(jsonObject);
        }
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        reply.put("data", result);
        printWriter.print(reply);
        logger.info("Search history for user:{} replied.", user.getUsername());
    }

    @GetMapping("/addVisitHistory")
    public void addVisitHistory(@RequestParam("token") String token, @RequestParam("name") String name, @RequestParam("subject") String subject, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        int userId = tokenServices.queryUserByToken(token).getId();
        historyServices.addVisitHistory(userId, name, subject);
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }
}
