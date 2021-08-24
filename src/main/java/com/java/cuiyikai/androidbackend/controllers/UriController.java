package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.SearchHistory;
import com.java.cuiyikai.androidbackend.entity.Uri;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.entity.VisitHistory;
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

@Controller
@RequestMapping("/api/uri")
public class UriController {

    @Autowired
    UriServices uriServices;

    @Autowired
    HistoryServices historyServices;

    @Autowired
    TokenServices tokenServices;

    Logger logger = LoggerFactory.getLogger(UriController.class);

    public static void setConnectionHeader(HttpURLConnection connection, String method) throws ProtocolException {
        System.out.printf("Set connection method : %s%n", method);
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        if(method.equals("POST")) {
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        }
        connection.setDoInput(true);
    }

    public static String buildForm(Map<String,String> form) {
        if(form.size() == 0)
            return "";
        StringBuilder builder = new StringBuilder();
        form.forEach((key, value) -> {
            try {
                builder.append(URLEncoder.encode(key, "UTF-8"));
                builder.append('=');
                builder.append(URLEncoder.encode(value, "UTF-8"));
                builder.append('&');
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

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

    private JSONObject getUriName(Uri uri, String id) throws Exception{
        JSONObject uriName = new JSONObject();
        URL url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/getKnowledgeCard");
        HttpURLConnection cardConnection = (HttpURLConnection) url.openConnection();
        setConnectionHeader(cardConnection, "POST");
        Map<String, String> args = new HashMap<>();
        args.put("id", id);
        args.put("uri", uri.getUri());
        args.put("course", uri.getSubject());
        logger.info(url.toString());
        logger.info("{} {} {}", id, uri.getUri(), uri.getSubject());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(cardConnection.getOutputStream(), StandardCharsets.UTF_8));
        writer.write(buildForm(args));
        writer.flush();
        if(cardConnection.getResponseCode() == 200)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(cardConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder buffer = new StringBuilder();
            while((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            JSONObject cardResponse = JSON.parseObject(buffer.toString());
            JSONObject data = cardResponse.getJSONObject("data");
            if(data.getString("entity_name") == null)
                return null;
            uriName.put("name", data.getString("entity_name"));
            uriName.put("subject", uri.getSubject());
            reader.close();
        }
        else
            return null;
        writer.close();
        cardConnection.disconnect();
        return uriName;
    }

    private JSONArray getSearchResult(Map<String, String> args) throws Exception{
        URL url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList?" + buildForm(args));
        HttpURLConnection cardConnection = (HttpURLConnection) url.openConnection();
        setConnectionHeader(cardConnection, "GET");
        logger.info(url.toString());
        JSONArray result;
        if(cardConnection.getResponseCode() == 200)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(cardConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder buffer = new StringBuilder();
            while((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            JSONObject cardResponse = JSON.parseObject(buffer.toString());
            result = cardResponse.getJSONArray("data");
        }
        else
            return null;
        cardConnection.disconnect();
        return result;
    }

    private JSONArray getRelate(Map<String, String> args) throws Exception{
        URL url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/relatedsubject");
        HttpURLConnection cardConnection = (HttpURLConnection) url.openConnection();
        setConnectionHeader(cardConnection, "POST");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(cardConnection.getOutputStream(), StandardCharsets.UTF_8));
        writer.write(buildForm(args));
        logger.info(url.toString());
        logger.info(buildForm(args));
        writer.flush();
        JSONArray result;
        if(cardConnection.getResponseCode() == 200)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(cardConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder buffer = new StringBuilder();
            while((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            JSONObject cardResponse = JSON.parseObject(buffer.toString());
            result = cardResponse.getJSONArray("data");
        }
        else
            return null;
        writer.close();
        cardConnection.disconnect();
        return result;
    }

    final String[] SUBJECTS = {"chinese", "english", "math", "physics", "chemistry", "biology", "history", "geo", "politics"};

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
        args.put("phone", "16688092093");
        args.put("password", "0730llhh");
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
//                JSONArray objectList = new JSONArray();
                Map<JSONObject, Integer> results = new HashMap<>();
                for(SearchHistory searchHistory : searchHistoryList) {
                    for(String sub : SUBJECTS) {
                        args = new HashMap<>();
                        args.put("id", id);
                        args.put("course", sub);
                        args.put("searchKey", searchHistory.getContent());
                        JSONArray data = getSearchResult(args);
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
                }
                for(VisitHistory visitHistory : visitHistoryList) {
                    args = new HashMap<>();
                    args.put("id", id);
                    args.put("course", visitHistory.getSubject());
                    args.put("subjectName", visitHistory.getName());
                    JSONArray result = getRelate(args);
                    if(result != null) {
                        logger.info(result.toString());
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
        for (Uri uri : uriList) {
            JSONObject uriName = getUriName(uri, id);
            if(uriName != null)
                uriNames.add(uriName);
        }
        reply.put("data", uriNames);
        printWriter.print(reply);
    }
}
