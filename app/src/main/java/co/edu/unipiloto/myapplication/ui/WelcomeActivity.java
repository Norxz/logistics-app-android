package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.myapplication.R;
// Importamos MaterialButton, ya que lo usamos en el XML
import com.google.android.material.button.MaterialButton;

/**
 * Actividad de bienvenida que presenta las opciones de inicio de sesión para diferentes roles de usuario,
 * además de la opción de rastreo de envíos para el público general.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Opciones principales (usando MaterialButton para coincidir con el XML dinámico)
        MaterialButton btnCheckStatus = findViewById(R.id.btnCheckStatus); // Botón NARANJA: Consultar Estado
        MaterialButton btnRequestShipping = findViewById(R.id.btnRequestShipping); // Botón AZUL: Solicitar Envío

        // Opciones de Roles (usando MaterialButton para consistencia)
        MaterialButton btnDrivers = findViewById(R.id.btnDrivers);
        MaterialButton btnOfficials = findViewById(R.id.btnOfficials);
        Button btnAdmin = findViewById(R.id.btnAdmin); // El botón de texto discreto puede ser Button o MaterialButton.

        // 1. 🚀 ACCIÓN DINÁMICA: CONSULTAR ESTADO (NARANJA SÓLIDO) 🚀
        // Abre la nueva pantalla de rastreo para el público.
        btnCheckStatus.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, TrackShippingActivity.class);
            startActivity(i);
        });

        // 2. Botón para usuarios/clientes que solicitan envío (AZUL SÓLIDO)
        btnRequestShipping.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(i);
        });

        // 3. Botón para conductores (VERDE DELINEADO)
        btnDrivers.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, LoginDriverActivity.class);
            startActivity(i);
        });

        // 4. Botón para funcionarios (VERDE DELINEADO)
        btnOfficials.setOnClickListener(v -> {
            // Nota: Aquí podrías necesitar un Login diferente (ej. LoginOfficialActivity) si las credenciales son distintas a las del cliente.
            Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(i);
        });

        // 5. Botón para gestión de administradores (Texto discreto)
        btnAdmin.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, AdminPanelActivity.class);
            startActivity(i);
        });
    }
}