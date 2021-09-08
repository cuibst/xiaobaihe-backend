package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.Problem;
import com.java.cuiyikai.androidbackend.mapper.ProblemsMapper;
import com.java.cuiyikai.androidbackend.mapper.SaveProblemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProblemServices {

    @Autowired
    private ProblemsMapper problemsMapper;

    @Autowired
    private SaveProblemMapper saveProblemMapper;

    /**
     * Add the problem to the database if not exist and return the problem.
     * @param json problem's json
     * @param subject problem's subject
     * @return The {@link Problem} matches the subject and json.
     */
    public Problem insertNewProblem(String json, String subject) {
        if(problemsMapper.queryProblemByJson(json) == null)
            problemsMapper.addNewProblem(json, subject);
        return problemsMapper.queryProblemByJson(json);
    }

    /**
     * Query all the user's saved problems.
     * @param user_id the user's id you want to ask.
     * @param offset the offset of the return array.
     * @return A {@link List} of {@link Problem} saved by the user start from offset
     */
    public List<Problem> querySavesById(int user_id, int offset) {
        return problemsMapper.queryProblemById(user_id, offset);
    }

    /**
     * Add a new save problem to the database.
     * @param user_id related user id.
     * @param problem_id related problem id.
     */
    public void addNewSave(int user_id, int problem_id) {
        saveProblemMapper.addNewSave(user_id, problem_id);
    }

    /**
     * Delete a save problem from the database.
     * @param user_id related user id.
     * @param json the problem's json.
     */
    public void deleteSave(int user_id, String json) {
        saveProblemMapper.deleteSave(user_id, problemsMapper.queryProblemByJson(json).getId());
    }

}
