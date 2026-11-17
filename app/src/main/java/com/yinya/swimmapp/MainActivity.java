package com.yinya.swimmapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.ComponentActivity;

public class MainActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPendientes = findViewById(R.id.btn_not_completed);
        Button btnCompletadas = findViewById(R.id.btn_completed);

        // Al pulsar, vamos a SessionListActivity pasándole el estado
        btnPendientes.setOnClickListener(v -> openSessionList(false));
        btnCompletadas.setOnClickListener(v -> openSessionList(true));
    }

    private void openSessionList(boolean completed) {
        Intent intent = new Intent(this, SessionListActivity.class);
        intent.putExtra("MOSTRAR_COMPLETADAS", completed);
        startActivity(intent);
    }
}