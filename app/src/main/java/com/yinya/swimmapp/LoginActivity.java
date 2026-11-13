package com.yinya.swimmapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends Activity {

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);
        Button btnLogin = findViewById(R.id.btn_login_google);
        btnLogin.setOnClickListener(v -> {
            signInWithGoogle();

        });
    }

    private void signInWithGoogle() {
        // 1. Configuramos qué queremos pedirle a Google
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Ponemos false para que nos deje elegir cuenta la primera vez
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true) // Intenta loguear automático si solo hay una cuenta
                .build();

        // 2. Empaquetamos la opción en una Petición general
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // 3. Lanzamos la petición al sistema (Gestor de Credenciales)
        credentialManager.getCredentialAsync(
                this,
                request,
                new android.os.CancellationSignal(),
                androidx.core.content.ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // ¡Éxito! Google nos ha respondido.
                        Log.d("SWIMMAPP", "Credencial obtenida con éxito");
                        // Aquí extraeremos el token para Firebase en el siguiente paso
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        // Algo ha fallado (ej: el usuario cerró la ventana)
                        Log.e("SWIMMAPP", "Error al obtener credencial: " + e.getMessage());
                    }
                }
        );
    }
}