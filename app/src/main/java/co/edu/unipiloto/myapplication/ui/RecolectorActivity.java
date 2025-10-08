package co.edu.unipiloto.myapplication.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.model.Solicitud;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class RecolectorActivity extends AppCompatActivity {

    private SolicitudRepository repo;
    private SolicitudAdapter adapter;
    private SessionManager session;
    private RecyclerView rv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        session = new SessionManager(this);
        repo = new SolicitudRepository(this);

        TextView tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        tvWelcomeTitle.setText("Bienvenido Recolector\nZona: " + session.getZona());

        rv = findViewById(R.id.rvSolicitudes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            session.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        cargarLista();
    }

    private void cargarLista() {
        // 1) obtener pendientes de la zona (para aceptar)
        List<Solicitud> pendientes = repo.pendientesPorZona(session.getZona());
        // 2) obtener las que ya están asignadas a este recolector
        List<Solicitud> asignadas = repo.asignadasA(session.getUserId());

        // 3) fusionar evitando duplicados (usar LinkedHashMap para mantener orden)
        java.util.Map<Long, Solicitud> map = new java.util.LinkedHashMap<>();
        if (pendientes != null) {
            for (Solicitud s : pendientes) map.put(s.id, s);
        }
        if (asignadas != null) {
            for (Solicitud s : asignadas) map.put(s.id, s);
        }

        List<Solicitud> lista = new java.util.ArrayList<>(map.values());

        adapter = SolicitudAdapter.forRecolector(lista);
        rv.setAdapter(adapter);

        // listeners: aceptar, en camino, entregado
        adapter.setOnAcceptListener((solicitudId, pos) -> {
            long recolectorId = session.getUserId();
            int rows = repo.asignarARecolector(solicitudId, recolectorId);
            if (rows > 0) {
                adapter.updateEstadoAt(pos, "ASIGNADA");
                Toast.makeText(this, "Solicitud aceptada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo aceptar (quizá ya fue tomada)", Toast.LENGTH_SHORT).show();
                cargarLista();
            }
        });

        adapter.setOnEnCaminoListener((solicitudId, pos) -> {
            long recolectorId = session.getUserId();
            int rows = repo.marcarEnCamino(solicitudId, recolectorId);
            if (rows > 0) {
                adapter.updateEstadoAt(pos, "EN_CAMINO");
                Toast.makeText(this, "Marcada como EN_CAMINO", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo actualizar a EN_CAMINO", Toast.LENGTH_SHORT).show();
                cargarLista();
            }
        });

        adapter.setOnEntregadoListener((solicitudId, pos) -> {
            long recolectorId = session.getUserId();
            int rows = repo.marcarEntregada(solicitudId, recolectorId);
            if (rows > 0) {
                adapter.updateEstadoAt(pos, "ENTREGADA");
                Toast.makeText(this, "Marcada como ENTREGADA", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo marcar ENTREGADA", Toast.LENGTH_SHORT).show();
                cargarLista();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }
}
