package com.tomoima.realmintentservicesample.task;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tomoima.realmintentservicesample.model.Person;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Created by tomoaki on 8/3/16.
 */
public class TestIntentService extends IntentService {
    private Realm realm;

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context, TestIntentService.class);
        return intent;
    }

    public TestIntentService() {
        super("TEST");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        realm = Realm.getDefaultInstance();
        Log.d("Intent","¥created");
        Observable<RealmResults<Person>> resultsObservable = realm.where(Person.class).findAllAsync().asObservable();
        resultsObservable.filter(responses -> responses.isLoaded())
                .subscribe(cnt -> {
                    Log.d("Intent","¥cnt " + cnt.size());
                    realm.close();
                });
    }
}
