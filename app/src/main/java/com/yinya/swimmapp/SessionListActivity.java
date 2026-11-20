package com.yinya.swimmapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yinya.swimmapp.models.SessionIndex;
import com.yinya.swimmapp.repository.SessionRepository;

import java.util.List;

public class SessionListActivity extends ComponentActivity {

    private com.facebook.shimmer.ShimmerFrameLayout shimmerLayout;
    private WearableRecyclerView recyclerView;
    private TextView textView;
    private boolean showCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);
        showCompleted = getIntent().getBooleanExtra("MOSTRAR_COMPLETADAS", false);
        Log.d("SWIMMAPP", "Cargando lista. ¿Mostrar completadas?: " + showCompleted);

        shimmerLayout = findViewById(R.id.shimmer_lista);
        recyclerView = findViewById(R.id.recycler_sesiones);
        textView = findViewById(R.id.tv_activity_session_list);
        shimmerLayout.startShimmer();
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        loadSessions();
    }

    private void loadSessions() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("SWIMMAPP", "Usuario no autenticado");
            finish();
            return;
        }

        String uid = currentUser.getUid();

        SessionRepository.getInstance().getSessions(uid, showCompleted, new SessionRepository.OnSessionsLoadedListener() {
            @Override
            public void onSuccess(List<SessionIndex> sessionList) {
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
                if (sessionList.isEmpty()) {
                    textView.setVisibility(View.VISIBLE);
                } else {
                    SessionAdapter adapter = new SessionAdapter(sessionList, session -> {
                        Log.d("SWIMMAPP", "Clicked session ID: " + session.getId());
                        handleSessionClick(uid, session);
                    });

                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("SWIMMAPP", "Error al cargar sesiones", e);
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
                Toast.makeText(SessionListActivity.this, "Error al cargar las sesiones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSessionClick(String uid, SessionIndex session) {
        if (showCompleted) {
            openSessionDetail(session.getId());
        } else {
            SessionRepository.getInstance().markSessionAsCompleted(uid, session.getId(), new SessionRepository.OnSessionUpdatedListener() {
                @Override
                public void onSuccess() {
                    openSessionDetail(session.getId());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("SWIMMAPP", "Error al marcar la sesión como realizada", e);
                    Toast.makeText(SessionListActivity.this, "Error de conexión. No se pudo abrir la sesión.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openSessionDetail(String sessionId) {
        Intent intent = new Intent(SessionListActivity.this, SessionDetailActivity.class);
        intent.putExtra("SESSION_ID", sessionId);
        startActivity(intent);
    }
}