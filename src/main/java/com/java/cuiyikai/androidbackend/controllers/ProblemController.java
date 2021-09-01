package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.callables.ProblemCallable;
import com.java.cuiyikai.androidbackend.callables.SearchResultCallable;
import com.java.cuiyikai.androidbackend.entity.*;
import com.java.cuiyikai.androidbackend.services.HistoryServices;
import com.java.cuiyikai.androidbackend.services.ProblemServices;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.buildForm;
import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.setConnectionHeader;

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

    final String[] SUBJECTS = {"chinese", "english", "math", "physics", "chemistry", "biology", "history", "geo", "politics"};

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @GetMapping("/")
    public void getSuggestProblem(@RequestParam("token") String token, @RequestParam(value = "offset", required = false, defaultValue = "0") int offset, HttpServletResponse response) throws Exception {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        String urlLogin = "http://open.edukg.cn/opedukg/api/typeAuth/user/login";
        URL url = new URL(urlLogin);
        HttpURLConnection loginConnection = (HttpURLConnection) url.openConnection();
        setConnectionHeader(loginConnection, "POST");
        Map<String, String> args = new HashMap<>();
//        args.put("phone", "15910826331");
//        args.put("password", "cbst20001117");
//        args.put("phone", "16688092093");
//        args.put("password", "0730llhh");
        args.put("phone", "18211517925");
        args.put("password", "ldx0881110103");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(loginConnection.getOutputStream(), StandardCharsets.UTF_8));
        writer.write(buildForm(args));
        writer.flush();
        String id = null;
        if(loginConnection.getResponseCode() == 200)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(loginConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder buffer = new StringBuilder();
            while((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            JSONObject loginResponse = JSON.parseObject(buffer.toString());
            id = loginResponse.getString("id");
        }
        if(id == null)
        {
            response.setStatus(500);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "cannot connect to server");
            printWriter.print(reply);
            return;
        }
        writer.close();
        loginConnection.disconnect();
        User user = tokenServices.queryUserByToken(token);
        List<VisitHistory> visitHistoryList = historyServices.getLatestVisitHistoryByUserId(user.getId());
        List<SearchHistory> searchHistoryList = historyServices.getLatestHistoryByUsername(user.getUsername());
        List<Future<JSONArray>> futureProblems = new ArrayList<>();
        Map<JSONObject, Integer> results = new HashMap<>();
        for(String sub : SUBJECTS) {
            List<Future<JSONArray>> futureSearchResultList = new ArrayList<>();
            for(SearchHistory searchHistory : searchHistoryList) {
                Map<String,String> args1 = new HashMap<>();
                args1.put("id", id);
                args1.put("course", sub);
                args1.put("searchKey", searchHistory.getContent());
                futureSearchResultList.add(executorService.submit(new SearchResultCallable(args1)));
            }
            for(Future<JSONArray> futureData : futureSearchResultList) {
                JSONArray data;
                try {
                    data = futureData.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                if(data != null) {
                    for (Object obj : data) {
                        JSONObject entity = JSON.parseObject(obj.toString());
                        String name = entity.getString("label");
                        Map<String, String> args1 = new HashMap<>();
                        args1.put("id", id);
                        args1.put("uriName", name);
                        futureProblems.add(executorService.submit(new ProblemCallable(args1)));
                    }
                }
            }
        }
        for(VisitHistory visitHistory : visitHistoryList) {
            Map<String, String> args1 = new HashMap<>();
            args1.put("id", id);
            args1.put("uriName", visitHistory.getName());
            futureProblems.add(executorService.submit(new ProblemCallable(args1)));
        }

        for(Future<JSONArray> futureProblem : futureProblems) {
            JSONArray problem;
            try {
                problem = futureProblem.get();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            for(Object object : problem) {
                JSONObject jsonObject = JSON.parseObject(object.toString());
                if(results.containsKey(jsonObject))
                    results.replace(jsonObject, results.get(jsonObject) + 1);
                else
                    results.put(jsonObject, 1);
            }
        }

        List<Map.Entry<JSONObject, Integer>> entryList = new ArrayList<>(results.entrySet());
        entryList.sort(Map.Entry.comparingByValue());
        Collections.reverse(entryList);

        List<JSONObject> result = new ArrayList<>();

        for (Map.Entry<JSONObject, Integer> entry : entryList.subList(Math.min(offset, entryList.size()),Math.min(offset + 10, entryList.size())))
            result.add(entry.getKey());

        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        reply.put("data", result);
        printWriter.print(reply);
    }

    @PostMapping("/addNewSave")
    public void addNewSaveProblem(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        if(!jsonParam.containsKey("token") || !jsonParam.containsKey("problem")) {
            JSONObject reply = new JSONObject();
            response.setStatus(406);
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }
        String token = jsonParam.getString("token");
        User user = tokenServices.queryUserByToken(token);
        if(user == null || !tokenServices.isTokenValid(token)) {
            JSONObject reply = new JSONObject();
            response.setStatus(403);
            reply.put("status", "fail");
            reply.put("message", "invalid token");
            printWriter.print(reply);
            return;
        }
        String problem = jsonParam.getJSONObject("problem").toString();
        if(!jsonParam.containsKey("subject"))
            jsonParam.put("subject", "");
        int id = problemServices.insertNewProblem(problem, jsonParam.getString("subject")).getId();
        problemServices.addNewSave(user.getId(), id);
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }

    @GetMapping("/getSaves")
    public void getSaves(@RequestParam("token") String token, @RequestParam(value = "offset", required = false, defaultValue = "0") int offset, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        User user = tokenServices.queryUserByToken(token);
        if(user == null || !tokenServices.isTokenValid(token)) {
            JSONObject reply = new JSONObject();
            response.setStatus(403);
            reply.put("status", "fail");
            reply.put("message", "invalid token");
            printWriter.print(reply);
            return;
        }
        List<Problem> problemList = problemServices.querySavesById(user.getId(), offset);
        JSONArray problemsArray = new JSONArray();
        for(Problem problem : problemList) {
            JSONObject jsonObject = JSON.parseObject(problem.getJson());
            jsonObject.put("subject", problem.getSubject());
            problemsArray.add(jsonObject);
        }
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        reply.put("data", problemsArray);
        printWriter.print(reply);
    }

    @PostMapping("/deleteSave")
    public void deleteSaveProblem(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        if(!jsonParam.containsKey("token") || !jsonParam.containsKey("problem")) {
            JSONObject reply = new JSONObject();
            response.setStatus(406);
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }
        logger.info(jsonParam.toString());
        String token = jsonParam.getString("token");
        User user = tokenServices.queryUserByToken(token);
        if(user == null || !tokenServices.isTokenValid(token)) {
            JSONObject reply = new JSONObject();
            response.setStatus(403);
            reply.put("status", "fail");
            reply.put("message", "invalid token");
            printWriter.print(reply);
            return;
        }
        JSONObject problem = jsonParam.getJSONObject("problem");
        problem.remove("subject");
        logger.info(problem.toString());
        problemServices.deleteSave(user.getId(), problem.toString());
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }
}
