package nl.familiarforest.familiarforestticketscanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView firstnameField,lastnameField,taskField,taskTimeField,transactionField,codeField,errorField,birthdayField;
    Toolbar toolbar;
    Window window;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        url = sharedPref.getString(SettingsActivity.PREF_URL, "");


        window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        firstnameField = (TextView)findViewById(R.id.firstnameField);
        lastnameField = (TextView)findViewById(R.id.lastnameField);
        birthdayField = (TextView)findViewById(R.id.birthdayField);
        taskField = (TextView)findViewById(R.id.taskField);
        taskTimeField = (TextView)findViewById(R.id.taskTimeField);
        transactionField = (TextView)findViewById(R.id.transactionField);
        codeField = (TextView)findViewById(R.id.codeField);
        errorField = (TextView)findViewById(R.id.errorField);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void scanTicket(View v) {
        Intent intent = new Intent(
                "com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    public void parseResult(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            String status = obj.getString("status");
            errorField.setText(status);
            if( status.equals("ERR") ){
                toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorRoyalRed)));
                window.setStatusBarColor(getResources().getColor(R.color.colorRoyalRed));
                firstnameField.setText("");
                lastnameField.setText("");
                birthdayField.setText("");
                taskField.setText("");
                taskTimeField.setText("");
                transactionField.setText("");
                codeField.setText("");
                errorField.setText(String.format("Error: %s", obj.getString("message")));
            } else if ( status.equals("WARN") || status.equals("OK")){
                if( status.equals("WARN") ) {
                    toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorBananaYellow)));
                    window.setStatusBarColor(getResources().getColor(R.color.colorBananaYellow));
                } else if( status.equals("OK") ) {
                    toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                }
                if( obj.has("firstname"))
                    firstnameField.setText(obj.getString("firstname"));
                if( obj.has("lastname"))
                    lastnameField.setText(obj.getString("lastname"));
                if( obj.has("birthdate"))
                    birthdayField.setText(obj.getString("birthdate"));
                if( obj.has("task"))
                    taskField.setText(obj.getString("task"));
                if( obj.has("startdate") && obj.has("enddate")) {
                    String dateString = obj.getString("startdate") + " - " + obj.getString("enddate");
                    taskTimeField.setText(dateString);
                }
                if( obj.has("transactionid"))
                    transactionField.setText(obj.getString("transactionid"));
                if( obj.has("code"))
                    codeField.setText(obj.getString("code"));
                if( obj.has("message"))
                    errorField.setText(obj.getString("message"));
            }
        } catch( JSONException e ) {
            errorField.setText(e.toString());
        }
    }

    public void setError(String error) {
        errorField.setText(error);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                final String contents = intent.getStringExtra("SCAN_RESULT"); // This will contain your scan result

                // Instantiate the RequestQueue.
                final RequestQueue queue = Volley.newRequestQueue(this);

                // Request a string response from the provided URL.
                final StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                parseResult(response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                setError(error.toString());
                            }
                        }){
                            @Override
                            protected Map<String,String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("ticket",contents);
                                return params;
                            }};
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        url = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.PREF_URL, "");
    }
}
