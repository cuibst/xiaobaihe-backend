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

import java.util.Map;

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

    public boolean checkFavouriteExist(String username) {
        return favouriteMapper.getFavouriteByUsername(username) != null;
    }

    public void updateFavourite(String username, String key, JSONObject value) {
        Favourite favourite = favouriteMapper.getFavouriteByUsername(username);
        JSONObject favouriteJson = JSON.parseObject(favourite.getJson());
        JSONArray array = favouriteJson.getJSONArray(key);
        if(array == null)
            array = new JSONArray();
        array.add(value);
        favouriteJson.put(key, array);
        favouriteMapper.updateUserFavourite(userMapper.queryUserByUsername(username).getId(), favouriteJson.toJSONString());
    }

    public void removeFromFavourite(String username, JSONObject value, String key) {
        Favourite favourite = favouriteMapper.getFavouriteByUsername(username);
        JSONObject favouriteJson = JSON.parseObject(favourite.getJson());
        JSONArray array = favouriteJson.getJSONArray(key);
        for (Object obj : array) {
            JSONObject jsonObject = JSON.parseObject(obj.toString());
            if(value.getString("subject").equals(jsonObject.getString("subject")) && value.getString("name").equals(jsonObject.getString("name"))) {
                array.remove(obj);
                favouriteJson.replace(key, array);
                break;
            }
        }
        favouriteMapper.updateUserFavourite(userMapper.queryUserByUsername(username).getId(), favouriteJson.toJSONString());
    }

    public Favourite getFavouriteByUsername(String username) {
        return favouriteMapper.getFavouriteByUsername(username);
    }
}
