package in.juspay.viesdemo.util;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.security.ProviderInstaller;

import in.juspay.hypersdk.core.PaymentConstants;
//import in.juspay.hypersdk.utils.network.JuspayHttpResponse;
import in.juspay.viesdemo.Preferences;
//import in.juspay.android_lib.netutils.JuspayHttpResponse;
//import in.juspay.godel.core.PaymentConstants;
import in.juspay.vies.Card;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Utils {
    private Utils() {
    }

    private static String getBaseUrl() {
        switch (Preferences.environment) {
            case PaymentConstants.ENVIRONMENT.SANDBOX:
                return "https://sandbox.juspay.in";
            case PaymentConstants.ENVIRONMENT.PRODUCTION:
                if(Preferences.test_mode.equals("true")){
                    return "https://sandbox.juspay.in";
                }else{
                    return "https://api.juspay.in";
                }
            default:
                return "http://localhost:8080";
        }
    }

    public static Card getCard(String cardNumber) {
        Card card = new Card();

        card.setBin(cardNumber.replaceAll("-", "").substring(0, 6));
        card.setAlias(String.valueOf(cardNumber.hashCode()));
        card.setMaskedCard(cardNumber.replaceAll("(-\\d{4}){2}-", "-XXXX-XXXX-"));

        return card;
    }

    public static JSONObject createTxnApi(Context context, String orderId, String amount, String cardNumber, String cardExpMonth, String cardExpYear, String cardCvv, String cardAlias) {
        final String url = "/txns";
        
        final Map<String, String> headers = new HashMap<>();
        final Map<String, String> payload = new HashMap<>();
        final Map<String, String> orderCreatePayload = new HashMap<>();

        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("x-merchantid", Preferences.merchantId);

        orderCreatePayload.put(PaymentConstants.ORDER_ID, orderId);
        orderCreatePayload.put(PaymentConstants.AMOUNT, amount);
        orderCreatePayload.put(PaymentConstants.CUSTOMER_ID,Preferences.getCustomerId(context));
        orderCreatePayload.put("return_url", getBaseUrl() + "/end");
        orderCreatePayload.put("customer_email", "test@juspay.in");
        orderCreatePayload.put("customer_phone", "9999999999");

        orderCreatePayload.put("options.get_client_auth_token", "true");
        if(!Preferences.gwRefId.isEmpty()) {
            orderCreatePayload.put("metadata.JUSPAY:gateway_reference_id", Preferences.gwRefId);
        }

        for (Map.Entry<String, String> entry : orderCreatePayload.entrySet()) {
            payload.put("order." + entry.getKey(), entry.getValue());
        }

        payload.put(PaymentConstants.MERCHANT_ID, Preferences.merchantId);
        payload.put("payment_method_type", "CARD"); // Accepted values are CARD/NB
        payload.put("payment_method", "VISA");      // VISA / Mastercard
        payload.put("auth_type", "VIES");
        payload.put("card_number", cardNumber.replaceAll("-", ""));
        payload.put("card_exp_month", cardExpMonth);
        payload.put("card_exp_year", cardExpYear);
        payload.put("card_security_code", cardCvv);
        payload.put("card_alias", cardAlias);
        payload.put("save_to_locker", "true");
        payload.put("format", "json");
        Log.d("OrderCreate", payload.toString());

        try {
            return fromResponse(createRequest(context, url, "POST", headers, payload));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject getSessionApi(Context context) {
        final String url = "/customers/" + Preferences.getCustomerId(context) + "?options.get_client_auth_token=true";

        final Map<String, String> headers = new HashMap<>();

        headers.put("x-merchantid", Preferences.merchantId);

        try {
            return fromResponse(createRequest(context, url, "GET", headers, null));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JSONObject fromResponse(JuspayHttpResponse response) throws JSONException {
        return new JSONObject(new String(response.responsePayload));
    }

    private static void initializeSSLContext(Context context) {
        try {
            SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            ProviderInstaller.installIfNeeded(context.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JuspayHttpResponse createRequest(Context context, String url, String method, Map<String, String> headers, Map<String, String> payload) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(getBaseUrl() + url).openConnection();

        connection.setRequestMethod(method);

        try {
            connection.setSSLSocketFactory(new TLSSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((Preferences.juspayApiKey + ":").getBytes(), Base64.DEFAULT));

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        initializeSSLContext(context);

        if (payload != null && method.equals("POST")) {
            connection.setDoOutput(true);
            OutputStream stream = connection.getOutputStream();
            stream.write(generateQueryString(payload).getBytes());
        }

        return new JuspayHttpResponse(connection);
    }

    private static String generateQueryString(Map<String, String> queryString) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        Map.Entry e;
        for (Iterator var2 = queryString.entrySet().iterator(); var2.hasNext(); sb.append(URLEncoder.encode((String) e.getKey(), "UTF-8")).append('=').append(URLEncoder.encode((String) e.getValue(), "UTF-8"))) {
            e = (Map.Entry) var2.next();
            if (sb.length() > 0) {
                sb.append('&');
            }
        }

        return sb.toString();
    }
}

