package co.edu.unipiloto.myapplication.ui;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class SolicitudActivity extends AppCompatActivity {
    EditText etDireccion, etFecha, etNotas;
    Spinner spFranja;
    Button btnCrear;
    SolicitudRepository repo;
    long userId;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_solicitud);

        SessionManager session = new SessionManager(this);
        userId = session.getUserId();
        if (userId == -1L){ finish(); return; }

        repo = new SolicitudRepository(this);

        etDireccion = findViewById(R.id.etDireccion);
        etFecha     = findViewById(R.id.etFecha);
        etNotas     = findViewById(R.id.etNotas);
        spFranja    = findViewById(R.id.spFranja);
        btnCrear    = findViewById(R.id.btnCrear);

        ArrayAdapter<String> franjas = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"08:00-12:00","12:00-16:00","16:00-20:00"});
        franjas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFranja.setAdapter(franjas);

        btnCrear.setOnClickListener(v -> crear());
    }

    private void crear(){
        String dir = etDireccion.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String franja = (String) spFranja.getSelectedItem();
        String notas = etNotas.getText().toString().trim();

        if (dir.length() < 8){ etDireccion.setError("Dirección muy corta"); return; }
        if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")){ etFecha.setError("Formato yyyy-MM-dd"); return; }

        long id = repo.crear(userId, dir, fecha, franja, notas);
        if (id > 0){
            Toast.makeText(this, "Solicitud creada (ID " + id + ")", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "No se pudo crear", Toast.LENGTH_SHORT).show();
        }
    }
}
