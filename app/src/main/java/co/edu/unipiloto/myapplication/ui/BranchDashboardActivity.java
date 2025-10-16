package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.storage.SessionManager;
// Importamos la nueva interfaz del adaptador
import co.edu.unipiloto.myapplication.ui.SolicitudAdapter.OnAssignListener;
// Las importaciones que ya tenías
import co.edu.unipiloto.myapplication.ui.LoginFunctionaryActivity;
import co.edu.unipiloto.myapplication.ui.SolicitudAdapter;
import co.edu.unipiloto.myapplication.ui.SolicitudDetailsActivity;

public class BranchDashboardActivity extends AppCompatActivity {

    // Vistas del encabezado
    private MaterialButton btnLogout;
    private MaterialButton btnNewRequest;

    // Repositorio y Adaptadores
    private SolicitudRepository repo;
    private SessionManager session;
    private SolicitudAdapter adapterPending;
    private SolicitudAdapter adapterInRoute;
    private SolicitudAdapter adapterCompleted;

    // Vistas de las listas desplegables
    private RecyclerView rvPending;
    private ImageButton btnTogglePending;
    // Usaremos el tvEmpty genérico de tu XML como mensaje de 'Lista Vacía' para todas las secciones
    private TextView tvEmpty;

    private RecyclerView rvInRoute;
    private ImageButton btnToggleInRoute;

