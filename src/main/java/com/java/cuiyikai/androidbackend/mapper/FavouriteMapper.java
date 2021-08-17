package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Favourite;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FavouriteMapper {

    @Insert("INSERT INTO favourite (user_id, JSON) VALUES (#{user_id}, #{content})")
    int addNewUserFavourite(int user_id, String content);

    @Update("UPDATE favourite json = #{favouriteJson} WHERE user_id = #{user_id}")
    int updateUserFavourite(int user_id, String favouriteJson);

    @Select("SELECT FROM favourite WHERE user_id in (SELECT id FROM username WHERE username = #{username} LIMIT 1)")
    Favourite getFavouriteByUsername(String username);
}
