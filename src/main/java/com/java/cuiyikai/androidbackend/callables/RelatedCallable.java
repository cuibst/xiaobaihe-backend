package com.java.cuiyikai.androidbackend.callables;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.buildForm;
import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.setConnectionHeader;

public class RelatedCallable implements Callable<JSONArray> {

    private final Map<String, String> args;

    private Logger logger = LoggerFactory.getLogger(RelatedCallable.class);

    public RelatedCallable(Map<String, String> args) {
        this.args = args;
    }

    @Override
    public JSONArray call() throws Exception {
        URL url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/relatedsubject");
        HttpURLConnection cardConnection = (HttpURLConnection) url.openConnection();
        setConnectionHeader(cardConnection, "POST");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(cardConnection.getOutputStream(), StandardCharsets.UTF_8));
        String formData = buildForm(args);
        writer.write(formData);
        logger.info("{}", url);
        logger.info("Relate callable form : {}", formData);
        writer.flush();
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
            result = cardResponse.getJSONArray(NetworkUtilityClass.PARAMETER_DATA);
        }
        else
            return new JSONArray();
        writer.close();
        cardConnection.disconnect();
        return result;
    }
}
