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
    private Button btnRegister, btnGoLogin;
    private ProgressBar progress;
    private UserRepository users;

    private Spinner spRol, spZona;
    private TextView tvZonaLabel;


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
        tvZonaLabel = findViewById(R.id.tvZonaLabel);
        spZona      = findViewById(R.id.spZona);

        users = new UserRepository(this);

        btnRegister.setOnClickListener(v -> doRegister());
        btnGoLogin.setOnClickListener(v -> finish());

        ArrayAdapter<String> roles = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"CLIENTE","RECOLECTOR", "FUNCIONARIO", "CONDUCTOR"});
        roles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRol.setAdapter(roles);

        ArrayAdapter<CharSequence> zonas = ArrayAdapter.createFromResource(
                this,
                R.array.zonas_bogota,
                android.R.layout.simple_spinner_item
        );
        zonas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spZona.setAdapter(zonas);


        spRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String rol = (String) spRol.getSelectedItem();
                int vis = "RECOLECTOR".equals(rol) || "CONDUCTOR".equalsIgnoreCase(rol) ? View.VISIBLE : View.GONE;
                tvZonaLabel.setVisibility(vis);
                spZona.setVisibility(vis);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        String requested = null;
        if (getIntent() != null) requested = getIntent().getStringExtra("role");
        if (requested != null) {
            String norm = requested.trim().toUpperCase();
            // mapear conductor->RECOLECTOR para la selección del spinner
            if ("CONDUCTOR".equals(norm)) norm = "CONDUCTOR";
            for (int i = 0; i < roles.getCount(); i++) {
                if (roles.getItem(i).equalsIgnoreCase(norm)) {
                    spRol.setSelection(i);
                    break;
                }
            }
        }
    }

    private void doRegister() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();
        String pass2 = etPassword2.getText().toString();
        String rolRaw = (String) spRol.getSelectedItem();

        // mapear alias: CONDUCTOR -> RECOLECTOR (guardamos el rol canónico)
        String rol = (rolRaw == null) ? "CLIENTE" : rolRaw.trim().toUpperCase();
        if ("CONDUCTOR".equalsIgnoreCase(rol)) {
            rol = "RECOLECTOR";
        }

        // zona sólo si es recolector
        String zona = null;
        if ("RECOLECTOR".equalsIgnoreCase(rol)) {
            Object sel = spZona.getSelectedItem();
            zona = (sel != null) ? sel.toString() : null;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            return;
        }
        if (pass.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }
        if (!pass.equals(pass2)) {
            etPassword2.setError("No coincide");
            return;
        }

        toggleLoading(true);

        try {
            // llamar al repo - asume users.register(email, pass, rol, zona)
            users.register(email, pass, rol, zona);

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
