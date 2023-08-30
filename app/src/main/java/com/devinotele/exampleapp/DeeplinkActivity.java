package com.devinotele.exampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class DeeplinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        Button back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> onBackPressed());

        if (savedInstanceState == null) {
            if (getIntent().getData() != null) {
                Log.d(getString(R.string.tag), "Deeplink = " + getIntent().getData().toString());
                if (getIntent().getData().toString().equals("devino://first/promo")) {
                    Log.d(getString(R.string.tag), "Deeplink captured ");
                    //TODO Make navigation to the desired screen if need
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(isTaskRoot()){
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}