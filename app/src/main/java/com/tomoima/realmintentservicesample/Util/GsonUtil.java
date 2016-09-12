package com.tomoima.realmintentservicesample.Util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tomoima.realmintentservicesample.model.Person;
import com.tomoima.realmintentservicesample.model.PersonSerializer;

import java.util.ArrayList;

import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by tomoaki on 9/12/16.
 */
public class GsonUtil {
    public static String parseTweetResponse(RealmResults<Person> realmResults) throws ClassNotFoundException {
        final Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(Class.forName("io.realm.PersonRealmProxy"), new PersonSerializer())
                .create();

        return gson.toJson(realmResults);
    }

    public static ArrayList<Person> fromJsonToArray(String jsonString){
        Gson gson = new Gson();
        return gson.fromJson(jsonString, new TypeToken<ArrayList<ArrayList<Person>>>() {}.getType());
    }
}
