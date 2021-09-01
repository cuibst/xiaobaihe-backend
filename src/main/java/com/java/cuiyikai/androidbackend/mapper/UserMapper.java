package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM username WHERE username=#{username}")
    User queryUserByUsername(String username);

    @Insert("INSERT INTO username (username, password, email, checked) VALUES (#{username}, #{password}, #{email}, FALSE)")
    int insertNewUser(String username, String password, String email);

    @Update("UPDATE username SET checked = true WHERE username = #{username}")
    int updateChekcedEmailStatus(String username);

    @Update("UPDATE username SET password = #{password}, email = #{email}, checked = #{checked} WHERE id = #{userId}")
    int updateUserInfo(String password, String email, boolean checked, int userId);
}
