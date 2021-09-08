package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.callables.LoginCallable;
import com.java.cuiyikai.androidbackend.callables.NameCallable;
import com.java.cuiyikai.androidbackend.callables.RelatedCallable;
import com.java.cuiyikai.androidbackend.callables.SearchResultCallable;
import com.java.cuiyikai.androidbackend.entity.*;
import com.java.cuiyikai.androidbackend.services.HistoryServices;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import com.java.cuiyikai.androidbackend.services.UriServices;
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

@Controller
@RequestMapping("/api/uri")
public class UriController {

    @Autowired
    UriServices uriServices;

    @Autowired
    HistoryServices historyServices;

    @Autowired
    TokenServices tokenServices;

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    Logger logger = LoggerFactory.getLogger(UriController.class);

    @PostMapping("/add")
    public void addUri(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        Uri uri = new Uri();
        uri.setUri(jsonParam.getString("uri"));
        uri.setSubject(jsonParam.getString(NetworkUtilityClass.PARAMETER_SUBJECT));
        int result = uriServices.insertNewUri(uri);
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, result > 0 ? NetworkUtilityClass.STATUS_OK : NetworkUtilityClass.STATUS_FAIL);
        printWriter.print(reply);
    }

    @GetMapping("/getname")
    public void getName(@RequestParam(name = NetworkUtilityClass.PARAMETER_SUBJECT, required = false, defaultValue = "") String subject,
                        @RequestParam(name = NetworkUtilityClass.PARAMETER_TOKEN, required = false, defaultValue = "") String token,
                        @RequestParam(name = "offset", required = false, defaultValue = "0") int offset, HttpServletResponse response)
            throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        List<Uri> uriList;
        String id = null;
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
        if(subject.equals("")) {
            if(!tokenServices.isTokenValid(token))
                uriList = uriServices.getRandomUri();
            else {
                User user = tokenServices.queryUserByToken(token);
                List<VisitHistory> visitHistoryList = historyServices.getLatestVisitHistoryByUserId(user.getId());
                List<SearchHistory> searchHistoryList = historyServices.getLatestHistoryByUsername(user.getUsername());
                Map<JSONObject, Integer> results = new HashMap<>();
                List<Future<JSONArray>> futureSearchResultList = new ArrayList<>();
                List<String> subjects = new ArrayList<>();
                List<Future<JSONArray>> futureRelateList = new ArrayList<>();
                for(SearchHistory searchHistory : searchHistoryList) {
                    Map<String,String> args1 = new HashMap<>();
                    args1.put(NetworkUtilityClass.PARAMETER_ID, id);
                    args1.put(NetworkUtilityClass.PARAMETER_COURSE, searchHistory.getSubject());
                    args1.put("searchKey", searchHistory.getContent());
                    futureSearchResultList.add(executorService.submit(new SearchResultCallable(args1)));
                    subjects.add(searchHistory.getSubject());
                }
                for(int i=0;i<futureSearchResultList.size();i++) {
                    Future<JSONArray> futureData = futureSearchResultList.get(i);
                    String sub = subjects.get(i);
                    JSONArray data;
                    try {
                        data = futureData.get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                        continue;
                    }
                    if(data != null) {
                        logger.info("Data : {}", data);
                        for (Object obj : data) {
                            JSONObject entity = JSON.parseObject(obj.toString());
                            JSONObject resultObject = new JSONObject();
                            resultObject.put(NetworkUtilityClass.PARAMETER_NAME, entity.getString("label"));
                            resultObject.put(NetworkUtilityClass.PARAMETER_SUBJECT, sub);
                            if(results.containsKey(resultObject))
                                results.replace(resultObject, results.get(resultObject) + 1);
                            else
                                results.put(resultObject, 1);
                        }
                    }
                }


                for(VisitHistory visitHistory : visitHistoryList) {
                    Map<String, String> args1 = new HashMap<>();
                    args1.put(NetworkUtilityClass.PARAMETER_ID, id);
                    args1.put(NetworkUtilityClass.PARAMETER_COURSE, visitHistory.getSubject());
                    args1.put("name", visitHistory.getName());
                    futureRelateList.add(executorService.submit(new RelatedCallable(args1)));
                }

                for(int i=0;i<futureRelateList.size();i++) {
                    JSONArray result;
                    try {
                        result = futureRelateList.get(i).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                        continue;
                    }
                    VisitHistory visitHistory = visitHistoryList.get(i);

                    if(result != null) {
                        for (Object obj : result) {
                            JSONObject entity = JSON.parseObject(obj.toString());
                            JSONObject resultObject = new JSONObject();
                            if(entity.containsKey(NetworkUtilityClass.PARAMETER_SUBJECT))
                                resultObject.put(NetworkUtilityClass.PARAMETER_NAME, entity.getString("subject_label"));
                            else
                                resultObject.put(NetworkUtilityClass.PARAMETER_NAME, entity.getString("object_label"));
                            resultObject.put(NetworkUtilityClass.PARAMETER_SUBJECT, visitHistory.getSubject());
                            if(results.containsKey(resultObject))
                                results.replace(resultObject, results.get(resultObject) + 1);
                            else
                                results.put(resultObject, 1);
                        }
                    }
                }

                logger.info("Result : {}", results);

                List<Map.Entry<JSONObject, Integer>> entryList = new ArrayList<>(results.entrySet());
                entryList.sort(Map.Entry.comparingByValue());
                Collections.reverse(entryList);

                List<JSONObject> result = new ArrayList<>();

                for (Map.Entry<JSONObject, Integer> entry : entryList.subList(Math.min(offset, entryList.size()),Math.min(offset + 10, entryList.size())))
                    result.add(entry.getKey());

                JSONObject reply = new JSONObject();
                reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
                reply.put(NetworkUtilityClass.PARAMETER_DATA, result);
                printWriter.print(reply);
                return;
            }
        }
        else
            uriList = uriServices.getRandomUriBySubject(subject);
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        JSONArray uriNames = new JSONArray();
        List<Future<JSONObject>> futureUris = new ArrayList<>();
        for (Uri uri : uriList) {
            futureUris.add(executorService.submit(new NameCallable(uri, uriServices, id)));
        }
        for(Future<JSONObject> futureUri : futureUris) {
            JSONObject object;
            try {
                object = futureUri.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
                continue;
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                continue;
            }
            if(object != null)
                uriNames.add(object);
        }
        logger.info("Result : {}", uriNames);
        reply.put(NetworkUtilityClass.PARAMETER_DATA, uriNames);
        printWriter.print(reply);
    }
}
