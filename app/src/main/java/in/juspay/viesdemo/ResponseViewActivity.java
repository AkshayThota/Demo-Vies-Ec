package in.juspay.viesdemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;
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
        setSupportActionBar(toolbar);
    }
}