    private RecyclerView rvCompleted;
    private ImageButton btnToggleCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_dashboard);

        session = new SessionManager(this);
        // Si no hay sesión, redirigir al login (buena práctica)
        if (session.getUserId() == -1L) {
            startActivity(new Intent(this, LoginFunctionaryActivity.class));
            finish();
            return;
        }

        repo = new SolicitudRepository(this);

        // 1. Inicialización de vistas clave
        btnLogout = findViewById(R.id.btnLogout);
        btnNewRequest = findViewById(R.id.btnNewRequest);
        tvEmpty = findViewById(R.id.tvEmpty); // TextView genérico para 'Lista Vacía'

        // 2. Inicialización de las listas y sus elementos

        // Pendientes
        rvPending = findViewById(R.id.rvPending);
        rvPending.setLayoutManager(new LinearLayoutManager(this));
        btnTogglePending = findViewById(R.id.btnTogglePending);

        // En Ruta
        rvInRoute = findViewById(R.id.rvInRoute);
        rvInRoute.setLayoutManager(new LinearLayoutManager(this));
        btnToggleInRoute = findViewById(R.id.btnToggleInRoute);

        // Finalizadas (Historial)
        rvCompleted = findViewById(R.id.rvCompleted);
        rvCompleted.setLayoutManager(new LinearLayoutManager(this));
        btnToggleCompleted = findViewById(R.id.btnToggleCompleted);


        // 3. Configurar Click Listeners
        btnNewRequest.setOnClickListener(v -> goToNewDeliveryRegistration());
        btnLogout.setOnClickListener(v -> logout());

        // Configurar los Toggles
        // Se pasa tvEmpty solo a la primera lista para controlarla globalmente.
        btnTogglePending.setOnClickListener(v -> toggleRecyclerView(rvPending, btnTogglePending));
        btnToggleInRoute.setOnClickListener(v -> toggleRecyclerView(rvInRoute, btnToggleInRoute));
        btnToggleCompleted.setOnClickListener(v -> toggleRecyclerView(rvCompleted, btnToggleCompleted));

        // 4. Cargar datos
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    // --- LÓGICA DE NAVEGACIÓN Y CIERRE DE SESIÓN ---

    private void goToNewDeliveryRegistration() {
        // Asumiendo que SolicitudDetailsActivity es donde se registra un nuevo envío
        startActivity(new Intent(this, SolicitudDetailsActivity.class));
    }

    private void logout() {
        new SessionManager(this).clear();
        Intent intent = new Intent(this, LoginFunctionaryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // --- LÓGICA DE CARGA DE DATOS Y FILTRADO ---

    /**
     * Carga y separa la lista de solicitudes asignadas al funcionario actual.
     */
    private void loadDashboardData() {
        long funcionarioId = session.getUserId();

        if (funcionarioId == -1L) {
            Toast.makeText(this, "Error: Sesión de funcionario no válida.", Toast.LENGTH_LONG).show();
            return;
        }

        // 🛑 Lógica clave: Listar solicitudes filtradas por el ID del funcionario (incluye PENDIENTES sin asignar)
        List<SolicitudRepository.SolicitudItem> allItems = repo.listarPorFuncionario(funcionarioId);

        List<SolicitudRepository.SolicitudItem> pendingItems = new ArrayList<>();
        List<SolicitudRepository.SolicitudItem> inRouteItems = new ArrayList<>();
        List<SolicitudRepository.SolicitudItem> completedItems = new ArrayList<>();

        if (allItems != null) {
            for (SolicitudRepository.SolicitudItem item : allItems) {
                String estado = item.estado.toUpperCase();

                if ("ENTREGADA".equals(estado) || "CANCELADA".equals(estado)) {
                    completedItems.add(item);
                } else if ("EN_CAMINO".equals(estado) || "EN_BODEGA".equals(estado) || "LLEGADA_DESTINO".equals(estado)) {
                    inRouteItems.add(item);
                } else {
                    // PENDIENTE, ASIGNADA, RECOLECCION, etc.
                    pendingItems.add(item);
                }
            }
        }

        // 1. Configuración de Pendientes
        adapterPending = SolicitudAdapter.forFuncionario(pendingItems);
        // CONFIGURAR EL NUEVO LISTENER DE ASIGNACIÓN SOLO PARA EL ADAPTADOR PENDIENTE
        adapterPending.setOnAssignListener(assignListenerImplementation);
        setupRecyclerView(rvPending, btnTogglePending, pendingItems, adapterPending);


        // 2. Configuración de En Ruta
        adapterInRoute = SolicitudAdapter.forFuncionario(inRouteItems);
        setupRecyclerView(rvInRoute, btnToggleInRoute, inRouteItems, adapterInRoute);

        // 3. Configuración de Finalizadas (Historial)
        adapterCompleted = SolicitudAdapter.forFuncionario(completedItems);
        setupRecyclerView(rvCompleted, btnToggleCompleted, completedItems, adapterCompleted);

        // 4. Manejo del estado vacío global
        if (pendingItems.isEmpty() && inRouteItems.isEmpty() && completedItems.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Implementación del listener para el botón 'Asignar'.
     */
    /**
     * Implementación del listener para el botón 'Asignar'.
     */
    private final OnAssignListener assignListenerImplementation = (solicitudId, conductorId, position) -> {
        // Asignar la solicitud al conductor en la base de datos
        // Usamos asignarARecolector ya que el conductor es un tipo de recolector en tu modelo
        int rowsAffected = repo.asignarARecolector(solicitudId, conductorId);

        if (rowsAffected > 0) {
            Toast.makeText(BranchDashboardActivity.this, "Solicitud #" + solicitudId + " asignada al conductor ID: " + conductorId, Toast.LENGTH_LONG).show();
            // Recargar los datos para que la solicitud se mueva de 'Pendientes' a 'En Ruta'
            loadDashboardData();
        } else {
            // LÍNEA CORREGIDA AQUÍ ⬇️
            Toast.makeText(BranchDashboardActivity.this, "Error al asignar. La solicitud ya puede estar asignada o su estado cambió.", Toast.LENGTH_LONG).show();
        }
    };


    // --- MÉTODOS DE UTILIDAD PARA VISTAS ---

    /**
     * Alterna la visibilidad de un RecyclerView y actualiza el ícono del botón.
     */
    private void toggleRecyclerView(RecyclerView recyclerView, ImageButton toggleButton) {
        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
            toggleButton.setImageResource(R.drawable.ic_arrow_up);
        } else {
            recyclerView.setVisibility(View.GONE);
            toggleButton.setImageResource(R.drawable.ic_arrow_down);
        }
    }

    /**
     * Configura el RecyclerView, el adaptador, y maneja la visibilidad inicial.
     */
    private void setupRecyclerView(RecyclerView rv, ImageButton toggleBtn, List<SolicitudRepository.SolicitudItem> items, SolicitudAdapter adapter) {
        rv.setAdapter(adapter);

        if (items.isEmpty()) {
            // Si está vacía, se oculta y se pone el ícono de cerrado
            rv.setVisibility(View.GONE);
            toggleBtn.setImageResource(R.drawable.ic_arrow_down);
        } else {
            // Si hay items, se usa la visibilidad definida en el XML, y se pone el ícono correcto
            // Solo rvPending está visible por defecto en tu XML.
            if (rv.getId() == R.id.rvPending) {
                rv.setVisibility(View.VISIBLE);
                toggleBtn.setImageResource(R.drawable.ic_arrow_up);
            } else {
                // Las demás listas (En Ruta, Historial) inician ocultas
                rv.setVisibility(View.GONE);
                toggleBtn.setImageResource(R.drawable.ic_arrow_down);
            }
        }
    }
}