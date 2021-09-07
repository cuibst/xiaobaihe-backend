package com.java.cuiyikai.androidbackend.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.util.Map;

public class NetworkUtilityClass {

    public static final String CONTENT_TYPE        = "Content-type";
    public static final String FORM_CONTENT_TYPE   = "application/x-www-form-urlencoded; charset=UTF-8";
    public static final String JSON_CONTENT_TYPE   = "application/json;charset=UTF-8";

    public static final String PARAMETER_STATUS    = "status";
    public static final String PARAMETER_MESSAGE   = "message";
    public static final String PARAMETER_TOKEN     = "token";
    public static final String PARAMETER_DIRECTORY = "directory";
    public static final String PARAMETER_DATA      = "data";
    public static final String PARAMETER_SUBJECT   = "subject";
    public static final String PARAMETER_PROBLEM   = "problem";
    public static final String PARAMETER_EMAIL     = "email";
    public static final String PARAMETER_ID        = "id";
    public static final String PARAMETER_COURSE    = "course";
    public static final String PARAMETER_JSON      = "json";
    public static final String PARAMETER_NAME      = "name";

    public static final String STATUS_OK           = "ok";
    public static final String STATUS_FAIL         = "fail";

    public static final String BAD_TOKEN_MESSAGE   = "bad token";
    public static final String BAD_REQUEST_MESSAGE = "bad request";

    public static final String REQUEST_PHONE       = "15910826331";
    public static final String REQUEST_PASSWORD    = "cbst20001117";

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtilityClass.class);

    private NetworkUtilityClass() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void setConnectionHeader(HttpURLConnection connection, String method) throws ProtocolException {
        logger.info("Set connection method : {}", method);
        connection.setRequestMethod(method);
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(3000);
        if(method.equals("POST")) {
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setDoOutput(true);
            connection.setRequestProperty(NetworkUtilityClass.CONTENT_TYPE, NetworkUtilityClass.FORM_CONTENT_TYPE);
        }
        connection.setDoInput(true);
    }

    public static String buildForm(Map<String,String> form) {
        if(form.size() == 0)
            return "";
        StringBuilder builder = new StringBuilder();
        form.forEach((key, value) -> {
            try {
                builder.append(URLEncoder.encode(key, "UTF-8"));
                builder.append('=');
                builder.append(URLEncoder.encode(value, "UTF-8"));
                builder.append('&');
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
}
