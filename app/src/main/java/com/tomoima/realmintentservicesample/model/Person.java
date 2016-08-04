package com.tomoima.realmintentservicesample.model;

import io.realm.RealmObject;

/**
 * Created by tomoaki on 8/3/16.
 */
public class Person extends RealmObject {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


}
