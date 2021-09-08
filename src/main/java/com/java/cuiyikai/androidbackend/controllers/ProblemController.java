package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.callables.LoginCallable;
import com.java.cuiyikai.androidbackend.callables.ProblemCallable;
import com.java.cuiyikai.androidbackend.callables.SearchResultCallable;
import com.java.cuiyikai.androidbackend.entity.*;
import com.java.cuiyikai.androidbackend.services.HistoryServices;
import com.java.cuiyikai.androidbackend.services.ProblemServices;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <p> {@link Controller} for the problem-related apis. </p>
 * <p> Mapped to url {@code "/api/problem"}</p>
 */
@Controller
@RequestMapping("/api/problem")
public class ProblemController {

    private static final Logger logger = LoggerFactory.getLogger(ProblemController.class);

    @Autowired
    private HistoryServices historyServices;

    @Autowired
    private TokenServices tokenServices;

    @Autowired
    private ProblemServices problemServices;

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * <p>Get the first 15 suggest problems after offset according to user's search history and visit history</p>
     * <p>Map to url {@code "/"}</p>
     * <p>Uses {@link RequestMethod#GET} method.</p>
     * <p>Reply a {@link JSONObject} as follows:</p>
     * <pre>{@code
     * {
     *     "status" : "ok" / "fail",
     *     "data"   : [{
     *         "problem" : {
     *             "qBody" : < problem statement >,
     *             "qAnswer" : < problem answer >
     *         },
     *         "subject" : < problem subject >
     *     }, ...]
     * }
     * }</pre>
     * @param token User's request token.
     * @param offset (optional) default value is 0. offset of the data array. result will begin from the offset th problem.
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
    @GetMapping("/")
    public void getSuggestProblem(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token,
                                  @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                                  HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();

        //Phase 1: Get the visit id of edukg backend.

        String id;
        try {
            id = executorService.submit(new LoginCallable()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            response.setStatus(500);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "Cannot connect to server.");
            printWriter.print(reply);
            return;
        } catch (ExecutionException e) {
            response.setStatus(500);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, "Cannot connect to server.");
            printWriter.print(reply);
            return;
        }

        //Phase 2: Get according user, and latest histories.
        User user = tokenServices.queryUserByToken(token);
        List<VisitHistory> visitHistoryList = historyServices.getLatestVisitHistoryByUserId(user.getId());
        List<SearchHistory> searchHistoryList = historyServices.getLatestHistoryByUsername(user.getUsername());
        if(searchHistoryList.size() > 5)
            searchHistoryList = searchHistoryList.subList(0,5);
        List<Future<JSONArray>> futureProblems = new ArrayList<>();
        Map<JSONObject, Integer> results = new HashMap<>();
        List<Future<JSONArray>> futureSearchResultList = new ArrayList<>();
        List<String> subjects = new ArrayList<>();
        List<String> problemSubjects = new ArrayList<>();
        for(SearchHistory searchHistory : searchHistoryList) {
            Map<String,String> args1 = new HashMap<>();
            args1.put(NetworkUtilityClass.PARAMETER_ID, id);
            args1.put(NetworkUtilityClass.PARAMETER_COURSE, searchHistory.getSubject());
            args1.put("searchKey", searchHistory.getContent());
            futureSearchResultList.add(executorService.submit(new SearchResultCallable(args1)));
            subjects.add(searchHistory.getSubject());
        }

        for(VisitHistory visitHistory : visitHistoryList) {
            Map<String, String> args1 = new HashMap<>();
            args1.put(NetworkUtilityClass.PARAMETER_ID, id);
            args1.put("uriName", visitHistory.getName());
            futureProblems.add(executorService.submit(new ProblemCallable(args1)));
            problemSubjects.add(visitHistory.getSubject());
        }

        //Phase 3: Based on the histories, get the related problems.
        //    3.1: From the search histories, get related entities.
        for(int i=0;i<futureSearchResultList.size();i++) {
            if(futureProblems.size() >= 10)
                break;
            Future<JSONArray> futureData = futureSearchResultList.get(i);
            JSONArray data;
            try {
                data = futureData.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
                continue;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
                continue;
            }
            if(data != null) {
                for (Object obj : data) {
                    JSONObject entity = JSON.parseObject(obj.toString());
                    String name = entity.getString("label");
                    Map<String, String> args1 = new HashMap<>();
                    args1.put(NetworkUtilityClass.PARAMETER_ID, id);
                    args1.put("uriName", name);
                    futureProblems.add(executorService.submit(new ProblemCallable(args1)));
                    problemSubjects.add(subjects.get(i));
                }
            }
        }

        //    3.2: Based on all the entities, get related problems.
        for(int i=0;i<futureProblems.size();i++) {
            Future<JSONArray> futureProblem = futureProblems.get(i);
            JSONArray problem;
            try {
                problem = futureProblem.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
                continue;
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                continue;
            }
            if(problem != null)
                for(Object object : problem) {
                    logger.info("Current Object: {}", object);
                    JSONObject jsonObject = JSON.parseObject(object.toString());
                    JSONObject object1 = new JSONObject();
                    object1.put(NetworkUtilityClass.PARAMETER_SUBJECT, problemSubjects.get(i));
                    object1.put(NetworkUtilityClass.PARAMETER_PROBLEM, jsonObject);
                    if(results.containsKey(object1))
                        results.replace(object1, results.get(object1) + 1);
                    else
                        results.put(object1, 1);
                }
        }

        //Phase 4: Fetch the most possible ones after offset.
        List<Map.Entry<JSONObject, Integer>> entryList = new ArrayList<>(results.entrySet());
        entryList.sort(Map.Entry.comparingByValue());
        Collections.reverse(entryList);

        List<JSONObject> result = new ArrayList<>();

        for (Map.Entry<JSONObject, Integer> entry : entryList.subList(Math.min(offset, entryList.size()),Math.min(offset + 10, entryList.size())))
            result.add(entry.getKey());

        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_DATA, result);
        logger.info("reply data : {}", result);
        printWriter.print(reply);
    }

    /**
     * <p>Add a new problem to user's problem book</p>
     * <p>Map to url {@code "/addNewSave"}</p>
     * <p>Use {@link RequestMethod#POST} method.</p>
     * <p>Return a {@link JSONObject} only include key status.</p>
     * @param jsonParam A request {@link JSONObject}, shoul contain following keys.
     *                  <p>
     *                  "token" : User's request token.
     *                  "problem" : A {@link JSONObject}, should contain qAnswer and qBody key.
     *                  "subject" : Problems subject.
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link PostMapping}.
     * @throws IOException see {@link PostMapping}
     */
    @PostMapping("/addNewSave")
    public void addNewSaveProblem(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        logger.info("Add new save problem : {}", jsonParam);
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(!jsonParam.containsKey(NetworkUtilityClass.PARAMETER_TOKEN) || !jsonParam.containsKey(NetworkUtilityClass.PARAMETER_PROBLEM) || !jsonParam.containsKey(NetworkUtilityClass.PARAMETER_SUBJECT)) {
            JSONObject reply = new JSONObject();
            response.setStatus(406);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String token = jsonParam.getString(NetworkUtilityClass.PARAMETER_TOKEN);
        User user = tokenServices.queryUserByToken(token);
        if(user == null || !tokenServices.isTokenValid(token)) {
            JSONObject reply = new JSONObject();
            response.setStatus(403);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String problem = jsonParam.getJSONObject(NetworkUtilityClass.PARAMETER_PROBLEM).toString();
        int id = problemServices.insertNewProblem(problem, jsonParam.getString(NetworkUtilityClass.PARAMETER_SUBJECT)).getId();
        problemServices.addNewSave(user.getId(), id);
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }

    /**
     * <p>Get all the problems from user's problem book</p>
     * <p>Map to url {@code "/getSaves"}</p>
     * <p>Use {@link RequestMethod#GET} method.</p>
     * <p>Reply a {@link JSONObject} as follows:</p>
     * <pre>{@code
     * {
     *     "status" : "ok" / "fail",
     *     "data"   : [{
     *         "problem" : {
     *             "qBody" : < problem statement >,
     *             "qAnswer" : < problem answer >
     *         },
     *         "subject" : < problem subject >
     *     }, ...]
     * }
     * }</pre>
     * @param token user's request token
     * @param offset (optional) default value is 0. offset of the data array. result will begin from the offset th problem.
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
    @GetMapping("/getSaves")
    public void getSaves(@RequestParam(NetworkUtilityClass.PARAMETER_TOKEN) String token, @RequestParam(value = "offset", required = false, defaultValue = "0") int offset, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        User user = tokenServices.queryUserByToken(token);
        if(user == null || !tokenServices.isTokenValid(token)) {
            JSONObject reply = new JSONObject();
            response.setStatus(403);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        List<Problem> problemList = problemServices.querySavesById(user.getId(), offset);
        JSONArray problemsArray = new JSONArray();
        for(Problem problem : problemList) {
            JSONObject jsonObject = JSON.parseObject(problem.getJson());
            JSONObject object = new JSONObject();
            object.put(NetworkUtilityClass.PARAMETER_SUBJECT, problem.getSubject());
            object.put(NetworkUtilityClass.PARAMETER_PROBLEM, jsonObject);
            problemsArray.add(object);
        }
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_DATA, problemsArray);
        printWriter.print(reply);
    }

    /**
     * <p>Delete a problem from user's problem book</p>
     * <p>Map to url {@code "/deleteSave"}</p>
     * <p>Use {@link RequestMethod#POST} method.</p>
     * <p>Return a {@link JSONObject} only include key status.</p>
     * @param jsonParam A request {@link JSONObject}, shoul contain following keys.
     *                  <p>
     *                  "token" : User's request token.
     *                  "problem" : A {@link JSONObject}, should contain qAnswer and qBody key.
     *                  "subject" : Problems subject.
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link PostMapping}.
     * @throws IOException see {@link PostMapping}
     */
    @PostMapping("/deleteSave")
    public void deleteSaveProblem(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        if(!jsonParam.containsKey(NetworkUtilityClass.PARAMETER_TOKEN) || !jsonParam.containsKey(NetworkUtilityClass.PARAMETER_PROBLEM) || !jsonParam.containsKey(NetworkUtilityClass.PARAMETER_SUBJECT)) {
            JSONObject reply = new JSONObject();
            response.setStatus(406);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }
        logger.info("Delete save problem : {}", jsonParam);
        String token = jsonParam.getString(NetworkUtilityClass.PARAMETER_TOKEN);
        User user = tokenServices.queryUserByToken(token);
        if(user == null || !tokenServices.isTokenValid(token)) {
            JSONObject reply = new JSONObject();
            response.setStatus(403);
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        JSONObject problem = jsonParam.getJSONObject(NetworkUtilityClass.PARAMETER_PROBLEM);
        logger.info("Retrived problem : {}", problem);
        problemServices.deleteSave(user.getId(), problem.toString());
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }
}
