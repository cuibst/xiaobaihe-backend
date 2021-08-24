package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Favourite;
import com.java.cuiyikai.androidbackend.entity.Token;
import com.java.cuiyikai.androidbackend.services.FavouriteServices;
import com.java.cuiyikai.androidbackend.services.TokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping("/api/favourite")
public class FavouriteController {

    @Autowired
    FavouriteServices favouriteServices;

    @Autowired
    TokenServices tokenServices;

    @GetMapping("/getFavourite")
    public void getFavourite(@RequestParam String token, HttpServletResponse response) throws IOException {
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
        System.out.println("Getting favourite json!!");
        String username = tokenServices.queryUserByToken(token).getUsername();
        Favourite favourite = favouriteServices.getFavouriteByUsername(username);
        if(favourite == null) {
            favouriteServices.createNewUserDefaultFavourite(username);
            favourite = favouriteServices.getFavouriteByUsername(username);
        }
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        reply.put("data", favourite.getJson());
        printWriter.print(reply);
    }

    @PostMapping("/updateFavourite")
    public void updateFavourite(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString("token");
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey("name") || !jsonParam.containsKey("subject") || !jsonParam.containsKey("checked")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }

        System.out.println(jsonParam.getString("name"));
        System.out.println(jsonParam.getString("subject"));
        System.out.println(jsonParam.getString("checked"));

        favouriteServices.updateFavourite(username, jsonParam);

        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }

    @PostMapping("/removeDirectory")
    public void removeDirectory(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString("token");
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey("directory")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }

        favouriteServices.removeDirectory(username, jsonParam.getString("directory"));
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }

    @PostMapping("/addDirectory")
    public void addDirectory(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString("token");
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey("directory")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }

        boolean res = favouriteServices.addDirectory(username, jsonParam.getString("directory"));
        JSONObject reply = new JSONObject();
        reply.put("status", res ? "ok" : "fail");
        printWriter.print(reply);
    }

    @PostMapping("/updateDirectory")
    public void updateDirectory(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString("token");
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey("directory") || !jsonParam.containsKey("json")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }

        System.out.printf("update %s%n", jsonParam);

        favouriteServices.updateDirectory(username, jsonParam.getString("directory"), jsonParam.getJSONArray("json"));
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }

    @PostMapping("/moveDirectory")
    public void moveDirectory(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString("token");
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey("directory") || !jsonParam.containsKey("json")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }

        System.out.printf("move %s%n", jsonParam);

        favouriteServices.moveDirectory(username, jsonParam.getString("directory"), jsonParam.getJSONArray("json"));
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }

}
