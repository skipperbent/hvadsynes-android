package bonnier.android.services;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ServiceHvadSynes {

    private static final String ServiceUrl = "http://52.19.1.159";

    public enum PostType {
        GET,
        POST,
        PUT,
        DELETE
    }

    public JSONObject api() throws Exception {
        return this.api("", PostType.GET, null);
    }

    public JSONObject api(String path) throws Exception {
        return this.api(path, PostType.GET, null);
    }

    public JSONObject api(String path, PostType type, String[] params) throws Exception {

        String url = ServiceHvadSynes.ServiceUrl + "/" + path + "/";

        String method = (type != PostType.GET) ? "POST" : "GET";

        ArrayList<String> postData = new ArrayList<String>();
        postData.add("_method=" + method);
        if(params != null && params.length > 0) {
            for (String param : params) {
                String[] tmp = param.split("=");

                String value = (tmp.length > 1) ? URLEncoder.encode(tmp[1], "UTF-8") : "";
                postData.add(URLEncoder.encode(tmp[0], "UTF-8") + "=" + value);
            }
        }

        String urlEncodePostData = "?" + TextUtils.join("&", postData);

        if(type == PostType.GET) {
            url += urlEncodePostData;
        }

        Log.d(getClass().getName(), "Service url: " + url + " method: " + method);

        HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
        httpConnection.setRequestMethod(method);

        // TODO: change with correct authorization from constructor

        String userCredentials = "netTest:A11555640D4747A5B27B46333260F2F3";
        String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), Base64.DEFAULT));
        httpConnection.setRequestProperty ("Authorization", basicAuth);
        httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        httpConnection.setConnectTimeout(5000);

        if(type != PostType.GET) {
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            httpConnection.setRequestProperty("Content-Length", Integer.toString(urlEncodePostData.getBytes().length));

            OutputStream os = httpConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(urlEncodePostData);
            writer.flush();
            writer.close();
            os.close();
        }

        int responseCode = httpConnection.getResponseCode();
        if (responseCode == 200) {

            BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));

            String responseLine;
            StringBuffer response = new StringBuffer();

            while ((responseLine = responseReader.readLine()) != null) {
                response.append(responseLine + "\n");
            }
            responseReader.close();

            //Log.d("SERVICEHVADSYNES", "Response: " + response);

            JSONObject jsonResponse = new JSONObject(response.toString());

            return jsonResponse;
        }

        return null;
    }

}
