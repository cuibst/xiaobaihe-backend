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

    public Problem insertNewProblem(String json, String subject) {
        if(problemsMapper.queryProblemByJson(json) == null)
            problemsMapper.addNewProblem(json, subject);
        return problemsMapper.queryProblemByJson(json);
    }

    public List<Problem> querySavesById(int user_id, int offset) {
        return problemsMapper.queryProblemById(user_id, offset);
    }

    public void addNewSave(int user_id, int problem_id) {
        saveProblemMapper.addNewSave(user_id, problem_id);
    }

    public void deleteSave(int user_id, String json) {
        saveProblemMapper.deleteSave(user_id, problemsMapper.queryProblemByJson(json).getId());
    }

}
