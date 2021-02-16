package in.juspay.viesdemo;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

//import in.juspay.godel.PaymentActivity;
import in.juspay.hypersdk.core.MerchantViewType;
import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallback;
import in.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter;
import in.juspay.hypersdk.ui.JuspayPaymentsCallback;
import in.juspay.hypersdk.ui.JuspayWebView;
import in.juspay.services.HyperServices;
//import in.juspay.services.PaymentServices;
import in.juspay.viesdemo.util.ViewBinder;
import in.juspay.viesdemo.util.ViewBinder.Binding;
import in.juspay.viesdemo.util.CardTextWatcher;
import in.juspay.viesdemo.util.Utils;
import in.juspay.vies.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

import static android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK;

public class MainActivity extends AppCompatActivity {
    /* TODO :
        1. Add functionality of both the eligibilty in comment
        2. Add Proper Logs
     */

    private HyperServices hyperServices;

    @Binding private EditText amountField;
    @Binding private EditText mobileNumberField;


    @Binding private Toolbar toolbar;

    @Binding private Button initiateButton;
    @Binding private Button initiateRequestCodeSnippet;
    @Binding private Button prefetchButton;
    @Binding private Button prefetchRequestCodeSnippet;
    @Binding private Button eligibilityButton;
    @Binding private Button payButton;
    @Binding private Button deEnrollButton;
    @Binding private Button deleteCardButton;
    @Binding private Button listCardButton;
    static final int SETTINGS_ACTIVITY_REQUEST_CODE = 9810;
    static boolean paymentServiceObject = false;
    ArrayList<String> endUrls = new ArrayList<>(Arrays.asList(".*sandbox.juspay.in\\/end.*",".*api.juspay.in\\/end.*"));
    ArrayList<AppPayEligibiltyData> appPayEligibiltyData;
    class AppPayEligibiltyData {
        public String mobile;
        public boolean cred;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI orientation
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        if (metrics.widthPixels > metrics.heightPixels) {
            setContentView(R.layout.activity_main_horizontal);
        } else {
            setContentView(R.layout.activity_main);
        }

        ViewBinder.bindAll(this);

        // Read the preferences
        Preferences.readPrefs(this);

        // Initialize the UI
        initUI();

//         Only call preFetch if there is going to be a gap of 2 seconds before actually initiating the vies SDK
//            prefetch();

        WebView.setWebContentsDebuggingEnabled(true);

    }

