package com.java.cuiyikai.androidbackend.callables;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Uri;
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

/**
 * Class {@link RelatedCallable} implements {@link Callable}.
 * <p>This {@link Callable} is used to get related entities for the corresponding entity name.</p>
 * <p> Returns data in {@link JSONArray}, each object inside as follows. </p>
 * <pre>{@code
 * {
 *     "subject"   : < A String represent entity name. >
 *     "predicate" : < A String represent relation name. >
 * }
 * }</pre>
 */
public class RelatedCallable implements Callable<JSONArray> {

    private final Map<String, String> args;

    private static final Logger logger = LoggerFactory.getLogger(RelatedCallable.class);

    /**
     * Only constructor for {@link RelatedCallable}
     * @param args A {@link Map} represent the request arguments, <strong>must</strong> include these keys:
     *             <p>"id" : request key for edukg. <br>
     *                "subjectName" : The name of the {@link Uri} you want to query. <br>
     *                "course" : The subject of uri
     *             </p>
     */
    public RelatedCallable(Map<String, String> args) {
        this.args = args;
    }

    /**
     * {@inheritDoc}
     * @return A {@link JSONArray} described above.
     * @throws Exception
     */
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
