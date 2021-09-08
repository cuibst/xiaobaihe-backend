package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Problem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.lang.Nullable;

import java.util.List;

@Mapper
public interface ProblemsMapper {

    /**
     * Query all the user's saved problems.
     * @param user_id the user's id you want to ask.
     * @param offset the offset of the return array.
     * @return A {@link List} of {@link Problem} saved by the user start from offset
     */
    @Select("SELECT * FROM problems WHERE id IN (SELECT problem_id FROM save_problems WHERE user_id = #{user_id}) LIMIT 10 OFFSET #{offset}")
    List<Problem> queryProblemById(int user_id, int offset);

    /**
     * Query a problem from the database with matching json
     * @param json the problem's json.
     * @return A {@link Problem} matches the json. {@code null} when problem doesn't exist.
     */
    @Nullable
    @Select("SELECT * FROM problems WHERE json = #{json}")
    Problem queryProblemByJson(String json);

    /**
     * Insert a new problem to the database.
     * @param json problem's json.
     * @param subject problem's subject.
     */
    @Insert("INSERT INTO problems (json, subject) VALUES (#{json}, #{subject})")
    void addNewProblem(String json, String subject);

}
