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

/**
 * <p> {@link Controller} for the favourite-related apis. </p>
 * <p> Mapped to url {@code "/api/favourite"}</p>
 */
@Controller
@RequestMapping("/api/favourite")
public class FavouriteController {

    @Autowired
    private FavouriteServices favouriteServices;

    @Autowired
    private TokenServices tokenServices;

    private static final Logger logger = LoggerFactory.getLogger(FavouriteController.class);

    /**
     * <p> Get the favourite json for the corresponding user</p>
     * <p> Mapped to uri {@code "/getFavourite"}</p>
     * <p> Use {@link RequestMethod#GET} method. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok",
     *      "data"   : {
     *          <directoryName> : [
     *              {
     *                  "name"    : < A String represent the favourite's name. >
     *                  "subject" : < A String represent the favourite's subject. >
     *              }, ... (every entry is in same pattern)
     *          ], ... (every entry is in same pattern)
     *      }
     * }}</pre>
     * @param token The request token for the user.
     * @param response A {@link HttpServletResponse}, see {@link GetMapping}.
     * @throws IOException see {@link GetMapping}
     */
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

    /**
     * <p> Update the favourite json for the corresponding user</p>
     * <p> More specifically, update the information (in which directories) of <strong>one</strong> entity</p>
     * <p> Mapped to uri {@code "/updateFavourite"}</p>
     * <p> Use {@link RequestMethod#POST} method. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param jsonParam A {@link JSONObject}, should contain following keys:
     *                  <p>
     *                  "token"   : Corresponding user's request token. <br>
     *                  "name"    : Entity's name. <br>
     *                  "subject" : Entity's subject <br>
     *                  "checked" : A {@link com.alibaba.fastjson.JSONArray} of the directory names that the entity in.
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link PostMapping}.
     * @throws IOException see {@link PostMapping}
     */
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

    /**
     * <p> Remove a directory in user's favourite</p>
     * <p> Mapped to uri {@code "/removeDirectory"}</p>
     * <p> Use {@link RequestMethod#POST} method. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param jsonParam A {@link JSONObject}, should contain following keys:
     *                  <p>
     *                  "token"     : Corresponding user's request token. <br>
     *                  "directory" : The name of the directory to be deleted.
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link PostMapping}.
     * @throws IOException see {@link PostMapping}
     */
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

    /**
     * <p> Add a new directory in user's favourite</p>
     * <p> Mapped to uri {@code "/addDirectory"}</p>
     * <p> Use {@link RequestMethod#POST} method. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param jsonParam A {@link JSONObject}, should contain following keys:
     *                  <p>
     *                  "token"     : Corresponding user's request token. <br>
     *                  "directory" : The name of the directory to be added.
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link PostMapping}.
     * @throws IOException see {@link PostMapping}
     */
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

    /**
     * <p> Change an existing directory to a favourite {@link com.alibaba.fastjson.JSONArray}</p>
     * <p> Mapped to uri {@code "/updateDirectory"}</p>
     * <p> Use {@link RequestMethod#POST} method. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param jsonParam A {@link JSONObject}, should contain following keys:
     *                  <p>
     *                  "token"     : Corresponding user's request token. <br>
     *                  "directory" : The name of the directory to be added. <br>
     *                  "json"      : A {@link com.alibaba.fastjson.JSONArray}, all of its elements are as follows :
     *                  <pre>{@code
     *    {
     *        "name"    : < entity's name >
     *        "subject" : < entity's subject >
     *    }
     *                  }</pre>
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link PostMapping}.
     * @throws IOException see {@link PostMapping}
     */
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

    /**
     * <p> Insert all the elements from a favourite {@link com.alibaba.fastjson.JSONArray} to an existing directory</p>
     * <p> Mapped to uri {@code "/moveDirectory"}</p>
     * <p> Use {@link RequestMethod#POST} method. And reply the json as follows (when success).</p>
     * <pre>{@code
     * {
     *      "status" : "ok"
     * }}</pre>
     * @param jsonParam A {@link JSONObject}, should contain following keys:
     *                  <p>
     *                  "token"     : Corresponding user's request token. <br>
     *                  "directory" : The name of the directory to be added. <br>
     *                  "json"      : A {@link com.alibaba.fastjson.JSONArray}, all of its elements are as follows :
     *                  <pre>{@code
     *    {
     *        "name"    : < entity's name >
     *        "subject" : < entity's subject >
     *    }
     *                  }</pre>
     *                  </p>
     * @param response A {@link HttpServletResponse}, see {@link PostMapping}.
     * @throws IOException see {@link PostMapping}
     */
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
