package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import co.edu.unipiloto.myapplication.R;

/**
 * Actividad de confirmación (Guía Exitosa) que se muestra después de crear una solicitud.
 * Contiene la lógica para regresar al panel principal.
 */
public class GuideActivity extends AppCompatActivity {

    private MaterialButton btnGenerateAndSend;
    private MaterialButton btnGenerateGuide;
    private MaterialButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usando el layout activity_guide.xml que proporcionaste
        setContentView(R.layout.activity_guide);

        // 1. Inicialización de Vistas
        btnGenerateAndSend = findViewById(R.id.btnGenerateAndSend);
        btnGenerateGuide = findViewById(R.id.btnGenerateGuide);
        btnBack = findViewById(R.id.btnBack); // ID del botón para regresar

        // 2. Lógica para el botón de Regresar
        btnBack.setOnClickListener(v -> navigateToMainActivity());

        // 3. Lógica (temporal) para los botones adicionales
        btnGenerateAndSend.setOnClickListener(v ->
                Toast.makeText(this, "Generando y enviando guía...", Toast.LENGTH_SHORT).show()
        );
        btnGenerateGuide.setOnClickListener(v ->
                Toast.makeText(this, "Descargando guía...", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Navega de vuelta al Dashboard principal (MainActivity).
     * Esta función es la que ejecuta el botón "Regresar".
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);

        // Flags para limpiar el historial de actividades:
        // Cierra GuideActivity y todas las actividades anteriores (como SolicitudDetailsActivity)
        // hasta llegar a MainActivity, colocándola en la cima del historial.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish(); // Cierra GuideActivity
    }
}