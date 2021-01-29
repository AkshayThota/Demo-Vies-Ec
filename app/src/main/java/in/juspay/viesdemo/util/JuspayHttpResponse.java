package in.juspay.viesdemo.util;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Simple wrapper to hold information sent by the connection.
 * Once the response is read, the connection is closed.
 *
 * @author Sriduth
 */
public class JuspayHttpResponse {
    public final int responseCode;
    public final Map<String, List<String>> headers;
    public final byte[] responsePayload;

    private static final String LOG_TAG = "JuspayHttpResponse";

    public JuspayHttpResponse(int responseCode, @NonNull byte[] responsePayload,
                              Map<String, List<String>> headers)
    {
        this.responseCode = responseCode;
        this.responsePayload = responsePayload;
        this.headers = headers;
    }

    @SuppressWarnings("WeakerAccess")
    public JuspayHttpResponse(HttpURLConnection connection) throws IOException {
        this.responseCode = connection.getResponseCode();
        this.headers = connection.getHeaderFields();

        BufferedInputStream in;
        if ((responseCode >= 200 && responseCode < 300) || responseCode == 302) {
            in = new BufferedInputStream(connection.getInputStream());
        } else {
            in = new BufferedInputStream(connection.getErrorStream());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String hce = connection.getContentEncoding();
        if(hce != null && hce.equals("gzip")) {
//            juspayServices.sdkDebug(LOG_TAG, "GZIP Header Present");
            GZIPInputStream zis = new GZIPInputStream(in);
            byte[] bytes = new byte[1024];

            int numRead;
            try {
                while ((numRead = zis.read(bytes)) >= 0) {
                    out.write(bytes, 0, numRead);
                }
                this.responsePayload = out.toByteArray();
            } finally {
//                juspayServices.sdkDebug(LOG_TAG, "CLOSING GZIP STREAM");
                zis.close();
            }
        } else {
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer, 0, 1024)) != -1)
                out.write(buffer, 0, numRead);

            this.responsePayload = out.toByteArray();
        }

        in.close();
        out.flush();
        out.close();
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();

        try {
            object.put("responseCode", responseCode);
            object.put("responsePayload", responsePayload);
            object.put("headers", headers);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object.toString();
    }


}
