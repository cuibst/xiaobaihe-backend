package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    /**
     * Get a user with matching username.
     * @param username the username you want to query.
     * @return The corresponding {@link User}
     */
    @Select("SELECT * FROM username WHERE username=#{username}")
    User queryUserByUsername(String username);

    /**
     * Insert a new user to the database.
     * @param username the user's username.
     * @param password the user's password.
     * @param email the user's email.
     */
    @Insert("INSERT INTO username (username, password, email, checked) VALUES (#{username}, #{password}, #{email}, FALSE)")
    void insertNewUser(String username, String password, String email);

    /**
     * Update a user's email status, i.e., validate a user's email.
     * @param username the user's username.
     */
    @Update("UPDATE username SET checked = true WHERE username = #{username}")
    void updateChekcedEmailStatus(String username);

    /**
     * Update a user's info.
     * @param password user's new password
     * @param email user's new email
     * @param checked user's new validate status
     * @param userId the user's id you want to modify.
     */
    @Update("UPDATE username SET password = #{password}, email = #{email}, checked = #{checked} WHERE id = #{userId}")
    void updateUserInfo(String password, String email, boolean checked, int userId);
}
