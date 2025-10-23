package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

// Importaciones de Material Components y Toolbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.UserRepository;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class LoginActivity extends AppCompatActivity {
    // Declaraciones de vistas
    TextInputEditText etEmail, etPass;
    MaterialButton btnLogin, btnGoRegister, btnForgotPassword;

    // NUEVA DECLARACIÓN: Toolbar
    Toolbar loginToolbar;

    SessionManager session;
    UserRepository users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Configurar la Toolbar
        setupToolbar();

        // 2. Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        // Eliminamos la inicialización del btnGoBack

        session = new SessionManager(this);
        users = new UserRepository(this);

        // Si ya hay sesión, redirige según rol
        if (session.getUserId() != -1L) {
            goToHomeByRole(session.getRole());
            finish();
            return;
        }

        // 3. Configurar listeners
        btnLogin.setOnClickListener(v -> doLogin());

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Botón de Olvidé Contraseña
        btnForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    /** Configura la Toolbar y el botón de navegación de regreso. */
    private void setupToolbar() {
        // Inicializa la Toolbar
        loginToolbar = findViewById(R.id.login_toolbar);

        // Establece esta Toolbar como la Action Bar de la actividad
        setSupportActionBar(loginToolbar);

        if (getSupportActionBar() != null) {
            // Habilita el botón de regreso (flecha)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Opcional: elimina el título si lo manejarás con un TextView más grande
            // getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Define la acción al hacer clic en el botón de regreso de la Toolbar
        loginToolbar.setNavigationOnClickListener(v -> {
            // Lógica para volver a la pantalla de bienvenida o a la actividad previa
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
            // finish();
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
            finish(); // Cierra LoginActivity para evitar que se regrese al login
        } else {
            Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToHomeByRole(String role) {
        if (role == null) role = "";

        // Normalizamos
        String r = role.trim().toUpperCase().replace("Ó", "O").replace("Í", "I");
        if ("CONDUCTOR".equals(r)) r = "RECOLECTOR";

        Intent intent;
        switch (r) {
            case "RECOLECTOR":
                intent = new Intent(this, GestorActivity.class);
                break;
            case "FUNCIONARIO":
                intent = new Intent(this, BranchDashboardActivity.class);
                break;
            default: // CLIENTE u otros
                intent = new Intent(this, MainActivity.class);
                break;
        }
        // Limpiamos la pila de actividades
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}