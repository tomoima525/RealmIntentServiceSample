package com.tomoima.realmintentservicesample.task;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.MetadataChangeSet;
import com.tomoima.realmintentservicesample.Const.Const;
import com.tomoima.realmintentservicesample.Util.GsonUtil;
import com.tomoima.realmintentservicesample.model.Person;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by tomoaki on 8/3/16.
 */
public class TestIntentService extends IntentService {
    private Realm realm;
    protected static final String LOAD_ERROR = "load_error";
    protected GoogleApiClient client;

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context, TestIntentService.class);
        return intent;
    }

    public TestIntentService() {
        super("TEST");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        client.blockingConnect(30, TimeUnit.SECONDS);
        //Retrieve all data from RealmDB
        realm = Realm.getDefaultInstance();

        RealmResults<Person> responses = realm.where(Person.class).findAll();
        String parsedResponse = null;
        try {
            parsedResponse = GsonUtil.parseTweetResponse(responses);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(TextUtils.isEmpty(parsedResponse)){
            parsedResponse = LOAD_ERROR;
        }

        GoogleDriveResult result = uploadToDrive(parsedResponse);
        long count = realm.where(Person.class).count();
        finishProcess(result, count);
        realm.close();
        // Observable can not be used in onHandleIntent since RealmChangeListener is used internally and
        // this will return Illegal state exception when used in a worker thread.
//        Observable<RealmResults<Person>> resultsObservable = realm.where(Person.class).findAll().asObservable();
//        resultsObservable.filter(responses -> responses.isLoaded())
//                .subscribe(cnt -> {
//                    Log.d("Intent","¥cnt " + cnt.size());
//                    realm.close();
//                });
    }

    private GoogleDriveResult uploadToDrive(String json){
        final CountDownLatch latch = new CountDownLatch(1);
        GoogleDriveResult googleDriveResult = new GoogleDriveResult();

        if(LOAD_ERROR.equals(json)) {
            googleDriveResult.status = GoogleDriveResult.ERROR;
            googleDriveResult.message = "Load Error";
            return googleDriveResult;
        }
        Drive.DriveApi.newDriveContents(client).setResultCallback(contentsResult ->{
            if(!contentsResult.getStatus().isSuccess()){
                googleDriveResult.status = GoogleDriveResult.ERROR;
                googleDriveResult.message = "Api Error";
            }
            OutputStream outputStream = contentsResult.getDriveContents().getOutputStream();

            try {
                outputStream.write(json.getBytes());
            } catch (IOException e) {
                googleDriveResult.status = GoogleDriveResult.ERROR;
                googleDriveResult.message = "Write Error";
            }

            MetadataChangeSet.Builder builder = new MetadataChangeSet.Builder();
            builder.setMimeType("application/json");
            String name = "test.json";
            builder.setTitle(name);
            MetadataChangeSet metadataChangeSet = builder.build();
            IntentSender intentSender = Drive.DriveApi
                    .newCreateFileActivityBuilder()
                    .setInitialMetadata(metadataChangeSet)
                    .setInitialDriveContents(contentsResult.getDriveContents())
                    .build(client);
            try {
                startIntentSender(intentSender, null, 1, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                googleDriveResult.status = GoogleDriveResult.ERROR;
                googleDriveResult.message = "Upload Error";
            }
            latch.countDown();
        });

        try {
            //Wait til the async task in Google Drive API ends
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(googleDriveResult.status == 0 ) {
            googleDriveResult.status = GoogleDriveResult.SUCCESS;
            googleDriveResult.size = json.getBytes().length/1024f;
            googleDriveResult.message = "Drive upload success" + googleDriveResult.size;

        }
        return googleDriveResult;
    }

    protected void finishProcess(GoogleDriveResult googleDriveResult, long count){

        //Add tweet counts
        googleDriveResult.message += count;

        /**
         *  Creates a new Intent containing a Uri object
         *  BROADCAST_ACTION is a custom Intent action
         **/
        Intent localIntent = new Intent(Const.BROADCAST_ACTION)
                .putExtra(Const.EXTENDED_DATA_STATUS, googleDriveResult);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public static class GoogleDriveResult implements Serializable {


        static final int SUCCESS = 1;
        static final int ERROR = -1;

        public int status;
        public String message;
        public float size;

        public GoogleDriveResult(){
        }
    }


}
