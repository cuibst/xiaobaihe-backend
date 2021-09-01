package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.SaveProblem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SaveProblemMapper {

    @Select("SELECT * FROM save_problems WHERE user_id = #{user_id} LIMIT 10 offset #{offset}")
    List<SaveProblem> querySavesByUserId(int user_id, int offset);

    @Insert("INSERT INTO save_problems (user_id, problem_id) VALUES (#{user_id}, #{problem_id})")
    void addNewSave(int user_id, int problem_id);

    @Delete("DELETE FROM save_problems WHERE user_id = #{user_id} and problem_id = #{problem_id}")
    void deleteSave(int user_id, int problem_id);

}
