package com.java.cuiyikai.androidbackend.callables;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Uri;
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
 * Class {@link SearchResultCallable} implements {@link Callable}.
 * <p>This {@link Callable} is used to simulate the search to retrieve the search result</p>
 * <p> Returns data in {@link JSONArray}, each object inside as follows. </p>
 * <pre>{@code
 * {
 *     "label"    : < A String represent entity name. >
 *     "category" : < A String represent entity's category. >
 *     "id"       : < Entity's uri in the edukg database. >
 * }
 * }</pre>
 */
public class SearchResultCallable implements Callable<JSONArray> {

    private final Map<String, String> args;

    /**
     * Only constructor for {@link SearchResultCallable}
     * @param args A {@link Map} represent the request arguments, <strong>must</strong> include these keys:
     *             <p>"id" : request key for edukg. <br>
     *                "searchKey" : The content of the search text <br>
     *                "course" : The subject in which you want to search.
     *             </p>
     */
    public SearchResultCallable(Map<String, String> args) {
        this.args = args;
    }

    /**
     * {@inheritDoc}
     * @return A {@link JSONArray} described above.
     * @throws Exception when the process failed.
     */
    @Override
    public JSONArray call() throws Exception {
        URL url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList?" + buildForm(args));
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
