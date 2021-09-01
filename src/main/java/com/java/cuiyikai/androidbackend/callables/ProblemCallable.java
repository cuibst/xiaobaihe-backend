package com.java.cuiyikai.androidbackend.callables;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.buildForm;
import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.setConnectionHeader;

public class ProblemCallable implements Callable<JSONArray> {

    private final Map<String, String> args;

    public ProblemCallable(Map<String, String> args) {
        this.args = args;
    }

    @Override
    public JSONArray call() throws Exception {
        String form = buildForm(args);
        URL url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName?" + form);
        HttpURLConnection cardConnection = (HttpURLConnection) url.openConnection();
        setConnectionHeader(cardConnection, "GET");
        JSONArray result;
        if(cardConnection.getResponseCode() == 200)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(cardConnection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder buffer = new StringBuilder();
            while((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            JSONObject cardResponse = JSON.parseObject(buffer.toString());
            result = cardResponse.getJSONArray("data");
        }
        else
            return new JSONArray();
        cardConnection.disconnect();
        return result;
    }
}
