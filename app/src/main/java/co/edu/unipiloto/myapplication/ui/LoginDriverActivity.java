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

public class LoginDriverActivity extends AppCompatActivity {
    EditText etEmail, etPass;
    Button btnLogin;
    ImageButton btnGoBack;
    SessionManager session;
    UserRepository users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_driver);

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoBack = findViewById(R.id.btnGoBack);

        session = new SessionManager(this);
        users = new UserRepository(this);

        // Si ya hay sesión, redirige si es conductor o recolector
        if (session.getUserId() != -1L && (
                "CONDUCTOR".equalsIgnoreCase(session.getRole()) ||
                        "RECOLECTOR".equalsIgnoreCase(session.getRole())
        )) {
            goToHome();
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> doLogin());

        // Listener para el botón de retroceso
        btnGoBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa a la anterior
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

        if (u != null && (
                "CONDUCTOR".equalsIgnoreCase(u.role) ||
                        "RECOLECTOR".equalsIgnoreCase(u.role) ||
                        // ✅ AGREGAR GESTOR A LA LISTA DE ROLES PERMITIDOS
                        "GESTOR".equalsIgnoreCase(u.role)
        )) {
            session.saveUser(u.id, u.role, u.zona);
            Toast.makeText(this, "Login OK", Toast.LENGTH_SHORT).show();
            goToHome();
            finish();
        } else {
            Toast.makeText(this, "Credenciales inválidas o no es conductor/recolector", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToHome() {
        startActivity(new Intent(this, DriverDashboardActivity.class));
    }
}
