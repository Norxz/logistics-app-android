package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.db.SolicitudRepository.SolicitudItem;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private SolicitudRepository repo;
    private RecyclerView rv;
    private Button btnNueva;
    private TextView tvEmpty;
    private SolicitudAdapter adapter;

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
        String role = session.getRole(); // asumo que session tiene getRole()
        if (role != null && role.equalsIgnoreCase("RECOLECTOR")) {
            startActivity(new Intent(this, RecolectorActivity.class));
            finish();
            return;
        }

        // ------ panel cliente (si llegamos aquí)
        repo = new SolicitudRepository(this);

        rv = findViewById(R.id.rvSolicitudes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        tvEmpty = findViewById(R.id.tvEmpty);

        btnNueva = findViewById(R.id.btnNuevaSolicitud);
        btnNueva.setOnClickListener(v ->
                startActivity(new Intent(this, SolicitudActivity.class)));

        cargarLista();

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new SessionManager(this).clear();   // borra userId y todo
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista() {
        long userId = session.getUserId();
        List<SolicitudItem> items = repo.listarPorUsuario(userId);

        if (items == null || items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        adapter = SolicitudAdapter.forCliente(items);
        rv.setAdapter(adapter);

        if (adapter != null) {
            adapter.setOnCancelListener((solicitudId, pos) -> {
                int rows = repo.cancelarSolicitud(solicitudId, session.getUserId());
                if (rows > 0) {
                    adapter.removeAt(pos);
                    Toast.makeText(this, "Solicitud cancelada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No se pudo cancelar (quizá ya fue aceptada)", Toast.LENGTH_SHORT).show();
                    cargarLista();
                }
            });
        }
    }
}
