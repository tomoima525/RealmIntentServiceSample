package com.tomoima.realmintentservicesample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.tomoima.realmintentservicesample.Const.Const;
import com.tomoima.realmintentservicesample.task.TestIntentService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SIGNUP = 999;
    private GoogleApiClient client;
    DriveTaskCompleteReceiver driveTaskCompleteReceiver;

    /**
     * Life cycles
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        startUpload();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> {
                    try {
                        if (connectionResult.hasResolution()) {
                            connectionResult.startResolutionForResult(this, REQUEST_SIGNUP);
                        }
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                })
                .build();

        driveTaskCompleteReceiver = new DriveTaskCompleteReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                driveTaskCompleteReceiver,
                new IntentFilter(Const.BROADCAST_ACTION));
    }


    @Override
    protected void onDestroy() {
        if (client != null) {
            client.disconnect();
        }
        if(driveTaskCompleteReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(driveTaskCompleteReceiver);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        switch (requestCode){
            case REQUEST_SIGNUP:
                startUpload();
                break;
        }
    }

    /**
     * Actions
     */

    public void startIntentService(View v) {
        connectClientAndStart();
        Log.d("Intent", "clicked ");
    }

    /**
     * Google Drive API should be connected on Activity since IntentService does not have onActivityResult
     * receive the callback from Google Drive API
     *
     */
    private void connectClientAndStart(){
        // If the client is not connected, a callback returns to OnConnectionFailedListener.
        // connectionResult.startResolutionForResult(this, REQUEST_SIGNUP);
        client.connect();
    }

    private void startUpload(){
        Intent intent = TestIntentService.createIntent(this);
        startService(intent);
    }

    private void updateView(String message){
        TextView.class.cast(findViewById(R.id.update_text)).setText(message);
    }

    /**
     * BroadCastReceiver
     */

    private class DriveTaskCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            TestIntentService.GoogleDriveResult googleDriveResult
                    =(TestIntentService.GoogleDriveResult) intent.getSerializableExtra(Const.EXTENDED_DATA_STATUS);
            Toast.makeText(context, googleDriveResult.message, Toast.LENGTH_LONG ).show();
            updateView(googleDriveResult.message);
        }
    }

}
