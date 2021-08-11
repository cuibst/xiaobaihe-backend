package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.History;
import com.java.cuiyikai.androidbackend.services.HistoryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    /**
     * Reply the last 10 search history of the user with the given name in json. <br>
     * <code>
     * {"data" : [(at most 10 related search history)], "status" : "ok" }
     * </code>
     * @param username The username you want to check, given in GET method.
     * @param response The response in json
     * @throws IOException when {@link java.io.PrintWriter} encounters some internal errors.
     */
    @RequestMapping(name = "/gethistory", method = RequestMethod.GET)
    public void getUserHistory(@RequestParam("username") String username, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        List<History> historyList = historyServices.getLatestHistoryByUsername(username);
        ArrayList<String> result = new ArrayList<>();
        for (History history: historyList) {
            result.add(history.getContent());
        }
        PrintWriter printWriter = response.getWriter();
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        reply.put("data", result);
        printWriter.print(reply);
    }
}
