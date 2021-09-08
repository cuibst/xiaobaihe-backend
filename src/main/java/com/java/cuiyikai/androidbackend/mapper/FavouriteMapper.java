package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Favourite;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FavouriteMapper {

    /**
     * Create a new favourite json including default directory for the user.
     * @param user_id the id of the user in the database.
     * @param content the default favourite json.
     */
    @Insert("INSERT INTO favourite (user_id, json) VALUES (#{user_id}, #{content})")
    void addNewUserFavourite(int user_id, String content);

    /**
     * Update a user's favourite to given json.
     * @param user_id the id of the user in database.
     * @param favouriteJson the new favourite json.
     */
    @Update("UPDATE favourite SET json = #{favouriteJson} WHERE user_id = #{user_id}")
    void updateUserFavourite(int user_id, String favouriteJson);

    /**
     * Query a user's favourite json by username.
     * @param username the user's name.
     * @return a {@link Favourite} including favourite json.
     */
    @Select("SELECT * FROM favourite WHERE user_id in (SELECT id FROM username WHERE username = #{username} LIMIT 1)")
    Favourite getFavouriteByUsername(String username);
}
