package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Problem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface ProblemsMapper {

    @Select("SELECT * FROM problems WHERE id IN (SELECT problem_id FROM save_problems WHERE user_id = #{user_id}) LIMIT 10 OFFSET #{offset}")
    List<Problem> queryProblemById(int user_id, int offset);

    @Select("SELECT * FROM problems WHERE json = #{json}")
    Problem queryProblemByJson(String json);

    @Insert("INSERT INTO problems (json, subject) VALUES (#{json}, #{subject})")
    void addNewProblem(String json, String subject);

}
