package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Uri;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

@Controller
@RequestMapping("/api/uri")
public class UriController {

    @Autowired
    UriServices uriServices;

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
            logger.info(buffer.toString());
            JSONObject cardResponse = JSON.parseObject(buffer.toString());
            JSONObject data = cardResponse.getJSONObject("data");
            uriName.put("name", data.getString("entity_name"));
            uriName.put("subject", uri.getSubject());
            reader.close();
        }
        writer.close();
        cardConnection.disconnect();
        return uriName;
    }

    @GetMapping("/getName")
    public void getName(@RequestParam(name = "subject", required = false, defaultValue = "") String subject, HttpServletResponse response) throws Exception {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        List<Uri> uriList;
        if(subject.equals(""))
            uriList = uriServices.getRandomUri();
        else
            uriList = uriServices.getRandomUriBySubject(subject);
        String urlLogin = "http://open.edukg.cn/opedukg/api/typeAuth/user/login";
        URL url = new URL(urlLogin);
        HttpURLConnection loginConnection = (HttpURLConnection) url.openConnection();
        setConnectionHeader(loginConnection, "POST");
        Map<String, String> args = new HashMap<>();
        args.put("phone", "15910826331");
        args.put("password", "cbst20001117");
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
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        JSONArray uriNames = new JSONArray();
        for (Uri uri : uriList) {
            uriNames.add(getUriName(uri, id));
        }
        reply.put("data", uriNames);
        printWriter.print(reply);
    }
}
