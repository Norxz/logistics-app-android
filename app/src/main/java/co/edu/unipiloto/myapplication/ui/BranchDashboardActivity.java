package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.storage.SessionManager;
// Importa tu nueva Activity
// import co.edu.unipiloto.myapplication.ui.NewDeliveryActivity;

public class BranchDashboardActivity extends AppCompatActivity {

    // Vistas del encabezado
    private MaterialButton btnLogout;
    private MaterialButton btnNewRequest; // 🛑 El botón que vamos a usar

    // Vistas de las listas desplegables (Solo se muestran las principales aquí)
    private RecyclerView rvPending;
    private ImageButton btnTogglePending;
    private RecyclerView rvInRoute;
    private ImageButton btnToggleInRoute;
    private RecyclerView rvCompleted;
    private ImageButton btnToggleCompleted;

    // ... otras vistas ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout del dashboard con listas desplegables
        setContentView(R.layout.activity_branch_dashboard);

        // Inicialización de vistas clave
        btnLogout = findViewById(R.id.btnLogout);
        btnNewRequest = findViewById(R.id.btnNewRequest); // 🛑 Inicializar el botón

        // Inicialización de las listas (Necesitas inicializar todas las demás vistas aquí)
        rvPending = findViewById(R.id.rvPending);
        btnTogglePending = findViewById(R.id.btnTogglePending);
        // ... (inicializar rvInRoute, btnToggleInRoute, rvCompleted, btnToggleCompleted) ...

        // 1. Configurar el Click Listener para el nuevo botón
        btnNewRequest.setOnClickListener(v -> goToNewDeliveryRegistration());

        // 2. Configurar el Click Listener para Logout
        btnLogout.setOnClickListener(v -> logout());

        // 3. Configurar los Toggles (ejemplo solo para la lista Pending)
        btnTogglePending.setOnClickListener(v -> toggleRecyclerView(rvPending, btnTogglePending));

        // 4. Cargar datos
        loadDashboardData();
    }

    // 🛑 MÉTODO PARA REDIRIGIR AL REGISTRO DE ENVÍO 🛑
    private void goToNewDeliveryRegistration() {
        startActivity(new Intent(this, SolicitudDetailsActivity.class));
    }

    // Método de ejemplo para la lógica de alternar (toggle)
    private void toggleRecyclerView(RecyclerView recyclerView, ImageButton toggleButton) {
        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
            toggleButton.setImageResource(R.drawable.ic_arrow_up); // Asumiendo que tienes este drawable
        } else {
            recyclerView.setVisibility(View.GONE);
            toggleButton.setImageResource(R.drawable.ic_arrow_down); // Asumiendo que tienes este drawable
        }
    }

    private void loadDashboardData() {
        // Lógica para cargar rvPending, rvInRoute, y rvCompleted filtrando por la zona del funcionario
        // ...
    }

    private void logout() {
        new SessionManager(this).clear();
        Intent intent = new Intent(this, LoginFunctionaryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}