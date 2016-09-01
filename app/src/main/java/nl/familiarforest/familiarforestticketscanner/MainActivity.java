package nl.familiarforest.familiarforestticketscanner;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
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

    TextView firstnameField,lastnameField,taskField,transactionField,codeField,errorField,birthdayField;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        firstnameField = (TextView)findViewById(R.id.firstnameField);
        lastnameField = (TextView)findViewById(R.id.lastnameField);
        birthdayField = (TextView)findViewById(R.id.birthdayField);
        taskField = (TextView)findViewById(R.id.taskField);
        transactionField = (TextView)findViewById(R.id.transactionField);
        codeField = (TextView)findViewById(R.id.codeField);
        errorField = (TextView)findViewById(R.id.errorField);
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
                toolbar.setBackground(new ColorDrawable(Color.RED));
                firstnameField.setText("");
                lastnameField.setText("");
                birthdayField.setText("");
                taskField.setText("");
                transactionField.setText("");
                codeField.setText("");
                errorField.setText(String.format("Error: %s", obj.getString("message")));
            } else if ( status.equals("WARN") || status.equals("OK")){
                if( status.equals("WARN") ) {
                    toolbar.setBackground(new ColorDrawable(Color.YELLOW));
                } else if( status.equals("OK") ) {
                    toolbar.setBackground(new ColorDrawable(Color.BLUE));
                }
                firstnameField.setText(obj.getString("firstname"));
                lastnameField.setText(obj.getString("lastname"));
                birthdayField.setText(obj.getString("birthdate"));
                taskField.setText(obj.getString("task"));
                transactionField.setText(obj.getString("id"));
                codeField.setText(obj.getString("code"));
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
                String url ="http://stichtingfamiliarforest.nl/verifyticket.php";

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
}
