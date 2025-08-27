package co.edu.unipiloto.myapplication.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.model.RegisterRequest;
import co.edu.unipiloto.myapplication.net.ApiClient;
import co.edu.unipiloto.myapplication.net.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPassword2;
    private Spinner spRol;
    private Button btnRegister, btnGoLogin;
    private ProgressBar progress;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        spRol = findViewById(R.id.spRol);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);
        progress = findViewById(R.id.progress);

        api = ApiClient.getClient(this).create(ApiService.class);

        btnRegister.setOnClickListener(v -> doRegister());
        btnGoLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();
        String pass2 = etPassword2.getText().toString();
        String rol   = (spRol.getSelectedItem() != null)
                ? spRol.getSelectedItem().toString()
                : "CLIENTE";

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido"); return;
        }
        if (pass.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres"); return;
        }
        if (!pass.equals(pass2)) {
            etPassword2.setError("No coincide"); return;
        }

        toggleLoading(true);

        RegisterRequest body = new RegisterRequest(email, pass, rol);
        api.register(body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> res) {
                toggleLoading(false);
                if (res.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            "Registro exitoso. Inicia sesión.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "No se pudo registrar (email usado u otros).",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<Void> call, Throwable t) {
                toggleLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Error de red: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnGoLogin.setEnabled(!loading);
    }
}
