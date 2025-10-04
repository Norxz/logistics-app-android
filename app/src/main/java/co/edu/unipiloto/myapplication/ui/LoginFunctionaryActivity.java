package co.edu.unipiloto.myapplication.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.UserRepository;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class LoginFunctionaryActivity extends Activity {
    EditText etEmail, etPass;
    Button btnLogin;
    SessionManager session;
    UserRepository users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_functionary);

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        session = new SessionManager(this);
        users   = new UserRepository(this);

        // Si ya hay sesión, redirige si es funcionario
        if (session.getUserId() != -1L && "FUNCIONARIO".equalsIgnoreCase(session.getRole())) {
            goToHome();
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPass.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRepository.UserInfo u = users.login(email, pass);
        if (u != null && "FUNCIONARIO".equalsIgnoreCase(u.role)) {
            session.saveUser(u.id, u.role, u.zona);
            Toast.makeText(this, "Login OK", Toast.LENGTH_SHORT).show();
            goToHome();
            finish();
        } else {
            Toast.makeText(this, "Credenciales inválidas o no es funcionario", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToHome() {
        startActivity(new Intent(this, FunctionaryActivity.class));
    }
}