    private void initUI() {
        setSupportActionBar(toolbar);

//        mobileNumberField.addTextChangedListener(new CardTextWatcher('-', 4));
//        cardExpiryField.addTextChangedListener(new CardTextWatcher('/', 2));

        eligibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Preferences.useLocalEligibility.equalsIgnoreCase("true")) {
                    getEligibility();
                }
                else {
//                    initiateECSdk();
                    getEligibility();
                }
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initiateECSdk();
                payOnViesSdk(MainActivity.this);
            }
        });

        prefetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefetch();
            }
        });

        prefetchRequestCodeSnippet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this , ResponseViewActivity.class);
                intent.putExtra("response", getString(R.string.prefetch_request));
                startActivity(intent);
            }
        });

        initiateRequestCodeSnippet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResponseViewActivity.class);
                intent.putExtra("response",getString(R.string.initiate_request));
                startActivity(intent);
            }
        });

        initiateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hyperServices = new HyperServices(MainActivity.this);

                final JSONObject object = new JSONObject();
                try{
                    object.put("requestId",new Random().nextInt(10000)+"");
                    object.put(PaymentConstants.SERVICE,"in.juspay.ec");
                    object.put("betaAssets",Preferences.useBetaAssets);

                    JSONObject payload = new JSONObject();

                    payload.put("action","initiate");
                    payload.put("merchantId", Preferences.merchantId);
                    payload.put("clientId", Preferences.clientId);
                    payload.put("customerId", Preferences.getCustomerId(MainActivity.this));
//                    payload.put("drawFromStatusBar", "true");
                    payload.put(PaymentConstants.ENV, Preferences.environment);
//                    payload.put(PaymentConstants.PACKAGE_NAME, Preferences.appId);
//                    payload.put("safetynetApiKey", Preferences.safetyNetApiKey);
                    object.put(PaymentConstants.PAYLOAD,payload);

                    Log.d("initiateReq", object.toString(2));


                }catch (Exception e){
                    e.printStackTrace();
                }

                hyperServices.initiate(object, new HyperPaymentsCallbackAdapter() {
                    @Override
                    public void onEvent(JSONObject json, JuspayResponseHandler handler) {
                        try {
                            String event = json.optString("event", "");
                            if (event.equals("initiate_result")){
                                Log.d("initiateResp",json.toString(2));
//                              show the initiate result here
                                Toast.makeText(MainActivity.this, json.toString(2), Toast.LENGTH_SHORT).show();
                            } else if (event.equals("process_result")){
                                Intent i = new Intent( MainActivity.this , ResponseViewActivity.class);

                                i.putExtra("response", json.toString(2));
                                startActivity(i);
                            }
                        } catch(JSONException e) {
                            Log.e("Exception", "Exception in onEvent", e);
                        }
                    }
                });
            }
        });

    }

        private void prefetch () {
//            boolean useBetaAssets = true; // If you want to use beta assets

            JSONObject payload = new JSONObject();
            JSONObject innerPayload = new JSONObject();

            try {
                innerPayload.put("clientId",Preferences.clientId );
                payload.put("betaAssets", Preferences.useBetaAssets);
                payload.put("payload", innerPayload);
                payload.put("service", "in.juspay.ec"); //juspay service
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HyperServices.preFetch(MainActivity.this, payload);
            try {
                Toast.makeText(MainActivity.this, payload.toString(2), Toast.LENGTH_SHORT).show();
                Log.d("prefetch log", payload.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void initiateECSdk () {
        hyperServices = new HyperServices(this);

        final JSONObject object = new JSONObject();
        try{
            object.put("requestId",new Random().nextInt(10000)+"");
            object.put(PaymentConstants.SERVICE,"in.juspay.ec");

            JSONObject payload = new JSONObject();

            payload.put("action","initiate");
            payload.put("merchantId", Preferences.merchantId);
            payload.put("clientId", Preferences.clientId);
            payload.put("customerId", Preferences.getCustomerId(this));
            payload.put(PaymentConstants.ENV, Preferences.environment);
            payload.put(PaymentConstants.PACKAGE_NAME, Preferences.appId);
            payload.put("safetynetApiKey", Preferences.safetyNetApiKey);
            object.put(PaymentConstants.PAYLOAD,payload);
            object.put("betaAssets", Preferences.useBetaAssets);

        }catch (Exception e){
            e.printStackTrace();
        }


        hyperServices.initiate(object, new HyperPaymentsCallback() {
            @Override
            public void onStartWaitingDialogCreated(@Nullable View view) {

            }

            @Override
            public void onWebViewReady(JuspayWebView juspayWebView) {

            }

            @Override
            public void onEvent(JSONObject json, JuspayResponseHandler handler) {
                String event = json.optString("event", "");
                String action = json.optString("action", "");
                try {
                    if (event.equals("show_loader")) {
                    }
                    else if (event.equals("hide_loader")) {
                    }
                    else if (event.equals("process_result")) {
                        Intent i = new Intent(MainActivity.this, ResponseViewActivity.class);
                        i.putExtra("response", json.toString(2));
                        startActivity(i);
                    } else if (event.equals("initiate_result")) {
                        Toast.makeText(MainActivity.this, object.toString(2), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("Exception", "Exception in onEvent", e);
                }
                if (action.equals("VIES_PAY")) {
                    try {
                        Intent i = new Intent(MainActivity.this, ResponseViewActivity.class);
                        i.putExtra("response", json.toString(2));
                        Log.d("vies_pay_response",json.toString(2));
                        startActivity(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (action.equals("VIES_DISENROLL")) {
                    try {
                        Intent i = new Intent(MainActivity.this, ResponseViewActivity.class);
                        i.putExtra("response", json.getJSONObject("payload").toString(2));
                        startActivity(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (action.equals("VIES_DELETE_CARD")) {
                    try {
                        Intent i = new Intent(MainActivity.this, ResponseViewActivity.class);
                        i.putExtra("response", json.getJSONObject("payload").toString(2));
                        startActivity(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                    else if (action.equals("eligibility")) {
                        try {
                            Intent i = new Intent(MainActivity.this, ResponseViewActivity.class);
                            i.putExtra("response", json.getJSONObject("payload").toString(2));
                            startActivity(i);
                            Log.d("EligibleResponse", json.toString(2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            }

            @Nullable
            @Override
            public View getMerchantView(ViewGroup viewGroup, MerchantViewType merchantViewType) {
                return null;
            }

            @Nullable
            @Override
            public WebViewClient createJuspaySafeWebViewClient() {
                return null;
            }

            @Nullable
//            @Override
            public View getMerchantView(ViewGroup viewGroup) {
                return null;
            }
        });
    }


    public void getEligibility(){
        try{

            JSONObject payload = new JSONObject();
            JSONObject innerpayload = new JSONObject();
            JSONObject data = new JSONObject();
            JSONArray apps = new JSONArray();
            JSONObject obj1 = new JSONObject();
            JSONObject udf = new JSONObject();
            JSONArray credArray = new JSONArray();
            credArray.put("cred");
            udf.put("key", "value");
            obj1.put("checkType", credArray);

            String mobileNumberCheck = mobileNumberField.getText().toString();


            if (mobileNumberCheck.equals("")){
                obj1.put("", "");
            }else
                obj1.put("mobile", mobileNumberCheck);
//            obj1.put("udf", udf);
            apps.put(obj1);
            data.put("apps", apps);
            data.put("cards", new JSONArray());
            data.put("wallets", new JSONArray());
            innerpayload.put("action", "eligibility");
            innerpayload.put("amount", amountField.getText().toString());
            innerpayload.put("data", data);
            payload.put("requestId", "2160");
            payload.put("service", "in.juspay.ec");
            payload.put("payload", innerpayload);
            payload.put("betaAssets", Preferences.useBetaAssets);

            Log.d("EligibleLog", payload.toString(2));
            Toast.makeText(MainActivity.this, payload.toString(2), Toast.LENGTH_SHORT).show();
            hyperServices.process(payload);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void payOnViesSdk(final MainActivity ctx) {

        // Generate an order ID. This ID can be anything unique
        final String orderId = "O" + (long) (Math.random() * 10000000000L);
        final String amount = ctx.amountField.getText().toString();
        final String cardNumber = ctx.mobileNumberField.getText().toString();

        /* We need to hit the Txn API on the backend. For that, we do that in an async task.
           The response will be used to send to the VIES SDK to initiate a PAY. */

        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                return Utils.createTxnApi(ctx , amount, orderId );
//                return Utils.getSessionApi(ctx);
            }

            @Override
            protected void onPostExecute(JSONObject response) {
                String sessionToken = null;
                try {
                    Log.d("MainActivity", response.toString(2));
                    sessionToken = response.getJSONObject("juspay").getString("client_auth_token");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctx.payOnViesSdk(sessionToken , orderId);
            }
        }.execute();
    }

    private void payOnViesSdk(String sessionToken, String orderId) {

        try {
            JSONObject payload = new JSONObject();
            JSONObject object = new JSONObject();

            object.put("requestId",new Random().nextInt(10000)+"" );
            object.put("service","in.juspay.ec" );
            object.put("betaAssets",true);


            payload.put("action", "appPayTxn");
            payload.put("orderId", orderId);
            payload.put("paymentMethod", "CRED");
            payload.put("clientAuthToken", sessionToken);
            payload.put("application", "CRED");
            payload.put("walletMobileNumber", mobileNumberField.getText().toString());

                object.put("payload", payload);

            Log.d("PayLOG", object.toString(2));
                hyperServices.process(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        hyperServices.terminate();
    }

    @Override
    public void onBackPressed() {


        if(!hyperServices.onBackPressed()) {
            super.onBackPressed();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settingsIcon) {
            Intent intent = new Intent(this, SettingsActivity.class);
            if(paymentServiceObject) {
                hyperServices.terminate();
            }
            startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            initiateECSdk();
        }
    }
}
