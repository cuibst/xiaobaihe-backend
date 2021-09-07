package com.java.cuiyikai.androidbackend.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Favourite;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.mapper.FavouriteMapper;
import com.java.cuiyikai.androidbackend.mapper.UserMapper;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class FavouriteServices {

    private static final Logger logger = LoggerFactory.getLogger(FavouriteServices.class);

    @Autowired
    private FavouriteMapper favouriteMapper;

    @Autowired
    private UserMapper userMapper;

    public void createNewUserDefaultFavourite(String username) {
        User user = userMapper.queryUserByUsername(username);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("default", new JSONArray());
        favouriteMapper.addNewUserFavourite(user.getId(), jsonObject.toJSONString());
    }

    public void updateFavourite(String username, JSONObject value) {
        Favourite favourite = favouriteMapper.getFavouriteByUsername(username);
        JSONObject favouriteJson = JSON.parseObject(favourite.getJson());
        Set<String> directorySet = new HashSet<>();
        JSONArray directoryArray = value.getJSONArray("checked");
        for(Object directoryNames : directoryArray)
            directorySet.add((String) directoryNames);
        for(Map.Entry<String, Object> entry : favouriteJson.entrySet()) {
            JSONArray array = JSON.parseArray(entry.getValue().toString());
            int flag = -1;
            for(Object obj:array) {
                JSONObject object = JSON.parseObject(obj.toString());
                if(object.getString(NetworkUtilityClass.PARAMETER_NAME).equals(value.getString(NetworkUtilityClass.PARAMETER_NAME))) {
                    flag = array.indexOf(obj);
                    break;
                }
            }
            if(directorySet.contains(entry.getKey()) && flag == -1) {
                JSONObject object = new JSONObject();
                object.put(NetworkUtilityClass.PARAMETER_NAME, value.getString(NetworkUtilityClass.PARAMETER_NAME));
                object.put(NetworkUtilityClass.PARAMETER_SUBJECT, value.getString(NetworkUtilityClass.PARAMETER_SUBJECT));
                array.add(object);
                favouriteJson.replace(entry.getKey(), array);
            } else if(!directorySet.contains(entry.getKey()) && flag != -1) {
                array.remove(flag);
                favouriteJson.replace(entry.getKey(), array);
            }
        }
        favouriteMapper.updateUserFavourite(userMapper.queryUserByUsername(username).getId(), favouriteJson.toJSONString());
    }

    public void removeDirectory(String username, String directoryName) {
        Favourite favourite = favouriteMapper.getFavouriteByUsername(username);
        JSONObject favouriteJson = JSON.parseObject(favourite.getJson());
        favouriteJson.remove(directoryName);
        favouriteMapper.updateUserFavourite(userMapper.queryUserByUsername(username).getId(), favouriteJson.toJSONString());
    }

    public boolean addDirectory(String username, String directoryName) {
        Favourite favourite = favouriteMapper.getFavouriteByUsername(username);
        JSONObject favouriteJson = JSON.parseObject(favourite.getJson());
        if(favouriteJson.containsKey(directoryName))
            return false;
        favouriteJson.put(directoryName, new JSONArray());
        favouriteMapper.updateUserFavourite(userMapper.queryUserByUsername(username).getId(), favouriteJson.toJSONString());
        return true;
    }

    public void updateDirectory(String username, String directoryName, JSONArray jsonArray) {
        Favourite favourite = favouriteMapper.getFavouriteByUsername(username);
        JSONObject favouriteJson = JSON.parseObject(favourite.getJson());
        if(!favouriteJson.containsKey(directoryName))
            return;
        favouriteJson.replace(directoryName, jsonArray);
        logger.info("update {}", favouriteJson);
        favouriteMapper.updateUserFavourite(userMapper.queryUserByUsername(username).getId(), favouriteJson.toJSONString());
    }

    public void moveDirectory(String username, String directoryName, JSONArray jsonArray) {
        Favourite favourite = favouriteMapper.getFavouriteByUsername(username);
        JSONObject favouriteJson = JSON.parseObject(favourite.getJson());
        if(!favouriteJson.containsKey(directoryName))
            return;
        Set<String> contains = new HashSet<>();
        JSONArray array = favouriteJson.getJSONArray(directoryName);
        for(Object obj : array)
            contains.add(JSON.parseObject(obj.toString()).getString(NetworkUtilityClass.PARAMETER_NAME));
        for(Object obj : jsonArray)
            if(!contains.contains(JSON.parseObject(obj.toString()).getString(NetworkUtilityClass.PARAMETER_NAME)))
                array.add(obj);
        favouriteJson.replace(directoryName, array);
        logger.info("move {}", favouriteJson);
        favouriteMapper.updateUserFavourite(userMapper.queryUserByUsername(username).getId(), favouriteJson.toJSONString());
    }

    public Favourite getFavouriteByUsername(String username) {
        return favouriteMapper.getFavouriteByUsername(username);
    }
}
