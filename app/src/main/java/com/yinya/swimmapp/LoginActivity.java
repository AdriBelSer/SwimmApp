package com.yinya.swimmapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

// NOTA! En el Galaxy Watch el selector de cuentas de Credential Manager (Samsung) crashea al
// elegir la cuenta ("No Activity found to handle null"), así que usamos el Google Sign-In
// clásico, que tiene su propia pantalla. Está deprecado, de ahí el SuppressWarnings.
@SuppressWarnings("deprecation")
public class LoginActivity extends ComponentActivity {

    private static final String TAG = "SWIMMAPP";

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private Button btnLogin;

    // Recibe el resultado de la pantalla de Google Sign-In
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

        // Si ya hay sesión iniciada, vamos directamente a la pantalla principal
        if (mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        // Pedimos el idToken con el client ID web, que es el que necesita Firebase
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin = findViewById(R.id.btn_login_google);
        btnLogin.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        btnLogin.setEnabled(false);

        // Si la cuenta ya autorizó la app antes, entra sin mostrar ninguna pantalla;
        // si no, abrimos la pantalla de Google para elegir cuenta
        googleSignInClient.silentSignIn()
                .addOnSuccessListener(this, this::authWithGoogle)
                .addOnFailureListener(this, e -> signInLauncher.launch(googleSignInClient.getSignInIntent()));
    }

    private void authWithGoogle(GoogleSignInAccount cuenta) {
        String idToken = cuenta.getIdToken();
        if (idToken == null) {
            Log.e(TAG, "Google no ha devuelto idToken");
            showErrorMessage(R.string.login_error);
            return;
        }

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
                    }
                });
    }

    private void showErrorMessage(int mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        btnLogin.setEnabled(true);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
