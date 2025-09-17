package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.model.Solicitud;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class RecolectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recolector);

        SessionManager session = new SessionManager(this);

        TextView tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText("Bienvenido Recolector\nZona: " + session.getZona());

        RecyclerView rv = findViewById(R.id.rvSolicitudes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // obtener solicitudes de la misma zona
        SolicitudRepository repo = new SolicitudRepository(this);
        List<Solicitud> lista = repo.getByZona(session.getZona());

        SolicitudAdapter adapter = new SolicitudAdapter(lista, true);
        rv.setAdapter(adapter);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            session.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
