package com.yinya.swimmapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

@SuppressWarnings("deprecation")
public class LoginActivity extends ComponentActivity {

    private static final String TAG = "SWIMMAPP";
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private Button btnLogin;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerLayout;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    GoogleSignInAccount cuenta = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                            .getResult(ApiException.class);
                    authWithGoogle(cuenta);
                } catch (ApiException e) {
                    Log.e(TAG, "Error en Google Sign-In, código " + e.getStatusCode() + " ("
                            + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()) + ")", e);
                    if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                        showErrorMessage(R.string.login_cancelado);
                    } else {
                        showErrorMessage(R.string.login_error);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        shimmerLayout = findViewById(R.id.shimmer_login);
        btnLogin = findViewById(R.id.btn_login_google);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Al pulsar el botón se lanza la ventana de Google
        btnLogin.setOnClickListener(v -> {
            // Ocultamos el botón y encendemos el skeleton
            btnLogin.setVisibility(View.GONE);
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();

            // Lanzamos la ventana
            signInLauncher.launch(googleSignInClient.getSignInIntent());
        });


        // La app al arrancar: Intenta entrar en silencio
        intentarLoginSilencioso();
    }

    private void intentarLoginSilencioso() {
        shimmerLayout.startShimmer();

        googleSignInClient.silentSignIn()
                .addOnSuccessListener(this, this::authWithGoogle)
                .addOnFailureListener(this, e -> {
                    // No hay sesión guardada: Ocultamos esqueleto y mostramos botón listo para pulsar
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(true);
                });
    }

    private void authWithGoogle(GoogleSignInAccount cuenta) {
        String idToken = cuenta.getIdToken();
        if (idToken == null) {
            Log.e(TAG, "Google no ha devuelto idToken");
            showErrorMessage(R.string.login_error);
            return;
        }

        // Si venimos del botón y ha tenido éxito, podemos volver a poner el skeleton mientras Firebase carga
        btnLogin.setVisibility(View.GONE);
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();

        AuthCredential credencialFirebase = GoogleAuthProvider.getCredential(idToken, null);
        authWithFirebase(credencialFirebase);
    }

    private void authWithFirebase(AuthCredential credencial) {
        mAuth.signInWithCredential(credencial)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "¡Inicio de sesión en Firebase ÉXITOSO!");
                        goToMain();
                    } else {
                        Log.e(TAG, "Error al iniciar sesión en Firebase", task.getException());
                        showErrorMessage(R.string.login_error);

                        // Si Firebase falla, devolvemos el botón
                        shimmerLayout.stopShimmer();
                        shimmerLayout.setVisibility(View.GONE);
                        btnLogin.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showErrorMessage(int mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();

        // Paramos el skeleton y volvemos a mostrar el botón
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);

        btnLogin.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(true);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}