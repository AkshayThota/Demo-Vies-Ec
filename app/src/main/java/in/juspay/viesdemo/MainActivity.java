package in.juspay.viesdemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallback;
import in.juspay.hypersdk.ui.JuspayPaymentsCallback;
import in.juspay.hypersdk.ui.JuspayWebView;
import in.juspay.viesdemo.util.ViewBinder;
import in.juspay.viesdemo.util.ViewBinder.Binding;
import in.juspay.viesdemo.util.CardTextWatcher;
import in.juspay.viesdemo.util.Utils;
//import in.juspay.godel.core.PaymentConstants;
//import in.juspay.godel.data.JuspayResponseHandler;
//import in.juspay.godel.ui.JuspayPaymentsCallback;
//import in.juspay.godel.ui.JuspayWebView;
import in.juspay.services.HyperServices;
import in.juspay.vies.Card;
//import in.juspay.vies.VIESConstants;
//import in.juspay.vies.VIESHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    /* TODO :
        1. Add functionality of both the eligibilty in comment
        2. Add Proper Logs
     */

    private HyperServices hyperServices;

    @Binding private EditText amountField;
    @Binding private EditText cardNumberField;
    @Binding private EditText cardExpiryField;
    @Binding private EditText cardCvvField;

    @Binding private Toolbar toolbar;

    @Binding private Button gpayEligibility;
    @Binding private Button eligibilityButton;
    @Binding private Button gpayPay;
    @Binding private Button payButton;
    @Binding private Button deEnrollButton;
    @Binding private Button deleteCardButton;
    @Binding private Button initiateButton;
    static final int SETTINGS_ACTIVITY_REQUEST_CODE = 9810;
    static boolean paymentServiceObject = false;

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

        /* Only call preFetch if there is going to be a gap of 2 seconds before actually initiating the vies SDK
           PaymentActivity.preFetch(this, MerchantConstants.MERCHANT_ID); */
        WebView.setWebContentsDebuggingEnabled(true);


    }

    private void initUI() {
        setSupportActionBar(toolbar);

        cardNumberField.addTextChangedListener(new CardTextWatcher('-', 4));
        cardExpiryField.addTextChangedListener(new CardTextWatcher('/', 2));

        eligibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Preferences.useLocalEligibility.equalsIgnoreCase("true")) {
//                    getLocalEligibility();
                }
                else {
//                    initiateViesSdk();
                    getEligibility();
                }
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initiateViesSdk();
                payOnViesSdk(MainActivity.this);
            }
        });

        deEnrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initiateViesSdk();
                deEnrollOnViesSdk(MainActivity.this);
            }
        });

        deleteCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initiateViesSdk();
                onDeleteCardButtonClicked();
            }
        });
        gpayEligibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                initiateViesSdk();
                gpayEligibilityCheck();
            }
        });
        gpayPay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                initiateViesSdk();
                gpayPay(MainActivity.this);
            }
        });
        initiateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                initiateViesSdk();
            }
        });

    }

    private void initiateViesSdk() {
        Bundle viesBundle = new Bundle();

        // Look into the Documentation for the keys. All the keys will be sent to the VIES SDK via the bundle
        viesBundle.putString(PaymentConstants.MERCHANT_ID, Preferences.merchantId);
        viesBundle.putString(PaymentConstants.CLIENT_ID, Preferences.clientId);
        viesBundle.putString(PaymentConstants.CUSTOMER_ID, Preferences.getCustomerId(this));
        viesBundle.putString(PaymentConstants.ENV, Preferences.environment);
        viesBundle.putString(PaymentConstants.SERVICE, "in.juspay.vies");
        viesBundle.putString(PaymentConstants.TEST_MODE, Preferences.test_mode);
        viesBundle.putString(PaymentConstants.PACKAGE_NAME, Preferences.appId);
        viesBundle.putString(PaymentConstants.SAFETYNET_API_KEY, Preferences.safetyNetApiKey);
        viesBundle.putString("betaAssets", "true");

        // Create the PaymentServices instance and initiate the SDK
        hyperServices = new HyperServices(this);
        paymentServiceObject = true ;
        hyperServices.initiate(viesBundle, new JuspayPaymentsCallback() {
            @Override
            public void onStartWaitingDialogCreated(@Nullable View view) {
            }

            @Override
            public void onWebViewReady(JuspayWebView juspayWebView) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            @Override
            public void onEvent(String json, JuspayResponseHandler juspayResponseHandler) {
                try {
                    // Parse the result here. These are the events that will be emitted by the VIES SDK.
                    JSONObject result = new JSONObject(json);
                    String event = result.getString("event");
                    JSONObject payload = result.getJSONObject(PaymentConstants.PAYLOAD);
                    Log.d("JRH (onEvent)", String.format("event: %s, payload: %s", event, payload.toString()));
                    // For demo purposes, we show the JSON response in the UI
                    if (event.equals("initiate_result")) {
                        Toast.makeText(MainActivity.this, json, Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(MainActivity.this, ResponseViewActivity.class);
                        intent.putExtra("response", result.toString(2));
                        hyperServices.terminate();
                        startActivityForResult(intent, 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Nullable
            @Override
            public View getMerchantView(ViewGroup viewGroup) {
                return null;
            }

            @Override
            public void onResult(int reqCode, int resCode, @NonNull Intent intent) {
            }
        });
    }

    private void gpayEligibilityCheck() {
        try {
            JSONObject payload = new JSONObject();
            payload.put("action", "GPAY_ELIGIBILITY");
            payload.put("service", "in.juspay.gpay");
            payload.put("request_id", "dummy_request_id1asdf");
            hyperServices.process("GPAY_ELIG", payload);
        }catch (Exception e){

        }

    }

//    private void getLocalEligibility() {
//
//        String cardNumber = cardNumberField.getText().toString();
//        Card card = Utils.getCard(cardNumber);
//        String amount = amountField.getText().toString();
//
//        try {
//            JSONObject payload = new JSONObject();
//
//            payload.put("customer_id", Preferences.getCustomerId(this));
//            payload.put("action", "VIES_ELIGIBILITY");
//            payload.put("amount", amount);
//            payload.put("cards", new JSONArray(Collections.singletonList(card.toJSON())));
//            payload.put(PaymentConstants.ENV, Preferences.environment);
//            payload.put("request_id", "dummy_request_id1");
//
////            String response = VIESHelper.getLocalEligibility(this, payload);
//
//            JSONObject res = new JSONObject(response);
//            JSONObject response_payload = res.getJSONObject(PaymentConstants.PAYLOAD);
//
//            Log.d("MainActivity", String.format("event: %s, payload: %s", "ELIGIBILITY", response_payload.toString()));
//
//            // Displaying result on Response View
//            Intent intent = new Intent(MainActivity.this, ResponseViewActivity.class);
//            intent.putExtra("response", response_payload.toString(2));
//            startActivityForResult(intent, 1);
//
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void getEligibility() {
        String cardNumber = cardNumberField.getText().toString();
        String amount = amountField.getText().toString();

        Card card = Utils.getCard(cardNumber);

        try {
            JSONObject payload = new JSONObject();

            payload.put("action", "VIES_ELIGIBILITY");
            payload.put("cards", new JSONArray(Collections.singletonList(card.toJSON())));
            payload.put("amount", amount);
            payload.put("service", "in.juspay.vies");

            hyperServices.process("ELIG", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void payOnViesSdk(final MainActivity ctx) {

        // Generate an order ID. This ID can be anything unique
        final String orderId = "O" + (long) (Math.random() * 10000000000L);
        final String amount = ctx.amountField.getText().toString();
        final String cardNumber = ctx.cardNumberField.getText().toString();
        final String cardCvv = ctx.cardCvvField.getText().toString();
        final String cardAlias = String.valueOf(cardNumber.hashCode());

        String[] expiry = ctx.cardExpiryField.getText().toString().split("/");
        final String cardExpMonth = expiry[0];
        final String cardExpYear = expiry[1];

        /* We need to hit the Txn API on the backend. For that, we do that in an async task.
           The response will be used to send to the VIES SDK to initiate a PAY. */

        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                return Utils.createTxnApi(ctx, orderId, amount, cardNumber, cardExpMonth, cardExpYear, cardCvv, cardAlias);
            }

            @Override
            protected void onPostExecute(JSONObject response) {
                try {
                    Log.d("MainActivity", response.toString(2));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctx.payOnViesSdk(response);
            }
        }.execute();
    }

    private static void gpayPay(final MainActivity ctx) {

        // Generate an order ID. This ID can be anything unique
        final String orderId = "O" + (long) (Math.random() * 10000000000L);
        final String amount = ctx.amountField.getText().toString();
        final String cardNumber = ctx.cardNumberField.getText().toString();
        final String cardCvv = ctx.cardCvvField.getText().toString();
        final String cardAlias = String.valueOf(cardNumber.hashCode());

        String[] expiry = ctx.cardExpiryField.getText().toString().split("/");
        final String cardExpMonth = expiry[0];
        final String cardExpYear = expiry[1];

        /* We need to hit the Txn API on the backend. For that, we do that in an async task.
           The response will be used to send to the VIES SDK to initiate a PAY. */

        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                return Utils.createGpayTxnApi(ctx, orderId, amount, cardNumber, cardExpMonth, cardExpYear, cardCvv, cardAlias);
            }

            @Override
            protected void onPostExecute(JSONObject response) {
                try {
                    Log.d("MainActivity", response.toString(2));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctx.payOnGpaySdk(response);
            }
        }.execute();
    }

    private void payOnGpaySdk(JSONObject txnsResponse) {
        String amount = amountField.getText().toString();


        try {
            JSONObject payload = new JSONObject();

            payload.put("action", "GPAY_PAY");
            payload.put("juspay_txn_resp", txnsResponse);
            payload.put("service", "in.juspay.gpay");
            payload.put(PaymentConstants.AMOUNT, amount);
            payload.put(PaymentConstants.MERCHANT_ID, Preferences.merchantId);

            hyperServices.process("GPAY_PAY", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void payOnViesSdk(JSONObject txnsResponse) {
        String cardNumber = cardNumberField.getText().toString();
        String amount = amountField.getText().toString();

        Card card = Utils.getCard(cardNumber);

        try {
            JSONObject payload = new JSONObject();

            payload.put("action", "VIES_PAY");
            payload.put("card", card.toJSON());
            payload.put("service", "in.juspay.vies");
            payload.put("juspay_txn_resp", txnsResponse);
            payload.put("safetynet_api_key", Preferences.safetyNetApiKey);
            payload.put(PaymentConstants.AMOUNT, amount);
            payload.put(PaymentConstants.MERCHANT_ID, Preferences.merchantId);
            payload.put("merchant_root_view", String.valueOf(R.id.main_view));

            hyperServices.process("PAY", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void deEnrollOnViesSdk(final MainActivity ctx) {
        // De-enrolling requires the client_auth_token. Do an API call in an async task and pass the result to VIES SDK.
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                return Utils.getSessionApi(ctx);
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
                ctx.deEnrollOnViesSdk(sessionToken);
            }
        }.execute();
    }

    private void deEnrollOnViesSdk(String sessionToken) {
        String cardNumber = cardNumberField.getText().toString();
        Card card = Utils.getCard(cardNumber);

        try {
            JSONObject payload = new JSONObject();

            payload.put("action", "VIES_DISENROLL");
            payload.put("card", card.toJSON());
            payload.put("session_token", sessionToken);
            payload.put("service", "in.juspay.vies");

            hyperServices.process("DISENROLL", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onDeleteCardButtonClicked() {
        String cardNumber = cardNumberField.getText().toString();
        Card card = Utils.getCard(cardNumber);

        try {
            JSONObject payload = new JSONObject();

            payload.put("action", "VIES_DELETE_CARD");
            payload.put("card", card.toJSON());
            payload.put("service", "in.juspay.vies");

            hyperServices.process("DELETE_CARD", payload);
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
//            initiateViesSdk();
        }
    }
}
