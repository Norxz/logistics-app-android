package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.storage.SessionManager;

/**
 * Actividad que muestra el panel de administrador con opciones para gestionar diferentes partes de la aplicación.
 */
public class AdminPanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        MaterialButton btnManageOfficials = findViewById(R.id.btnManageOfficials);
        MaterialButton btnManageDrivers = findViewById(R.id.btnManageDrivers);
        MaterialButton btnViewAllRequests = findViewById(R.id.btnViewAllRequests);
        MaterialButton btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin);

        // Listener para gestionar funcionarios
        btnManageOfficials.setOnClickListener(v -> {
            // TODO: Implementar la navegación a la pantalla de gestión de funcionarios
            Toast.makeText(AdminPanelActivity.this, "Próximamente: Gestionar Funcionarios", Toast.LENGTH_SHORT).show();
        });

        // Listener para gestionar conductores
        btnManageDrivers.setOnClickListener(v -> {
            // TODO: Implementar la navegación a la pantalla de gestión de conductores
            Toast.makeText(AdminPanelActivity.this, "Próximamente: Gestionar Conductores", Toast.LENGTH_SHORT).show();
        });

        // Listener para ver todas las solicitudes
        btnViewAllRequests.setOnClickListener(v -> {
            // TODO: Implementar la navegación a la pantalla de visualización de solicitudes
            Toast.makeText(AdminPanelActivity.this, "Próximamente: Ver Todas las Solicitudes", Toast.LENGTH_SHORT).show();
        });

        // Listener para cerrar sesión
        btnLogoutAdmin.setOnClickListener(v -> {
            new SessionManager(AdminPanelActivity.this).clear();
            Intent intent = new Intent(AdminPanelActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
