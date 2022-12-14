package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.entity.VisitHistory;
import com.java.cuiyikai.androidbackend.services.HistoryServices;

import com.java.cuiyikai.androidbackend.services.TokenServices;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;
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
 * <p> {@link Controller} for the history-related apis. </p>
 * <p> Mapped to url {@code "/api/history"}</p>
 */
@Controller
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryServices historyServices;

    @Autowired
    private TokenServices tokenServices;

    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

    /**
     * <p>Reply the last 10 search history of the user with the given name in json. </p>
     * <p> Mapped to uri {@code "/getHistory"}</p>
     * <p>Response a {@link JSONObject} as follows. </p>
     * <p>Use {@link RequestMethod#GET} Method</p>
     * <pre>{@code
     * {
     *     "data"   : [
     *     {
     *         "content" : < search content >,
     *         "subject" : < correspond subject >
     *     },...],
     *     "status" : "ok"
     * }
     * }</pre>
     * @param token The request token of the user.
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
    @GetMapping("/getHistory")
    public void getUserHistory(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        List<SearchHistory> searchHistoryList = historyServices.getLatestHistoryByUsername(username);
        ArrayList<JSONObject> result = new ArrayList<>();
        for (SearchHistory searchHistory : searchHistoryList) {
            JSONObject object = new JSONObject();
            object.put("content", searchHistory.getContent());
            object.put(NetworkUtilityClass.PARAMETER_SUBJECT, searchHistory.getSubject());
            result.add(object);
        }
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_DATA, result);
        printWriter.print(reply);
        logger.info("Search history for user:{} replied.", username);
    }

    /**
     * <p>Add a new search history of the given user to the database</p>
     * <p>Uses {@link RequestMethod#GET} for simplicity. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param token The request token of the corresponding user.
     * @param content The search content of the history
     * @param subject The subject of the history
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
    @GetMapping("/addHistory")
    public void addHistory(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token, @RequestParam("content") String content, @RequestParam(NetworkUtilityClass.PARAMETER_SUBJECT) String subject, HttpServletResponse response) throws IOException {
        logger.info("token = {}, content = {}", token, content);
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        historyServices.addHistory(username, content, subject);
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }

    /**
     * <p>Remove a search history of the given user from the database</p>
     * <p>Uses {@link RequestMethod#GET} for simplicity. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param token The request token of the corresponding user.
     * @param content (optional) The search content of the history
     * @param subject (optional) The subject of the history
     * @param allFLag (optional) If {@code allFlag} is true, it will remove all the search history of the user.
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
    @GetMapping("/removeHistory")
    public void removeHistory(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token,
                              @RequestParam(value = "content", required = false, defaultValue = "") String content,
                              @RequestParam(value = NetworkUtilityClass.PARAMETER_SUBJECT, required = false, defaultValue = "") String subject,
                              @RequestParam(value = "all", required = false, defaultValue = "false") boolean allFLag,
                              HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(content.equals("") && !allFLag) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        User user = tokenServices.queryUserByToken(token);
        if(allFLag) {
            historyServices.deleteAllUserHistory(user.getId());
        } else {
            historyServices.deleteSingleUserHistory(user.getId(), content, subject);
        }
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }

    /**
     * <p>Reply the last 10 visit history of the user with the given name in json. </p>
     * <p> Mapped to uri {@code "/getVisitHistory"}</p>
     * <p>Response a {@link JSONObject} as follows. </p>
     * <p>Use {@link RequestMethod#GET} Method</p>
     * <pre>{@code
     * {
     *     "data"   : [{
     *         "name"    : < entity name >,
     *         "subject" : < correspond subject >
     *     }],
     *     "status" : "ok"
     * }
     * }</pre>
     * @param token The request token of the user.
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
    @GetMapping("/getVisitHistory")
    public void getVisitHistory(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token,
                                HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        User user = tokenServices.queryUserByToken(token);
        List<VisitHistory> visitHistoryList = historyServices.getVisitHistoryByUserId(user.getId());
        ArrayList<JSONObject> result = new ArrayList<>();
        for (VisitHistory visitHistory : visitHistoryList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NetworkUtilityClass.PARAMETER_NAME, visitHistory.getName());
            jsonObject.put(NetworkUtilityClass.PARAMETER_SUBJECT, visitHistory.getSubject());
            jsonObject.put("time", visitHistory.getTime());
            result.add(jsonObject);
        }
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_DATA, result);
        printWriter.print(reply);
        logger.info("Search history for user:{} replied.", user.getUsername());
    }

    /**
     * <p>Add a new visit history of the given user to the database</p>
     * <p>Uses {@link RequestMethod#GET} for simplicity. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param token The request token of the corresponding user.
     * @param name The entity name of the visit history
     * @param subject The subject of the history
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
    @GetMapping("/addVisitHistory")
    public void addVisitHistory(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token,
                                @RequestParam(NetworkUtilityClass.PARAMETER_NAME) String name,
                                @RequestParam(NetworkUtilityClass.PARAMETER_SUBJECT) String subject,
                                HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        int userId = tokenServices.queryUserByToken(token).getId();
        historyServices.addVisitHistory(userId, name, subject);
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }

    /**
     * <p>Remove a visit history of the given user from the database</p>
     * <p>Uses {@link RequestMethod#GET} for simplicity. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param token The request token of the corresponding user.
     * @param name (optional) The entity name of the history
     * @param subject (optional) The subject of the history
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
    @GetMapping("/removeVisitHistory")
    public void removeVisitHistory(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token,
                                   @RequestParam(NetworkUtilityClass.PARAMETER_NAME) String name,
                                   @RequestParam(NetworkUtilityClass.PARAMETER_SUBJECT) String subject,
                                   HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        User user = tokenServices.queryUserByToken(token);
        logger.info("remove visit history for user {},  name = {}, subject = {}", user.getUsername(), name, subject);
        historyServices.deleteSingleVisitHistory(user.getId(), name, subject);
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }
}
