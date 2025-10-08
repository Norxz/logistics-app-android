package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.myapplication.R;

/**
 * Actividad de bienvenida que presenta las opciones de inicio de sesión para diferentes roles de usuario.
 * Los usuarios pueden elegir entre iniciar sesión como cliente (solicitar envío), conductor o funcionario.
 */
public class WelcomeActivity extends AppCompatActivity {
    /**
     * Se llama cuando se crea la actividad por primera vez.
     *
     * @param savedInstanceState Si la actividad se reinicia después de haber sido destruida,
     *                           este Bundle contiene los datos que suministró más recientemente en onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnRequestShipping = findViewById(R.id.btnRequestShipping);
        Button btnDrivers = findViewById(R.id.btnDrivers);
        Button btnOfficials = findViewById(R.id.btnOfficials);

        // Botón para usuarios/clientes que solicitan envío
        btnRequestShipping.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(i);
        });

        // Botón para conductores
        btnDrivers.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, LoginDriverActivity.class);
            startActivity(i);
        });

        // Botón para funcionarios
        btnOfficials.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, LoginFunctionaryActivity.class);
            startActivity(i);
        });
    }
}
