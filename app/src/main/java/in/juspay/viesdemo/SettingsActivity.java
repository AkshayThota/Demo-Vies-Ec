package in.juspay.viesdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.viesdemo.util.ViewBinder;
import in.juspay.viesdemo.util.ViewBinder.Binding;
//import in.juspay.godel.core.PaymentConstants;

public class SettingsActivity extends AppCompatActivity {

    @Binding private EditText merchantIdField;
    @Binding private EditText testModeField;
    @Binding private EditText safetyNetApiKeyField;
    @Binding private EditText juspayApiKeyField;
    @Binding private EditText appIdField;
    @Binding private EditText gwRefIdField;

    @Binding private Spinner environment;
    @Binding private Spinner localEligibility;
    @Binding private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ViewBinder.bindAll(this);

        initUI();
    }

    private void initUI() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, "Settings not saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        resetFields();
    }

    private void resetFields() {
        merchantIdField.setText(Preferences.merchantId);
        testModeField.setText(Preferences.test_mode);
        safetyNetApiKeyField.setText(Preferences.safetyNetApiKey);
        juspayApiKeyField.setText(Preferences.juspayApiKey);
        appIdField.setText(Preferences.appId);
        gwRefIdField.setText(Preferences.gwRefId);

        if(Preferences.useLocalEligibility.equalsIgnoreCase("true")) {
            localEligibility.setSelection(0);
        }
        else {
            localEligibility.setSelection(1);
        }

        if (PaymentConstants.ENVIRONMENT.PRODUCTION.equals(Preferences.environment)) {
            environment.setSelection(1);
        } else if (PaymentConstants.ENVIRONMENT.SANDBOX.equals(Preferences.environment)) {
            environment.setSelection(0);
        } else {
            environment.setSelection(0);
            Preferences.environment = PaymentConstants.ENVIRONMENT.SANDBOX;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.saveIcon) {

            int environmentSelected = environment.getSelectedItemPosition();
            int eligibilitySelected = localEligibility.getSelectedItemPosition();

            Preferences.merchantId = merchantIdField.getText().toString();
            Preferences.test_mode = testModeField.getText().toString();
            Preferences.clientId = Preferences.merchantId + "_android";
            Preferences.safetyNetApiKey = safetyNetApiKeyField.getText().toString();
            Preferences.juspayApiKey = juspayApiKeyField.getText().toString();
            Preferences.appId = appIdField.getText().toString();
            Preferences.gwRefId = gwRefIdField.getText().toString();

            if (environmentSelected == 0) {
                Preferences.environment = PaymentConstants.ENVIRONMENT.SANDBOX;
            } else if (environmentSelected == 1) {
                Preferences.environment = PaymentConstants.ENVIRONMENT.PRODUCTION;
            }

            if(eligibilitySelected == 0) {
                Preferences.useLocalEligibility = "true";
            }
            else {
                Preferences.useLocalEligibility = "false";
            }

            Preferences.writePrefs(this);

            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();

            Intent data = new Intent();
            data.putExtra("SAVED","TRUE");
            setResult(RESULT_OK, data);
            finish();

        } else if (item.getItemId() == R.id.refreshIcon) {
            Preferences.readPrefs(this);
            resetFields();
            Toast.makeText(this, "Settings restored!", Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}
