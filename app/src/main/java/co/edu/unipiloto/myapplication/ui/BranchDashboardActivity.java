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
import co.edu.unipiloto.myapplication.db.UserRepository; // ¡NUEVO!
import co.edu.unipiloto.myapplication.storage.SessionManager;
import co.edu.unipiloto.myapplication.ui.SolicitudAdapter.OnAssignListener;
import co.edu.unipiloto.myapplication.ui.LoginFunctionaryActivity;
import co.edu.unipiloto.myapplication.ui.SolicitudAdapter;
import co.edu.unipiloto.myapplication.ui.SolicitudDetailsActivity;

public class BranchDashboardActivity extends AppCompatActivity {

    private MaterialButton btnLogout;
    private MaterialButton btnNewRequest;

    private SolicitudRepository repo;
    private UserRepository usersRepo; // <--- Instancia de UserRepository
    private SessionManager session;
    private SolicitudAdapter adapterPending;
    private SolicitudAdapter adapterInRoute;
    private SolicitudAdapter adapterCompleted;

    private RecyclerView rvPending;
    private ImageButton btnTogglePending;
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
        if (session.getUserId() == -1L) {
            startActivity(new Intent(this, LoginFunctionaryActivity.class));
            finish();
            return;
        }

        repo = new SolicitudRepository(this);
        usersRepo = new UserRepository(this); // <--- INICIALIZACIÓN

        btnLogout = findViewById(R.id.btnLogout);
        btnNewRequest = findViewById(R.id.btnNewRequest);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvPending = findViewById(R.id.rvPending);
        rvPending.setLayoutManager(new LinearLayoutManager(this));
        btnTogglePending = findViewById(R.id.btnTogglePending);

        rvInRoute = findViewById(R.id.rvInRoute);
        rvInRoute.setLayoutManager(new LinearLayoutManager(this));
        btnToggleInRoute = findViewById(R.id.btnToggleInRoute);

        rvCompleted = findViewById(R.id.rvCompleted);
        rvCompleted.setLayoutManager(new LinearLayoutManager(this));
        btnToggleCompleted = findViewById(R.id.btnToggleCompleted);

        btnNewRequest.setOnClickListener(v -> goToNewDeliveryRegistration());
        btnLogout.setOnClickListener(v -> logout());

        btnTogglePending.setOnClickListener(v -> toggleRecyclerView(rvPending, btnTogglePending));
        btnToggleInRoute.setOnClickListener(v -> toggleRecyclerView(rvInRoute, btnToggleInRoute));
        btnToggleCompleted.setOnClickListener(v -> toggleRecyclerView(rvCompleted, btnToggleCompleted));

        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void goToNewDeliveryRegistration() {
        startActivity(new Intent(this, SolicitudDetailsActivity.class));
    }

    private void logout() {
        new SessionManager(this).clear();
        Intent intent = new Intent(this, LoginFunctionaryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadDashboardData() {
        long funcionarioId = session.getUserId();

        if (funcionarioId == -1L) {
            Toast.makeText(this, "Error: Sesión de funcionario no válida.", Toast.LENGTH_LONG).show();
            return;
        }

        List<SolicitudRepository.SolicitudItem> allItems = repo.listarPorFuncionario(funcionarioId);

        List<SolicitudRepository.SolicitudItem> pendingItems = new ArrayList<>();
        List<SolicitudRepository.SolicitudItem> inRouteItems = new ArrayList<>();
        List<SolicitudRepository.SolicitudItem> completedItems = new ArrayList<>();

        if (allItems != null) {
            for (SolicitudRepository.SolicitudItem item : allItems) {
                String estado = item.estado.toUpperCase();

                // 🛑 FILTRADO CORREGIDO
                if ("PENDIENTE".equals(estado) || "ASIGNADA".equals(estado)) {
                    // La vista de asignación debe mostrar PENDIENTES y las ya ASIGNADAS
                    // para que el Gestor pueda ver a quién se la asignó.
                    pendingItems.add(item);
                } else if ("EN_CAMINO".equals(estado) || "EN_BODEGA".equals(estado) || "LLEGADA_DESTINO".equals(estado)) {
                    inRouteItems.add(item);
                } else if ("ENTREGADA".equals(estado) || "CANCELADA".equals(estado)) {
                    completedItems.add(item);
                }
            }
        }

        // Obtenemos la lista de conductores (GESTOR/CONDUCTOR)
        List<UserRepository.ConductorInfo> conductores = usersRepo.getConductores();

        // 1. Configuración de Pendientes
        adapterPending = SolicitudAdapter.forFuncionario(pendingItems);
        adapterPending.setConductores(conductores); // <--- INYECTAMOS LA LISTA DE CONDUCTORES
        adapterPending.setOnAssignListener(assignListenerImplementation);
        setupRecyclerView(rvPending, btnTogglePending, pendingItems, adapterPending);

        // 2. Configuración de En Ruta
        adapterInRoute = SolicitudAdapter.forFuncionario(inRouteItems);
        // Si el adaptador de En Ruta necesita conductores, también se inyectan aquí
        adapterInRoute.setConductores(conductores);
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

    private final OnAssignListener assignListenerImplementation = (solicitudId, conductorId, position) -> {
        int rowsAffected = repo.asignarAConductor(solicitudId, conductorId); // <--- USAMOS EL NUEVO MÉTODO

        if (rowsAffected > 0) {
            Toast.makeText(BranchDashboardActivity.this, "Solicitud #" + solicitudId + " asignada al conductor ID: " + conductorId, Toast.LENGTH_LONG).show();
            loadDashboardData();
        } else {
            Toast.makeText(BranchDashboardActivity.this, "Error al asignar. La solicitud ya puede estar asignada o su estado cambió.", Toast.LENGTH_LONG).show();
        }
    };

    private void toggleRecyclerView(RecyclerView recyclerView, ImageButton toggleButton) {
        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
            toggleButton.setImageResource(R.drawable.ic_arrow_up);
        } else {
            recyclerView.setVisibility(View.GONE);
            toggleButton.setImageResource(R.drawable.ic_arrow_down);
        }
    }

    private void setupRecyclerView(RecyclerView rv, ImageButton toggleBtn, List<SolicitudRepository.SolicitudItem> items, SolicitudAdapter adapter) {
        rv.setAdapter(adapter);

        if (items.isEmpty()) {
            rv.setVisibility(View.GONE);
            toggleBtn.setImageResource(R.drawable.ic_arrow_down);
        } else {
            if (rv.getId() == R.id.rvPending) {
                rv.setVisibility(View.VISIBLE);
                toggleBtn.setImageResource(R.drawable.ic_arrow_up);
            } else {
                rv.setVisibility(View.GONE);
                toggleBtn.setImageResource(R.drawable.ic_arrow_down);
            }
        }
    }
}