package com.yinya.swimmapp;

import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;

import com.yinya.swimmapp.models.Session;
import com.yinya.swimmapp.repository.SessionRepository;

import java.util.List;

public class SessionDetailActivity extends ComponentActivity {
    private TextView tvName;
    private TextView tvMeters;
    private TextView tvDetails;
    private String sessionId;

    //Variables para estricción de movimiento en la pantalla con el bisel y el botón
    private static final int buttonMovement = 150;
    private static final float biselMovement = 150f;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        tvName = findViewById(R.id.nameSessionName);
        tvMeters = findViewById(R.id.metersSessionName);
        tvDetails = findViewById(R.id.detailSessionName);
        scrollView = findViewById(R.id.scrollView);


        //Mantener pantalla encendida durante la sesion
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // 1. Recuperamos el ID que nos envía SessionListActivity
        sessionId = getIntent().getStringExtra("SESSION_ID");
        if (sessionId == null) {
            Log.e("SWIMMAPP", "No se recibió SESSION_ID");
            finish();
            return;
        }

        loadSessionDetails();
        // Interceptamos el botón físico inferior (Atrás) para hacer scroll hacia abajo
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                textDown();
            }
        });
    }

    private void loadSessionDetails() {
        SessionRepository.getInstance().getSessionById(sessionId, new SessionRepository.OnSessionDetailLoadedListener() {
            @Override
            public void onSuccess(Session session) {
                tvName.setText(session.getName());
                tvMeters.setText(session.getMeters() + " m");

                List<String> exercises = session.getExercises();
                if (exercises != null && !exercises.isEmpty()) {
                    // Unimos todos los ejercicios con un salto de línea entre cada uno
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < exercises.size(); i++) {
                        sb.append(exercises.get(i));
                        if (i < exercises.size() - 1) {
                            sb.append("\n\n"); // Doble salto para leerlo mejor en la piscina
                        }
                    }
                    tvDetails.setText(sb.toString());
                } else {
                    tvDetails.setText("Sin ejercicios");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("SWIMMAPP", "Error al cargar la sesión", e);
                Toast.makeText(SessionDetailActivity.this, "Error al cargar la sesión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void textDown() {
        if (scrollView != null) {
            scrollView.smoothScrollBy(0, buttonMovement);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        return true;
    }


    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_SCROLL &&
                event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {

            if (scrollView != null) {
                float delta = -event.getAxisValue(MotionEvent.AXIS_SCROLL);
                scrollView.smoothScrollBy(0, Math.round(delta * biselMovement));
                Log.d("BISEL", "delta=" + delta + " t=" + event.getEventTime());
            }
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }
}