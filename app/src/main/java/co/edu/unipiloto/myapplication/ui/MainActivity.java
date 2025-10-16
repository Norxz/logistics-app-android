package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.db.SolicitudRepository.SolicitudItem;
import co.edu.unipiloto.myapplication.storage.SessionManager;

/**
 * Actividad principal (Dashboard del Cliente) que muestra la lista de solicitudes.
 * Ahora con dos secciones separadas para solicitudes activas e historial.
 */
public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private SolicitudRepository repo;
    private RecyclerView rvSolicitados;
    private RecyclerView rvFinalizados;
    private SolicitudAdapter adapterSolicitados;
    private SolicitudAdapter adapterFinalizados;
    private ImageButton btnToggleSolicitados;
    private ImageButton btnToggleFinalizados;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);

        // Si no hay sesión -> Login
        if (session.getUserId() == -1L) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Si el usuario es RECOLECTOR, redirigir al panel recolector
        String role = session.getRole();
        if (role != null && role.equalsIgnoreCase("RECOLECTOR")) {
            startActivity(new Intent(this, GestorActivity.class));
            finish();
            return;
        }

        // ------ panel cliente (si llegamos aquí)
        repo = new SolicitudRepository(this);

        // 1. Inicialización y Layout de Solicitudes Activas
        rvSolicitados = findViewById(R.id.rvSolicitados);
        rvSolicitados.setLayoutManager(new LinearLayoutManager(this));
        btnToggleSolicitados = findViewById(R.id.btnToggleSolicitados);

        // 2. Inicialización y Layout de Historial Finalizado
        rvFinalizados = findViewById(R.id.rvFinalizados);
        rvFinalizados.setLayoutManager(new LinearLayoutManager(this));
        btnToggleFinalizados = findViewById(R.id.btnToggleFinalizados);

        tvEmpty = findViewById(R.id.tvEmpty);

        // Botón Nueva Solicitud
        Button btnNueva = findViewById(R.id.btnNuevaSolicitud);
        // MODIFICACIÓN CLAVE: Iniciar UserAddressActivity en lugar de SolicitudActivity
        btnNueva.setOnClickListener(v ->
                startActivity(new Intent(this, SolicitudActivity.class)));

        // Lógica de Toggle (Desplegar/Colapsar)
        btnToggleSolicitados.setOnClickListener(v -> toggleVisibility(rvSolicitados, btnToggleSolicitados));
        btnToggleFinalizados.setOnClickListener(v -> toggleVisibility(rvFinalizados, btnToggleFinalizados));

        cargarLista();

        // Botón Cerrar Sesión
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new SessionManager(this).clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    /**
     * Alterna la visibilidad de un RecyclerView y actualiza el ícono del botón.
     * @param rv El RecyclerView a mostrar/ocultar.
     * @param btn El ImageButton que controla el toggle.
     */
    private void toggleVisibility(RecyclerView rv, ImageButton btn) {
        if (rv.getVisibility() == View.VISIBLE) {
            rv.setVisibility(View.GONE);
            btn.setImageResource(R.drawable.ic_arrow_down);
        } else {
            rv.setVisibility(View.VISIBLE);
            btn.setImageResource(R.drawable.ic_arrow_up);
        }
    }

    /**
     * Carga y separa la lista de solicitudes del usuario actual en Activas y Finalizadas.
     */
    private void cargarLista() {
        long userId = session.getUserId();
        List<SolicitudItem> allItems = repo.listarPorUsuario(userId);

        List<SolicitudItem> itemsActivas = new ArrayList<>();
        List<SolicitudItem> itemsFinalizadas = new ArrayList<>();

        if (allItems != null) {
            for (SolicitudItem item : allItems) {
                // Lógica de filtrado simple:
                if ("ENTREGADA".equalsIgnoreCase(item.estado) || "CANCELADA".equalsIgnoreCase(item.estado)) {
                    itemsFinalizadas.add(item);
                } else {
                    itemsActivas.add(item);
                }
            }
        }

        // --- 1. CONFIGURACIÓN DE LISTA ACTIVA ---
        adapterSolicitados = SolicitudAdapter.forCliente(itemsActivas);
        rvSolicitados.setAdapter(adapterSolicitados);

        if (adapterSolicitados != null) {
            adapterSolicitados.setOnCancelListener((solicitudId, pos) -> {
                int rows = repo.cancelarSolicitud(solicitudId, session.getUserId());
                if (rows > 0) {
                    Toast.makeText(this, "Solicitud cancelada", Toast.LENGTH_SHORT).show();
                    cargarLista();
                } else {
                    Toast.makeText(this, "No se pudo cancelar (quizá ya fue aceptada)", Toast.LENGTH_SHORT).show();
                    cargarLista();
                }
            });
        }

        // --- 2. CONFIGURACIÓN DE LISTA FINALIZADA ---
        adapterFinalizados = SolicitudAdapter.forCliente(itemsFinalizadas);
        rvFinalizados.setAdapter(adapterFinalizados);

        // --- 3. MANEJO DEL ESTADO VACÍO Y VISIBILIDAD ---
        if (itemsActivas.isEmpty() && itemsFinalizadas.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        if (itemsActivas.isEmpty()) {
            rvSolicitados.setVisibility(View.GONE);
            btnToggleSolicitados.setImageResource(R.drawable.ic_arrow_down);
        }

        if (itemsFinalizadas.isEmpty() && rvFinalizados.getVisibility() == View.VISIBLE) {
            rvFinalizados.setVisibility(View.GONE);
            btnToggleFinalizados.setImageResource(R.drawable.ic_arrow_down);
        }
    }
}