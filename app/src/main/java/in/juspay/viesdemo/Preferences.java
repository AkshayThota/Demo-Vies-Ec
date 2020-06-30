package in.juspay.viesdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import org.json.JSONException;
import org.json.JSONObject;

public class Preferences {
    private static final String PREFS_KEY = "viesDemoPrefs";

    public static String merchantId = "";
    public static String juspayApiKey = "";
    public static String environment = "sandbox";
    public static String test_mode= "false";
    public static String gwRefId = "";
    public static String useLocalEligibility = "true";

    static String clientId = "";
    static String safetyNetApiKey = "";
    static String appId = "in.juspay.amazonpay";

    @SuppressLint("HardwareIds")
    public static String getCustomerId(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), "android_id");
    }

    static void readPrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);

        try {
            JSONObject json = new JSONObject(preferences.getString(PREFS_KEY, "{}"));

            merchantId = json.optString("merchantId", merchantId);
            clientId = merchantId + "_android";
            safetyNetApiKey = json.optString("safetyNetApiKey", safetyNetApiKey);
            juspayApiKey = json.optString("juspayApiKey", juspayApiKey);
            appId = json.optString("appId", appId);
            gwRefId = json.optString("gwRefId", gwRefId);
            environment = json.optString("environment", environment);
            test_mode = json.optString("test_mode", test_mode);
            useLocalEligibility = json.optString("useLocalEligibility", useLocalEligibility);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void writePrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        JSONObject json = new JSONObject();

        try {
            json.put("merchantId", merchantId);
            json.put("safetyNetApiKey", safetyNetApiKey);
            json.put("juspayApiKey", juspayApiKey);
            json.put("appId", appId);
            json.put("gwRefId", gwRefId);
            json.put("environment", environment);
            json.put("test_mode", test_mode);
            json.put("useLocalEligibility", useLocalEligibility);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREFS_KEY, json.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
