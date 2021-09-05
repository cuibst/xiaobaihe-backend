package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.callables.NameCallable;
import com.java.cuiyikai.androidbackend.callables.RelatedCallable;
import com.java.cuiyikai.androidbackend.callables.SearchResultCallable;
import com.java.cuiyikai.androidbackend.entity.*;
import com.java.cuiyikai.androidbackend.services.HistoryServices;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import com.java.cuiyikai.androidbackend.services.UriServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.buildForm;
import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.setConnectionHeader;

@Controller
@RequestMapping("/api/uri")
public class UriController {

    @Autowired
    UriServices uriServices;

    @Autowired
    HistoryServices historyServices;

    @Autowired
    TokenServices tokenServices;

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    Logger logger = LoggerFactory.getLogger(UriController.class);

    @PostMapping("/add")
    public void addUri(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        Uri uri = new Uri();
        uri.setUri(jsonParam.getString("uri"));
        uri.setSubject(jsonParam.getString("subject"));
        int result = uriServices.insertNewUri(uri);
        JSONObject reply = new JSONObject();
        reply.put("status", result > 0 ? "ok" : "fail");
        printWriter.print(reply);
    }

    @GetMapping("/getname")
    public void getName(@RequestParam(name = "subject", required = false, defaultValue = "") String subject, @RequestParam(name = "token", required = false, defaultValue = "") String token, @RequestParam(name = "offset", required = false, defaultValue = "0") int offset, HttpServletResponse response) throws Exception {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        List<Uri> uriList;
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
                for(SearchHistory searchHistory : searchHistoryList) {
                    Map<String,String> args1 = new HashMap<>();
                    args1.put("id", id);
                    args1.put("course", searchHistory.getSubject());
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    if(data != null) {
                        logger.info(data.toString());
                        for (Object obj : data) {
                            JSONObject entity = JSON.parseObject(obj.toString());
                            JSONObject resultObject = new JSONObject();
                            resultObject.put("name", entity.getString("label"));
                            resultObject.put("subject", sub);
                            if(results.containsKey(resultObject))
                                results.replace(resultObject, results.get(resultObject) + 1);
                            else
                                results.put(resultObject, 1);
                        }
                    }
                }

                List<Future<JSONArray>> futureRelateList = new ArrayList<>();

                for(VisitHistory visitHistory : visitHistoryList) {
                    Map<String, String> args1 = new HashMap<>();
                    args1.put("id", id);
                    args1.put("course", visitHistory.getSubject());
                    args1.put("subjectName", visitHistory.getName());
                    futureRelateList.add(executorService.submit(new RelatedCallable(args1)));
                }

                for(int i=0;i<futureRelateList.size();i++) {
                    JSONArray result;
                    try {
                        result = futureRelateList.get(i).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    VisitHistory visitHistory = visitHistoryList.get(i);
                    if(result != null) {
                        for (Object obj : result) {
                            JSONObject entity = JSON.parseObject(obj.toString());
                            JSONObject resultObject = new JSONObject();
                            resultObject.put("name", entity.getString("subject"));
                            resultObject.put("subject", visitHistory.getSubject());
                            if(results.containsKey(resultObject))
                                results.replace(resultObject, results.get(resultObject) + 1);
                            else
                                results.put(resultObject, 1);
                        }
                    }
                }

                logger.info(results.toString());

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
                return;
            }
        }
        else
            uriList = uriServices.getRandomUriBySubject(subject);
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        JSONArray uriNames = new JSONArray();
        List<Future<JSONObject>> futureUris = new ArrayList<>();
        for (Uri uri : uriList) {
            futureUris.add(executorService.submit(new NameCallable(uri, uriServices, id)));
        }
        for(Future<JSONObject> futureUri : futureUris) {
            JSONObject object;
            try {
                object = futureUri.get();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if(object != null)
                uriNames.add(object);
        }
        logger.info(uriNames.toString());
        reply.put("data", uriNames);
        printWriter.print(reply);
    }
}
