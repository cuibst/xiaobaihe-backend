package com.java.cuiyikai.androidbackend.callables;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.java.cuiyikai.androidbackend.entity.Entity;
import com.java.cuiyikai.androidbackend.entity.Uri;
import com.java.cuiyikai.androidbackend.services.UriServices;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.buildForm;
import static com.java.cuiyikai.androidbackend.utilities.NetworkUtilityClass.setConnectionHeader;

/**
 * Class {@link NameCallable} implements {@link Callable}.
 * <p>This {@link Callable} is used to get the name for the corresponding {@link Uri}.</p>
 * <p>It will first search through the whole database through {@link UriServices}, <br>
 * and fetch it from backend and insert it into the database when the corresponding name doesn't exist. </p>
 * <p> Returns data in {@link JSONObject} as follows. </p>
 * <pre>{@code
 * {
 *      "name" :    < String represents Uri's name. >
 *      "subject" : < String represents Uri's subject. >
 * }
 * }</pre>
 */
public class NameCallable implements Callable<JSONObject> {

    private final Uri uri;
    private final UriServices uriServices;
    private final String id;
    private static final Logger logger = LoggerFactory.getLogger(NameCallable.class);

    /**
     * Only constructor for the {@link NameCallable} class.
     * @param uri The {@link Uri} whose name is wanted.
     * @param uriServices The {@link UriServices} being used to visit the database.
     * @param id The backend visit token for edukg.
     */
    public NameCallable(Uri uri, UriServices uriServices, String id) {
        this.uri = uri;
        this.uriServices = uriServices;
        this.id = id;
    }

    /**
     * {@inheritDoc}
     * @return Data in {@link JSONObject} as follows.
     * <p><pre>{@code
     * {
     *      "name" :    < String represents Uri's name. >
     *      "subject" : < String represents Uri's subject. >
     * }
     * }</pre></p>
     * @throws Exception
     */
    @Override
    public JSONObject call() throws Exception {
        if(uri.getEntity_id() == -1) {
            JSONObject uriName = new JSONObject();
            URL url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/getKnowledgeCard");
            HttpURLConnection cardConnection = (HttpURLConnection) url.openConnection();
            setConnectionHeader(cardConnection, "POST");
            Map<String, String> args = new HashMap<>();
            args.put(NetworkUtilityClass.PARAMETER_ID, id);
            args.put("uri", uri.getUri());
            args.put(NetworkUtilityClass.PARAMETER_COURSE, uri.getSubject());
            logger.info("{}", url);
            logger.info("Name callable : {} {} {}", id, uri.getUri(), uri.getSubject());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(cardConnection.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(buildForm(args));
            writer.flush();
            if (cardConnection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(cardConnection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                StringBuilder buffer = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                JSONObject cardResponse = JSON.parseObject(buffer.toString());
                JSONObject data = cardResponse.getJSONObject(NetworkUtilityClass.PARAMETER_DATA);
                if (data == null || data.getString("entity_name") == null)
                    return null;
                uriName.put("name", data.getString("entity_name"));
                uriName.put(NetworkUtilityClass.PARAMETER_SUBJECT, uri.getSubject());
                uriServices.insertNewEntity(uriName.toString());
                uriServices.updateEntityId(uri.getId(), uriServices.getEntityByJson(uriName.toString()).getId());
                reader.close();
            } else
                return null;
            writer.close();
            cardConnection.disconnect();
            return uriName;
        } else {
            Entity entity = uriServices.getEntityById(uri.getEntity_id());
            return JSON.parseObject(entity.getJson());
        }
    }
}
