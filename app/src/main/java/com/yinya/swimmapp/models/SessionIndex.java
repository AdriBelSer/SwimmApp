package com.yinya.swimmapp.models;

public class SessionIndex {
    private String id;
    private String name;

    public SessionIndex() {
    }

    public SessionIndex(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}