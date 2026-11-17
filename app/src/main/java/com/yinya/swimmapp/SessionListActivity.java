package com.yinya.swimmapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yinya.swimmapp.models.SessionIndex;
import com.yinya.swimmapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class SessionListActivity extends ComponentActivity {

    private com.facebook.shimmer.ShimmerFrameLayout shimmerLayout;
    private WearableRecyclerView recyclerView;
    private boolean showCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);
        showCompleted = getIntent().getBooleanExtra("MOSTRAR_COMPLETADAS", false);
        Log.d("SWIMMAPP", "Cargando lista. ¿Mostrar completadas?: " + showCompleted);

        shimmerLayout = findViewById(R.id.shimmer_lista);
        recyclerView = findViewById(R.id.recycler_sesiones);

        shimmerLayout.startShimmer();
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        loadSessions();


    }

    private void loadSessions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    User user = userDoc.toObject(User.class);
                    List<String> completedIds = new ArrayList<>();

                    if (user != null && user.getCompletedSessions() != null) {
                        completedIds = user.getCompletedSessions();
                    }
                    final List<String> finalListCompleted = completedIds;

                    db.collection("system").document("session_index").get()
                            .addOnSuccessListener(indexDoc -> {
                                List<SessionIndex> sessionList = new ArrayList<>();

                                java.util.Map<String, Object> mapaSesiones = indexDoc.getData();

                                if (mapaSesiones != null) {
                                    for (java.util.Map.Entry<String, Object> campo : mapaSesiones.entrySet()) {
                                        String sessionId = campo.getKey();
                                        String sessionName = String.valueOf(campo.getValue());

                                        SessionIndex item = new SessionIndex(sessionId, sessionName);

                                        boolean isCompleted = finalListCompleted.contains(item.getId());

                                        if (showCompleted == isCompleted) {
                                            sessionList.add(item);
                                        }
                                    }
                                }

                                shimmerLayout.stopShimmer();
                                shimmerLayout.setVisibility(View.GONE);
                                SessionAdapter adapter = new SessionAdapter(sessionList, session -> {
                                    Log.d("SWIMMAPP", "Clicked session ID: " + session.getId());
                                    // Aquí abriremos la pantalla de ejercicios en el siguiente paso
                                });

                                recyclerView.setAdapter(adapter);
                                recyclerView.setVisibility(View.VISIBLE);

                            });
                });
    }
}
