package com.tomoima.realmintentservicesample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.tomoima.realmintentservicesample.task.TestIntentService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startIntentService(View v) {
        Intent intent = TestIntentService.createIntent(this);
        startService(intent);
        Log.d("Intent","clicked ");
    }

}
