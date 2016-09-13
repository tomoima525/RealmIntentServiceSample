package com.tomoima.realmintentservicesample.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by tomoaki on 9/12/16.
 */
public class PersonSerializer implements JsonSerializer<Person> {

    public PersonSerializer() {
    }

    @Override
    public JsonElement serialize(Person src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("age", src.getAge());
        return jsonObject;
    }
}
