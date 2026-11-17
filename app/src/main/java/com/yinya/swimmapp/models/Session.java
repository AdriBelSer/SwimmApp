package com.yinya.swimmapp.models;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

public class Session {
    @DocumentId
    private String id;
    private String name;
    private int meters;
    private List<SessionExercise> exercises;

    public Session() {
    }

    public Session(String name, int meters, List<SessionExercise> exercises) {
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

    public List<SessionExercise> getExercises() {
        return exercises;
    }

    // Representa cada elemento dentro del array 'exercises' de una sesión
    public static class SessionExercise {
        private String id; // Referencia al id del ejercicio en la colección 'system'
        private int meters;
        private int repetitions;

        public SessionExercise() {}

        public String getId() { return id; }

        public int getMeters() { return meters; }

        public int getRepetitions() { return repetitions; }
    }

}
