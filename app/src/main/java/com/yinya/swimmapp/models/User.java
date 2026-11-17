package com.yinya.swimmapp.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class User {

    @DocumentId
    private String uid;
    @PropertyName("completed_sessions")
    private List<String> completedSessions; // Lista de IDs de las sesiones realizadas

    public User() {
    }

    public String getUid() {
        return uid;
    }
    @PropertyName("completed_sessions")
    public List<String> getCompletedSessions() {
        return completedSessions;
    }
}