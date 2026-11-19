package com.yinya.swimmapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yinya.swimmapp.repository.SessionRepository;

public class MainActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNotCompleted = findViewById(R.id.btn_not_completed);
        Button btnCompleted = findViewById(R.id.btn_completed);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SessionRepository.getInstance().getSessions(user.getUid(), false, null);
        }

        btnNotCompleted.setOnClickListener(v -> openSessionList(false));
        btnCompleted.setOnClickListener(v -> openSessionList(true));
    }

    private void openSessionList(boolean completed) {
        Intent intent = new Intent(this, SessionListActivity.class);
        intent.putExtra("MOSTRAR_COMPLETADAS", completed);
        startActivity(intent);
    }
}