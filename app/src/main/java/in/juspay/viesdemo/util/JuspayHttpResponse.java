package in.juspay.viesdemo.util;


import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by Veera.Subbiah on 25/05/18.
 */

public class JuspayHttpResponse {
    public final int responseCode;
    public final Map<String, List<String>> headers;
    public final String responsePayload;

    public JuspayHttpResponse(int responseCode, @NonNull String responsePayload, Map<String, List<String>> headers) {
        this.responseCode = responseCode;
        this.responsePayload = responsePayload;
        this.headers = headers;
    }

    public JuspayHttpResponse(HttpURLConnection connection) throws IOException {
        this.responseCode = connection.getResponseCode();
        this.headers = connection.getHeaderFields();
        InputStreamReader responseReader;
        if((this.responseCode < 200 || this.responseCode >= 300) && this.responseCode != 302) {
            responseReader = new InputStreamReader(connection.getErrorStream());
        } else {
            responseReader = new InputStreamReader(connection.getInputStream());
        }

        BufferedReader in = new BufferedReader(responseReader);
        StringBuilder response = new StringBuilder();

        String inputLine;
        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();
        this.responsePayload = response.toString();
        connection.disconnect();
    }

    public String toString() {
        JSONObject object = new JSONObject();

        try {
            object.put("responseCode", this.responseCode);
            object.put("responsePayload", this.responsePayload);
            object.put("headers", this.headers);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return object.toString();
    }
}