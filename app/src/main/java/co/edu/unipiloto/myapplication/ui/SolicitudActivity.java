package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import co.edu.unipiloto.myapplication.R;
// Importa las clases que necesites para la siguiente etapa, como SolicitudDetailsActivity
// import co.edu.unipiloto.myapplication.ui.SolicitudDetailsActivity;

/**
 * Actividad que gestiona la captura de la dirección de recolección del envío.
 * Corresponde al layout que incluye el campo de dirección, el botón de GPS, y el botón Continuar.
 */
public class SolicitudActivity extends AppCompatActivity {

    // 🛑 Declaración de las variables de vista del nuevo layout
    private TextInputEditText etAddress;
    private ImageButton btnUseGps;
    private MaterialButton btnContinue;
    private ImageButton btnGoBack;
    private TextInputLayout tilAddress; // Para manejar errores de campo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Carga el layout de la dirección de recolección
        setContentView(R.layout.activity_solicitud); // Asumo que renombraste el XML a activity_solicitud

        // 1. Inicialización de Vistas
        etAddress = findViewById(R.id.etAddress);
        tilAddress = findViewById(R.id.tilAddress);
        btnUseGps = findViewById(R.id.btnUseGps);
        btnContinue = findViewById(R.id.btnContinue);
        btnGoBack = findViewById(R.id.btnGoBack);

        // 2. Manejo de Botones

        // Botón Regresar
        btnGoBack.setOnClickListener(v -> {
            // Vuelve a la actividad anterior (MainActivity)
            finish();
        });

        // Botón Usar GPS
        btnUseGps.setOnClickListener(v -> {
            // Lógica para solicitar permisos y obtener la ubicación actual
            Toast.makeText(this, "Funcionalidad de GPS en desarrollo...", Toast.LENGTH_SHORT).show();
            useGpsLocation();
        });

        // Botón Continuar
        btnContinue.setOnClickListener(v -> {
            validateAndProceed();
        });
    }

    /**
     * Lógica para solicitar la ubicación por GPS y rellenar el campo de dirección.
     */
    private void useGpsLocation() {
        // Aquí iría el código para:
        // 1. Pedir permisos (ACCESS_FINE_LOCATION)
        // 2. Obtener las coordenadas actuales
        // 3. Usar un Geocoder para convertir coordenadas a una dirección legible
        // 4. Rellenar etAddress.setText(direccion_obtenida);

        // Ejemplo de relleno:
        // etAddress.setText("Calle 100 # 19-30, Bogotá");
    }

    /**
     * Valida que la dirección no esté vacía y pasa a la siguiente actividad.
     */
    private void validateAndProceed() {
        String address = etAddress.getText().toString().trim();

        if (address.isEmpty()) {
            tilAddress.setError(getString(R.string.error_required_field)); // Necesitas esta string en strings.xml
            Toast.makeText(this, "Por favor, ingresa la dirección de recolección.", Toast.LENGTH_LONG).show();
            return;
        }

        // Si la dirección es válida, pasamos a la siguiente etapa (detalles del envío)
        Toast.makeText(this, "Dirección confirmada: " + address, Toast.LENGTH_SHORT).show();

        // 🛑 Lógica para pasar a la siguiente actividad (asumo SolicitudDetailsActivity)
        Intent intent = new Intent(this, SolicitudDetailsActivity.class);
        intent.putExtra("PICKUP_ADDRESS", address);
        startActivity(intent);

        // Opcional: finish() si no quieres volver aquí
        // finish();
    }
}