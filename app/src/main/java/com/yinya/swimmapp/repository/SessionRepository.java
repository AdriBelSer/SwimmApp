package com.yinya.swimmapp.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.yinya.swimmapp.models.Session;
import com.yinya.swimmapp.models.SessionIndex;
import com.yinya.swimmapp.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionRepository {
    private static SessionRepository instance;
    private final FirebaseFirestore db;
    private List<String> cachedCompletedIds;
    private Map<String, Object> cachedSessionIndex;

    public static SessionRepository getInstance() {
        if (instance == null) {
            instance = new SessionRepository();
        }
        return instance;
    }

    private SessionRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnSessionsLoadedListener {
        void onSuccess(List<SessionIndex> sessionIndexList);
        void onError(Exception e);
    }

    public interface OnSessionUpdatedListener {
        void onSuccess();
        void onError(Exception e);
    }

    public interface OnSessionDetailLoadedListener {
        void onSuccess(Session session);
        void onError(Exception e);
    }

    private int extractSessionNumber(String sessionId) {
        if (sessionId == null) {
            return Integer.MAX_VALUE;
        }
        String[] partes = sessionId.trim().split("\\D+");
        if (partes.length >= 2) {
            try {
                return Integer.parseInt(partes[1]);
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        }
        return Integer.MAX_VALUE;
    }

    private List<SessionIndex> filterSessions(boolean showCompleted) {
        List<SessionIndex> sessionList = new ArrayList<>();

        if (cachedSessionIndex != null) {
            for (Map.Entry<String, Object> campo : cachedSessionIndex.entrySet()) {
                String sessionId = campo.getKey();
                String sessionName = String.valueOf(campo.getValue());

                SessionIndex item = new SessionIndex(sessionId, sessionName);
                boolean isCompleted = cachedCompletedIds.contains(item.getId());

                if (showCompleted == isCompleted) {
                    sessionList.add(item);
                }
            }
        }

        // Ordenamos la lista por el número extraído del ID (1, 2, 3... 10)
        sessionList.sort((s1, s2) -> {
            int num1 = extractSessionNumber(s1.getId());
            int num2 = extractSessionNumber(s2.getId());
            return Integer.compare(num1, num2);
        });

        return sessionList;
    }

    public void getSessions(String uid, boolean showCompleted, OnSessionsLoadedListener listener) {
        if (cachedCompletedIds != null && cachedSessionIndex != null) {
            if (listener != null) {
                listener.onSuccess(filterSessions(showCompleted));
            }
            return;
        }

        Task<DocumentSnapshot> userTask = db.collection("users").document(uid).get();
        Task<DocumentSnapshot> indexTask = db.collection("system").document("session_index").get();

        Tasks.whenAllSuccess(userTask, indexTask)
                .addOnSuccessListener(resultados -> {
                    DocumentSnapshot userDoc = (DocumentSnapshot) resultados.get(0);
                    DocumentSnapshot indexDoc = (DocumentSnapshot) resultados.get(1);

                    User user = userDoc.toObject(User.class);
                    List<String> completedIds = new ArrayList<>();
                    if (user != null && user.getCompletedSessions() != null) {
                        completedIds = new ArrayList<>(user.getCompletedSessions());
                    }

                    this.cachedCompletedIds = completedIds;
                    this.cachedSessionIndex = indexDoc.getData() != null ? indexDoc.getData() : new HashMap<>();

                    if (listener != null) {
                        listener.onSuccess(filterSessions(showCompleted));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
    }

    public void markSessionAsCompleted(String uid, String sessionId, OnSessionUpdatedListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("completed_sessions", FieldValue.arrayUnion(sessionId));

        db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    if (cachedCompletedIds != null && !cachedCompletedIds.contains(sessionId)) {
                        cachedCompletedIds.add(sessionId);
                    }
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
    }

    public void getSessionById(String sessionId, OnSessionDetailLoadedListener listener) {
        db.collection("sessions").document(sessionId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Session session = documentSnapshot.toObject(Session.class);
                    if (session != null) {
                        if (listener != null) {
                            listener.onSuccess(session);
                        }
                    } else {
                        if (listener != null) {
                            listener.onError(new Exception("Sesión no encontrada"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
    }
}