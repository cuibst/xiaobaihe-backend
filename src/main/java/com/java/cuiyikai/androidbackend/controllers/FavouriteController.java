package com.java.cuiyikai.androidbackend.controllers;

import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Favourite;
import com.java.cuiyikai.androidbackend.services.FavouriteServices;
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

@Controller
@RequestMapping("/api/favourite")
public class FavouriteController {

    @Autowired
    private FavouriteServices favouriteServices;

    @Autowired
    private TokenServices tokenServices;

    private static Logger logger = LoggerFactory.getLogger(FavouriteController.class);

    @GetMapping("/getFavourite")
    public void getFavourite(@RequestParam String token, HttpServletResponse response) throws IOException {
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
        logger.info("Getting favourite for user : {}", username);
        Favourite favourite = favouriteServices.getFavouriteByUsername(username);
        if(favourite == null) {
            favouriteServices.createNewUserDefaultFavourite(username);
            favourite = favouriteServices.getFavouriteByUsername(username);
        }
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        reply.put(NetworkUtilityClass.PARAMETER_DATA, favourite.getJson());
        printWriter.print(reply);
    }

    @PostMapping("/updateFavourite")
    public void updateFavourite(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString(NetworkUtilityClass.PARAMETER_TOKEN);
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey("name") || !jsonParam.containsKey(NetworkUtilityClass.PARAMETER_SUBJECT) || !jsonParam.containsKey("checked")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }

        favouriteServices.updateFavourite(username, jsonParam);

        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }

    @PostMapping("/removeDirectory")
    public void removeDirectory(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString(NetworkUtilityClass.PARAMETER_TOKEN);
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey(NetworkUtilityClass.PARAMETER_DIRECTORY)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }

        favouriteServices.removeDirectory(username, jsonParam.getString(NetworkUtilityClass.PARAMETER_DIRECTORY));
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }

    @PostMapping("/addDirectory")
    public void addDirectory(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString(NetworkUtilityClass.PARAMETER_TOKEN);
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey(NetworkUtilityClass.PARAMETER_DIRECTORY)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }

        boolean res = favouriteServices.addDirectory(username, jsonParam.getString(NetworkUtilityClass.PARAMETER_DIRECTORY));
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, res ? NetworkUtilityClass.STATUS_OK : NetworkUtilityClass.STATUS_FAIL);
        printWriter.print(reply);
    }

    @PostMapping("/updateDirectory")
    public void updateDirectory(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString(NetworkUtilityClass.PARAMETER_TOKEN);
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey(NetworkUtilityClass.PARAMETER_DIRECTORY) || !jsonParam.containsKey("json")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }

        logger.info("update {}", jsonParam);

        favouriteServices.updateDirectory(username, jsonParam.getString(NetworkUtilityClass.PARAMETER_DIRECTORY), jsonParam.getJSONArray("json"));
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }

    @PostMapping("/moveDirectory")
    public void moveDirectory(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        response.setHeader(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.JSON_CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        String token = jsonParam.getString(NetworkUtilityClass.PARAMETER_TOKEN);
        if(!tokenServices.isTokenValid(token)) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_TOKEN_MESSAGE);
            printWriter.print(reply);
            return;
        }
        String username = tokenServices.queryUserByToken(token).getUsername();
        if(!jsonParam.containsKey(NetworkUtilityClass.PARAMETER_DIRECTORY) || !jsonParam.containsKey("json")) {
            response.setStatus(406);
            JSONObject reply = new JSONObject();
            reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_FAIL);
            reply.put(NetworkUtilityClass.PARAMETER_MESSAGE, NetworkUtilityClass.BAD_REQUEST_MESSAGE);
            printWriter.print(reply);
            return;
        }

        logger.info("move {}", jsonParam);

        favouriteServices.moveDirectory(username, jsonParam.getString(NetworkUtilityClass.PARAMETER_DIRECTORY), jsonParam.getJSONArray("json"));
        JSONObject reply = new JSONObject();
        reply.put(NetworkUtilityClass.PARAMETER_STATUS, NetworkUtilityClass.STATUS_OK);
        printWriter.print(reply);
    }

}
