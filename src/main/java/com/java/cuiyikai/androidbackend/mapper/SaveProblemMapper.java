package com.java.cuiyikai.androidbackend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SaveProblemMapper {

    /**
     * Add a new save problem to the database.
     * @param user_id related user id.
     * @param problem_id related problem id.
     */
    @Insert("INSERT INTO save_problems (user_id, problem_id) VALUES (#{user_id}, #{problem_id})")
    void addNewSave(int user_id, int problem_id);

    /**
     * Delete a save problem from the database.
     * @param user_id related user id.
     * @param problem_id related problem id.
     */
    @Delete("DELETE FROM save_problems WHERE user_id = #{user_id} and problem_id = #{problem_id}")
    void deleteSave(int user_id, int problem_id);

}
