package in.juspay.viesdemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.Toast;

//import in.juspay.godel.PaymentActivity;
import in.juspay.hypersdk.core.MerchantViewType;
import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallback;
import in.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter;
import in.juspay.hypersdk.ui.JuspayWebView;
import in.juspay.services.HyperServices;
//import in.juspay.services.PaymentServices;
import in.juspay.vies.VIESHelper;
import in.juspay.viesdemo.util.ViewBinder;
import in.juspay.viesdemo.util.ViewBinder.Binding;
import in.juspay.viesdemo.util.CardTextWatcher;
import in.juspay.viesdemo.util.Utils;
import in.juspay.vies.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    /* TODO :
        1. Add functionality of both the eligibilty in comment
        2. Add Proper Logs
     */

    private HyperServices paymentServices;

    @Binding private EditText amountField;
    @Binding private EditText cardNumberField;
    @Binding private EditText cardExpiryField;
    @Binding private EditText cardCvvField;

    @Binding private Toolbar toolbar;

    @Binding private Button initiateButton;
    @Binding private Button initiateAddCode;
    @Binding private Button prefetchButton;
    @Binding private Button prefetchAddCode;
    @Binding private Button eligibilityButton;
    @Binding private Button payButton;
    @Binding private Button deEnrollButton;
    @Binding private Button deleteCardButton;
    @Binding private Button deleteButtonAddCode;
    @Binding private Button deEnrollButtonAddCode;
    @Binding private Button payButtonAddCode;
    @Binding private Button eligibilityButtonAddCode;
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

