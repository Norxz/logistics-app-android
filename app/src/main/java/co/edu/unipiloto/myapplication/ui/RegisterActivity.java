package co.edu.unipiloto.myapplication.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPassword2;
    private Spinner spRol;
    private Button btnRegister, btnGoLogin;
    private ProgressBar progress;
    private UserRepository users;

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

        users = new UserRepository(this);

        btnRegister.setOnClickListener(v -> doRegister());
        btnGoLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();
        String pass2 = etPassword2.getText().toString();

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

        try {
            users.register(email, pass); // (opcional: hashear pass)
            toggleLoading(false);
            Toast.makeText(this, "Registro exitoso. Inicia sesión.", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            toggleLoading(false);
            etEmail.setError("Email ya registrado");
        }
    }

    private void toggleLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnGoLogin.setEnabled(!loading);
    }
}
