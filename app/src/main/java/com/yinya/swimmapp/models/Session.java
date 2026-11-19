package com.yinya.swimmapp.models;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class Session {
    @DocumentId
    private String id;
    private String name;
    private int meters;
    private List<String> exercises;

    public Session() {
    }

    public Session(String name, int meters, List<String> exercises) {
        this.name = name;
        this.meters = meters;
        this.exercises = exercises;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMeters() {
        return meters;
    }


    public List<String> getExercises() {
        return exercises;
    }
}
