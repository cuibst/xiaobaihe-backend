package com.java.cuiyikai.androidbackend.callables;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Uri;
import com.java.cuiyikai.androidbackend.services.UriServices;
import com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.buildForm;
import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.setConnectionHeader;

/**
 * Class {@link ProblemCallable} implements {@link Callable}.
 * <p>This {@link Callable} is used to get related problems for the corresponding entity name.</p>
 * <p> Returns data in {@link JSONArray}, each object inside as follows. </p>
 * <pre>{@code
 * {
 *     "qBody"   : < A string represent problem's statement, include choices >
 *     "qAnswer" : < A String represent problem's answer >
 *     "id"      : An integer, the problem's id
 * }
 * }</pre>
 * <p>Note that the answer and the choices might in <strong>multiple</strong> kinds of forms</p>
 */
public class ProblemCallable implements Callable<JSONArray> {

    private final Map<String, String> args;

    /**
     * Only constructor for {@link ProblemCallable}
     * @param args A {@link Map} represent the request arguments, <strong>must</strong> include these keys:
     *             <p>"id" : request key for edukg. <br>
     *                "uriName" : The name of the {@link Uri} you want to query. </p>
     */
    public ProblemCallable(Map<String, String> args) {
        this.args = args;
    }

    /**
     * {@inheritDoc}
     * @return A {@link JSONArray} described above.
     * @throws Exception
     */
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
            result = cardResponse.getJSONArray(NetworkUtilityClass.PARAMETER_DATA);
        }
        else
            return new JSONArray();
        cardConnection.disconnect();
        return result;
    }
}