//         Only call preFetch if there is going to be a gap of 2 seconds before actually initiating the vies SDK
//            prefetch();

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
                    getLocalEligibility();
                }
                else {
//                    initiateECSdk();
                    getEligibility();
                }
            }
        });

        eligibilityButtonAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEligibilityAddCode();
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initiateECSdk();
                payOnViesSdk(MainActivity.this);
            }
        });

        payButtonAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initiateECSdk();
                payOnViesSdkAddCode(MainActivity.this);
            }
        });

        prefetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefetch();
            }
        });

        prefetchAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_dialog, null);

                builder.setCancelable(false);
                builder.setView(dialogView);

                Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);
                Button btn_negative = (Button) dialogView.findViewById(R.id.dialog_negative_btn);
                final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);

                final AlertDialog dialog = builder.create();

                btn_positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        final String name = et_name.getText().toString();
                        name.replace("[;\\/:*?\"<>|&']"," ");

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            HyperServices.preFetch(MainActivity.this, jsonObject);
                            Toast.makeText(MainActivity.this, jsonObject.toString(2), Toast.LENGTH_SHORT).show();
                            Log.d("prefetchReq", jsonObject.toString(2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                btn_negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Toast.makeText(getApplication(),
                                "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
            }
        });

        initiateAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentServices = new HyperServices(MainActivity.this);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_dialog, null);

                builder.setCancelable(false);
                builder.setView(dialogView);

                Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);
                Button btn_negative = (Button) dialogView.findViewById(R.id.dialog_negative_btn);
                final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);

                final AlertDialog dialog = builder.create();

                btn_positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        final String name = et_name.getText().toString();
                        name.replace("[;\\/:*?\"<>|&']"," ");

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        paymentServices.initiate(jsonObject , new HyperPaymentsCallbackAdapter() {
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
                        Toast.makeText(getApplication(),
                                "Payload : \n" + name, Toast.LENGTH_SHORT).show();
                        Log.d("InitiateReq",name);
                    }
                });

                btn_negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Toast.makeText(getApplication(),
                                "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
            }
        });

        initiateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                paymentServices = new HyperServices(MainActivity.this);

                final JSONObject object = new JSONObject();
                try{
                    object.put("requestId",new Random().nextInt(10000)+"");
                    object.put(PaymentConstants.SERVICE,"in.juspay.ec");

                    JSONObject payload = new JSONObject();

                    payload.put("action","initiate");
                    payload.put("merchantId", Preferences.merchantId);
                    payload.put("clientId", Preferences.clientId);
                    payload.put("customerId", Preferences.getCustomerId(MainActivity.this));
                    payload.put(PaymentConstants.ENV,Preferences.environment);
                    payload.put(PaymentConstants.PACKAGE_NAME, Preferences.appId);
                    payload.put("safetynetApiKey", Preferences.safetyNetApiKey);
                    object.put(PaymentConstants.PAYLOAD,payload);

                    Log.d("initiateReq", object.toString(2));


                }catch (Exception e){
                    e.printStackTrace();
                }

                paymentServices.initiate(object, new HyperPaymentsCallbackAdapter() {
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

        deEnrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateECSdk();
                deEnrollOnViesSdk(MainActivity.this);
            }
        });

        deEnrollButtonAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateECSdk();
                deEnrollOnViesSdkAddCode(MainActivity.this);
            }
        });

        deleteCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initiateECSdk();
                onDeleteCardButtonClicked();
            }
        });

        deleteButtonAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initiateECSdk();
                onDeleteAddCodeClicked();
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
        paymentServices = new HyperServices(this);

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
            object.put("betaAssets", true);

        }catch (Exception e){
            e.printStackTrace();
        }


        paymentServices.initiate(object, new HyperPaymentsCallback() {
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
                    else if (action.equals("VIES_ELIGIBILITY")) {
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


    private void getEligibility() {
        String cardNumber = cardNumberField.getText().toString();
        String amount = amountField.getText().toString();

        Card card = Utils.getCard(cardNumber);

        try {
            JSONObject payload = new JSONObject();
            JSONObject object = new JSONObject();

            object.put("requestId", new Random().nextInt(10000)+"");
            object.put("service", "in.juspay.vies");

            payload.put("action", "VIES_ELIGIBILITY");
            payload.put("cards", new JSONArray(Collections.singletonList(card.toJSON())));
            payload.put("amount", amount);
            object.put("payload", payload);

            Log.d("EligibilityReq", object.toString(2));

            paymentServices.process(object);
//            Log.d("logging eligi", object.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getEligibilityAddCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        builder.setCancelable(false);
        builder.setView(dialogView);

        Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);
        Button btn_negative = (Button) dialogView.findViewById(R.id.dialog_negative_btn);
        final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);

        final AlertDialog dialog = builder.create();

        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                final String name = et_name.getText().toString();
                name.replace("[;\\/:*?\"<>|&']"," ");

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                paymentServices.process(jsonObject);
                try {
                    Toast.makeText(MainActivity.this, jsonObject.toString(2), Toast.LENGTH_SHORT).show();
                    Log.d("EligibilityCheck", jsonObject.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(getApplication(),
                        "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
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

    private void payOnViesSdk(JSONObject txnsResponse) {
        String cardNumber = cardNumberField.getText().toString();
        String amount = amountField.getText().toString();

        Card card = Utils.getCard(cardNumber);

        try {
            JSONObject payload = new JSONObject();
            JSONObject object = new JSONObject();

            object.put("requestId",new Random().nextInt(10000)+"" );
            object.put("service","in.juspay.vies" );

            payload.put("action", "VIES_PAY");
            payload.put("card", card.toJSON());
            payload.put("juspay_txn_resp", txnsResponse);
//            payload.put("safetyNetApiKey", Preferences.safetyNetApiKey);
            payload.put(PaymentConstants.AMOUNT, amount);
            object.put("payload", payload);
            payload.put(PaymentConstants.MERCHANT_ID, Preferences.merchantId);
            payload.put("merchant_root_view", String.valueOf(R.id.main_view));
            paymentServices.process(object);
            Log.d("payReq", object.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static void payOnViesSdkAddCode(final MainActivity ctx) {

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
                ctx.payOnViesSdkAddCode(response);
            }
        }.execute();
    }

    private void payOnViesSdkAddCode(final JSONObject txnsResponse) {

        String cardNumber = cardNumberField.getText().toString();
        String amount = amountField.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        builder.setCancelable(false);
        builder.setView(dialogView);

        Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);
        Button btn_negative = (Button) dialogView.findViewById(R.id.dialog_negative_btn);
        final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);

        final AlertDialog dialog = builder.create();

        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                final String name = et_name.getText().toString();
                name.replace("[;\\/:*?\"<>|&']"," ");

                JSONObject jsonObject = null;
                try {
                    JSONObject payload = new JSONObject();
                    jsonObject = new JSONObject(name);
                    payload = jsonObject.getJSONObject("payload");
                    payload.put("juspay_txn_resp",txnsResponse);
                    jsonObject.put("payload",payload);
                    Log.d("JuspayTxnResponse", jsonObject.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                paymentServices.process(jsonObject);
                try {
                    Toast.makeText(MainActivity.this, jsonObject.toString(2), Toast.LENGTH_SHORT).show();
                    Log.d("PayReq", jsonObject.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(getApplication(),
                        "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
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
            JSONObject object = new JSONObject();

            object.put("requestId", new Random().nextInt(10000)+"");
            object.put("service", "in.juspay.vies");
            payload.put("action", "VIES_DISENROLL");
            payload.put("card", card.toJSON());
            payload.put("session_token", sessionToken);
            object.put("payload", payload);
            Log.d("DeenrollReq", object.toString(2));

            paymentServices.process(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static void deEnrollOnViesSdkAddCode(final MainActivity ctx) {
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
                ctx.deEnrollOnViesSdkAddCode(sessionToken);
            }
        }.execute();
    }

    private void deEnrollOnViesSdkAddCode(final String sessionToken) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        builder.setCancelable(false);
        builder.setView(dialogView);

        Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);
        Button btn_negative = (Button) dialogView.findViewById(R.id.dialog_negative_btn);
        final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);

        final AlertDialog dialog = builder.create();

        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                final String name = et_name.getText().toString();
                name.replace("[;\\/:*?\"<>|&']"," ");

                JSONObject jsonObject = null;
                try {
                    JSONObject payload = new JSONObject();
                    jsonObject = new JSONObject(name);
                    payload = jsonObject.getJSONObject("payload");
                    payload.put("session_token",sessionToken);
                    jsonObject.put("payload",payload);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                paymentServices.process(jsonObject);
                try {
                    Toast.makeText(MainActivity.this, jsonObject.toString(2), Toast.LENGTH_SHORT).show();
                    Log.d("DeEnrollReq", jsonObject.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(getApplication(),
                        "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void onDeleteCardButtonClicked() {
        String cardNumber = cardNumberField.getText().toString();
        Card card = Utils.getCard(cardNumber);

        try {
            JSONObject payload = new JSONObject();
            JSONObject object = new JSONObject();

            object.put("requestId", new Random().nextInt(10000)+"" );
            object.put("service", "in.juspay.vies");
            payload.put("action", "VIES_DELETE_CARD");
            payload.put("card", card.toJSON());
            object.put("payload",payload);
            Log.d("DeleteReq", object.toString(2));

            paymentServices.process(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void onDeleteAddCodeClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        builder.setCancelable(false);
        builder.setView(dialogView);

        Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);
        Button btn_negative = (Button) dialogView.findViewById(R.id.dialog_negative_btn);
        final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);

        final AlertDialog dialog = builder.create();

        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                final String name = et_name.getText().toString();
                name.replace("[;\\/:*?\"<>|&']"," ");

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                paymentServices.process(jsonObject);
                try {
                    Toast.makeText(MainActivity.this, jsonObject.toString(2), Toast.LENGTH_SHORT).show();
                    Log.d("DeleteReq", jsonObject.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(getApplication(),
                        "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void getLocalEligibility() {

        String cardNumber = cardNumberField.getText().toString();
        Card card = Utils.getCard(cardNumber);
        String amount = amountField.getText().toString();

        try {
            JSONObject payload = new JSONObject();

            payload.put("customer_id", Preferences.getCustomerId(this));
            payload.put("action", "VIES_ELIGIBILITY");
            payload.put("amount", amount);
            payload.put("cards", new JSONArray(Collections.singletonList(card.toJSON())));
            payload.put("environment", Preferences.environment);
            payload.put("request_id", "dummy_request_id1");

            String response = VIESHelper.getLocalEligibility(this, payload);

            JSONObject res = new JSONObject(response);
            JSONObject response_payload = res.getJSONObject(PaymentConstants.PAYLOAD);

            Log.d("MainActivity", String.format("event: %s, payload: %s", "ELIGIBILITY", response_payload.toString()));

            // Displaying result on Response View
            Intent intent = new Intent(MainActivity.this, ResponseViewActivity.class);
            intent.putExtra("response", response_payload.toString(2));
            startActivityForResult(intent, 1);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        paymentServices.terminate();
    }

    @Override
    public void onBackPressed() {


        if(!paymentServices.onBackPressed()) {
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
                paymentServices.terminate();
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
