package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.model.LoginRequest;
import co.edu.unipiloto.myapplication.model.LoginResponse;
import co.edu.unipiloto.myapplication.net.ApiClient;
import co.edu.unipiloto.myapplication.net.ApiService;
import co.edu.unipiloto.myapplication.storage.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPass;
    Button btnLogin, btnGoRegister;
    ApiService api;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        api = ApiClient.getClient(this).create(ApiService.class);
        session = new SessionManager(this);

        btnLogin.setOnClickListener(v -> doLogin());
        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void doLogin() {
        String email = etEmail.getText().toString();
        String pass = etPass.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        api.login(new LoginRequest(email, pass)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    session.saveToken(response.body().token);
                    Toast.makeText(LoginActivity.this, "Login OK", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Error de credenciales", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
