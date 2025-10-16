package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.UserRepository;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPass;
    Button btnLogin, btnGoRegister, btnForgotPassword;
    ImageButton btnGoBack;
    SessionManager session;
    UserRepository users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        btnGoBack = findViewById(R.id.btnGoBack);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        session = new SessionManager(this);
        users = new UserRepository(this);

        // Si ya hay sesión, redirige según rol
        if (session.getUserId() != -1L) {
            goToHomeByRole(session.getRole());
            finish();
            return;
        }

        // Configurar listeners
        btnLogin.setOnClickListener(v -> doLogin());
        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, GestorActivity.class)));

        // Botón de Devolver
        btnGoBack.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
        });

        // Botón de Olvidé Contraseña
        btnForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRepository.UserInfo u = users.login(email, pass);
        if (u != null) {
            session.saveUser(u.id, u.role, u.zona);
            Toast.makeText(this, "Login OK", Toast.LENGTH_SHORT).show();
            goToHomeByRole(u.role);
            finish();
        } else {
            Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToHomeByRole(String role) {
        if (role == null) role = "";

        // Normalizamos
        String r = role.trim().toUpperCase();
        if ("CONDUCTOR".equals(r)) r = "RECOLECTOR";

        switch (r) {
            case "RECOLECTOR":
                startActivity(new Intent(this, RecolectorActivity.class));
                break;
            case "FUNCIONARIO":
                startActivity(new Intent(this, FunctionaryActivity.class));
                break;
            default: // CLIENTE u otros
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }
}
