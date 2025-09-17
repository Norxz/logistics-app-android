package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        if (session.getUserId() == -1L) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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
        rv.setAdapter(new SolicitudAdapter(items));
    }
}
