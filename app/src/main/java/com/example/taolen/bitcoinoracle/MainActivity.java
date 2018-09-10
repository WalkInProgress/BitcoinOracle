package com.example.taolen.bitcoinoracle;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String BPI_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private ProgressDialog progressDialog;
    private TextView displayText,tvDescription;
    private Button btnLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayText = findViewById(R.id.txt);
        tvDescription = findViewById(R.id.tvDescription);
        btnLoad = findViewById(R.id.btnLoad);
        tvDescription.setText(R.string.info);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("BPI Loading");
        progressDialog.setMessage("wait ...");
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection()){
                    loadPrice();
                }
            }
        });
        //make button press itself
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnLoad.performClick();
            }
        }, 5000);
    }
    private boolean checkInternetConnection(){
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();

        //if not info is received
        if (networkInfo==null){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getResources().getString(R.string.app_name))
                    .setMessage("No Internet Connection")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            return false;
        }else {
            return true;
        }
    }
    private void loadPrice(){
        final Request request = new Request.Builder().url(BPI_ENDPOINT).build();
        progressDialog.show();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "Error Loading BPI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            try{
                    final String body = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            try {
                                parseBPIResponse(body);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    } catch (NullPointerException e){
                     e.printStackTrace();
                }
            }
        });
    }
    private void parseBPIResponse(String body) throws JSONException {
        StringBuilder builder = new StringBuilder();
        try {
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(body);
            JSONObject timeObject = jsonObject.getJSONObject("time");
            builder.append(timeObject.getString("updated")).append("\n\n");

            JSONObject BpiObject = jsonObject.getJSONObject("bpi");
            JSONObject usdObject = BpiObject.getJSONObject("USD");
            builder.append(usdObject.getString("rate")).append(" $").append("\n");

            JSONObject gbpObject = BpiObject.getJSONObject("GBP");
            builder.append(gbpObject.getString("rate")).append(" £").append("\n");

            JSONObject euroOBject = BpiObject.getJSONObject("EUR");
            builder.append(euroOBject.getString("rate")).append(" €").append("\n");

            displayText.setText(builder.toString());
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}
