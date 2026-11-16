package com.yinya.swimmapp;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.wear.widget.WearableRecyclerView;

public class SessionListActivity extends ComponentActivity {

    private com.facebook.shimmer.ShimmerFrameLayout shimmerLayout;
    private WearableRecyclerView recyclerView;
    private boolean mostrarCompletadas; // Nos dirá qué botón pulsó el usuario en el menú

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

        // Recogemos el dato que nos mandó el MainActivity (por defecto false)
        mostrarCompletadas = getIntent().getBooleanExtra("MOSTRAR_COMPLETADAS", false);
        Log.d("SWIMMAPP", "Cargando lista. ¿Mostrar completadas?: " + mostrarCompletadas);

        shimmerLayout = findViewById(R.id.shimmer_lista);
        recyclerView = findViewById(R.id.recycler_sesiones);

        // Encendemos la animación de carga nada más entrar
        shimmerLayout.startShimmer();

        // TODO: Llamar a Firebase para pedir las sesiones correspondientes
    }
}
