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

    @GetMapping("/getfavourite")
    public void getFavourite(@RequestParam String token, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
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

    @PostMapping("/removefavourite")
    public void removeFavourite(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        String token = jsonParam.getString("token");
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey("name") || !jsonParam.containsKey("subject")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }

        String key;
        if(jsonParam.getString("key") != null)
            key = jsonParam.getString("key");
        else
            key = "default";

        favouriteServices.removeFromFavourite(username, jsonParam, key);
        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }

    @PostMapping("/addfavourite")
    public void addFavourite(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        String token = jsonParam.getString("token");
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad token");
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey("name") || !jsonParam.containsKey("subject")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put("status", "fail");
            reply.put("message", "bad request");
            printWriter.print(reply);
            return;
        }

        JSONObject obj = new JSONObject();
        obj.put("name", jsonParam.getString("name"));
        obj.put("subject", jsonParam.getString("subject"));

        String key;
        if(jsonParam.getString("key") != null)
            key = jsonParam.getString("key");
        else
            key = "default";

        favouriteServices.updateFavourite(username, key, obj);

        JSONObject reply = new JSONObject();
        reply.put("status", "ok");
        printWriter.print(reply);
    }

}
