package com.losak.chirpcoin;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coinbase.android.sdk.OAuth;
import com.coinbase.api.exception.CoinbaseException;

import org.json.JSONException;
import org.json.JSONObject;

import io.chirp.sdk.CallbackCreate;
import io.chirp.sdk.CallbackRead;
import io.chirp.sdk.ChirpSDK;
import io.chirp.sdk.ChirpSDKListener;
import io.chirp.sdk.model.Chirp;
import io.chirp.sdk.model.ChirpError;
import io.chirp.sdk.model.ShortCode;

public class MainActivity extends AppCompatActivity {
    private ChirpSDK chirpSDK;
    private TextView textView1;

    String API_KEY = "dvd019NCpaUCWn8TeIH96lX6B";
    String API_SECRET = "rPUMGtsPsn6ucH0qnLB0Bh33u3pVi3Xj36zWYxZzeo99lIsSXB";
    String CLIENT_ID = "5b86f05a70970ac9932055ae65b4cfdd63c25b6a52a53d67a31420153378374a";
    String REDIRECT_URI = "chirpcoin://coinbase-oauth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        textView1 = (TextView) findViewById(R.id.textViewListening);
        Button button = (Button)findViewById(R.id.buttonLogin);
        final EditText editText = (EditText)findViewById(R.id.editTextAmount);

        setSupportActionBar(toolbar);

        //Coinbase
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    OAuth.beginAuthorization(MainActivity.this, CLIENT_ID, "user", REDIRECT_URI, null);
                    textView1.setText("Success");
                } catch (CoinbaseException e) {
                    e.getStackTrace();
                }
            }
        });

        //ChirpCoin
        chirpSDK = new ChirpSDK(this, API_KEY, API_SECRET);
        chirpSDK.setListener(new ChirpSDKListener() {

            @Override
            public void onChirpHeard(ShortCode shortCode) {
                chirpSDK.read(shortCode, new CallbackRead() {
                    @Override
                    public void onReadResponse(final Chirp chirp) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView1.setText(chirp.getJsonData().toString());
                            }
                        });
                        Log.e("onReadResponse", chirp.getJsonData().toString());
                    }

                    @Override
                    public void onReadError(ChirpError chirpError) {
                        Log.e("onReadError", chirpError.getMessage());
                    }
                });
            }

            @Override
            public void onChirpError(ChirpError chirpError) {
                Log.e("onChirpError", chirpError.getMessage());
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("name", "Martin Losak");
                    jsonObj.put("address", "1CrgTPwJ57ZNTE9XU51q2ErxUUt6JYwcuu");
                    jsonObj.put("amount", editText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Chirp chirp = new Chirp(jsonObj);
                chirpSDK.create(chirp, new CallbackCreate() {

                    @Override
                    public void onCreateResponse(ShortCode shortCode) {
                        chirpSDK.play(shortCode);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Chirp sent", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    @Override
                    public void onCreateError(ChirpError chirpError) {
                        Toast.makeText(MainActivity.this, "" + chirpError.getCode(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chirpSDK.startListening();
        Log.e("onResume", "startListening");
    }

    @Override
    protected void onPause() {
        super.onPause();
        chirpSDK.stopListening();
        Log.e("onPause", "stopListening");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
