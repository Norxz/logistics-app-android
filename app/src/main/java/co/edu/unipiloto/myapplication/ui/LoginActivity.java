package co.edu.unipiloto.myapplication.ui;


import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.UserRepository;
import co.edu.unipiloto.myapplication.storage.SessionManager;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPass;
    MaterialButton btnLogin, btnGoRegister;
    SessionManager session;
    UserRepository users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        session = new SessionManager(this);
        users   = new UserRepository(this);

        // Si ya hay sesión, redirige según rol
        if (session.getUserId() != -1L) {
            goToHomeByRole(session.getRole());
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPass.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                UserRepository.UserInfo userInfo = users.login(email, password);

                if (userInfo == null) {
                    Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Extraer el rol del usuario
                String role = userInfo.role;

                if ("RECOLECTOR".equalsIgnoreCase(role) || "CONDUCTOR".equalsIgnoreCase(role)) {
                    // Redirigir a DriverActivity con el rol
                    Intent intent = new Intent(this, DriverActivity.class);
                    intent.putExtra("role", role);
                    startActivity(intent);
                } else if ("FUNCIONARIO".equalsIgnoreCase(role)) {
                    Intent intent = new Intent(this, FunctionaryActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
            }
        });
        btnGoRegister.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }

    private void goToHomeByRole(String role) {
        if (role == null) role = "";

        // normalizamos
        String r = role.trim().toUpperCase();
        if ("CONDUCTOR".equals(r)) r = "RECOLECTOR";

        switch (r) {
            case "RECOLECTOR":
                startActivity(new Intent(this, RecolectorActivity.class));
                break;
            case "FUNCIONARIO":
                startActivity(new Intent(this, FunctionaryActivity.class)); // crea esta Activity si la necesitas
                break;
            default: // CLIENTE u otros
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }
}
