package in.juspay.viesdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import in.juspay.viesdemo.util.ViewBinder;
import in.juspay.viesdemo.util.ViewBinder.Binding;

public class ResponseViewActivity extends AppCompatActivity {

    @Binding("responseMessage") private TextView textView;
    @Binding private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_view);

        ViewBinder.bindAll(this);
            textView.setText(getIntent().getStringExtra("response"));
            String view = getIntent().getStringExtra("response");
        Log.d("responsePay",view );
        setSupportActionBar(toolbar);
    }
}
