package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.History;
import com.java.cuiyikai.androidbackend.services.HistoryServices;

import com.java.cuiyikai.androidbackend.services.TokenServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    @GetMapping("/gethistory")
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
        List<History> historyList = historyServices.getLatestHistoryByUsername(username);
        ArrayList<String> result = new ArrayList<>();
        for (History history: historyList) {
            result.add(history.getContent());
        }
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        reply.put("data", result);
        printWriter.print(reply);
        logger.info("Search history for user:{} replied.", username);
    }

    @GetMapping("/addhistory")
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
}
