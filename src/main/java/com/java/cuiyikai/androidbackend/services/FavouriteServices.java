package com.java.cuiyikai.androidbackend.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Favourite;
import com.java.cuiyikai.androidbackend.entity.User;
import com.java.cuiyikai.androidbackend.mapper.FavouriteMapper;
import com.java.cuiyikai.androidbackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class FavouriteServices {

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
                if(object.getString("name").equals(value.getString("name"))) {
                    flag = array.indexOf(obj);
                    break;
                }
            }
            if(directorySet.contains(entry.getKey()) && flag == -1) {
                JSONObject object = new JSONObject();
                object.put("name", value.getString("name"));
                object.put("subject", value.getString("subject"));
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
        System.out.printf("update %s%n", favouriteJson);
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
            contains.add(JSON.parseObject(obj.toString()).getString("name"));
        for(Object obj : jsonArray)
            if(!contains.contains(JSON.parseObject(obj.toString()).getString("name")))
                array.add(obj);
        favouriteJson.replace(directoryName, array);
        System.out.printf("move %s%n", favouriteJson);
        favouriteMapper.updateUserFavourite(userMapper.queryUserByUsername(username).getId(), favouriteJson.toJSONString());
    }

    public Favourite getFavouriteByUsername(String username) {
        return favouriteMapper.getFavouriteByUsername(username);
    }
}
